package nox.scripts.noxscape.tasks.tutorialisland.nodes;

import com.sun.demo.jvmti.hprof.Tracker;
import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.tasks.tutorialisland.TutorialIslandUtil;
import nox.scripts.noxscape.util.Sleep;
import org.osbot.rs07.api.HintArrow;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.RS2Widget;

import java.util.List;
import java.util.Random;

public class TalkToGuide extends NoxScapeNode {

    public final String NPC_NAME_GUIDE = "Gielinor Guide";
    public final String[] DIALOGUE_GUIDE_OPTIONS = { "I am brand new", "I am an experienced", "played in the past" };
    public final String DIALOGUE_GUIDE_OPTIONS_TITLE = "What's your experience";

    public TalkToGuide(List children, ScriptContext ctx, String message) {
        super(children, ctx, message);
    }

    @Override
    public boolean isValid() {
        HintArrow hint = ctx.getHintArrow();
        return hint != null && hint.getType() == HintArrow.HintArrowType.NPC && hint.getNPC().getName().equals(NPC_NAME_GUIDE);
    }

    @Override
    public int execute() {
        Random r = new Random();
        RS2Widget cct = TutorialIslandUtil.getClickToContinueWidget(ctx);
        if (cct != null) {
            TutorialIslandUtil.clickToContinue(ctx);
        } else if (ctx.getWidgets().getAll().stream().anyMatch(f -> f.getMessage() != null && f.getMessage().contains(DIALOGUE_GUIDE_OPTIONS_TITLE))) {
            String option = DIALOGUE_GUIDE_OPTIONS[r.nextInt(DIALOGUE_GUIDE_OPTIONS.length)];
            RS2Widget optionWidget = ctx.getWidgets().getAll().stream().filter(f -> f.getMessage().contains(option)).findFirst().orElse(null);
            if (optionWidget != null) {
                optionWidget.interact();
            }
        } else {
            NPC guide = ctx.getNpcs().closest(NPC_NAME_GUIDE);
            if (guide != null && guide.interact()) {
                Sleep.sleepUntil(() -> TutorialIslandUtil.getClickToContinueWidget(ctx) != null, 5000, 500);
            }
        }
        return r.nextInt(2000) + 1000;
    }
}
