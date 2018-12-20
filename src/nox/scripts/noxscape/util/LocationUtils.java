package nox.scripts.noxscape.util;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.event.WebWalkEvent;
import org.osbot.rs07.script.MethodProvider;

public final class LocationUtils {

    public static Position getClosestPositionByWebWalking(MethodProvider provider, Position startPos, Position... destinations) {
        WebWalkEvent wwe = new WebWalkEvent(destinations);
        wwe.setSourcePosition(startPos);
        wwe.prefetchRequirements(provider);
        return wwe.getDestination();
    }

    public static Position getClosestAreaByWebWalking(MethodProvider provider, Position startPos, Area... destinations) {
        WebWalkEvent wwe = new WebWalkEvent(destinations);
        wwe.setSourcePosition(startPos);
        wwe.prefetchRequirements(provider);
        return wwe.getDestination();
    }

    public static int manhattenDistance(Position pos1, Position pos2) {
        return Math.abs(pos1.getX() - pos2.getX()) + Math.abs(pos1.getY() - pos2.getY());
    }
}
