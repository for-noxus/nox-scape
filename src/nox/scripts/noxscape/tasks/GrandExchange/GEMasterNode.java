package nox.scripts.noxscape.tasks.GrandExchange;

import nox.scripts.noxscape.core.MasterNodeInformation;
import nox.scripts.noxscape.core.NoxScapeMasterNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.enums.Duration;
import nox.scripts.noxscape.core.enums.Frequency;
import nox.scripts.noxscape.core.enums.MasterNodeType;

import java.util.List;

public class GEMasterNode extends NoxScapeMasterNode {

    public GEMasterNode(ScriptContext ctx) {
        super(ctx);
        this.nodeInformation = new MasterNodeInformation(
                "Grand Exchange",
                "Handling items at the Grand Exchange",
                Frequency.MANUAL,
                Duration.COMPLETION,
                MasterNodeType.OTHER);
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    @Override
    public void initializeNodes() {

    }

    @Override
    public boolean requiresPreExecution() {
        return false;
    }

    public static class Configuration {
        private List<GEItem> itemsToHandle;
    }
}
