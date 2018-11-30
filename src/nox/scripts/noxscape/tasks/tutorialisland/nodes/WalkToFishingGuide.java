package nox.scripts.noxscape.tasks.tutorialisland.nodes;

import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.Tracker;
import nox.scripts.noxscape.tasks.tutorialisland.TutorialIslandUtil;
import nox.scripts.noxscape.util.Sleep;
import org.osbot.rs07.api.HintArrow;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.RS2Widget;

import java.util.Arrays;
import java.util.Random;

public class WalkToFishingGuide extends NoxScapeNode {

    final String NPC_NAME_SURVIALEXPERT = "Survival Expert";

    private final Position POS_EXIT_DOOR = new Position(3098, 3107, 0);

    private final String INSTRUCTIONS_MOVEON = "will walk you to that point";

    public WalkToFishingGuide(NoxScapeNode child, ScriptContext ctx, String message, Tracker tracker) {
        super(child, ctx, message, tracker);
    }

    @Override
    public boolean isValid() {
        NPC expert = ctx.getNpcs().closest(NPC_NAME_SURVIALEXPERT);
        return TutorialIslandUtil.isInstructionVisible(ctx,INSTRUCTIONS_MOVEON) && !ctx.getMap().isWithinRange(expert, 5);
    }

    @Override
    public int execute() {
        NPC expert = ctx.getNpcs().closest(NPC_NAME_SURVIALEXPERT);
        if (!ctx.getMap().canReach(expert)) {
            if (POS_EXIT_DOOR.interact(ctx.getBot(),"Open")) {
                Sleep.sleepUntil(() -> ctx.getMap().canReach(expert), 4000, 400);
            }
        }
        if (ctx.getWalking().walk(expert)) {
            if (expert.interact())
                return 500;
        }
        return new Random().nextInt(1000) + 800;
    }
}
