package nox.scripts.noxscape.tasks.base;

import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.interfaces.ILocateable;
import nox.scripts.noxscape.util.NRandom;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.event.Event;
import org.osbot.rs07.event.WalkingEvent;
import org.osbot.rs07.event.WebWalkEvent;
import org.osbot.rs07.event.webwalk.PathPreferenceProfile;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.utility.Condition;

import java.util.Arrays;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class WalkingNode extends NoxScapeNode {

    protected Area destinationArea;
    protected Position destinationPosition;
    private BooleanSupplier breakCondition;
    private PathPreferenceProfile pathPreferenceProfile;
    private boolean isWebWalk = false;
    private boolean isExact = false;
    private Function<WalkingEvent, WalkingEvent> walkingEventConfig;

    public WalkingNode(ScriptContext ctx) {
        super(ctx);
    }

    public WalkingNode toPosition(Position pos) {
        this.destinationPosition = pos;
        return this;
    }

    public WalkingNode toPosition(ILocateable locateable) {
        this.destinationPosition = locateable.getPosition();
        return this;
    }

    public WalkingNode toArea(Area area) {
        this.destinationArea = area;
        return this;
    }

    public WalkingNode toClosestBankFrom(Area... bankAreas) {
        if (bankAreas.length > 0) {
            WebWalkEvent wwe = new WebWalkEvent(bankAreas);
            wwe.prefetchRequirements(ctx);
            destinationArea = Arrays.stream(bankAreas).filter(f -> f.contains(wwe.getDestination())).findFirst().orElse(null);
        }

        return this;
    }

    public WalkingNode breakOn(BooleanSupplier breakCondition) {
        this.breakCondition = breakCondition;
        return this;
    }

    public WalkingNode configureWalkEvent(Function<WalkingEvent, WalkingEvent> cfg) {
        this.walkingEventConfig = cfg;
        return this;
    }

    public WalkingNode isWebWalk(boolean isWebWalk) {
        this.isWebWalk = isWebWalk;
        return this;
    }

    public WalkingNode isExactWebWalk(boolean isWebWalk) {
        this.isWebWalk = isWebWalk;
        this.isExact = true;
        return this;
    }

    public WalkingNode setPathProfile(PathPreferenceProfile pathPreferenceProfile) {
        this.pathPreferenceProfile = pathPreferenceProfile;
        return this;
    }

    @Override
    public boolean isValid() {
        boolean isExactWalk = destinationPosition == null || (isWebWalk && isExact) || walkingEventConfig == null || walkingEventConfig.apply(new WalkingEvent()).getMinDistanceThreshold() == 1;
        boolean playerByPosition = destinationPosition != null && ctx.myPosition().distance(destinationPosition) <= 8;
        boolean playerInArea = destinationArea != null && destinationArea.contains(ctx.myPosition());
        return (!playerByPosition || !playerInArea) || (isExactWalk && destinationPosition != null && ctx.myPosition().distance(destinationPosition) != 0);
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

            ((WebWalkEvent)event).prefetchRequirements(ctx);

            ((WebWalkEvent)event).setBreakCondition(new Condition() {
                @Override
                public boolean evaluate() {
                    return (breakCondition != null && breakCondition.getAsBoolean());
                }
            });

            ((WebWalkEvent)event).setSourcePosition(ctx.myPosition());

        } else {
            if (destinationArea != null)
                event = new WalkingEvent(destinationArea);
            else if (destinationPosition != null)
                event = new WalkingEvent(destinationPosition);

            if (walkingEventConfig != null)
                walkingEventConfig.apply(((WalkingEvent)event));

            if (breakCondition != null)
                ((WalkingEvent)event).setBreakCondition(new Condition() {
                    @Override
                    public boolean evaluate() {
                        return breakCondition.getAsBoolean();
                    }
                });
        }

        ctx.execute(event);

        if (event.hasFinished() || (event.hasFailed() && breakCondition != null && breakCondition.getAsBoolean())) {
            if (isWebWalk && isExact && !ctx.myPosition().equals(destinationPosition)) {
                WalkingEvent e = new WalkingEvent(destinationPosition).setMinDistanceThreshold(0);
                ctx.execute(e);
                if (e.hasFailed())
                    abort(String.format("Error walking to exact destination. Script is at (%s) failed to reach (%s)", ctx.myPosition(), destinationPosition));
                else
                    complete("Successfully completed exact walking event to " + (destinationPosition != null ? destinationPosition.toString() : destinationArea.toString()));
            } else
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
