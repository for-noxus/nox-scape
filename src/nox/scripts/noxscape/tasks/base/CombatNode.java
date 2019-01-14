package nox.scripts.noxscape.tasks.base;

import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.api.CombatHelper;
import nox.scripts.noxscape.core.interfaces.ICombatable;
import nox.scripts.noxscape.core.interfaces.INameable;
import nox.scripts.noxscape.tasks.base.combat.CombatPreferenceProfile;
import nox.scripts.noxscape.util.NRandom;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Character;
import org.osbot.rs07.api.model.NPC;

import java.util.function.BooleanSupplier;

public class CombatNode extends NoxScapeNode {

    private ICombatable npcToFight;

    private CombatPreferenceProfile combatProfile;
    private CombatHelper combatHelper;
    private boolean shouldRunAway;

    public CombatNode(ScriptContext ctx) {
        super(ctx);
    }

    private Position[] runAwayDestinations;
    private BooleanSupplier interruptCondition;

    public CombatNode withProfile(CombatPreferenceProfile profile) {
        combatProfile = profile;
        this.combatHelper = ctx.getCombatHelper(combatProfile);
        return this;
    }

    public CombatNode runAwayFromAllCombat() {
        this.shouldRunAway = true;
        return this;
    }

    public CombatNode runAwayTo(Position... runAwayDestinations) {
        this.runAwayDestinations = runAwayDestinations;
        return this;
    }

    public CombatNode interruptIf(BooleanSupplier interruptCondition) {
        this.interruptCondition = interruptCondition;
        return this;
    }

    @Override
    public boolean isValid() {
        return ctx.getCombat().isFighting() && combatHelper.hasFood();
    }

    @Override
    public int execute() throws InterruptedException {
        Character beingFought = ctx.getCombat().getFighting();
        while (ctx.getCombat().getCombat().isFighting()) { //Todo <-- This line NPE's sometimes???
            if (interruptCondition != null && interruptCondition.getAsBoolean()) {
                ctx.logClass(this, "Interrupting combat prematurely for break condition");
                break;
            }

            if (combatHelper.hasFood()) {
                if (!combatHelper.checkHealth()) {
                    abort("Needs to eat and has food, but was unable to.");
                    return 50;
                }
            }
        }

        ctx.setTargetEntity(null);
        notifyAction("Killed " + beingFought.getName());
        complete("Finished fighting " + beingFought.getName());
        return NRandom.humanized();
    }
}
