package nox.scripts.noxscape.tools;

import com.google.gson.*;
import nox.scripts.noxscape.core.DecisionMaker;
import nox.scripts.noxscape.core.QueuedNode;
import nox.scripts.noxscape.core.StopWatcher;
import nox.scripts.noxscape.tasks.grand_exchange.GEAction;
import nox.scripts.noxscape.tasks.grand_exchange.GEItem;
import nox.scripts.noxscape.tasks.grand_exchange.GrandExchangeMasterNode;
import nox.scripts.noxscape.tasks.mining.MiningMasterNode;
import nox.scripts.noxscape.tasks.woodcutting.WoodcuttingEntity;
import nox.scripts.noxscape.tasks.woodcutting.WoodcuttingMasterNode;
import nox.scripts.noxscape.util.QueuedNodeDeserializer;

import java.util.Stack;

public class NodeSerializerTest {
    public static void main(String[] args) throws ClassNotFoundException {
        DecisionMaker.init();

        WoodcuttingMasterNode.Configuration cfg = new WoodcuttingMasterNode.Configuration();
        cfg.setTreeToChop(WoodcuttingEntity.YEW);
        StopWatcher stopWatcher = StopWatcher.create(null).stopAfter(5).levelsGained();

        StopWatcher miningWatcher = StopWatcher.create(null).stopAfter(100_000).gpMade();

        GrandExchangeMasterNode.Configuration gecfg = new GrandExchangeMasterNode.Configuration();
        gecfg.setItemsToHandle(new GEItem("Yew logs", GEAction.SELL, 100));

        DecisionMaker.addPriorityTask(MiningMasterNode.class, null, miningWatcher);
        DecisionMaker.addPriorityTask(WoodcuttingMasterNode.class, cfg, stopWatcher);
        DecisionMaker.addPriorityTask(GrandExchangeMasterNode.class, gecfg, null);

        Stack<QueuedNode> nodes = DecisionMaker.getQueuedTasks();

        String json = new Gson().newBuilder().setPrettyPrinting().create().toJson(nodes);
        Class test = Class.forName(cfg.getClass().getName());

        Gson gson = new Gson().newBuilder().registerTypeAdapter(QueuedNode.class, new QueuedNodeDeserializer()).create();
        QueuedNode[] deserialized = gson.fromJson(json, QueuedNode[].class);
    }
}