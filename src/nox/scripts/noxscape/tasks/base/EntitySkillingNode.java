package nox.scripts.noxscape.tasks.base;

import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.interfaces.ISkillable;
import nox.scripts.noxscape.tasks.mining.MiningEntity;
import nox.scripts.noxscape.tasks.mining.MiningMasterNode;
import nox.scripts.noxscape.util.LocationUtils;
import nox.scripts.noxscape.util.NRandom;
import nox.scripts.noxscape.util.Sleep;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.script.MethodProvider;

import java.awt.*;
import java.util.function.Predicate;

public class EntitySkillingNode extends NoxScapeNode {

    private Predicate<RS2Object> entityValidationCondition;
    private Predicate<RS2Object> fnFindEntity;

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

    public EntitySkillingNode findEntityWith(Predicate<RS2Object> fnFindEntity) {
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

        if (fnFindEntity == null)
            fnFindEntity = ent -> ent.getName().equals(skillableEntity.getName()) &&
                    ((area != null && area.contains(ent)) || (centerTile != null && LocationUtils.manhattenDistance(ent.getPosition(), centerTile) < radius));

        if (ctx.getDialogues().isPendingContinuation())
            ctx.getDialogues().completeDialogue();

        if (isBusy() && (previouslyInteractedEntity == null || entityValidationCondition.test(previouslyInteractedEntity))) {
            return NRandom.humanized() * 2;
        }

        RS2Object entity = ctx.getObjects().closest(ent -> fnFindEntity.test(ent));

        if (entity == null) {
            entity = Sleep.untilNotNull(() -> ctx.getObjects().closest(ent -> fnFindEntity.test(ent)), 120_000, 700);
            if (entity == null) {
                abort(String.format("Unable to locate entity (%s) for skilling node (%s)", skillableEntity.getName(), skillableEntity.getSkill().name()));
                return 500;
            }
        }

        findAttempts = 0;

        ctx.setTargetEntity(entity);

        Point mp = ctx.getMouse().getPosition();
        if (ctx.getMouse().isOnCursor(entity)) {
            if (MethodProvider.random(4) == 1) {
                int fuzzedX = mp.x + NRandom.fuzzedBounds(-4, 5, 4, 5);
                int fuzzedY = mp.y + NRandom.fuzzedBounds(-4, 5, 4, 5);
                ctx.getMouse().move(fuzzedX,fuzzedY);
            }
        }

        if (!entity.hasAction(skillableEntity.getInteractAction())) {
            abort(String.format("Unable to interact with entity (%s) and action (%s), it contains \"%s\"", entity.getName(), skillableEntity.getInteractAction(), String.join(", ", entity.getActions())));
        }

        if (!entity.interact(skillableEntity.getInteractAction())) {
            abort(String.format("Error interacting interact with entity (%s) and action (%s)", entity.getName(), skillableEntity.getInteractAction()));
        }

        long interactTime = System.currentTimeMillis();
        if (entityValidationCondition != null) {
            previouslyInteractedEntity = entity;
            Sleep.until(() -> {
                RS2Object obj = ctx.getObjects().closest(f -> f.getPosition().equals(previouslyInteractedEntity.getPosition()) && fnFindEntity.test(f));
                boolean passesTest = entityValidationCondition.test(obj);
                boolean longTimeSinceInteracting = (System.currentTimeMillis() - interactTime > 5000) && !ctx.myPlayer().isAnimating();
                return ctx.getDialogues().isPendingContinuation() || longTimeSinceInteracting || passesTest;
            }, postInteractWaitTimeout, postInteractWaitInterval);
        }

        return NRandom.humanized();
    }

    private boolean isBusy() {
        return ctx.myPlayer().isMoving() || ctx.myPlayer().isAnimating();
    }
}
