package nox.scripts.noxscape.tasks.base.NpcStore;

import nox.scripts.noxscape.core.CachedItem;
import nox.scripts.noxscape.core.interfaces.ILocateable;
import nox.scripts.noxscape.core.interfaces.INameable;
import org.osbot.rs07.api.map.Position;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public enum NpcStoreLocation implements INameable, ILocateable {
    GERRANT_FISH_PORTSARIM("Gerrant's Fishy Business", "Gerrant", new Position(3014, 3225, 0), StoreItems::fishing),
    GENERAL_STORE_ALKHARID("Al-Kharid General Store", "Shop keeper", new Position(3313, 3180, 0), StoreItems::general),
    GENERAL_STORE_FALADOR("Falador General Store", "Shop keeper", new Position(2959, 3588, 0), StoreItems::general),
    GENERAL_STORE_LUMBRIDGE("Lumbridge General Store", "Shop keeper", new Position(3210, 3245, 0), StoreItems::general),
    GENERAL_STORE_RIMMINGTON("Rimmington General Store", "Shop keeper", new Position(2947, 3213, 0), StoreItems::general),
    GENERAL_STORE_VARROCK("Varrock General Store", "Shop keeper", new Position(3215, 3415, 0), StoreItems::general);

    private final String shopName;
    private final String npcName;
    private final Position shopPosition;
    private final Supplier<List<CachedItem>> shopItems;
    private final boolean isGeneralStore;

    NpcStoreLocation(String shopName, String npcName, Position shopPosition, Supplier<List<CachedItem>> shopItems) {
        this(shopName, npcName, shopPosition, shopItems, false);
    }

    NpcStoreLocation(String shopName, String npcName, Position shopPosition, Supplier<List<CachedItem>> shopItems, boolean isGeneralStore) {
        this.shopName = shopName;
        this.npcName = npcName;
        this.shopPosition = shopPosition;
        this.shopItems = shopItems;
        this.isGeneralStore = isGeneralStore;
    }

    public String getNpcName() {
        return npcName;
    }

    public static NpcStoreLocation forItem(String itemName, Position fromPosition) {
        return Arrays.stream(NpcStoreLocation.values())
                .filter(f -> f.getShopItems().stream().anyMatch(a -> a.getName().equals(itemName)))
                .min(Comparator.comparingInt(store -> store.getPosition().distance(fromPosition))   )
                .orElse(null);
    }

    public static NpcStoreLocation forItem(String itemName) {
        return Arrays.stream(NpcStoreLocation.values())
                .filter(f -> f.getShopItems().stream().anyMatch(a -> a.getName().equals(itemName)))
                .findAny().orElse(null);
    }

    public List<CachedItem> getShopItems() {
        return shopItems.get();
    }

    @Override
    public Position getPosition() {
        return shopPosition;
    }

    @Override
    public String getName() {
        return shopName;
    }

    public boolean isGeneralStore() {
        return isGeneralStore;
    }
}
