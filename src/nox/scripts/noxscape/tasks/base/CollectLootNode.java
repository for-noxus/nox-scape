package nox.scripts.noxscape.tasks.base;

import com.sun.net.httpserver.Authenticator;
import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.util.NRandom;
import nox.scripts.noxscape.util.Sleep;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.script.MethodProvider;

import java.util.Arrays;
import java.util.List;

public class CollectLootNode extends NoxScapeNode {

    private List<String> itemsToLoot;
    private boolean shouldWait;

    public CollectLootNode(ScriptContext ctx) {
        super(ctx);
    }

    public CollectLootNode lootItems(String... itemsToLoot) {
        this.itemsToLoot = Arrays.asList(itemsToLoot);
        return this;
    }

    public CollectLootNode lootItems(List<String> itemsToLoot) {
        this.itemsToLoot = itemsToLoot;
        return this;
    }

    public CollectLootNode waitForItemToRespawn() {
        this.shouldWait = true;
        return this;
    }

    protected boolean baseExecutionCondition() {
        if (itemsToLoot == null || itemsToLoot.size() == 0)
            throw new IllegalArgumentException("A LootNode was specified, but no items were specified to be picked up!");

        return shouldWait || getItemsToPickup().size() != 0;
    }

    @Override
    public int execute() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        List<GroundItem> itemsToPickup = getItemsToPickup();
        while (itemsToPickup == null || itemsToPickup.size() == 0) {
            if (System.currentTimeMillis() - startTime >= 120_000) {
                abort("Waited over 2 minutes for GroundItem to respawn");
                return 5;
            }
            itemsToPickup = getItemsToPickup();
            MethodProvider.sleep(1000);
        }
        for (GroundItem i: itemsToPickup) {
            long amountOfItemInInventory = ctx.getInventory().getAmount(i.getName());
            if (i.interact("Take"))
                Sleep.until(() -> ctx.getInventory().getAmount(i.getName()) != amountOfItemInInventory, 3000);

            if (amountOfItemInInventory != ctx.getInventory().getAmount(i.getName()))
                ctx.logClass(this, "Error picking up GroundItem " + i.getName());
        }

        complete("Successfully acquired all GroundItems we could acquire.");

        return NRandom.humanized();
    }

    private List<GroundItem> getItemsToPickup() {
        boolean isFull = ctx.getInventory().isFull();

        return ctx.getGroundItems().filter(f ->
                itemsToLoot.contains(f.getName()) &&
                        (!isFull || (isFull && ctx.getInventory().contains(f.getName()) && f.getDefinition().getNotedId() == -1)));
    }
}
