package nox.scripts.noxscape.core.enums;

import nox.scripts.noxscape.core.interfaces.INameable;

public enum StopCondition implements INameable {
    UNSET("Unset"),
    TIME_ELAPSED("Time Elapsed"),
    MONEY_MADE("Money Made"),
    LEVELS_GAINED("Levels Gained"),
    XP_GAINED("XP Gained"),
    RESOURCES_ACTIONED("Actions Performed");

    private String name;

    StopCondition(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
