package nox.scripts.noxscape.tasks.npc_store;

import nox.scripts.noxscape.core.*;
import nox.scripts.noxscape.core.enums.Duration;
import nox.scripts.noxscape.core.enums.Frequency;
import nox.scripts.noxscape.core.enums.MasterNodeType;
import nox.scripts.noxscape.core.enums.NodePipeline;
import nox.scripts.noxscape.tasks.base.BankingNode;
import nox.scripts.noxscape.tasks.base.NpcStore.NpcStoreLocation;
import nox.scripts.noxscape.tasks.base.NpcStoreNode;
import nox.scripts.noxscape.tasks.base.WalkingNode;
import nox.scripts.noxscape.tasks.base.banking.BankAction;
import nox.scripts.noxscape.tasks.base.banking.BankItem;
import nox.scripts.noxscape.tasks.base.banking.BankLocation;
import nox.scripts.noxscape.util.Pair;
import nox.scripts.noxscape.util.prices.RSBuddyExchangeOracle;
import org.osbot.rs07.api.ui.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
        import java.util.List;
        import java.util.stream.Collectors;

public class NpcStoreMasterNode extends NoxScapeMasterNode<NpcStoreMasterNode.Configuration> {

    public NpcStoreMasterNode(ScriptContext ctx) {
        super(ctx);
        this.nodeInformation = new MasterNodeInformation(
                "NPC Shop",
                "Handling items at an NPC Shop",
                Frequency.MANUAL,
                Duration.COMPLETION,
                MasterNodeType.OTHER);
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    @Override
    public void initializeNodes() {
        ctx.logClass(this, "Initializing NPC Store MasterNode");

        if (configuration == null || (configuration.itemsToBuy.size() == 0 && configuration.itemsToSell.size() == 0)) {
            abort("NPC Store MasterNode was assigned no items. Aborting");
            return;
        }

        if (configuration.itemsToBuy != null && configuration.itemsToBuy.size() > 28) {
            abort("Let's not try to handle more than 28 items in one trip, yeah??");
            return;
        }

        if (configuration.npcStoreLocation == null) {
            abort("NPC Store MasterNode wasn't assigned a store!");
            return;
        }

        List<BankItem> bankItems = configuration.itemsToSell
                .stream()
                .filter(f -> !ctx.getInventory().contains(f.a) || ctx.getInventory().getAmount(f.a) < f.b || f.b == -1)
                .map(m -> new BankItem(m.a, BankAction.WITHDRAW, m.b == -1 ? Integer.MAX_VALUE : m.b))
                .collect(Collectors.toList());

        if (configuration.itemsToBuy != null && configuration.itemsToBuy.size() > 0) {
            bankItems.add(new BankItem("Coins", BankAction.WITHDRAW, Integer.MAX_VALUE));
        }

        BankLocation bankArea = BankLocation.closestToMeOrDestination(ctx, configuration.npcStoreLocation.getPosition());

        NoxScapeNode preExecutionWalkNode = new WalkingNode(ctx)
                .isWebWalk(true)
                .toArea(bankArea.getBankArea())
                .hasMessage("Walking to closest bank")
                .forPipeline(NodePipeline.PRE_EXECUTION);

        NoxScapeNode preExecutionBankNode = new BankingNode(ctx)
                .depositAllBackpackItems()
                .asNoted()
                .bankingAt(bankArea)
                .handlingItems(bankItems)
                .hasMessage("Grabbing items needed for NPC Store from the bank")
                .forPipeline(NodePipeline.PRE_EXECUTION);

        NoxScapeNode toNpcStoreNode = new WalkingNode(ctx)
                .isWebWalk(true)
                .toPosition(configuration.npcStoreLocation)
                .hasMessage("Walking to NPC Store");

        NoxScapeNode npcStoreNode = new NpcStoreNode(ctx)
                .purchaseItems(configuration.itemsToBuy)
                .sellItems(configuration.itemsToSell)
                .shopAt(configuration.npcStoreLocation)
                .addListener(ctx.getScriptProgress())
                .hasMessage("Handling Store Items");

        NoxScapeNode finishNode = new BankingNode(ctx)
                .bankingAt(BankLocation.GRAND_EXCHANGE)
                .depositAllBackpackItems()
                .depositAllWornItems()
                .forPipeline(NodePipeline.POST_EXECUTION)
                .hasMessage("Depositing our goodies");

        preExecutionWalkNode.setChildNode(preExecutionBankNode);
        preExecutionBankNode.setChildNode(npcStoreNode);
        toNpcStoreNode.setChildNode(npcStoreNode);

        setNodes(Arrays.asList(preExecutionWalkNode, preExecutionBankNode, toNpcStoreNode, npcStoreNode, finishNode));

        ctx.logClass(this, String.format("Initialized %d nodes.", getNodes().size()));
    }

    @Override
    public boolean requiresPreExecution() {
        long costOfItems = 0;
        try {
            costOfItems = getCostOfItems();
        } catch (IOException e) {
            abort("Unable to retrieve cost of items for NPC Store");
            return false;
        }

        boolean needsToWithdrawGold = ctx.getInventory().getAmount("Coins") < costOfItems;
        boolean hasItemsToSell = (configuration.itemsToSell == null || configuration.itemsToSell.size() == 0) ||
                (configuration.itemsToSell.stream().allMatch(a -> ctx.getInventory().contains(a.a)));
        return needsToWithdrawGold || !hasItemsToSell;
    }

    @Override
    public void onMessage(Message message) throws InterruptedException {

    }

    private long getCostOfItems() throws IOException {
        if (configuration.itemsToBuy.size() == 0)
            return 0;

        RSBuddyExchangeOracle.retrievePriceGuideIfNecessary();

        return configuration.itemsToBuy
                .stream()
                .map(m -> RSBuddyExchangeOracle.getItemByName(m.a).getStorePrice() * m.b)
                .mapToInt(m -> m)
                .sum();
    }

    public static class Configuration {
        List<Pair<String, Integer>> itemsToSell = new ArrayList<>();
        List<Pair<String, Integer>> itemsToBuy = new ArrayList<>();
        NpcStoreLocation npcStoreLocation;

        public Configuration(NpcStoreLocation npcStoreLocation) {
            this.npcStoreLocation = npcStoreLocation;
        }

        public List<Pair<String, Integer>> getItemsToSell() {
            return itemsToSell;
        }

        public void setItemsToSell(List<Pair<String, Integer>> itemsToSell) {
            this.itemsToSell = itemsToSell;
        }

        public List<Pair<String, Integer>> getItemsToBuy() {
            return itemsToBuy;
        }

        public void setItemsToBuy(List<Pair<String, Integer>> itemsToBuy) {
            this.itemsToBuy = itemsToBuy;
        }

        public NpcStoreLocation getNpcStoreLocation() {
            return npcStoreLocation;
        }

        public void setNpcStoreLocation(NpcStoreLocation npcStoreLocation) {
            this.npcStoreLocation = npcStoreLocation;
        }

        @Override
        public String toString() {
            return "Configuration{" +
                    "itemsToSell=" + itemsToSell +
                    ", itemsToBuy=" + itemsToBuy +
                    ", npcStoreLocation=" + npcStoreLocation +
                    '}';
        }
    }
}
