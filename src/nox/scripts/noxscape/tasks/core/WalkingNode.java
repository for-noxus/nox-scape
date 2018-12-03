package nox.scripts.noxscape.tasks.core;

import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.Tracker;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;

public class WalkingNode extends NoxScapeNode {

    protected Area destinationArea;
    protected Position destinationPosition;

    public WalkingNode toPosition(Position pos) {
        this.destinationPosition = pos;
        return this;
    }

    public WalkingNode toArea(Area area) {
        this.destinationArea = area;
        return this;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public int execute() throws InterruptedException {
        return 0;
    }
}
