package nox.scripts.noxscape.tasks.base;

import com.thoughtworks.xstream.mapper.ImmutableTypesMapper;
import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.tasks.base.NpcStore.NpcStoreLocation;
import nox.scripts.noxscape.util.NRandom;
import nox.scripts.noxscape.util.Pair;
import nox.scripts.noxscape.util.Sleep;
import nox.scripts.noxscape.util.prices.RSBuddyExchangeOracle;
import nox.scripts.noxscape.util.prices.RSBuddyExchangePrice;
import org.osbot.rs07.api.model.NPC;

import java.io.IOException;
import java.util.List;

public class NpcStoreNode extends NoxScapeNode {

    private NpcStoreLocation storeLocation;
    private List<Pair<String, Integer>> itemsToPurchase;
    private List<Pair<String, Integer>> itemsToSell;

    public NpcStoreNode(ScriptContext ctx) {
        super(ctx);
    }

    public NpcStoreNode shopAt(NpcStoreLocation storeLocation) {
        this.storeLocation = storeLocation;
        return this;
    }

    public NpcStoreNode purchaseItems(List<Pair<String, Integer>> itemsToPurchase) {
        this.itemsToPurchase = itemsToPurchase;
        return this;
    }

    public NpcStoreNode sellItems(List<Pair<String, Integer>> itemsToSell) {
        this.itemsToSell = itemsToSell;
        return this;
    }

    @Override
    protected boolean baseExecutionCondition() {
        NPC storeNpc = getShopNpc();
        ctx.logClass(this, (storeNpc == null) + "");
        return storeNpc != null &&
                ctx.getMap().canReach(storeNpc) &&
                ((itemsToPurchase == null || itemsToPurchase.size() == 0) || ctx.getInventory().contains("Coins"));
    }

    @Override
    public int execute() throws InterruptedException {
        if (itemsToPurchase != null && itemsToPurchase.size() > 0) {
            try {
                RSBuddyExchangeOracle.retrievePriceGuideIfNecessary();
            } catch (IOException e) {
                e.printStackTrace();
                abort("Unable to download price guide");
                return 500;
            }
        }
        NPC storeNpc = getShopNpc();

        if (storeNpc == null) {
            abort(String.format("Couldn't locate Store NPC (%s)", storeLocation.getNpcName()));
            return 500;
        }

        if (!storeNpc.interact("trade")) {
            abort(String.format("Couldn't trade with Store NPC (%s)", storeNpc.getName()));
            return 500;
        }

        Sleep.until(() -> ctx.getStore().isOpen(), 8_000, 800);

        if (!ctx.getStore().isOpen()) {
            abort("Store wasn't open to buy from NPC");
            return 500;
        }

        for (Pair<String, Integer> p : itemsToSell) {
            if (!SellItem(p)) {
                abort("Error handling sale of item " + p.a);
                return 500;
            }
        }

        for (Pair<String, Integer> p : itemsToPurchase) {
            if (!BuyItem(p)) {
                abort("Error handling purchase of item " + p.a);
                return 500;
            }
        }

        complete(String.format("Successfully bought %s and sold %s items from %s", itemsToPurchase.size(), itemsToSell.size(), storeNpc.getName()));
        return NRandom.humanized();
    }

    private boolean SellItem(Pair<String, Integer> itemToSell) {
        if (itemToSell == null)
            return false;

        if (!ctx.getInventory().contains(itemToSell.a))
            return false;

        int amt = itemToSell.b;
        while (ctx.getInventory().contains(itemToSell.a) && amt > 0) {
            long prevAmt = ctx.getInventory().getAmount(itemToSell.a);
            if (!ctx.getStore().sell(itemToSell.a, amt)) {
                ctx.logClass(this, String.format("Error selling item (%s) to store!", itemToSell.a));
                return false;
            }
            Sleep.until(() -> ctx.getInventory().getAmount(itemToSell.a) != prevAmt, 5000, 500);
            amt = amt - (int) (prevAmt - (ctx.getInventory().getAmount(itemToSell.a)));
        }

        return true;
    }

    private boolean BuyItem(Pair<String, Integer> itemToBuy) {
        if (itemToBuy == null || itemToBuy.a == null)
            return false;

        if (ctx.getInventory().isFull() && (RSBuddyExchangeOracle.getItemDefinitionByName(itemToBuy.a).getNotedId() != -1)) {
            ctx.logClass(this, "Couldn't purchase item due to full inventory!");
            return false;
        }

        RSBuddyExchangePrice item = RSBuddyExchangeOracle.getItemByName(itemToBuy.a);

        if (item == null) {
            abort("Couldn't determine price for item " + itemToBuy.toString());
            return false;
        }

        long storePrice = item.getStorePrice() * itemToBuy.b;

        if (ctx.getInventory().getAmount("Coins") < storePrice) {
            ctx.logClass(this, String.format("Tried to buy %sx %s at %s, but didn't have enough gold (had %s)", itemToBuy.b, itemToBuy.a, storePrice, ctx.getInventory().getAmount("Coins")));
            return false;
        }

        int amt = itemToBuy.b;
        while (amt > 0) {
            long prevAmt = ctx.getInventory().getAmount(itemToBuy.a);
            if (!ctx.getStore().buy(itemToBuy.a, amt)) {
                ctx.logClass(this, String.format("Error selling item (%s) to store!", itemToBuy.a));
                return false;
            }
            Sleep.until(() -> ctx.getInventory().getAmount(itemToBuy.a) != prevAmt, 5000, 500);
            amt = amt - (int) (ctx.getInventory().getAmount(itemToBuy.a) - prevAmt);
        }

        return true;
    }

    private NPC getShopNpc() {
        return ctx.getNpcs().closest(f -> f.getName() != null && f.getName().equals(storeLocation.getNpcName()));
    }
}
