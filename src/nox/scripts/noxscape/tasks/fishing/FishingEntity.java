package nox.scripts.noxscape.tasks.fishing;

import nox.scripts.noxscape.core.interfaces.INameable;

public enum FishingEntity implements INameable {
    NET("Netting", "Net", "Small fishing net"),
    BAIT("Fishing", "Bait", "Fishing rod", "Fishing bait"),
    FLY("Fly fishing", "Lure", "Fly fishing rod", "Feather"),
    HARPOON("Harpooning", "Harpoon", "Harpoon"),
    POT("Caging Lobs", "Cage", "Lobster pot");

    private final String friendlyName;
    private final String actionName;
    private final String primaryItemName;

    private final String secondaryItemName;

    FishingEntity(String friendlyName, String actionName, String primaryItemName) {
        this(friendlyName, actionName, primaryItemName, null);
    }

    FishingEntity(String friendlyName, String actionName, String primaryItemName, String secondaryItemName) {
        this.friendlyName = friendlyName;
        this.actionName = actionName;
        this.primaryItemName = primaryItemName;
        this.secondaryItemName = secondaryItemName;
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
}
