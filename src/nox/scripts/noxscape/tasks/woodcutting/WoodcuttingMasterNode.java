package nox.scripts.noxscape.tasks.woodcutting;

import nox.scripts.noxscape.core.*;
import nox.scripts.noxscape.core.enums.*;
import nox.scripts.noxscape.core.CachedItem;
import nox.scripts.noxscape.core.interfaces.IActionListener;
import nox.scripts.noxscape.tasks.base.BankingNode;
import nox.scripts.noxscape.tasks.base.EntitySkillingNode;
import nox.scripts.noxscape.tasks.base.WalkingNode;
import nox.scripts.noxscape.tasks.base.banking.BankAction;
import nox.scripts.noxscape.tasks.base.banking.BankItem;
import nox.scripts.noxscape.tasks.base.banking.BankLocation;
import nox.scripts.noxscape.tasks.base.banking.PurchaseLocation;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.event.webwalk.PathPreferenceProfile;
import org.osbot.rs07.listener.MessageListener;

import java.util.*;
import java.util.stream.Collectors;

public class WoodcuttingMasterNode<k> extends NoxScapeMasterNode<WoodcuttingMasterNode.Configuration> {

    public WoodcuttingMasterNode(ScriptContext ctx) {
        super(ctx);
        nodeInformation = new MasterNodeInformation(
                "Woodcutting",
                "Cuts trees for logs at various locations",
                Frequency.UNCOMMON,
                Duration.MEDIUM,
                MasterNodeType.SKILLING);
        configuration = new Configuration();
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    @Override
    public void initializeNodes() {

        if (configuration == null)
            configuration = new Configuration();

        // Get the highest level tree we can currently cut
        if (configuration.treeToChop == null)
            configuration.treeToChop = Arrays.stream(WoodcuttingEntity.values())
                .filter(f -> f.getRequiredLevel() <= ctx.getSkills().getStatic(Skill.WOODCUTTING))
                .max(Comparator.comparingInt(WoodcuttingEntity::getRequiredLevel))
                .get();

        if (stopWatcher.getStopCondition() == StopCondition.UNSET)
            setDefaultStopWatcher();

        // Get the closest WoodCutting location to ours
        final Position curPos = ctx.myPosition();
        WoodcuttingLocation woodcuttingLocation = Arrays.stream(WoodcuttingLocation.values())
                .filter(f -> f.containsTree(configuration.treeToChop))
                .min(Comparator.comparingInt(a -> a.distanceToCenterPoint(curPos)))
                .get();

        BankItem[] axesToWithdraw = WoodcuttingItems.axes().stream()
                .filter(f -> f.canUse(ctx))
                .map(m -> new BankItem(m.getName(), BankAction.WITHDRAW, 1, "Woodcutting", m.requiredLevelSum(), m.canEquip(ctx)).buyIfNecessary(1, PurchaseLocation.GRAND_EXCHANGE))
                .toArray(BankItem[]::new);
        BankItem logsToBank = new BankItem(configuration.treeToChop.producesItemName(), BankAction.DEPOSIT, 100);

        List<BankItem> itemsToBank = new ArrayList<>(Arrays.asList(axesToWithdraw));
        itemsToBank.add(logsToBank);

        PathPreferenceProfile ppp = new PathPreferenceProfile()
                .checkBankForItems(true)
                .checkEquipmentForItems(true)
                .checkInventoryForItems(true)
                .setAllowTeleports(true);

        BankLocation preExecutionBankLocation = woodcuttingLocation.getBank().isDepositBox() ? BankLocation.closestTo(ctx, ctx.myPosition(), false) : woodcuttingLocation.getBank();
        NoxScapeNode preExecutionWalkNode = new WalkingNode(ctx)
                .isWebWalk(true)
                .setPathProfile(ppp)
                .toArea(preExecutionBankLocation.getBankArea())
                .hasMessage(String.format("Walking to %s bank for the first time", preExecutionBankLocation.getName()))
                .forPipeline(NodePipeline.PRE_EXECUTION);

        NoxScapeNode preExecutionBankNode = new BankingNode(ctx)
                .bankingAt(preExecutionBankLocation)
                .depositAllWornItems()
                .depositAllBackpackItems()
                .handlingItems(axesToWithdraw)
                .hasMessage("Getting player ready to cut some trees")
                .forPipeline(NodePipeline.PRE_EXECUTION);

        NoxScapeNode toTreeNode = new WalkingNode(ctx)
                .toPosition(woodcuttingLocation)
                .isWebWalk(true)
                .hasMessage("Walking to Trees (" + configuration.treeToChop.getName() + ")");

        NoxScapeNode toBankNode = new WalkingNode(ctx)
                .toArea(woodcuttingLocation.getBank().getBankArea())
                .isWebWalk(true)
                .hasMessage("Walking to Bank");

        NoxScapeNode bankNode = new BankingNode(ctx)
                .bankingAt(woodcuttingLocation.getBank())
                .handlingItems(itemsToBank)
                .hasMessage("Banking " + configuration.treeToChop.producesItemName());

        NoxScapeNode interactNode = new EntitySkillingNode(ctx)
                .interactWith(configuration.treeToChop)
                .boundedBy(woodcuttingLocation.centerPoint(), 14)
                .entityInvalidWhen(ent -> ent.getName().equals("Tree stump"), 30000, 1000)
                .hasMessage("Chopping " + configuration.treeToChop.getName());

        NoxScapeNode finishNode = new WalkingNode(ctx)
                .toArea(woodcuttingLocation.getBank().getBankArea())
                .isWebWalk(true)
                .hasMessage("Walking to Bank for Completion")
                .forPipeline(NodePipeline.POST_EXECUTION);

        toTreeNode.setChildNode(interactNode);
        interactNode.setChildNode(toBankNode);
        toBankNode.setChildNode(bankNode);
        bankNode.setChildNode(toTreeNode);

        preExecutionWalkNode.setChildNode(preExecutionBankNode);

        setNodes(Arrays.asList(preExecutionWalkNode, preExecutionBankNode, toTreeNode, interactNode, toBankNode, bankNode, finishNode));

        ctx.getBot().addMessageListener(this);
        ctx.logClass(this, String.format("Initialized %d nodes.", getNodes().size()));
    }

    @Override
    public boolean requiresPreExecution() {
        String[] axeNames = WoodcuttingItems.axes().stream().filter(f -> f.canUse(ctx)).map(CachedItem::getName).toArray(String[]::new);
        Set<String> axeset = Arrays.stream(axeNames).collect(Collectors.toSet());

        boolean inventoryHasAxe = ctx.getInventory().contains(axeNames);
        boolean wieldingAxe = ctx.getEquipment().isWieldingWeaponThatContains(axeNames);
        boolean hasStuffInInventory = !ctx.getInventory().isEmpty() && Arrays.stream(ctx.getInventory().getItems()).noneMatch(a -> a!= null && !axeset.contains(a.getName()) && a.getName().equals(configuration.treeToChop.producesItemName()));

        return !(inventoryHasAxe || wieldingAxe) || ctx.getInventory().isFull() || hasStuffInInventory;
    }

    @Override
    public void onMessage(Message message) throws InterruptedException {
        if (message.getType() == Message.MessageType.GAME) {
            if (message.getMessage().toLowerCase().contains("you've just advanced your woodcutting level")) {
                ctx.getScriptProgress().onLevelUp(Skill.WOODCUTTING);
            }
            if (message.getMessage().toLowerCase().contains("you get some")) {
                Item item = ctx.getInventory().getItem(configuration.treeToChop.producesItemName());
                if (item == null) {
                    ctx.logClass(this, "Unable to log action for mining entity " + configuration.treeToChop);
                }
                else {
                    ctx.getScriptProgress().onItemAcquired(item.getId(), 1);
                }
            }
        }
    }

    public static class Configuration {
        protected WoodcuttingEntity treeToChop;

        public void setTreeToChop(WoodcuttingEntity treeToChop) {
            this.treeToChop = treeToChop;
        }
    }
}
