package nox.scripts.noxscape.tasks.core;

import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.Tracker;
import nox.scripts.noxscape.core.interfaces.ISkillable;

import java.util.List;
import java.util.function.BooleanSupplier;

public abstract class EntitySkillingNode extends NoxScapeNode {

    protected BooleanSupplier postInteractWaitCondition;

    public EntitySkillingNode(List children, ScriptContext ctx, String message, Tracker tracker) {
        super(children, ctx, message, tracker);
    }

    public EntitySkillingNode(NoxScapeNode child, ScriptContext ctx, String message, Tracker tracker) {
        super(child, ctx, message, tracker);
    }

    protected void setPostInteractWaitCondition(BooleanSupplier postInteractWaitCondition) {
        this.postInteractWaitCondition = postInteractWaitCondition;
    }

    protected boolean hasRequiredLevelForEntity(ISkillable skillable) {
        return ctx.getSkills().getStatic(skillable.getSkill()) >= skillable.getRequiredLevel();
    }
}
