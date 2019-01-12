package nox.scripts.noxscape.tasks.base.combat;

import nox.scripts.noxscape.core.interfaces.INameable;

public enum Food implements INameable {
    SHRIMP("Shrimp", 3, false),
    SARDINE("Sardine", 4, false),
    BREAD("Bread", 5, false),
    HERRING("Herring", 5, false),
    MACKEREL("Mackerel", 6, true),
    TROUT("Trout", 7, false),
    COD("Cod", 7, true),
    PIKE("Pike", 8, false),
    SALMON("Salmon", 9, false),
    TUNA("Tuna", 10, false),
    RAINBOW_FISH("Rainbow fish", 11, true),
    STEW("Stew", 11, false),
    LOBSTER("Lobster", 12, false),
    BASS("Bass", 13, true),
    SWORDFISH("Swordfish", 14, false),
    MONKFISH("Monkfish", 16, true),
    CURRY("Curry", 19, true),
    SHARK("Shark", 20, true),
    SEA_TURTLE("Sea turtle", 21, true),
    MANTA_RAY("Manta ray", 22, true),
    TUNA_POTATO("Tuna potato", 22, true),
    DARK_CRAB("Dark crab", 22, true);

    private final String name;
    private final int healAmount;
    private final boolean isMembers;

    Food(String name, int healAmount, boolean isMembers) {
        this.name = name;
        this.healAmount = healAmount;
        this.isMembers = isMembers;
    }

    @Override
    public String getName() {
        return name;
    }
}
