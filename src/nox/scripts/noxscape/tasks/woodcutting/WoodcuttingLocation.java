package nox.scripts.noxscape.tasks.woodcutting;

import nox.scripts.noxscape.core.interfaces.IBankable;
import nox.scripts.noxscape.core.interfaces.ILocateable;
import nox.scripts.noxscape.core.interfaces.INameable;
import nox.scripts.noxscape.tasks.base.banking.BankLocation;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.map.constants.Banks;

import java.util.Arrays;

public enum WoodcuttingLocation implements IBankable, INameable, ILocateable {
    FALADOR_YEWS("Yews south of Falador", BankLocation.FALADOR_WEST, new Position[] { new Position(2998, 3313, 0), new Position(3018, 3316, 0), new Position(3041, 3320, 0) }, WoodcuttingEntity.YEW),
    DRAYNOR_WILLOWS("Draynor willows by the river", BankLocation.DRAYNOR, new Position(3085, 3236, 0), WoodcuttingEntity.WILLOW),
    DRAYNOR_OAKS("Oak trees directly east of Draynor Bank", BankLocation.DRAYNOR, new Position(3100, 3240, 0), WoodcuttingEntity.OAK),
    LUMBRIDGE("Lumbridge, directly west of the castle.", BankLocation.LUMBRIDGE_UPPER, new Position(3190, 3220, 0), WoodcuttingEntity.TREE, WoodcuttingEntity.OAK),
    EDGEVILLE("Yews south of Edgeville bank", BankLocation.EDGEVILLE, new Position(3085, 3475, 0), WoodcuttingEntity.YEW);

    private final String name;
    private final BankLocation closestBank;
    private final Position[] positions;
    private final WoodcuttingEntity[] treesInArea;

    WoodcuttingLocation(String name, BankLocation closestBank, Position position, WoodcuttingEntity... treesInArea) {
        this(name, closestBank, new Position[] { position }, treesInArea);
    }

    WoodcuttingLocation(String name, BankLocation closestBank, Position[] positions, WoodcuttingEntity... treesInArea) {
        this.name = name;
        this.closestBank = closestBank;
        this.positions = positions;
        this.treesInArea = treesInArea;
    }

    public String getName() {
        return name;
    }

    public Position[] getPositions() {
        return positions;
    }

    public WoodcuttingEntity[] getTreesInArea() {
        return treesInArea;
    }

    public boolean containsTree(WoodcuttingEntity e) {
        return Arrays.stream(this.treesInArea).anyMatch(e::equals);
    }

    public Position centerPoint() {
        if (positions.length == 0)
            return null;

        int avgX = 0, avgY = 0;
        for (Position p: positions) {
            avgX += p.getX();
            avgY += p.getY();
        }

        avgX /= positions.length;
        avgY /= positions.length;

        return new Position(avgX, avgY, positions[0].getZ());
    }

    public int distanceToCenterPoint(Position pos) {
        return pos.distance(centerPoint());
    }

    @Override
    public BankLocation getBank() {
        return closestBank;
    }

    @Override
    public BankLocation getDepositBox() {
        return null;
    }

    @Override
    public Position getPosition() {
        return centerPoint();
    }
}
