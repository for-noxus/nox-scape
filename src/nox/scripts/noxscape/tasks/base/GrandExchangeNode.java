package nox.scripts.noxscape.tasks.base;

import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.tasks.grand_exchange.GEAction;
import nox.scripts.noxscape.tasks.grand_exchange.GEItem;
import nox.scripts.noxscape.tasks.base.banking.BankLocation;
import nox.scripts.noxscape.util.NRandom;
import nox.scripts.noxscape.util.Sleep;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GrandExchangeNode extends NoxScapeNode {

    private List<GEItem> geItems;

    public GrandExchangeNode(ScriptContext ctx) {
        super(ctx);
    }

    public GrandExchangeNode handlingItems(GEItem... items) {
        geItems = Arrays.asList(items);
        return this;
    }

    public GrandExchangeNode handlingItems(List<GEItem> items) {
        geItems = items;
        return this;
    }

    @Override
    public boolean isValid() {
        return BankLocation.GRAND_EXCHANGE.getBankArea().contains(ctx.myPosition());
    }

    @Override
    public int execute() throws InterruptedException {
        if (!ctx.getQuickExchange().isOpen()) {
            if (!ctx.getQuickExchange().open()) {
                abort("Unable to open Grand Exchange");
                return 5;
            }

            Sleep.until(() -> ctx.getQuickExchange().isOpen(), 10000, 1000);
        }

        Map<Boolean, List<GEItem>> isForSale = geItems.stream().collect(Collectors.partitioningBy(m -> m.getAction() == GEAction.SELL));

        for (GEItem item: isForSale.get(true)) {
            int amountToSell = (int) Math.min(ctx.getInventory().getAmount(item.getName()), item.getAmount());
            if (!ctx.getQuickExchange().quickSell(item.getName(), amountToSell)) {
                abort("Error selling GEItem " + item.getName());
                return 5;
            }
            notifyAction("Sold " + item.getName(), item.getAmount());
        }

        for (GEItem item: isForSale.get(false)) {
            if (!ctx.getQuickExchange().quickBuy(item.getName(), item.getAmount(), true)) {
                abort("Error purchasing GEItem " + item.getName());
                return 5;
            }
            notifyAction("Bought " + item.getName(), item.getAmount());
        }

        complete(String.format("Successfully sold %d items and bought %d items.", isForSale.get(true).size(), isForSale.get(false).size()));
        return NRandom.humanized();
    }
}
