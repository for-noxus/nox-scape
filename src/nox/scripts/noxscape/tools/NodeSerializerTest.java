package nox.scripts.noxscape.tools;

import com.google.gson.Gson;
import nox.scripts.noxscape.core.DecisionMaker;
import nox.scripts.noxscape.core.QueuedNode;
import nox.scripts.noxscape.core.StopWatcher;
import nox.scripts.noxscape.tasks.woodcutting.WoodcuttingEntity;
import nox.scripts.noxscape.tasks.woodcutting.WoodcuttingMasterNode;

import java.util.Stack;

public class NodeSerializerTest {
    public static void main(String[] args) {
        DecisionMaker.init();

        WoodcuttingMasterNode.Configuration cfg = new WoodcuttingMasterNode.Configuration();
        cfg.setTreeToChop(WoodcuttingEntity.YEW);

        StopWatcher stopWatcher = StopWatcher.create(null).stopAfter(5).levelsGained();

        DecisionMaker.addPriorityTask(WoodcuttingMasterNode.class, cfg, stopWatcher);

        Stack<QueuedNode> nodes = DecisionMaker.getQueuedTasks();

        String json = new Gson().newBuilder().setPrettyPrinting().create().toJson(nodes);
    }
}
