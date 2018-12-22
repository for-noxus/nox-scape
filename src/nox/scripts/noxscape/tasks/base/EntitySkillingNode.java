package nox.scripts.noxscape.tasks.base;

import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.interfaces.ISkillable;
import nox.scripts.noxscape.util.LocationUtils;
import nox.scripts.noxscape.util.NRandom;
import nox.scripts.noxscape.util.Sleep;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.script.MethodProvider;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class EntitySkillingNode extends NoxScapeNode {

    private Predicate<RS2Object> entityValidationCondition;
    private Function<MethodProvider, RS2Object> fnFindEntity;

    private ISkillable skillableEntity;
    private int postInteractWaitTimeout;
    private int postInteractWaitInterval;
    private boolean powerFarming;
    private String[] dropAllExcept;

    private RS2Object previouslyInteractedEntity;
    private Position centerTile;
    private int radius;
    private Area area;

    private int findAttempts = 0;

    public EntitySkillingNode(ScriptContext ctx) {
        super(ctx);
    }

    public EntitySkillingNode boundedBy(Position centerTile, int radius) {
        this.centerTile = centerTile;
        this.radius = radius;
        return this;
    }

    public EntitySkillingNode boundedBy(Area area) {
        this.area = area;
        return this;
    }

    public EntitySkillingNode entityInvalidWhen(Predicate<RS2Object> entityValidationCondition, int timeout, int interval) {
        this.entityValidationCondition = entityValidationCondition;
        this.postInteractWaitTimeout = timeout;
        this.postInteractWaitInterval = interval;
        return this;
    }

    public EntitySkillingNode findEntityWith(Function<MethodProvider, RS2Object> fnFindEntity) {
        this.fnFindEntity = fnFindEntity;
        return this;
    }

    public EntitySkillingNode interactWith(ISkillable skillableEntity) {
        this.skillableEntity = skillableEntity;
        return this;
    }

    public EntitySkillingNode shouldPowerFarm(boolean powerFarming) {
        this.powerFarming = powerFarming;
        return this;
    }

    public EntitySkillingNode dropAllExcept(String[] itemNames) {
        this.dropAllExcept = itemNames;
        return this;
    }

    @Override
    public boolean isValid() {
        boolean isInArea = area != null && area.contains(ctx.myPosition());
        boolean isWithinBoundedRadius = centerTile != null && LocationUtils.manhattenDistance(centerTile, ctx.myPosition()) < radius;

        return !ctx.getInventory().isFull() && (isInArea || isWithinBoundedRadius);
    }

    @Override
    public int execute() throws InterruptedException {
        if (ctx.getInventory().isFull()) {
            if (powerFarming) {
                ctx.getInventory().dropAllExcept(dropAllExcept);
                ctx.sleep(0, 200);
            } else {
                this.complete("Inventory full, unable to acquire more " + skillableEntity.getName() + ".");
                return NRandom.humanized();
            }
        }

        if (isBusy() && (previouslyInteractedEntity == null || entityValidationCondition.test(previouslyInteractedEntity)))
            return NRandom.humanized() * 2;

        RS2Object entity = fnFindEntity != null ?
                fnFindEntity.apply(ctx) :
                ctx.getObjects().closest(skillableEntity.getName());

        if (entity == null) {
            if (++findAttempts > 25) {
                abort(String.format("Unable to locate entity (%s) for skilling node (%s)", skillableEntity.getName(), skillableEntity.getSkill().name()));
            }
            return 200;
        }

        findAttempts = 0;

        ctx.setTargetEntity(entity);

        if (!entity.hasAction(skillableEntity.getInteractAction())) {
            abort(String.format("Unable to interact with entity (%s) and action (%s), it contains \"%s\"", entity.getName(), skillableEntity.getInteractAction(), String.join(", ", entity.getActions())));
        }

        if (!entity.interact(skillableEntity.getInteractAction())) {
            abort(String.format("Error interacting interact with entity (%s) and action (%s)", entity.getName(), skillableEntity.getInteractAction()));
        }

        if (entityValidationCondition != null) {
            previouslyInteractedEntity = entity;
            Sleep.sleepUntil(() -> entityValidationCondition.test(ctx.getObjects().closest(f -> f.getPosition().equals(entity.getPosition()))), postInteractWaitTimeout, postInteractWaitInterval);
            ctx.logClass(this, "Rock has been mined out, switching...");
        }

        return NRandom.humanized();
    }

    private boolean isBusy() {
        return ctx.myPlayer().isMoving() || ctx.myPlayer().isAnimating();
    }
}
