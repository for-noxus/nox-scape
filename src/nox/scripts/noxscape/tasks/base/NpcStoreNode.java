package nox.scripts.noxscape.tasks.base;

import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.tasks.base.NpcStore.NpcStoreLocation;
import nox.scripts.noxscape.util.Pair;
import org.osbot.rs07.api.model.NPC;

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
        return storeNpc != null &&
                ctx.getMap().canReach(storeNpc) &&
                (itemsToPurchase != null && itemsToPurchase.size() > 0 && ctx.getInventory().contains("Coins"));
    }

    @Override
    public int execute() throws InterruptedException {
        NPC storeNpc = getShopNpc();

        if (storeNpc == null) {
            abort(String.format("Couldn't locate Store NPC (%s)", storeLocation.getNpcName()));
            return 500;
        }


    }

    private NPC getShopNpc() {
        return ctx.getNpcs().closest(f -> f.getName() != null && f.getName().equals(storeLocation.getNpcName()));
    }
}
