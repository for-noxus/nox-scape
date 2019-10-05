package nox.scripts.noxscape.tasks.fishing;

import nox.scripts.noxscape.core.interfaces.INameable;

import java.util.Arrays;
import java.util.List;

public enum FishingTool implements INameable {
    NET("Netting", "Small fishing net", "Small fishing net", 1, Arrays.asList("Shrimps")),
    BAIT("Fishing", "Bait", "Fishing rod", "Fishing bait", 5, Arrays.asList("Sardines", "Herring", "Pike")),
    FLY("Fly fishing", "Lure", "Fly fishing rod", "Feather", 20, Arrays.asList("Trout, Salmon")),
    HARPOON("Harpooning", "Harpoon", "Harpoon", 40, Arrays.asList("Tuna, Swordfish")),
    POT("Caging Lobs", "Cage", "Lobster pot", 35, Arrays.asList("Lobster"));

    private final String friendlyName;

    private final String verbName;
    private final String primaryItemName;
    private final String secondaryItemName;

    private final int minLevel;
    private final List<String> possibleFish;
    FishingTool(String verbName, String friendlyName, String primaryItemName, int minLevel, List<String> possibleFish) {
        this(verbName, friendlyName, primaryItemName, null, minLevel, possibleFish);
    }

    FishingTool(String verbName, String friendlyName, String primaryItemName, String secondaryItemName, int minLevel, List<String> possibleFish) {
        this.friendlyName = friendlyName;
        this.verbName = verbName;
        this.primaryItemName = primaryItemName;
        this.secondaryItemName = secondaryItemName;
        this.minLevel = minLevel;
        this.possibleFish = possibleFish;
    }

    @Override
    public String getName() {
        return friendlyName;
    }

    public String getVerbName() {
        return verbName;
    }

    public String getPrimaryItemName() {
        return primaryItemName;
    }

    public String getSecondaryItemName() {
        return secondaryItemName;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public List<String> getPossibleFish() {
        return possibleFish;
    }
}
