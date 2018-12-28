package nox.scripts.noxscape.tasks.GrandExchange;

import nox.scripts.noxscape.core.MasterNodeInformation;
import nox.scripts.noxscape.core.NoxScapeMasterNode;
import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.enums.Duration;
import nox.scripts.noxscape.core.enums.Frequency;
import nox.scripts.noxscape.core.enums.MasterNodeType;
import nox.scripts.noxscape.tasks.base.BankingNode;
import nox.scripts.noxscape.tasks.base.GrandExchangeNode;
import nox.scripts.noxscape.tasks.base.WalkingNode;
import nox.scripts.noxscape.tasks.base.banking.BankAction;
import nox.scripts.noxscape.tasks.base.banking.BankItem;
import nox.scripts.noxscape.tasks.base.banking.BankLocation;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GrandExchangeMasterNode extends NoxScapeMasterNode<GrandExchangeMasterNode.Configuration> {

    public GrandExchangeMasterNode(ScriptContext ctx) {
        super(ctx);
        this.nodeInformation = new MasterNodeInformation(
                "Grand Exchange",
                "Handling items at the Grand Exchange",
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
        ctx.logClass(this, "Initializing GE MasterNode");

        if (configuration == null || configuration.itemsToHandle == null || configuration.itemsToHandle.size() == 0) {
            abort("GE MasterNode was assigned no items. Aborting");
            return;
        }

        if (configuration.itemsToHandle.size() > 28) {
            abort("Let's not try to handle more than 28 items with the GE yet, yeah?");
            return;
        }

        NoxScapeNode preExecutionWalkNode = new WalkingNode(ctx)
                .isWebWalk(true)
                .toArea(BankLocation.GRAND_EXCHANGE.getBankArea());

        List<BankItem> bankItems = configuration.itemsToHandle
                .stream()
                .filter(f -> f.getAction() == GEAction.SELL)
                .filter(f -> !ctx.getInventory().contains(f.getName()) || ctx.getInventory().getAmount(f.getName()) < f.getAmount())
                .map(m -> new BankItem(m.getName(), BankAction.WITHDRAW, m.getAmount()))
                .collect(Collectors.toList());
        NoxScapeNode preExecutionBankNode = new BankingNode(ctx)
                .depositAllBackpackItems()
                .asNoted()
                .bankingAt(BankLocation.GRAND_EXCHANGE)
                .handlingItems(bankItems);

        NoxScapeNode geNode = new GrandExchangeNode(ctx)
                .handlingItems(configuration.itemsToHandle);

        preExecutionWalkNode.setChildNode(preExecutionBankNode);
        preExecutionBankNode.setChildNode(geNode);

        setPreExecutionNode(preExecutionWalkNode);
        setReturnToBankNode(preExecutionBankNode);
        setNodes(Arrays.asList(preExecutionWalkNode, preExecutionBankNode, geNode));

        ctx.logClass(this, String.format("Initialized %d nodes.", getNodes().size()));
    }

    @Override
    public boolean requiresPreExecution() {
        boolean isInBankArea = BankLocation.GRAND_EXCHANGE.getBankArea().contains(ctx.myPosition());
        boolean needsToWithdraw = configuration.itemsToHandle.stream().anyMatch(a -> !ctx.getInventory().contains(a.getName()) || (ctx.getInventory().getAmount(a.getName()) < a.getAmount()));
        return !isInBankArea || needsToWithdraw;
    }

    public static class Configuration {
        protected List<GEItem> itemsToHandle;

        public void setItemsToHandle(List<GEItem> itemsToHandle) {
            this.itemsToHandle = itemsToHandle;
        }

        public void setItemsToHandle(GEItem... itemsToHandle) {
            this.itemsToHandle = Arrays.asList(itemsToHandle);
        }

        @Override
        public String toString() {
            return "Configuration{" +
                    "itemsToHandle=" + itemsToHandle +
                    '}';
        }
    }
}