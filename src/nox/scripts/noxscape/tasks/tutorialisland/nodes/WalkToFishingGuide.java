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
    final int WIDGET_ROOT_INSTRUCTIONS = 263;

    final String NPC_NAME_SURVIALEXPERT = "Survival Expert";
    final String WIDGET_LOOKUP_FIRSTINSTRUCTOR = "time to meet your first instructor";
    final String WIDGET_LOOKUP_FIRSTINSTRUCTOR_PT2 = "Follow the path to find the next instructor";

    public WalkToFishingGuide(NoxScapeNode child, ScriptContext ctx, String message, Tracker tracker) {
        super(child, ctx, message, tracker);
    }

    @Override
    public boolean isValid() {

        HintArrow arrow = ctx.getHintArrow();
        RS2Object door = ctx.getObjects().closest("Door");

        return arrow != null && door != null && door.getPosition().equals(arrow.getPosition());
    }

    @Override
    public int execute() {
        HintArrow arrow = ctx.getHintArrow();
        Position pos = arrow.getPosition();
        if (pos.interact(ctx.getBot(),"Open")) {
            Sleep.sleepUntil(() -> {
                HintArrow arr = ctx.getHintArrow();
                return arr != null && arr.getType() == HintArrow.HintArrowType.NPC;
            },3000 , 400);
            NPC expert = ctx.getNpcs().closest(NPC_NAME_SURVIALEXPERT);
            if (expert != null) {
                if (ctx.getWalking().walk(expert)) {
                    if (expert.interact())
                        return 500;
                }
            }
        }
        return new Random().nextInt(1000) + 800;
    }
}
