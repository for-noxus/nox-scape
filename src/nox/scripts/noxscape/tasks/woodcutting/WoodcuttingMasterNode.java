package nox.scripts.noxscape.tasks.woodcutting;

import nox.scripts.noxscape.core.MasterNodeInformation;
import nox.scripts.noxscape.core.NoxScapeMasterNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.enums.Duration;
import nox.scripts.noxscape.core.enums.Frequency;
import nox.scripts.noxscape.core.enums.MasterNodeType;
import nox.scripts.noxscape.tasks.tutorialisland.TutorialIslandTracker;
import nox.scripts.noxscape.tasks.tutorialisland.nodes.*;

import java.util.ArrayList;
import java.util.Arrays;

public class WoodcuttingMasterNode extends NoxScapeMasterNode {

    public WoodcuttingMasterNode(ScriptContext ctx) {
        super(ctx);
        this.tracker = new TutorialIslandTracker();
        this.nodeInformation = getMasterNodeInformation();
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    @Override
    public void initializeNodes() {


        setEntryPoint();

        if (this.getCurrentNode() == null) {
            this.abort("Unable to find a valid entrypoint.");
        }

        ctx.logClass(this, String.format("Initialized %d nodes.", nodes.size()));
    }

    @Override
    public MasterNodeInformation getMasterNodeInformation() {
        if (nodeInformation != null)
            return nodeInformation;

        nodeInformation = new MasterNodeInformation(
                "Woodcutting",
                "Completes Tutorial Island",
                Frequency.MANUAL,
                Duration.COMPLETION,
                MasterNodeType.QUEST);

        return nodeInformation;
    }

    @Override
    public boolean isCompleted() {
        return !canExecute();
    }
}
