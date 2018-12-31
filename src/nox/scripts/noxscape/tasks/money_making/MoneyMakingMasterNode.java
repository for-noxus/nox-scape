package nox.scripts.noxscape.tasks.money_making;

import nox.scripts.noxscape.core.MasterNodeInformation;
import nox.scripts.noxscape.core.NoxScapeMasterNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.StopWatcher;
import nox.scripts.noxscape.core.enums.Duration;
import nox.scripts.noxscape.core.enums.Frequency;
import nox.scripts.noxscape.core.enums.MasterNodeType;
import nox.scripts.noxscape.core.enums.StopCondition;
import nox.scripts.noxscape.core.interfaces.IMoneyMaker;
import nox.scripts.noxscape.core.interfaces.INodeSupplier;
import org.osbot.rs07.api.ui.Message;

import java.util.*;

public class MoneyMakingMasterNode extends NoxScapeMasterNode implements INodeSupplier {

    private NoxScapeMasterNode chosenMethod;

    private List<NoxScapeMasterNode> moneyMakingNodes = Arrays.asList(
            new ClayMasterNode(ctx)
    );

    public MoneyMakingMasterNode(ScriptContext ctx) {
        super(ctx);
        nodeInformation = new MasterNodeInformation(
                "Money Making",
                "Various methods for making dosh",
                Frequency.UNCOMMON,
                Duration.COMPLETION,
                MasterNodeType.MONEYMAKING);
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    @Override
    public void setDefaultStopWatcher() {
        stopWatcher = StopWatcher.createDefault(ctx);
    }

    @Override
    public void initializeNodes() {
        chosenMethod = moneyMakingNodes.stream()
                .filter(NoxScapeMasterNode::canExecute)
                .max(Comparator.comparingInt(i -> ((IMoneyMaker) i).getProfitIndex()))
                .orElse(null);

        if (chosenMethod == null) {
            abort("Unable to determine a valid MoneyMaking method");
            return;
        }

        if (stopWatcher.getStopCondition() == StopCondition.UNSET)
            chosenMethod.setDefaultStopWatcher();

        chosenMethod.reset();
        chosenMethod.setStopWatcher(stopWatcher);
        chosenMethod.initializeNodes();
    }

    @Override
    public boolean requiresPreExecution() {
        return false;
    }

    @Override
    public NoxScapeMasterNode getNextMasterNode() {
        return chosenMethod;
    }

    @Override
    public void onMessage(Message message) throws InterruptedException {

    }
}
