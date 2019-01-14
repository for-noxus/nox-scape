package nox.scripts.noxscape.tasks.base.combat;

import nox.scripts.noxscape.core.interfaces.IBankable;
import nox.scripts.noxscape.core.interfaces.ICombatable;
import nox.scripts.noxscape.core.interfaces.INameable;
import nox.scripts.noxscape.tasks.base.banking.BankLocation;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum CombatLocation implements INameable, IBankable {
    LUMBRIDGE_CHICKENS("Lumbridge Chickens", BankLocation.LUMBRIDGE_UPPER, "Chicken", 1, 3, new Area(3225, 3301, 3240, 3287)),
    LUMBRIDGE_GOBLINS("Lumbridge Goblins", BankLocation.LUMBRIDGE_UPPER, "Goblin", 2, 10, new Area(3238, 3260, 3265, 3218)),
    LUMBRIDGE_COWS("Lumbridge Cows", BankLocation.LUMBRIDGE_UPPER, "Cow", 2, 10, new Area(3239, 3298, 3265, 3255)),
    LUMBRIDGE_FROGS("Lumbridge Frogs", BankLocation.LUMBRIDGE_UPPER, "Giant frog", 13, 20, new Area(3183, 3161, 3223, 3193)),
    EDGEVILLE_MEN("Edgeville Men", BankLocation.EDGEVILLE, "Man", 2, 5, new Area(3091, 3507, 3100, 3513)),
    ALKHARID_WARRIORS("Al-kharid Warriors", BankLocation.AL_KHARID, "Al-kharid warrior", 14, 20, new Area(3282, 3159, 3303, 3177));

    private static Position pos(int x, int y, int z) { return new Position(x, y, z); }

    private final int suggestedCombatLevel;

    private final Area combatArea;

    private final String name;
    private final BankLocation bankLocation;
    private String monsterName;
    private final int[] combatLevels;
    CombatLocation(String name, BankLocation bankLocation, String monsterName, int combatLevels, int suggestedCombatLevel, Position... boundedPositions) {
        this(name, bankLocation, monsterName, new int[] { combatLevels }, suggestedCombatLevel, new Area(boundedPositions));
    }

    CombatLocation(String name, BankLocation bankLocation, String monsterName, int[] combatLevels, int suggestedCombatLevel, Position... boundedPositions) {
        this(name, bankLocation, monsterName, combatLevels, suggestedCombatLevel, new Area(boundedPositions));
    }

    CombatLocation(String name, BankLocation bankLocation, String monsterName, int combatLevels, int suggestedCombatLevel, Area boundedArea) {
        this(name, bankLocation, monsterName, new int[] { combatLevels }, suggestedCombatLevel, boundedArea);
    }

    CombatLocation(String name, BankLocation bankLocation, String monsterName, int[] combatLevels, int suggestedCombatLevel, Area boundedArea) {
        this.name = name;
        this.bankLocation = bankLocation;
        this.monsterName = monsterName;
        this.combatLevels = combatLevels;
        this.suggestedCombatLevel = suggestedCombatLevel;
        this.combatArea = boundedArea;
    }

    public List<ICombatable> getAsCombatable() {
        return Arrays.stream(combatLevels).mapToObj(m -> new ICombatable() {
            @Override
            public String getName() {
                return monsterName;
            }

            @Override
            public int getLevel() {
                return m;
            }
        }).collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return name;
    }

    public String getMonsterName() {
        return monsterName;
    }

    public int getSuggestedCombatLevel() {
        return suggestedCombatLevel;
    }

    public Area getCombatArea() {
        return this.combatArea;
    }

    @Override
    public BankLocation getBank() {
        return bankLocation;
    }

    @Override
    public BankLocation getDepositBox() {
        return bankLocation;
    }
}
