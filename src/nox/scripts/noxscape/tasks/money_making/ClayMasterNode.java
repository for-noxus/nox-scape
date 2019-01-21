package nox.scripts.noxscape.tasks.money_making;

import nox.scripts.noxscape.core.MasterNodeInformation;
import nox.scripts.noxscape.core.NoxScapeMasterNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.StopWatcher;
import nox.scripts.noxscape.core.enums.Duration;
import nox.scripts.noxscape.core.enums.Frequency;
import nox.scripts.noxscape.core.enums.MasterNodeType;
import nox.scripts.noxscape.core.interfaces.IMoneyMaker;
import nox.scripts.noxscape.core.interfaces.INodeSupplier;
import nox.scripts.noxscape.tasks.mining.MiningEntity;
import nox.scripts.noxscape.tasks.mining.MiningMasterNode;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Skill;

import java.util.Arrays;
import java.util.List;

public class ClayMasterNode extends NoxScapeMasterNode implements IMoneyMaker, INodeSupplier {

    private MiningMasterNode miningMasterNode;

    public ClayMasterNode(ScriptContext ctx) {
        super(ctx);
        nodeInformation = new MasterNodeInformation(
                "Mining Clay",
                "Mining Clay for that sweet dosh",
                Frequency.UNCOMMON,
                Duration.MEDIUM,
                MasterNodeType.MONEYMAKING);
    }

    @Override
    public boolean canExecute() {
        return ctx.getSkills().getStatic(Skill.MINING) > 20;
    }

    @Override
    public void initializeNodes() {
        miningMasterNode = new MiningMasterNode(ctx);

        MiningMasterNode.Configuration cfg = new MiningMasterNode.Configuration();
        cfg.setRockToMine(MiningEntity.CLAY);
        cfg.setPurchaseNewPick(false);
        miningMasterNode.setConfiguration(cfg);

        if (stopWatcher == null) {
            stopWatcher = StopWatcher.create(ctx).stopAfter(10_000).gpMade();
        }

        miningMasterNode.setStopWatcher(stopWatcher);
        miningMasterNode.initializeNodes();
    }

    @Override
    public boolean requiresPreExecution() {
        return false;
    }

    @Override
    public int getProfitIndex() {
        return 5;
    }

    @Override
    public List<String> itemsHarvestedForMoney() {
        return Arrays.asList(MiningEntity.CLAY.producesItemName());
    }

    @Override
    public NoxScapeMasterNode getNextMasterNode() {
        return miningMasterNode;
    }

    @Override
    public void onMessage(Message message) throws InterruptedException {

    }
}
