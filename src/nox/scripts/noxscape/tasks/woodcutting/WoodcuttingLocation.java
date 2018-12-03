package nox.scripts.noxscape.tasks.woodcutting;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;

public enum WoodcuttingLocation {
    FALADOR_YEWS("Yews south of Falador", new Position[] { new Position(2998, 3313, 0), new Position(3018, 3316, 0), new Position(3041, 3320, 0) }, WoodcuttingEntities.YEW),
    DRAYNOR_WILLOWS("Draynor willows by the river", new Position(3085, 3236, 0), WoodcuttingEntities.WILLOW),
    DRAYNOR_OAKS("Oak trees directly east of the bank", new Position(3100, 3240, 0), WoodcuttingEntities.OAK),
    LUMBRIDGE("Lumbridge, directly west of the castle.", new Position(3190, 3220, 0), WoodcuttingEntities.TREE, WoodcuttingEntities.OAK),
    EDGEVILLE("Yews south of Edgeville bank", new Position(3085, 3475, 0), WoodcuttingEntities.YEW);

    private final String name;
    private final Position[] positions;
    private final WoodcuttingEntities[] treesInArea;

    WoodcuttingLocation(String name, Position position, WoodcuttingEntities... treesInArea) {
        this(name, new Position[] { position }, treesInArea);
    }

    WoodcuttingLocation(String name, Position[] positions, WoodcuttingEntities... treesInArea) {
        this.name = name;
        this.positions = positions;
        this.treesInArea = treesInArea;
    }

}
