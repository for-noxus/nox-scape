package nox.scripts.noxscape.tasks.tutorialisland.nodes;

import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.tasks.tutorialisland.TutorialIslandUtil;
import nox.scripts.noxscape.util.Sleep;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.NPC;

import java.util.Random;

public class WalkToFishingGuide extends NoxScapeNode {

    final String NPC_NAME_SURVIALEXPERT = "Survival Expert";

    private final Position POS_EXIT_DOOR = new Position(3098, 3107, 0);

    private final String INSTRUCTIONS_MOVEON = "time to meet your first instructor";
    private final String INSTRUCTIONS_MOVEON2 = "will walk you to that point";

    public WalkToFishingGuide(NoxScapeNode child, ScriptContext ctx, String message) {
        super(child, ctx, message);
    }

    protected boolean baseExecutionCondition() {
        NPC expert = ctx.getNpcs().closest(NPC_NAME_SURVIALEXPERT);
        return TutorialIslandUtil.isInstructionVisible(ctx,INSTRUCTIONS_MOVEON, INSTRUCTIONS_MOVEON2) && !ctx.getMap().isWithinRange(expert, 5);
    }

    @Override
    public int execute() {
        NPC expert = ctx.getNpcs().closest(NPC_NAME_SURVIALEXPERT);
        if (!ctx.getMap().canReach(expert)) {
            if (POS_EXIT_DOOR.interact(ctx.getBot(),"Open")) {
                Sleep.until(() -> ctx.getMap().canReach(expert), 4000, 400);
            }
        }
        if (ctx.getWalking().walk(expert)) {
            if (expert.interact())
                return 500;
        }
        return new Random().nextInt(1000) + 800;
    }
}
