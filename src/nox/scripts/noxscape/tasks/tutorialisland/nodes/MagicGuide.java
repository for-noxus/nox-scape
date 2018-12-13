package nox.scripts.noxscape.tasks.tutorialisland.nodes;

import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.tasks.tutorialisland.TutorialIslandUtil;
import nox.scripts.noxscape.util.Sleep;
import org.osbot.rs07.api.HintArrow;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Spells;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.MethodProvider;

public class MagicGuide extends NoxScapeNode {

    private final String NPC_NAME_MAGICGUIDE = "Magic Instructor";

    private final String INSTRUCTIONS_TALKTO = "When you get there, just talk with the magic instructor";
    private final String INSTRUCTIONS_TALKTO2 = "All of your spells are listed here. Talk to the instructor";
    private final String INSTRUCTIONS_TALKTO3 = "Just speak with the magic instructor";
    private final String INSTRUCTIONS_CHICKEN_HIT = "Look for the Wind Strike spell";
    private final String INSTRUCTIONS_MAGIC_CLICK = "Open up the magic interface";
    private final String INSTRUCTIONS_MOVEON = "leading to your final instructor";
    private final String INSTRUCTIONS_FINAL = "When you get to Lumbridge";

    private final Position POSITION_MAGIC_GUIDE = new Position(3141, 3086, 0);

    public MagicGuide(NoxScapeNode child, ScriptContext ctx, String message) {
        super(child, ctx, message);
    }

    @Override
    public boolean isValid() {
        HintArrow arrow = ctx.getHintArrow();
        NPC magicrGuide = ctx.getNpcs().closest(NPC_NAME_MAGICGUIDE);

        boolean isArrowOverGuide = arrow != null && magicrGuide != null && arrow.getPosition() != null && arrow.getPosition().equals(magicrGuide.getPosition());
        boolean talkingToMagicGuide = magicrGuide != null && magicrGuide.isInteracting(ctx.myPlayer());

        return isArrowOverGuide || talkingToMagicGuide  ||
                TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_FINAL, INSTRUCTIONS_TALKTO, INSTRUCTIONS_TALKTO2, INSTRUCTIONS_TALKTO3, INSTRUCTIONS_MAGIC_CLICK, INSTRUCTIONS_MOVEON, INSTRUCTIONS_CHICKEN_HIT, INSTRUCTIONS_MOVEON);
    }

    @Override
    public int execute() throws InterruptedException {
        MagicState state = getState();
        ctx.logClass(this, "Executing state: " + state.name());
        switch (state) {
            case TALKTO: {
                if (handleMainlandQuestion()) {
                    break;
                }
                if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_MAGIC_CLICK)) {
                    ctx.getTabs().open(Tab.MAGIC);
                    break;
                }
                if (ctx.getMap().distance(POSITION_MAGIC_GUIDE) > 7) {
                    ctx.getWalking().walk(POSITION_MAGIC_GUIDE);
                    Sleep.sleepUntil(() -> ctx.getMap().distance(POSITION_MAGIC_GUIDE) < 3, 10000, 1000);
                }
                NPC magicGuide = ctx.getNpcs().closest(NPC_NAME_MAGICGUIDE);
                if (magicGuide != null && magicGuide.interact("Talk-to")) {
                    Sleep.sleepUntil(() -> TutorialIslandUtil.getClickToContinueWidget(ctx) != null, 5000, 500);
                } else {
                    logError("Error talking to prayer guide");
                }
                break;
            }
            case CHICKEN_HIT: {
                if (ctx.getTabs().getOpen() != Tab.MAGIC) {
                    ctx.getTabs().open(Tab.MAGIC);
                }
                NPC chicken = ctx.getNpcs().closest(f -> f.getName().equals("Chicken") && !f.isUnderAttack());
                if (chicken != null) {
                    if (ctx.getMagic().castSpellOnEntity(Spells.NormalSpells.WIND_STRIKE, chicken)) {
                        Sleep.sleepUntil(() -> TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_TALKTO3), 7000, 700);
                    } else {
                        logError("Error casting Wind Strike");
                    }
                } else {
                    logError("Error finding chicken to cast Wind Strike on");
                }
                break;
            }
            case TELEPORT: {
                if (TutorialIslandUtil.clickToContinue(ctx)) {
                    Sleep.sleepUntil(() -> ctx.getNpcs().closest("Lubridge Guide") != null, 10000, 1000);
                    ctx.logClass(this, "Successfully completed Tutorial Island");
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

    private boolean handleMainlandQuestion() {
        if (ctx.getDialogues().isPendingOption()) {
            ctx.getDialogues().selectOption("No, I'm not planning to do that.", "Yes.", "I'm fine, thanks.");
            return true;
        }
        return false;
    }

    private MagicState getState() {
        if (ctx.getWidgets().getWidgetContainingText(193, INSTRUCTIONS_FINAL) != null) {
            return MagicState.TELEPORT;
        }
        if (TutorialIslandUtil.getClickToContinueWidget(ctx) != null) {
            TutorialIslandUtil.clickToContinue(ctx);
            return MagicState.HANDLED;
        }
        if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_MAGIC_CLICK, INSTRUCTIONS_TALKTO, INSTRUCTIONS_TALKTO2, INSTRUCTIONS_TALKTO3) || ctx.getDialogues().isPendingOption()) {
            return MagicState.TALKTO;
        }
        if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_CHICKEN_HIT)) {
            return MagicState.CHICKEN_HIT;
        }
        return MagicState.UNDEFINED;
    }

    private enum MagicState {
        UNDEFINED,
        HANDLED,
        TALKTO,
        CHICKEN_HIT,
        TELEPORT
    }
}
