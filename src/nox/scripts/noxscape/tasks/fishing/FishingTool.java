package nox.scripts.noxscape.tasks.fishing;

import nox.scripts.noxscape.core.interfaces.INameable;

public enum FishingTool implements INameable {
    NET("Netting", "Net", "Small fishing net", 1),
    BAIT("Fishing", "Bait", "Fishing rod", "Fishing bait", 5),
    FLY("Fly fishing", "Lure", "Fly fishing rod", "Feather", 20),
    HARPOON("Harpooning", "Harpoon", "Harpoon", 40),
    POT("Caging Lobs", "Cage", "Lobster pot", 35);

    private final String friendlyName;
    private final String actionName;
    private final String primaryItemName;

    private final String secondaryItemName;
    private final int minLevel;

    FishingTool(String actionName, String friendlyName, String primaryItemName, int minLevel) {
        this(actionName, friendlyName, primaryItemName, null, minLevel);
    }

    FishingTool(String actionName, String friendlyName, String primaryItemName, String secondaryItemName, int minLevel) {
        this.friendlyName = friendlyName;
        this.actionName = actionName;
        this.primaryItemName = primaryItemName;
        this.secondaryItemName = secondaryItemName;
        this.minLevel = minLevel;
    }

    @Override
    public String getName() {
        return friendlyName;
    }

    public String getActionName() {
        return actionName;
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
}
