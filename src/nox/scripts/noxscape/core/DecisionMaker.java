package nox.scripts.noxscape.core;

import nox.scripts.noxscape.core.enums.Frequency;
import nox.scripts.noxscape.tasks.tutorialisland.TutorialIslandMasterNode;

import java.util.*;

public class DecisionMaker {

    private Random random;

    private ArrayList<NoxScapeMasterNode> masterNodes = new ArrayList<>();
    private ArrayList<NoxScapeMasterNode> priorityNodes = new ArrayList<>();

    private ScriptContext ctx;

    public DecisionMaker(ScriptContext ctx) {
        this.ctx = ctx;
        this.random = new Random();
        initializeNodes();
        ctx.logClass(this, "DecisionMaker has been initialized with " + masterNodes.size() + " nodes.");
    }

    public NoxScapeMasterNode getNextMasterNode() {
        NoxScapeMasterNode priorityNode  = priorityNodes.stream().filter(NoxScapeMasterNode::canExecute).findFirst().orElse(null);
        if (priorityNode != null) {
            priorityNodes.remove(priorityNode);
            priorityNode.initializeNodes();
            ctx.logClass(this, "Priority task selected: " + priorityNode.getMasterNodeInformation().getFriendlyName());
            return priorityNode;
        }

        int nodesSelectionRange = masterNodes.stream().map(m -> getStandardizedWeight(m.nodeInformation.getFrequency())).reduce(0, Integer::sum);
        int selectedValue = random.nextInt(nodesSelectionRange);
        NoxScapeMasterNode nextNode = locateMasterNodeFromSelectedValue(selectedValue);

        ctx.logClass(this, String.format("Task selected with a random value of %d with a range from 0-%d: %s", selectedValue, nodesSelectionRange, nextNode.getMasterNodeInformation().getFriendlyName()));

        nextNode.initializeNodes();
        return nextNode;
    }

    public void addPriorityItem(Class<? extends NoxScapeMasterNode> node) {
        NoxScapeMasterNode existing = masterNodes.stream().filter(f -> f.getClass().equals(node)).findFirst().orElseThrow(IllegalArgumentException::new);
        addPriorityItem(existing);
    }

    public void addPriorityItem(NoxScapeMasterNode node) {
        priorityNodes.add(node);
    }

    public void addMasterNode(Class<? extends NoxScapeMasterNode> clazz) {
        NoxScapeMasterNode existing = findExistingNode(clazz);
        if (existing == null) {
            try {
                NoxScapeMasterNode node = clazz.getConstructor(ScriptContext.class).newInstance(ctx);
                masterNodes.add(node);
                ctx.logClass(this, "Successfully added MasterNode: " + node.getMasterNodeInformation().getFriendlyName());
            } catch (Exception e) {
            }
        }
    }

    private void initializeNodes() {
        addMasterNode(TutorialIslandMasterNode.class);

        NoxScapeMasterNode tutIsland = findExistingNode(TutorialIslandMasterNode.class);
        if (tutIsland.canExecute()) {
            addPriorityItem(tutIsland);
        }
    }

    private NoxScapeMasterNode findExistingNode(Class<? extends NoxScapeMasterNode> clazz) {
        return masterNodes.stream().filter(f -> f.getClass().equals(clazz)).findFirst().orElse(null);
    }

    private NoxScapeMasterNode findPriorityNode(Class<? extends NoxScapeMasterNode> clazz) {
        return masterNodes.stream().filter(f -> f.getClass().equals(clazz)).findFirst().orElse(null);
    }

    private int getStandardizedWeight(Frequency freq) {
        return (int) (freq.getWeight() * 100);
    }

    private NoxScapeMasterNode locateMasterNodeFromSelectedValue(int value) {
        int runningTotal = 0;
        for (NoxScapeMasterNode node: masterNodes) {
            runningTotal += getStandardizedWeight(node.nodeInformation.getFrequency());
            if (value <= runningTotal) return node;
        }
        return null;
    }
}
