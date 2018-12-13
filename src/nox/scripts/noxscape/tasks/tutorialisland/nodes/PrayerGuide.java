package nox.scripts.noxscape.tasks.tutorialisland.nodes;

import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.tasks.tutorialisland.TutorialIslandUtil;
import nox.scripts.noxscape.util.Sleep;
import org.osbot.rs07.api.HintArrow;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.MethodProvider;

public class PrayerGuide extends NoxScapeNode {

    private final String NPC_NAME_PRAYERGUIDE = "Brother Brace";

    private final String INSTRUCTIONS_TALKTO = "Follow the path to the chapel";
    private final String INSTRUCTIONS_TALKTO2 = "Talk with Brother Brace";
    private final String INSTRUCTIONS_TALKTO3 = "Speak with Brother Brace to learn more";
    private final String INSTRUCTIONS_PRAYER_CLICK = "Click on the flashing icon to open the Prayer menu";
    private final String INSTRUCTIONS_FRIENDS_CLICK = "Click on the flashing face icon to open your friends";
    private final String INSTRUCTIONS_MOVEON = "leading to your final instructor";

    private final Position POSITION_PRAYER_GUIDE = new Position(3126, 3107, 0);
    private final Position POSITION_PRAYER_DOOR = new Position(3122, 3102, 0);

    public PrayerGuide(NoxScapeNode child, ScriptContext ctx, String message) {
        super(child, ctx, message);
    }

    @Override
    public boolean isValid() {
        HintArrow arrow = ctx.getHintArrow();
        NPC prayerGuide = ctx.getNpcs().closest(NPC_NAME_PRAYERGUIDE);

        boolean isArrowOverGuide = arrow != null && prayerGuide != null && arrow.getPosition() != null && arrow.getPosition().equals(prayerGuide.getPosition());
        boolean talkingToPrayerGuide = prayerGuide != null && prayerGuide.isInteracting(ctx.myPlayer());

        return isArrowOverGuide || talkingToPrayerGuide  ||
                TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_TALKTO, INSTRUCTIONS_TALKTO2, INSTRUCTIONS_TALKTO3, INSTRUCTIONS_PRAYER_CLICK, INSTRUCTIONS_FRIENDS_CLICK, INSTRUCTIONS_MOVEON);
    }

    @Override
    public int execute() throws InterruptedException {
        PrayerState state = getState();
        ctx.logClass(this, "Executing state: " + state.name());
        switch (state) {
            case TALKTO: {
                if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_FRIENDS_CLICK)) {
                    ctx.getTabs().open(Tab.FRIENDS);
                    break;
                }
                if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_PRAYER_CLICK)) {
                    ctx.getTabs().open(Tab.PRAYER);
                    break;
                }
                if (ctx.getMap().distance(POSITION_PRAYER_GUIDE) > 7) {
                    ctx.getWalking().walk(POSITION_PRAYER_GUIDE);
                    Sleep.sleepUntil(() -> ctx.getMap().distance(POSITION_PRAYER_GUIDE) < 3, 10000, 1000);
                }
                NPC prayerGuide = ctx.getNpcs().closest(NPC_NAME_PRAYERGUIDE);
                if (prayerGuide != null && prayerGuide.interact("Talk-to")) {
                    Sleep.sleepUntil(() -> TutorialIslandUtil.getClickToContinueWidget(ctx) != null, 5000, 500);
                } else {
                    logError("Error talking to prayer guide");
                }
                break;
            }
            case MOVEON: {
                RS2Object door = ctx.getObjects().closest(f -> f.getPosition().equals(POSITION_PRAYER_DOOR));
                if (door != null && door.interact("Open")) {
                    Sleep.sleepUntil(() -> !TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_MOVEON), 8000, 800);
                } else {
                    logError("Error leaving prayer guide");
                }
            }
            case HANDLED:
                break;
            case UNDEFINED:
            default: {
                logError("Undefined state");
            }
        }
        return MethodProvider.random(650, 2500);
    }

    private PrayerState getState() {
        if (TutorialIslandUtil.getClickToContinueWidget(ctx) != null) {
            TutorialIslandUtil.clickToContinue(ctx);
            return PrayerState.HANDLED;
        }
        if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_PRAYER_CLICK, INSTRUCTIONS_TALKTO, INSTRUCTIONS_TALKTO2, INSTRUCTIONS_TALKTO3, INSTRUCTIONS_FRIENDS_CLICK)) {
            return PrayerState.TALKTO;
        }
        if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_MOVEON)) {
            return PrayerState.MOVEON;
        }
        return PrayerState.UNDEFINED;
    }

    private enum PrayerState {
        UNDEFINED,
        HANDLED,
        TALKTO,
        MOVEON
    }
}
