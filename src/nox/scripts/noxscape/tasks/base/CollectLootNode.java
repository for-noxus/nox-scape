package nox.scripts.noxscape.tasks.base;

import com.sun.net.httpserver.Authenticator;
import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.util.NRandom;
import nox.scripts.noxscape.util.Sleep;
import org.osbot.rs07.api.model.GroundItem;

import java.util.Arrays;
import java.util.List;

public class CollectLootNode extends NoxScapeNode {

    private List<String> itemsToLoot;

    public CollectLootNode lootItems(String... itemsToLoot) {
        this.itemsToLoot = Arrays.asList(itemsToLoot);
        return this;
    }

    @Override
    public boolean isValid() {
        return getItemsToPickup().size() != 0;
    }

    @Override
    public int execute() throws InterruptedException {
        List<GroundItem> itemsToPickup = getItemsToPickup();
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

        List<GroundItem> itemsToPickup = ctx.getGroundItems().filter(f ->
                itemsToLoot.contains(f.getName()) &&
                        ctx.getMap().canReach(f) &&
                        (!isFull || (isFull && ctx.getInventory().contains(f.getName()) && f.getDefinition().getNotedId() == -1)));

        return itemsToPickup;
    }
}
