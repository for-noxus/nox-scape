package nox.scripts.noxscape.tasks.base;

import nox.scripts.noxscape.core.NoxScapeNode;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.event.Event;
import org.osbot.rs07.event.WalkingEvent;
import org.osbot.rs07.event.WebWalkEvent;
import org.osbot.rs07.event.webwalk.PathPreferenceProfile;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.utility.Condition;

import java.util.function.BooleanSupplier;

public class WalkingNode extends NoxScapeNode {

    protected Area destinationArea;
    protected Position destinationPosition;
    private BooleanSupplier breakCondition;
    private PathPreferenceProfile pathPreferenceProfile;
    private boolean isWebWalk;

    public WalkingNode toPosition(Position pos) {
        this.destinationPosition = pos;
        return this;
    }

    public WalkingNode toArea(Area area) {
        this.destinationArea = area;
        return this;
    }

    public WalkingNode breakOn(BooleanSupplier breakCondition) {
        this.breakCondition = breakCondition;
        return this;
    }

    public WalkingNode isWebWalk(boolean isWebWalk) {
        this.isWebWalk = isWebWalk;
        return this;
    }

    public WalkingNode setPathProfile(PathPreferenceProfile pathPreferenceProfile) {
        this.pathPreferenceProfile = pathPreferenceProfile;
        return this;
    }

    @Override
    public boolean isValid() {
        boolean playerByPosition = destinationPosition != null && ctx.myPosition().distance(destinationPosition) <= 8;
        boolean playerInArea = destinationArea != null && destinationArea.contains(ctx.myPlayer().getPosition());
        return !playerByPosition || !playerInArea;
    }

    @Override
    public int execute() throws InterruptedException {
        Event event = null;
        if (isWebWalk) {
            if (destinationArea != null)
                event = new WebWalkEvent(destinationArea);
            else if (destinationPosition != null)
                event = new WebWalkEvent(destinationPosition);

            if (event == null)
                abort(String.format("Unable to set event via area (%s) or position (%s)", destinationArea, destinationPosition));

            if (pathPreferenceProfile != null)
                ((WebWalkEvent)event).setPathPreferenceProfile(pathPreferenceProfile);

            if (breakCondition != null)
                ((WebWalkEvent)event).setBreakCondition(new Condition() {
                    @Override
                    public boolean evaluate() {
                        return breakCondition.getAsBoolean();
                    }
                });

            ctx.execute(event);
        } else {
            if (destinationArea != null)
                event = new WalkingEvent(destinationArea);
            else if (destinationPosition != null)
                event = new WalkingEvent(destinationPosition);

            if (breakCondition != null)
                ((WalkingEvent)event).setBreakCondition(new Condition() {
                    @Override
                    public boolean evaluate() {
                        return breakCondition.getAsBoolean();
                    }
                });
        }

        ctx.execute(event);

        if (event.hasFinished()) {
            complete("Successfully completed walking event to " + (destinationPosition != null ? destinationPosition.toString() : destinationArea.toString()));
        } else if (event.hasFailed()) {
            if (isWebWalk)
                abort("WebWalking event failed, aborting. Stopped at " + ((WebWalkEvent)event).getCompletion() + "%");
            else
                abort("Walking event failed. Aborting");
        }

        return MethodProvider.random(0, 1200);
    }
}
