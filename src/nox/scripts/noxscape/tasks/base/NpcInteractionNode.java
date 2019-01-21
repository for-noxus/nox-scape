package nox.scripts.noxscape.tasks.base;

import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.api.CombatHelper;
import nox.scripts.noxscape.core.interfaces.INameable;
import nox.scripts.noxscape.tasks.base.combat.CombatPreferenceProfile;
import nox.scripts.noxscape.util.Sleep;
import org.osbot.rs07.api.model.NPC;

import java.util.Arrays;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public class NpcInteractionNode extends NoxScapeNode {

    private final int MAX_INTERACTION_ATTEMPTS = 4;

    private String npcName;
    private String interactAction;
    private String[] dialogueOptions;
    private CombatHelper combatHelper;
    private CombatPreferenceProfile combatProfile;
    private BooleanSupplier postInteractWaitCondition;
    private int postInteractTimeout;
    private int postInteractInterval;
    private Predicate<NPC> npcValidationCondition;

    public NpcInteractionNode(ScriptContext ctx) {
        super(ctx);
    }

    public NpcInteractionNode interactWith(INameable npc) {
        this.npcName = npc.getName();

        return this;
    }

    public NpcInteractionNode interactWith(INameable npc, String action) {
        this.npcName = npc.getName();
        this.interactAction = action;
        return this;
    }

    public NpcInteractionNode afterInteractingWaitFor(BooleanSupplier condition, int timeout, int interval) {
        this.postInteractWaitCondition = condition;
        this.postInteractTimeout = timeout;
        this.postInteractInterval = interval;
        return this;
    }

    public NpcInteractionNode addNpcValidation(Predicate<NPC> condition) {
        this.npcValidationCondition = condition;
        return this;
    }

    public NpcInteractionNode walkThroughDialogue(String... dialogueOptions) {
        this.dialogueOptions = dialogueOptions;
        return this;
    }

    public NpcInteractionNode isCombat(CombatPreferenceProfile combatProfile) {
        this.combatHelper = ctx.getCombatHelper(combatProfile);
        this.combatProfile = combatProfile;
        this.interactAction = "Attack";
        return this;
    }

    protected boolean baseExecutionCondition() {
        if (npcName == null && combatHelper == null) {
            abort("Queued up an NPCNode, but no NPCs were set!");
            return false;
        }

        return (combatHelper != null && combatHelper.isInCombatArea()) ||
                (ctx.getNpcs().closest(f -> f.getPosition() != null && ctx.myPosition().distance(f.getPosition()) < 10 && ctx.getMap().canReach(f.getPosition())) != null);
    }

    @Override
    public int execute() throws InterruptedException {
        NPC npc;

        if (combatHelper != null) {
            if (ctx.getCombatStyles().getCurrentStyle() != combatProfile.getStyle()) {
                if (!ctx.getCombatStyles().setStyle(combatProfile.getStyle()))
                    ctx.logClass(this, "Error setting combat style to " + combatProfile.getStyle());
            }
            if (combatHelper.hasFood()) {
                if (!combatHelper.checkHealth()) {
                    abort("Needed to eat, but was unable to for some reason");
                    return 50;
                }
            }
            npc = combatHelper.getNextTarget();

        } else {
            npc = ctx.getNpcs().closest(f -> f.getName().equals(npcName) &&
                    (npcValidationCondition == null || npcValidationCondition.test(f)) &&
                    (interactAction == null || (f.getActions() != null && Arrays.asList(f.getActions()).contains(interactAction))));
        }

        if (npc == null) {
            abort("Unable to locate closest NPC named " + npcName);
            return 5;
        }

        ctx.setTargetEntity(npc);

        if (!ctx.getMap().canReach(npc)) {
            if (!ctx.getWalking().webWalk(npc.getPosition())) {
                abort("Unable to walk to located NPC " + npc.getName());
            }
        }

        int interactAttempts = 0;
        while (++interactAttempts <= MAX_INTERACTION_ATTEMPTS) {
            if (!npc.interact(interactAction) && interactAttempts >= MAX_INTERACTION_ATTEMPTS) {
                abort(String.format("Unable to interact with NPC (%s) with action %s", npc.getName(), interactAction));
                return 5;
            }
            else
                break;
        }

        if (dialogueOptions != null) {
            Sleep.until(() -> ctx.getDialogues().inDialogue(), 10_000, 1_000);
            if (!ctx.getDialogues().inDialogue()) {
                abort("Unable to get in dialogue with NPC " + npc.getName());
                return 50;
            } else {
                ctx.getDialogues().completeDialogue(dialogueOptions);
            }
        } else if (combatHelper != null) {
            Sleep.until(() -> ctx.getCombat().isFighting(), 10_000, 1_000);

        } else if (postInteractWaitCondition != null) {
            Sleep.until(postInteractWaitCondition, postInteractTimeout, postInteractInterval);
        }

        notifyAction("Interacted with " + npc.getName());
        complete("Successfully interacted with (" + npc.getName() + ")");
        return 100;
    }
}
