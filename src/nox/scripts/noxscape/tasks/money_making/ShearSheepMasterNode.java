package nox.scripts.noxscape.tasks.money_making;

import nox.scripts.noxscape.NoxScape;
import nox.scripts.noxscape.core.MasterNodeInformation;
import nox.scripts.noxscape.core.NoxScapeMasterNode;
import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.enums.Duration;
import nox.scripts.noxscape.core.enums.Frequency;
import nox.scripts.noxscape.core.enums.MasterNodeType;
import nox.scripts.noxscape.core.enums.NodePipeline;
import nox.scripts.noxscape.core.interfaces.IMoneyMaker;
import nox.scripts.noxscape.tasks.base.BankingNode;
import nox.scripts.noxscape.tasks.base.CollectLootNode;
import nox.scripts.noxscape.tasks.base.WalkingNode;
import nox.scripts.noxscape.tasks.base.banking.BankAction;
import nox.scripts.noxscape.tasks.base.banking.BankItem;
import nox.scripts.noxscape.tasks.base.banking.BankLocation;
import org.osbot.rs07.api.ui.Message;

import java.util.Arrays;
import java.util.List;

public class ShearSheepMasterNode extends NoxScapeMasterNode implements IMoneyMaker {

    public ShearSheepMasterNode(ScriptContext ctx) {
        super(ctx);
        this.nodeInformation = new MasterNodeInformation(
                "Shearing Sheep",
                "Sheers sheep for low-level money-making" ,
                Frequency.UNCOMMON,
                Duration.MEDIUM,
                MasterNodeType.MONEYMAKING);
    }

    @Override
    public boolean canExecute() {
        return ctx.myPlayer().getCombatLevel() <= 10;
    }

    @Override
    public void initializeNodes() {
        NoxScapeNode preExecutionWalkNode = new WalkingNode(ctx)
                .isWebWalk(true)
                .toArea(BankLocation.LUMBRIDGE_UPPER.getBankArea())
                .hasMessage("Walking to bank  before shearing sheep")
                .forPipeline(NodePipeline.PRE_EXECUTION);

        NoxScapeNode preExecutionBankNode = new BankingNode(ctx)
                .handlingItems(new BankItem("Shears", BankAction.WITHDRAW, 1))
                .forPipeline(NodePipeline.PRE_EXECUTION)
                .hasMessage("Depositing items");

        NoxScapeNode acquireShearesNode = new CollectLootNode()
                .lootItems("Shears")
                //todo: conditional execution ala .onlyExecuteIf(() -> !ctx.getInventory().contains("Shears"))
                .hasMessage("Acquiring shears");
    }

    @Override
    public boolean requiresPreExecution() {
        return false;
    }

    @Override
    public void onMessage(Message message) throws InterruptedException {

    }

    @Override
    public int getProfitIndex() {
        return 5;
    }

    @Override
    public List<String> itemsHarvestedForMoney() {
        return Arrays.asList("Wool");
    }
}
