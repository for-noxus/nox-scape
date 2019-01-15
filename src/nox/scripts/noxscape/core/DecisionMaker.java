package nox.scripts.noxscape.core;

import com.google.gson.Gson;
import com.sun.javafx.sg.prism.NGExternalNode;
import nox.scripts.noxscape.core.enums.Frequency;
import nox.scripts.noxscape.core.interfaces.INodeSupplier;
import nox.scripts.noxscape.tasks.combat.CombatMasterNode;
import nox.scripts.noxscape.tasks.grand_exchange.GrandExchangeMasterNode;
import nox.scripts.noxscape.tasks.mining.MiningMasterNode;
import nox.scripts.noxscape.tasks.money_making.MoneyMakingMasterNode;
import nox.scripts.noxscape.tasks.tutorialisland.TutorialIslandMasterNode;
import nox.scripts.noxscape.tasks.woodcutting.WoodcuttingMasterNode;
import nox.scripts.noxscape.util.Pair;
import nox.scripts.noxscape.util.QueuedNodeDeserializer;
import org.osbot.P;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public final class DecisionMaker {

    private static ArrayList<NoxScapeMasterNode> masterNodes = new ArrayList<>();
    private static Stack<QueuedNode> priorityNodes = new Stack<>();

    private static QueuedNode lastPoppedNode;

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
            addPriorityTask(TutorialIslandMasterNode.class, null, null, false);
        }
    }

    public static NoxScapeMasterNode getNextMasterNode() {
        NoxScapeMasterNode nextNode = null;

        if (!priorityNodes.empty()) {
            try {
                lastPoppedNode = priorityNodes.pop();
                writeTasksToFile();
                Class nodeClass = Class.forName(lastPoppedNode.className);
                nextNode = findExistingNode(nodeClass);
                if (nextNode != null) {
                    nextNode.reset();
                    nextNode.configuration = lastPoppedNode.configuration;
                    nextNode.setStopWatcher(lastPoppedNode.stopWatcher);
                    ctx.logClass(DecisionMaker.class, "Priority task selected: " + nextNode.getMasterNodeInformation().getFriendlyName());
                }
            } catch (ClassNotFoundException e) {
                // Having issues with that priorityNode, let's just uhh...not use it
                e.printStackTrace();
            }
        } else {
            lastPoppedNode = null;
            nextNode = chooseNextMasterNode();
        }

        if (nextNode == null) {
            ctx.logClass(DecisionMaker.class, "Somehow ended up with a null node, choosing anew..");
            return getNextMasterNode();
        }

        if (nextNode.getStopWatcher() == null)
            nextNode.setStopWatcher(StopWatcher.createDefault(ctx));

        nextNode.initializeNodes();

        while (nextNode instanceof INodeSupplier) {
            NoxScapeMasterNode suppliedNode = ((INodeSupplier)nextNode).getNextMasterNode();
            if (suppliedNode != null) {
                ctx.log(String.format("Node %s is a node supplier, supplying node (%s)", nextNode.getMasterNodeInformation().getFriendlyName(), suppliedNode.getMasterNodeInformation().getFriendlyName()));
                nextNode = suppliedNode;
            } else {
                ctx.log(String.format("Node %s is a node supplier, but no node was given. Moving on.", nextNode.getMasterNodeInformation().getFriendlyName()));
                nextNode.reset();
                return getNextMasterNode();
            }
        }

        return nextNode;
    }

    public static void addPriorityTask(Class<? extends NoxScapeMasterNode> node, Object configuration, StopWatcher stopWatcher, boolean isDependent) {
        boolean exists = masterNodes.stream().anyMatch(f -> f.getClass().equals(node));

        if (!exists) {
            ctx.logClass(DecisionMaker.class, String.format("Attempted to add a priority node that was not a registered masternode (%s)", node.getSimpleName()));
            return;
        }

        if (ctx != null)
            ctx.logClass(DecisionMaker.class, "Added PriorityNode " + node.getSimpleName());

        QueuedNode newtask = new QueuedNode();
        newtask.isDependant = isDependent;
        newtask.className = node.getTypeName();
        try {
            newtask.configClassName = node.getDeclaredClasses()[0].getTypeName();
        } catch (Exception e) {
            if (ctx != null)
                ctx.log(newtask.className + " has no configuration class!");
        }
        newtask.configuration = configuration;
        newtask.stopWatcher = stopWatcher;

        priorityNodes.push(newtask);

        writeTasksToFile();
    }

    public static Stack<QueuedNode> getQueuedTasks() {
        return priorityNodes;
    }

    public static void clearDependentNodeStack() {
        if (priorityNodes == null)
            return;

        while (!priorityNodes.empty() && priorityNodes.peek().isDependant) {
            QueuedNode node = priorityNodes.pop();
            ctx.log("Abandoning node " + node.className + " because it was dependent on an abandoned ancestor");
        }
    }

    public static void shutdown() {
        if (ctx.getCurrentMasterNode() != null)
            addPriorityTask(ctx.getCurrentMasterNode().getClass(), ctx.getCurrentMasterNode().getConfiguration(), ctx.getCurrentMasterNode().getStopWatcher(), lastPoppedNode != null && lastPoppedNode.isDependant);

        writeTasksToFile();

        masterNodes.forEach(NoxScapeMasterNode::reset);
    }

    private static void initializeNodes() {
        addMasterNode(TutorialIslandMasterNode.class);
        addMasterNode(WoodcuttingMasterNode.class);
        addMasterNode(MiningMasterNode.class);
        addMasterNode(GrandExchangeMasterNode.class);
        addMasterNode(MoneyMakingMasterNode.class);
        addMasterNode(CombatMasterNode.class);

        priorityNodes = readTaskFile();

        if (ctx != null)
            ctx.logClass(DecisionMaker.class, String.format("DecisionMaker has been initialized with %d nodes and %d priorityNodes.", masterNodes.size(), priorityNodes.size()));
        else
            System.out.println(String.format("DecisionMaker has been initialized with %d nodes and %d priorityNodes.", masterNodes.size(), priorityNodes.size()));

    }

    private static NoxScapeMasterNode chooseNextMasterNode() {
        List<Pair<NoxScapeMasterNode, Integer>> availableNodes = masterNodes
                .stream()
                .filter(NoxScapeMasterNode::canExecute)
                .filter(f -> f.nodeInformation.getFrequency() != Frequency.MANUAL)
                .map(m -> new Pair<>(m, getStandardizedWeight(m.nodeInformation.getFrequency())))
                .collect(Collectors.toList());
        int nodesSelectionRange = availableNodes.stream().map(Pair::getB).reduce(0, Integer::sum);
        int selectedValue = new Random().nextInt(nodesSelectionRange);

        NoxScapeMasterNode nextNode = null;
        int runningTotal = 0;
        for (Pair<NoxScapeMasterNode, Integer> p: availableNodes) {
            runningTotal += p.b;
            if (runningTotal >= selectedValue) {
                nextNode = p.a;
                break;
            }
        }

        if (nextNode == null) {
            ctx.logClass(DecisionMaker.class, "Couldn't find next task from range to " + nodesSelectionRange);
            return null;
        }

        ctx.logClass(DecisionMaker.class, String.format("Task selected with a random value of %d with a range from 0-%d: %s", selectedValue, nodesSelectionRange, nextNode.getMasterNodeInformation().getFriendlyName()));

        nextNode.reset();
        return nextNode;
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

    private static Stack<QueuedNode> readTaskFile() {
        try {
            File logFile = getTaskFile();

            Reader fileReader = new FileReader(logFile);

            Gson gson = new Gson().newBuilder()
                    .registerTypeAdapter(QueuedNode.class, new QueuedNodeDeserializer())
                    .create();

            Stack<QueuedNode> stack = new Stack<>();
            QueuedNode[] nodes = gson.fromJson(fileReader, QueuedNode[].class);

            if (nodes == null || nodes.length == 0)
                return stack;

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
            if (ctx != null)
                ctx.log(e.getMessage() == null ? e : e.getMessage() + "\n" + e.toString());
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
