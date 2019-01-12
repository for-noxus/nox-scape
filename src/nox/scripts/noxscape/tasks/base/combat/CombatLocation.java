package nox.scripts.noxscape.tasks.base.combat;

import nox.scripts.noxscape.core.interfaces.ICombatable;
import nox.scripts.noxscape.core.interfaces.ILocateable;
import nox.scripts.noxscape.core.interfaces.INameable;
import nox.scripts.noxscape.tasks.base.banking.BankLocation;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;

public enum CombatLocation implements ICombatable, INameable {
    LUMBRIDGE_MEN("Lumridge Men", BankLocation.LUMBRIDGE_UPPER, 2, 5, pos(1, 2, 0)),
    LUMBRIDGE_CHICKENS("Lumbridge Chickens", BankLocation.LUMBRIDGE_UPPER, 2, 3, pos(1, 2, 0)),
    LUMBRIDGE_GOBLINS("Lumbridge Goblins", BankLocation.LUMBRIDGE_UPPER, 5, 10, pos(1, 2, 0)),
    LUMBRIDGE_COWS("Lumbridge Cows", BankLocation.LUMBRIDGE_UPPER, 5, 10, pos(1, 2, 0)),
    LUMBRIDGE_FROGS("Lumbridge Frogs", BankLocation.LUMBRIDGE_UPPER, 13, 20, pos(1,2,0)),
    EDGEVILLE_MEN("Edgeville Men", BankLocation.EDGEVILLE, 2, 5, pos(1, 2, 0)),
    ALKHARID_WARRIORS("Al-kharid Warriors", BankLocation.AL_KHARID, 14, 20, pos(1, 2, 0));

    private static Position pos(int x, int y, int z) { return new Position(x, y, z); }

    private final int suggestedCombatLevel;
    private final Area combatArea;
    private final String name;
    private final BankLocation bankLocation;
    private final int combatLevel;

    CombatLocation(String name, BankLocation bankLocation, int combatLevel, int suggestedCombatLevel, Position... boundedPositions) {
        this.name = name;
        this.bankLocation = bankLocation;
        this.combatLevel = combatLevel;
        this.suggestedCombatLevel = suggestedCombatLevel;
        this.combatArea = new Area(boundedPositions);
    }

    @Override
    public int getLevel() {
        return combatLevel;
    }

    @Override
    public String getName() {
        return name;
    }

    public int getSuggestedCombatLevel() {
        return suggestedCombatLevel;
    }

    public Area getCombatArea() {
        return this.combatArea;
    }
}
