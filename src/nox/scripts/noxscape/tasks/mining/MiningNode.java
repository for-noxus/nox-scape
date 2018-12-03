package nox.scripts.noxscape.tasks.mining;

import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.Tracker;
import nox.scripts.noxscape.tasks.core.EntitySkillingNode;

import java.util.List;

public class MiningNode extends EntitySkillingNode {

    public MiningNode(List children, ScriptContext ctx, String message, Tracker tracker) {
        super(children, ctx, message, tracker);
    }

    public MiningNode(NoxScapeNode child, ScriptContext ctx, String message, Tracker tracker) {
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
