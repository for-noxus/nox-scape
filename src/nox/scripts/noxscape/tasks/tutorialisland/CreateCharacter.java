package nox.scripts.noxscape.tasks.tutorialisland;

import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.Tracker;

public class CreateCharacter extends NoxScapeNode {

    public CreateCharacter(NoxScapeNode next, ScriptContext ctx, String message, Tracker tracker) {
        super(next, ctx, message, tracker);
    }

    @Override
    public boolean isValid() {
        return ctx.getWidgets().isVisible(TutorialIslandConstants.WIDGET_ROOT_DISPLAYNAME);
    }

    @Override
    public int execute() {
        ctx.log("creating character...");
        return 5000;
    }
}
