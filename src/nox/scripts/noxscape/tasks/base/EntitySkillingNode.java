package nox.scripts.noxscape.tasks.base;

import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.interfaces.ISkillable;
import nox.scripts.noxscape.util.Sleep;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.script.MethodProvider;

import java.util.function.BooleanSupplier;

public class EntitySkillingNode extends NoxScapeNode {

    private BooleanSupplier postInteractWaitCondition;

    private ISkillable skillableEntity;
    private int postInteractWaitTimeout;
    private int postInteractWaitInterval;
    private boolean powerFarming;
    private String[] dropAllExcept;

    public EntitySkillingNode(ScriptContext ctx) {
        super(ctx);
    }

    public EntitySkillingNode afterInteractingWaitFor(BooleanSupplier postInteractWaitCondition, int timeout, int interval) {
        this.postInteractWaitCondition = postInteractWaitCondition;
        this.postInteractWaitTimeout = timeout;
        this.postInteractWaitInterval = interval;
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
        return ctx.getObjects().closest(skillableEntity.getName()) != null;
    }

    @Override
    public int execute() throws InterruptedException {
        if (ctx.getInventory().isFull()) {
            if (powerFarming) {
                ctx.getInventory().dropAllExcept(dropAllExcept);
                ctx.sleep(0, 100);
            } else {
                this.complete("Inventory full, unable to acquire more " + skillableEntity.getName() + ".");
                return MethodProvider.random(50, 1000);
            }
        }
        RS2Object entity = ctx.getObjects().closest(skillableEntity.getName());

        if (entity == null) {
            abort(String.format("Unable to locate entity (%s) for skilling node (%s)", skillableEntity.getName(), skillableEntity.getSkill().name()));
        }

        if (!entity.hasAction(skillableEntity.getInteractAction())) {
            abort(String.format("Unable to interact with entity (%s) and action (%s), it contains \"%s\"", entity.getName(), skillableEntity.getInteractAction(), String.join(", ", entity.getActions())));
        }

        if (!entity.interact(skillableEntity.getInteractAction())) {
            abort(String.format("Error interacting interact with entity (%s) and action (%s)", entity.getName(), skillableEntity.getInteractAction()));
        }

        Sleep.sleepUntil(postInteractWaitCondition, postInteractWaitTimeout, postInteractWaitInterval);

        return MethodProvider.random(100, 1000);
    }
}
