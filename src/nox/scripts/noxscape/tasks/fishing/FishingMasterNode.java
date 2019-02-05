package nox.scripts.noxscape.tasks.fishing;

import nox.scripts.noxscape.core.MasterNodeInformation;
import nox.scripts.noxscape.core.NoxScapeMasterNode;
import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.enums.Duration;
import nox.scripts.noxscape.core.enums.Frequency;
import nox.scripts.noxscape.core.enums.MasterNodeType;
import nox.scripts.noxscape.core.enums.NodePipeline;
import nox.scripts.noxscape.tasks.base.BankingNode;
import nox.scripts.noxscape.tasks.base.NpcInteractionNode;
import nox.scripts.noxscape.tasks.base.WalkingNode;
import nox.scripts.noxscape.tasks.base.banking.BankAction;
import nox.scripts.noxscape.tasks.base.banking.BankItem;
import nox.scripts.noxscape.tasks.base.banking.BankLocation;
import nox.scripts.noxscape.tasks.base.banking.PurchaseLocation;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Skill;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class FishingMasterNode extends NoxScapeMasterNode<FishingMasterNode.Configuration> {

    public FishingMasterNode(ScriptContext ctx) {
        super(ctx);
        nodeInformation = new MasterNodeInformation("Fishing",
                "Fishing fishies",
                Frequency.UNCOMMON,
                Duration.LONG,
                MasterNodeType.SKILLING);
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    @Override
    public void initializeNodes() {
        if (configuration == null)
            configuration = new Configuration(getDefaultFishingLocation());

        if (configuration.fishingLocation == null) {
            abort("No FishingLocation selected");
            return;
        }

        if (configuration.fishingLocation.getFishingTool().getMinLevel() > ctx.getSkills().getStatic(Skill.FISHING)) {
            abort(String.format("Not high enough fishing for chosen fishing spot (%s) ", configuration.fishingLocation.getName()));
            return;
        }

        BankItem fishingTool = new BankItem(configuration.fishingLocation.getFishingTool().getPrimaryItemName(), BankAction.WITHDRAW, 1).buyIfNecessary(1, PurchaseLocation.NPC_STORE);
        BankItem fishingSupplement = new BankItem(configuration.fishingLocation.getFishingTool().getSecondaryItemName(), BankAction.WITHDRAW, 2000).buyIfNecessary(2000, PurchaseLocation.GRAND_EXCHANGE);

        BankLocation bankLocation = BankLocation.closestToMeOrDestination(ctx, configuration.fishingLocation.getBank().getPosition());

        NoxScapeNode preExecutionWalkNode = new WalkingNode(ctx)
                .isWebWalk(true)
                .toArea(bankLocation.getBankArea())
                .hasMessage("Walking to bank to acquire Fishing Items")
                .forPipeline(NodePipeline.PRE_EXECUTION);

        NoxScapeNode preExecutionBankNode = new BankingNode(ctx)
                .bankingAt(bankLocation)
                .depositAllWornItems()
                .handlingItems(Arrays.asList(fishingTool, fishingSupplement))
                .hasMessage("Withdrawing Fishing Items")
                .forPipeline(NodePipeline.PRE_EXECUTION);

        NoxScapeNode toFishNode = new WalkingNode(ctx)
                .isWebWalk(true)
                .toPosition(configuration.fishingLocation.getPosition())
                .hasMessage("Walking to fishing area");

        NoxScapeNode fishNode = new NpcInteractionNode(ctx)
                .interactWith(() -> configuration.fishingLocation.getFishingSpotName(), configuration.fishingLocation.getFishingTool().getActionName())
                .afterInteractingWaitFor(() -> ctx.myPlayer().isAnimating(), 600, 600)
                .hasMessage(configuration.fishingLocation.getFishingTool().getActionName());

        NoxScapeNode toBankNode = new WalkingNode(ctx)
                .isWebWalk(true)
                .toArea(configuration.fishingLocation.getBank().getBankArea())
                .hasMessage("Depositing fished fish");

        NoxScapeNode bankNode = new BankingNode(ctx)
                .bankingAt(configuration.fishingLocation.getBank())
                .depositAllExcept(Arrays.asList(fishingTool.getName(), fishingSupplement.getName()))
                .hasMessage("Depositing fish");

        NoxScapeNode postExecutionWalkNode = new WalkingNode(ctx)
                .isWebWalk(true)
                .toArea(configuration.fishingLocation.getBank().getBankArea())
                .hasMessage("Wrapping up fishing node")
                .forPipeline(NodePipeline.POST_EXECUTION);

        preExecutionWalkNode.setChildNode(preExecutionBankNode);
        preExecutionBankNode.setChildNode(toFishNode);
        toFishNode.setChildNode(fishNode);
        fishNode.setChildNode(toBankNode);
        toBankNode.setChildNode(bankNode);
        bankNode.setChildNode(toFishNode);

        setNodes(Arrays.asList(preExecutionWalkNode, preExecutionBankNode, toFishNode, fishNode, toBankNode, bankNode, postExecutionWalkNode));

        ctx.getBot().addMessageListener(this);
        ctx.logClass(this, String.format("Initialized %d nodes.", getNodes().size()));
    }

    @Override
    public boolean requiresPreExecution() {
        return false;
    }

    @Override
    public void onMessage(Message message) throws InterruptedException {

    }

    private FishingLocation getDefaultFishingLocation() {
        List<FishingTool> toolsToUse = Arrays.asList(FishingTool.NET, FishingTool.FLY, FishingTool.POT, FishingTool.HARPOON);
        return Arrays.stream(FishingLocation.values())
                .filter(f -> toolsToUse.contains(f.getFishingTool()))
                .filter(f -> f.getFishingTool().getMinLevel() <= ctx.getSkills().getStatic(Skill.FISHING))
                .filter(f -> f.meetsCondition(ctx))
                .max(Comparator.comparingInt(i -> i.getFishingTool().getMinLevel()))
                .orElse(null);
    }

    public static class Configuration {
        final FishingLocation fishingLocation;

        public Configuration(FishingLocation fishingLocation) {
            this.fishingLocation = fishingLocation;
        }

        public FishingLocation getFishingLocation() {
            return fishingLocation;
        }
    }
}
