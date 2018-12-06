package nox.scripts.noxscape.tasks.woodcutting;

import javafx.geometry.Pos;
import nox.scripts.noxscape.NoxScape;
import nox.scripts.noxscape.core.MasterNodeInformation;
import nox.scripts.noxscape.core.NoxScapeMasterNode;
import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.enums.Duration;
import nox.scripts.noxscape.core.enums.Frequency;
import nox.scripts.noxscape.core.enums.MasterNodeType;
import nox.scripts.noxscape.tasks.base.WalkingNode;
import nox.scripts.noxscape.tasks.tutorialisland.TutorialIslandTracker;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.ui.Skill;

import java.util.Arrays;
import java.util.Comparator;

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
        ctx.logClass(this, "Initializing Woodcutting Nodes");
        // Get the highest level tree we can currently cut
        WoodcuttingEntity entity = Arrays.stream(WoodcuttingEntity.values())
                .filter(f -> f.getRequiredLevel() <= ctx.getSkills().getStatic(Skill.WOODCUTTING))
                .max(Comparator.comparingInt(WoodcuttingEntity::getRequiredLevel))
                .get();

        // Get the closest WoodCutting location to ours
        final Position curPos = ctx.myPosition();
        WoodcuttingLocation location = Arrays.stream(WoodcuttingLocation.values())
                .filter(f -> f.containsTree(entity))
                .min(Comparator.comparingInt(a -> a.distanceToCenterPoint(curPos)))
                .get();

        NoxScapeNode toTreeNode = new WalkingNode(ctx)
                .toPosition(location.centerPoint())
                .isWebWalk(true)
                .hasMessage("Walking to Trees (" + entity.getName() + ")");

        NoxScapeNode toBankNode = new WalkingNode(ctx)
                .toPosition()

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
                MasterNodeType.SKILLING);

        return nodeInformation;
    }

    @Override
    public boolean isCompleted() {
        return !canExecute();
    }
}
