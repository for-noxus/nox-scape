package nox.scripts.noxscape.tasks.walking;

import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.Tracker;

import java.util.List;

public class WalkingNode extends NoxScapeNode {

    public WalkingNode(List children, ScriptContext ctx, String message, Tracker tracker) {
        super(children, ctx, message, tracker);
    }

    public WalkingNode(NoxScapeNode child, ScriptContext ctx, String message, Tracker tracker) {
        super(child, ctx, message, tracker);
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public int execute() throws InterruptedException {
        return 0;
    }
}
