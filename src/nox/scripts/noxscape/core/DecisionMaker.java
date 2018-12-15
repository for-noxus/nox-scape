package nox.scripts.noxscape.core;

import com.google.gson.Gson;
import nox.scripts.noxscape.core.enums.Frequency;
import nox.scripts.noxscape.tasks.mining.MiningMasterNode;
import nox.scripts.noxscape.tasks.tutorialisland.TutorialIslandMasterNode;
import nox.scripts.noxscape.tasks.woodcutting.WoodcuttingMasterNode;
import nox.scripts.noxscape.util.QueuedNodeDeserializer;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Stack;

public final class DecisionMaker {

    private static ArrayList<NoxScapeMasterNode> masterNodes = new ArrayList<>();
    private static Stack<QueuedNode> priorityNodes = new Stack<>();

    private static ScriptContext ctx;

    public static void init() {
        System.out.println("Warning! This will fail if you use this in production you fucking moron.");
        initializeNodes();
    }

    public static void init(ScriptContext context) {
        ctx = context;
        initializeNodes();
        NoxScapeMasterNode tutIsland = findExistingNode(TutorialIslandMasterNode.class);
        if (tutIsland.canExecute()) {
            addPriorityTask(TutorialIslandMasterNode.class, null, null);
        }
    }

    private DecisionMaker() {
    }

    public static NoxScapeMasterNode getNextMasterNode() {

        if (!priorityNodes.empty()) {
            QueuedNode nodeInfo = priorityNodes.pop();
            NoxScapeMasterNode priorityNode = null;// findExistingNode(nodeInfo.clazz);
            if (priorityNode != null) {
                priorityNodes.remove(priorityNode);
                priorityNode.initializeNodes();
                ctx.logClass(DecisionMaker.class, "Priority task selected: " + priorityNode.getMasterNodeInformation().getFriendlyName());
                return priorityNode;
            }
        }

        int nodesSelectionRange = masterNodes.stream().map(m -> getStandardizedWeight(m.nodeInformation.getFrequency())).reduce(0, Integer::sum);
        int selectedValue = new Random().nextInt(nodesSelectionRange);
        NoxScapeMasterNode nextNode = locateMasterNodeFromSelectedValue(selectedValue);

        ctx.logClass(DecisionMaker.class, String.format("Task selected with a random value of %d with a range from 0-%d: %s", selectedValue, nodesSelectionRange, nextNode.getMasterNodeInformation().getFriendlyName()));

        nextNode.initializeNodes();
        return nextNode;
    }

    public static void addPriorityTask(Class<? extends NoxScapeMasterNode> node, Object configuration, StopWatcher stopWatcher) {
        boolean exists = masterNodes.stream().anyMatch(f -> f.getClass().equals(node));

        if (!exists) {
            ctx.logClass(DecisionMaker.class, String.format("Attempted to add a priority node that was not a registered masternode (%s)", node.getSimpleName()));
            return;
        }

        QueuedNode newtask = new QueuedNode();
        newtask.className = node.getTypeName();
        try {
            newtask.configClassName = node.getDeclaredClasses()[0].getTypeName();
        } catch (Exception e) {
        }
        newtask.configuration = configuration;
        newtask.stopWatcher = stopWatcher;

        priorityNodes.push(newtask);

        writeTasksToFile();
    }

    public static Stack<QueuedNode> getQueuedTasks() {
        return priorityNodes;
    }

    private static void initializeNodes() {
        addMasterNode(TutorialIslandMasterNode.class);
        addMasterNode(WoodcuttingMasterNode.class);
        addMasterNode(MiningMasterNode.class);

        priorityNodes = readTaskFile();

        if (ctx != null)
            ctx.logClass(DecisionMaker.class, String.format("DecisionMaker has been initialized with %d nodes and %d priorityNodes.", masterNodes.size(), priorityNodes.size()));
        else
            System.out.println(String.format("DecisionMaker has been initialized with %d nodes and %d priorityNodes.", masterNodes.size(), priorityNodes.size()));

    }

    private static NoxScapeMasterNode findExistingNode(Class<? extends NoxScapeMasterNode> clazz) {
        return masterNodes.stream().filter(f -> f.getClass().equals(clazz)).findFirst().orElse(null);
    }

    private static void addMasterNode(Class<? extends NoxScapeMasterNode> clazz) {
        NoxScapeMasterNode existing = findExistingNode(clazz);
        if (existing == null) {
            try {
                NoxScapeMasterNode node = clazz.getConstructor(ScriptContext.class).newInstance(ctx);
                masterNodes.add(node);
                ctx.logClass(DecisionMaker.class, "Successfully added MasterNode: " + node.getMasterNodeInformation().getFriendlyName());
            } catch (Exception e) {
            }
        }
    }

    private static int getStandardizedWeight(Frequency freq) {
        return (int) (freq.getWeight() * 100.0);
    }

    private static NoxScapeMasterNode locateMasterNodeFromSelectedValue(int value) {
        int runningTotal = 0;
        for (NoxScapeMasterNode node : masterNodes) {
            runningTotal += getStandardizedWeight(node.nodeInformation.getFrequency());
            if (value <= runningTotal) return node;
        }
        return null;
    }

    private static Stack<QueuedNode> readTaskFile() {
        try {
            File logFile = getTaskFile();

            Reader fileReader = new FileReader(logFile);

            Gson gson = new Gson().newBuilder().registerTypeAdapter(QueuedNode.class, new QueuedNodeDeserializer()).create();

            Stack<QueuedNode> stack = new Stack<>();
            QueuedNode[] nodes = gson.fromJson(fileReader, QueuedNode[].class);

            stack.addAll(Arrays.asList(nodes));

            return stack;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Stack<>();
    }

    private static void writeTasksToFile() {
        try {
            File logFile = getTaskFile();
            String json = new Gson().newBuilder().setPrettyPrinting().create().toJson(priorityNodes);

            BufferedWriter out = new BufferedWriter(new FileWriter(logFile, false));
            out.write(json);
            out.close();

        } catch (Exception e) {

        }
    }

    private static File getTaskFile() {
        File logFile = null;
        try {
            if (ctx == null) {
                logFile = new File("tasks" + File.separator + "tasks.txt");
            } else {
                logFile = new File(ctx.logDir + File.separator + "tasks" + File.separator + "tasks.txt");
            }
            logFile.getParentFile().mkdirs();
            logFile.createNewFile();
        } catch (Exception e) {

        }
        return logFile;
    }
}
