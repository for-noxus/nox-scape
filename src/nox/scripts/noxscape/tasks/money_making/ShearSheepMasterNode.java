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
import nox.scripts.noxscape.core.interfaces.IActionListener;
import nox.scripts.noxscape.core.interfaces.IMoneyMaker;
import nox.scripts.noxscape.tasks.base.BankingNode;
import nox.scripts.noxscape.tasks.base.CollectLootNode;
import nox.scripts.noxscape.tasks.base.NpcInteractionNode;
import nox.scripts.noxscape.tasks.base.WalkingNode;
import nox.scripts.noxscape.tasks.base.banking.BankAction;
import nox.scripts.noxscape.tasks.base.banking.BankItem;
import nox.scripts.noxscape.tasks.base.banking.BankLocation;
import nox.scripts.noxscape.util.NRandom;
import nox.scripts.noxscape.util.Sleep;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;

import java.util.Arrays;
import java.util.HashSet;
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
        return true;
    }

    @Override
    public void initializeNodes() {
        NoxScapeNode preExecutionWalkNode = new WalkingNode(ctx)
                .isWebWalk(true)
                .toArea(BankLocation.LUMBRIDGE_UPPER.getBankArea())
                .hasMessage("Walking to bank  before shearing sheep")
                .forPipeline(NodePipeline.PRE_EXECUTION);

        BankItem shears = new BankItem("Shears", BankAction.WITHDRAW, 1);

        NoxScapeNode preExecutionBankNode = new BankingNode(ctx)
                .handlingItems(shears)
                .forPipeline(NodePipeline.PRE_EXECUTION)
                .hasMessage("Depositing items");

        Area shearsArea = new Area(3189, 3274, 3191, 3271);

        NoxScapeNode toShearsWalkingNode = new WalkingNode(ctx)
                .isWebWalk(true)
                .toArea(shearsArea)
                .andExecuteIf(() -> !ctx.inventory.contains("Shears"))
                .hasMessage("Walking to shears");

        NoxScapeNode acquireShearsNode = new CollectLootNode(ctx)
                .lootItems("Shears")
                .onlyExecuteIf(() -> !ctx.getInventory().contains("Shears") && shearsArea.contains(ctx.myPosition()))
                .hasMessage("Acquiring shears");

        Area sheepArea = new Area(
                new int[][]{
                        { 3193, 3277 },
                        { 3193, 3257 },
                        { 3213, 3257 },
                        { 3213, 3269 },
                        { 3212, 3270 },
                        { 3212, 3274 },
                        { 3210, 3276 },
                        { 3206, 3276 },
                        { 3205, 3277 },
                        { 3193, 3277 }
                }
        );

        NoxScapeNode walkToSheepArea = new WalkingNode(ctx)
                .isWebWalk(true)
                .toArea(sheepArea)
                .andExecuteIf(() -> ctx.getInventory().onlyContains("Shears") || ctx.getInventory().isEmpty())
                .hasMessage("Walking to sheep");

        List<Integer> disguisedSheepModelIds = Arrays.asList(14084, 14085);
        NoxScapeNode shearSheepNode = new NpcInteractionNode(ctx)
                .interactWith(() -> "Sheep", "Shear")
                .afterInteractingWaitFor(() -> {
                    long curWool = ctx.getInventory().getAmount("Wool");
                    Sleep.until(() -> ctx.getInventory().getAmount("Wool") != curWool, 2_000, NRandom.fuzzedBounds(800, 50, 200));
                    Sleep.until(() -> !ctx.myPlayer().isAnimating(), 2000, NRandom.fuzzedBounds(800, 50, 200));
                    return true;
                }, 6_500, 1000)
                .addNpcValidation(npc -> npc.getModelIds() != null && Arrays.stream(npc.getModelIds()).noneMatch(disguisedSheepModelIds::contains))
                .andExecuteIf(() -> !ctx.getInventory().isFull())
                .hasMessage("Shearing sheeps");

        NoxScapeNode toBankNode = new WalkingNode(ctx)
                .toArea(BankLocation.LUMBRIDGE_UPPER.getBankArea())
                .isWebWalk(true)
                .andExecuteIf(() -> ctx.getInventory().isFull())
                .hasMessage("Walking to deposit wools");

        NoxScapeNode bankNode = new BankingNode(ctx)
                .bankingAt(BankLocation.LUMBRIDGE_UPPER)
                .handlingItems(new BankItem("Wool", BankAction.DEPOSIT, 100), shears)
                .hasMessage("Depositing wools");

        NoxScapeNode postExecutionToBankNode = new WalkingNode(ctx)
                .toArea(BankLocation.LUMBRIDGE_UPPER.getBankArea())
                .isWebWalk(true)
                .andExecuteIf(() -> ctx.getInventory().isFull())
                .forPipeline(NodePipeline.POST_EXECUTION)
                .hasMessage("Walking to wrap up sheep shearing");

        NoxScapeNode postExecutionBankNode = new BankingNode(ctx)
                .bankingAt(BankLocation.LUMBRIDGE_UPPER)
                .depositAllBackpackItems()
                .depositAllWornItems()
                .forPipeline(NodePipeline.POST_EXECUTION)
                .hasMessage("Depositing all items");

        preExecutionBankNode.setChildNode(preExecutionWalkNode);
        toShearsWalkingNode.setChildNode(acquireShearsNode);
        acquireShearsNode.setChildNode(walkToSheepArea);
        walkToSheepArea.setChildNode(shearSheepNode);
        shearSheepNode.setChildNodes(Arrays.asList(shearSheepNode, toBankNode));
        toBankNode.setChildNode(bankNode);
        bankNode.setChildNode(walkToSheepArea);

        setNodes(Arrays.asList(preExecutionBankNode, preExecutionWalkNode, toShearsWalkingNode, acquireShearsNode, walkToSheepArea, shearSheepNode, toBankNode, bankNode, postExecutionToBankNode, postExecutionBankNode));

        ctx.getBot().addMessageListener(this);
        ctx.logClass(this, String.format("Initialized %d nodes.", getNodes().size()));
    }

    @Override
    public boolean requiresPreExecution() {
        return false;
    }

    @Override
    public void onMessage(Message message) throws InterruptedException {
        if (message.getType() == Message.MessageType.GAME) {
            if (message.getMessage().equals("You get some wool.")) {
                ctx.getTabs().open(Tab.INVENTORY);
                Item wool = ctx.getInventory().getItem("Wool");
                if (wool == null)
                    ctx.log("Unable to register acquisition of wool!");
                else
                    ctx.getScriptProgress().onItemAcquired(wool.getId(), 1);
            }
        }
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
