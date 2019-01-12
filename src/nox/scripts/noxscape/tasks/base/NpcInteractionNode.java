package nox.scripts.noxscape.tasks.base;

import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.api.CombatHelper;
import nox.scripts.noxscape.core.interfaces.INameable;
import nox.scripts.noxscape.tasks.base.combat.CombatPreferenceProfile;
import nox.scripts.noxscape.util.Sleep;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;
import sun.plugin.com.PropertyGetDispatcher;

import java.util.Arrays;

public class NpcInteractionNode extends NoxScapeNode {

    private String npcName;
    private String interactAction;
    private String[] dialogueOptions;
    private CombatHelper combatHelper;

    public NpcInteractionNode interactWith(INameable npc) {
        this.npcName = npc.getName();

        return this;
    }

    public NpcInteractionNode interactWith(INameable npc, String action) {
        this.npcName = npc.getName();
        this.interactAction = action;
        return this;
    }

    public NpcInteractionNode walkThroughDialogue(String... dialogueOptions) {
        this.dialogueOptions = dialogueOptions;
        return this;
    }

    public NpcInteractionNode isCombat(CombatPreferenceProfile combatProfile) {
        this.combatHelper = ctx.getCombatHelper(combatProfile);
        return this;
    }

    @Override
    public boolean isValid() {
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
            if (combatHelper.hasFood()) {
                if (!combatHelper.checkHealth()) {
                    abort("Needed to eat, but was unable to for some reason");
                    return 50;
                }
            }
            npc = combatHelper.getNextTarget();

        } else {
            npc = ctx.getNpcs().closest(f -> f.getName().equals(npcName));
        }

        if (npc == null) {
            abort("Unable to locate closest NPC named " + npcName);
            return 5;
        }

        if (!npc.interact(interactAction)) {
            abort(String.format("Unable to interact with NPC (%s) with action %s", npcName, interactAction));
            return 5;
        }

        if (dialogueOptions != null) {
            Sleep.until(() -> ctx.getDialogues().inDialogue(), 10_000, 1_000);
            if (!ctx.getDialogues().inDialogue()) {
                abort("Unable to get in dialogue with NPC " + npc.getName());
                return 50;
            } else {
                ctx.getDialogues().completeDialogue(dialogueOptions);
            }
        } else if (combatHelper != null)
            Sleep.until(() -> ctx.getCombat().isFighting(), 10_000, 1_000);

        complete("Successfully interacted with (" + npcName + ")");
        return 100;
    }
}
