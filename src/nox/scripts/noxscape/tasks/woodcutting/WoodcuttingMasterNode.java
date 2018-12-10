package nox.scripts.noxscape.tasks.woodcutting;

import nox.scripts.noxscape.core.MasterNodeInformation;
import nox.scripts.noxscape.core.NoxScapeMasterNode;
import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.enums.Duration;
import nox.scripts.noxscape.core.enums.Frequency;
import nox.scripts.noxscape.core.enums.MasterNodeType;
import nox.scripts.noxscape.core.items.CachedItem;
import nox.scripts.noxscape.core.items.WoodcuttingItems;
import nox.scripts.noxscape.tasks.base.BankingNode;
import nox.scripts.noxscape.tasks.base.EntitySkillingNode;
import nox.scripts.noxscape.tasks.base.WalkingNode;
import nox.scripts.noxscape.tasks.base.banking.BankAction;
import nox.scripts.noxscape.tasks.base.banking.BankItem;
import nox.scripts.noxscape.tasks.tutorialisland.TutorialIslandTracker;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.event.webwalk.PathPreferenceProfile;

import java.util.Arrays;
import java.util.Comparator;

public class WoodcuttingMasterNode extends NoxScapeMasterNode {

    public WoodcuttingMasterNode(ScriptContext ctx) {
        super(ctx);
        this.tracker = new TutorialIslandTracker();
        this.nodeInformation = getMasterNodeInformation();
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    @Override
    public void initializeNodes() {
        ctx.logClass(this, "Initializing Woodcutting Nodes");

        BankItem[] axesToWithdraw = WoodcuttingItems.axes().stream().map(m -> new BankItem(m.getName(), BankAction.WITHDRAW, 1, "Woodcutting", m.requiredLevelSum(), true)).toArray(BankItem[]::new);

        // Get the highest level tree we can currently cut
        WoodcuttingEntity entity = Arrays.stream(WoodcuttingEntity.values())
                .filter(f -> f.getRequiredLevel() <= ctx.getSkills().getStatic(Skill.WOODCUTTING))
                .max(Comparator.comparingInt(WoodcuttingEntity::getRequiredLevel))
                .get();

        // Get the closest WoodCutting location to ours
        final Position curPos = ctx.myPosition();
        WoodcuttingLocation location = Arrays.stream(WoodcuttingLocation.values())
                .filter(f -> f.containsTree(entity))
                .min(Comparator.comparingInt(a -> a.distanceToCenterPoint(curPos)))
                .get();

        BankItem logsToBank = new BankItem(entity.producesItemName(), BankAction.DEPOSIT, 100);

        PathPreferenceProfile ppp = new PathPreferenceProfile()
                .checkBankForItems(true)
                .checkEquipmentForItems(true)
                .checkInventoryForItems(true)
                .setAllowTeleports(true);

        NoxScapeNode preExecutionWalkNode = new WalkingNode(ctx)
                .isWebWalk(true)
                .setPathProfile(ppp)
                .toArea(location.getBank());

        NoxScapeNode preExecutioBankNode = new BankingNode(ctx)
                .bankingAt(location.getBank())
                .depositAllWornItems()
                .depositAllBackpackItems()
                .handlingItems(axesToWithdraw);

        NoxScapeNode toTreeNode = new WalkingNode(ctx)
                .toPosition(location.centerPoint())
                .isWebWalk(true)
                .hasMessage("Walking to Trees (" + entity.getName() + ")");

        NoxScapeNode toBankNode = new WalkingNode(ctx)
                .toClosestBankFrom(location.getBank())
                .isWebWalk(true)
                .hasMessage("Walking to Bank");

        NoxScapeNode bankNode = new BankingNode(ctx)
                .bankingAt(location.getBank())
                .handlingItems(logsToBank)
                .hasMessage("Banking " + entity.producesItemName());

        NoxScapeNode interactNode = new EntitySkillingNode(ctx)
                .interactWith(entity)
                .afterInteractingWaitFor(() -> !ctx.myPlayer().isAnimating(), 20000, 1000)
                .hasMessage("Chopping " + entity.getName());

        toTreeNode.setChildNode(interactNode);
        interactNode.setChildNode(toBankNode);
        toBankNode.setChildNode(bankNode);
        bankNode.setChildNode(toTreeNode);
        preExecutioBankNode.setChildNode(toTreeNode);
        preExecutionWalkNode.setChildNode(preExecutioBankNode);

        nodes = Arrays.asList(toTreeNode, interactNode, toBankNode, bankNode, preExecutioBankNode, preExecutionWalkNode);

        setReturnToBankNode(toBankNode);

        ctx.logClass(this, String.format("Initialized %d nodes.", nodes.size()));
    }

    @Override
    public boolean requiresPreExecution() {
        String[] axeNames = WoodcuttingItems.axes().stream().map(CachedItem::getName).toArray(String[]::new);
        return !ctx.getInventory().contains(axeNames) || !ctx.getEquipment().isWieldingWeaponThatContains(axeNames);
    }

    @Override
    public MasterNodeInformation getMasterNodeInformation() {
        if (nodeInformation != null)
            return nodeInformation;

        nodeInformation = new MasterNodeInformation(
                "Woodcutting",
                "Completes Tutorial Island",
                Frequency.COMMON,
                Duration.COMPLETION,
                MasterNodeType.SKILLING);

        return nodeInformation;
    }

    @Override
    public boolean shouldComplete() {
        return !canExecute();
    }
}
