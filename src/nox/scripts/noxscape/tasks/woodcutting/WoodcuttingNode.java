package nox.scripts.noxscape.tasks.woodcutting;

import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.Tracker;
import nox.scripts.noxscape.tasks.core.EntitySkillingNode;

import java.util.List;

public class WoodcuttingNode extends EntitySkillingNode {

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public int execute() throws InterruptedException {
        return 0;
    }
}
