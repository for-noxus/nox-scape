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
    PORT_SARIM_TREES("Trees North of Port Sarim", BankLocation.PORTSARIM, new Position(3050, 3265, 0), WoodcuttingEntity.TREE),
    VARROCK_TREES("Trees East of Varrock", BankLocation.VARROCK_EAST, new Position(3276, 3450, 0), WoodcuttingEntity.TREE),
    VARROCK_OAKS("Oaks East of Varrock", BankLocation.VARROCK_EAST, new Position(3280, 3429, 0), WoodcuttingEntity.OAK),
    FALADOR_N_OAKS("Oaks North of Falador", BankLocation.FALADOR_WEST, new Position(2951, 3403, 0), WoodcuttingEntity.OAK),
    PORT_SARIM_OAKS("Oaks North of Port Sarim", BankLocation.PORTSARIM, new Position(3039, 3264, 0), WoodcuttingEntity.OAK),
    DRAYNOR_OAKS("Oak trees directly east of Draynor Bank", BankLocation.DRAYNOR, new Position(3100, 3240, 0), WoodcuttingEntity.OAK),
    DRAYNOR_WILLOWS("Draynor willows by the river", BankLocation.DRAYNOR, new Position(3085, 3237, 0), WoodcuttingEntity.WILLOW),
    EDGEVILLE("Yews south of Edgeville bank", BankLocation.EDGEVILLE, new Position(3085, 3475, 0), WoodcuttingEntity.YEW),
    FALADOR_YEWS("Yews south of Falador", BankLocation.FALADOR_WEST, new Position[] { new Position(2998, 3313, 0), new Position(3018, 3316, 0), new Position(3041, 3320, 0) }, WoodcuttingEntity.YEW);

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
        return Arrays.asList(this.treesInArea).contains(e);
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
