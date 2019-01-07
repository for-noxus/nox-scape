package nox.scripts.noxscape.tasks.base;

import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.interfaces.INameable;
import nox.scripts.noxscape.util.Sleep;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.NPC;
import sun.plugin.com.PropertyGetDispatcher;

public class NpcInteractionNode extends NoxScapeNode {

    private String npcName;
    private String interactAction;
    private String[] dialogueOptions;
    private boolean isCombat;

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

    public NpcInteractionNode markAsCombat() {
        this.isCombat = true;
        return this;
    }

    @Override
    public boolean isValid() {
        if (npcName == null) {
            abort("Queued up and NPCNode, and no NPC was set!");
            return false;
        }

        NPC targetNpc =  ctx.getNpcs().closest(f -> f.getName() != null && f.getName().equals(npcName));
        Position targetPosition = targetNpc == null ? null : targetNpc.getPosition();
        return targetNpc != null && targetPosition != null && ctx.myPosition().distance(targetPosition) < 10 && ctx.getMap().canReach(targetPosition);
    }

    @Override
    public int execute() throws InterruptedException {
        NPC npc = ctx.getNpcs().closest(f -> f.getName().equals(npcName));

        if (npc == null) {
            abort("Unable to locate closest NPC named " + npcName);
            return 5;
        }

        if (!npc.interact(interactAction)) {
            abort(String.format("Unable to interact with NPC (%s) with action %s", npcName, interactAction));
            return 5;
        }

        if (dialogueOptions != null)
            Sleep.until(() -> ctx.getDialogues().inDialogue(), 10_000, 1_000);
        else if (isCombat)
            Sleep.until(() -> ctx.getCombat().isFighting(), 10_000, 1_000);

        complete("Successfully interacted with (" + npcName + ")");
        return 100;
    }
}
