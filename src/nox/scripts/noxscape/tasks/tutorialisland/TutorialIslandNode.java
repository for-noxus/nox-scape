package nox.scripts.noxscape.tasks.tutorialisland;

import nox.scripts.noxscape.NoxScape;
import nox.scripts.noxscape.core.MasterNodeInformation;
import nox.scripts.noxscape.core.NoxScapeMasterNode;
import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.enums.Duration;
import nox.scripts.noxscape.core.enums.Frequency;
import nox.scripts.noxscape.core.enums.MasterNodeType;

import java.util.ArrayList;
import java.util.Arrays;

public class TutorialIslandNode extends NoxScapeMasterNode {

    public TutorialIslandNode(ScriptContext ctx) {
        super(ctx);
        this.tracker = new TutorialIslandTracker();
        this.nodeInformation = getMasterNodeInformation();
    }

    @Override
    public boolean canExecute() {
        return ctx.getWidgets().isVisible(TutorialIslandConstants.WIDGET_ROOT_PROGRESS);
    }

    @Override
    public void initializeNodes() {
        CreateCharacter createCharacter = new CreateCharacter(null, ctx, "Creating your character.", tracker);

        nodes = new ArrayList(Arrays.asList(createCharacter));
        setEntryPoint();

        ctx.logClass(this, String.format("Initialized %d nodes.", nodes.size()));
    }

    @Override
    public MasterNodeInformation getMasterNodeInformation() {
        if (nodeInformation != null)
            return nodeInformation;

        nodeInformation = new MasterNodeInformation(
                "Tutorial Island",
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
