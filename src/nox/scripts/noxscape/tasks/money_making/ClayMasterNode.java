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
        return true;
    }

    @Override
    public void initializeNodes() {
        miningMasterNode = new MiningMasterNode(ctx);

        MiningMasterNode.Configuration cfg = new MiningMasterNode.Configuration();
        cfg.setRockToMine(MiningEntity.CLAY);

        if (stopWatcher == null) {
            stopWatcher = StopWatcher.create(ctx).stopAfter(10_000).gpMade();
        }
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
    public NoxScapeMasterNode getNextMasterNode() {
        return miningMasterNode;
    }
}
