package nox.scripts.noxscape.core;

import javafx.fxml.Initializable;
import nox.scripts.noxscape.core.enums.Frequency;
import nox.scripts.noxscape.core.interfaces.IAmountable;
import nox.scripts.noxscape.tasks.tutorialisland.TutorialIslandMasterNode;
import nox.scripts.noxscape.tasks.tutorialisland.TutorialIslandUtil;
import nox.scripts.noxscape.tasks.woodcutting.WoodcuttingMasterNode;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
        ctx.logClass(DecisionMaker.class, "DecisionMaker has been initialized with " + masterNodes.size() + " nodes.");
    }

    private DecisionMaker() { }

    public static NoxScapeMasterNode getNextMasterNode() {

        if (!priorityNodes.empty()) {
            QueuedNode nodeInfo = priorityNodes.pop();
            NoxScapeMasterNode priorityNode = findExistingNode(nodeInfo.clazz);
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
        newtask.clazz = node;
        newtask.configuration = configuration;
        newtask.stopWatcher = stopWatcher;

        priorityNodes.push(newtask);
    }

    public static Stack<QueuedNode> getQueuedTasks() {
        return priorityNodes;
    }

    private static void initializeNodes() {
        addMasterNode(TutorialIslandMasterNode.class);
        addMasterNode(WoodcuttingMasterNode.class);
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
        for (NoxScapeMasterNode node: masterNodes) {
            runningTotal += getStandardizedWeight(node.nodeInformation.getFrequency());
            if (value <= runningTotal) return node;
        }
        return null;
    }
}
