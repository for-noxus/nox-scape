package nox.scripts.noxscape.tasks.tutorialisland.nodes;

import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.tasks.tutorialisland.TutorialIslandUtil;
import nox.scripts.noxscape.util.Sleep;
import org.osbot.rs07.api.HintArrow;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.script.MethodProvider;

import java.util.Arrays;

public class MiningGuide extends NoxScapeNode {

    private final int WIDGET_ROOT_SMITHING = 312;

    private final String NPC_MINEGUIDE_NAME = "Mining Instructor";

    private final String INSTRUCTIONS_WALKTO = "the mining instructor will help you. Talk to him";
    private final String INSTRUCTIONS_MINE = "select the prospect option.";
    private final String INSTRUCTIONS_SMELT = "can smelt these into a bronze bar";
    private final String INSTRUCTIONS_SMITH = "click on the anvil";
    private final String INSTRUCTIONS_SMITH_OTHER = "Only the dagger can be made at your skill level";
    private final String INSTRUCTIONS_TALKTO = "Speak to the mining instructor and he'll show you how to make it into a weapon.";
    private final String INSTRUCTIONS_MOVEON = "mining instructor for a recap at any time.";

    private final Position POSITION_INSTRUCTOR = new Position(3080, 9504, 0);

    public MiningGuide(NoxScapeNode child, ScriptContext ctx, String message) {
        super(child, ctx, message);
    }

    @Override
    public boolean isValid() {
        NPC mineGuide = ctx.getNpcs().closest(NPC_MINEGUIDE_NAME);
        HintArrow arrow = ctx.getHintArrow();

        boolean talkingToGuide = mineGuide != null && mineGuide.isInteracting(ctx.myPlayer());
        boolean arrowOverGuide = arrow != null && arrow.getNPC() != null && arrow.getNPC().equals(mineGuide);
        boolean justMined = ctx.getWidgets().getWidgetContainingText("You manage to mine some") != null;

        return (justMined && mineGuide != null) || talkingToGuide || arrowOverGuide || TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_WALKTO, INSTRUCTIONS_MINE, INSTRUCTIONS_SMELT, INSTRUCTIONS_TALKTO, INSTRUCTIONS_SMITH, INSTRUCTIONS_MOVEON, INSTRUCTIONS_SMITH_OTHER);
    }

    @Override
    public int execute() throws InterruptedException {
        NPC mineGuide = ctx.getNpcs().closest(NPC_MINEGUIDE_NAME);

        switch(getState()) {
            case TALKTO: {
                if (mineGuide == null) {
                    if (ctx.getWalking().walk(POSITION_INSTRUCTOR)) {
                        Sleep.sleepUntil(() -> ctx.getNpcs().closest(NPC_MINEGUIDE_NAME) != null, 5000, 500);
                    }
                }
                if (mineGuide != null && mineGuide.interact()) {
                    Sleep.sleepUntil(() -> TutorialIslandUtil.getClickToContinueWidget(ctx) != null, 8500, 500);
                } else {
                    logError("Error talking to mineguide");
                }
                break;
            }
            case MINE: {
                boolean success;
                if (TutorialIslandUtil.isInstructionVisible(ctx, "copper")) {
                    success = Rock.COPPER.getClosestWithOre(ctx).interact("Mine");
                } else {
                    success = Rock.TIN.getClosestWithOre(ctx).interact("Mine");
                }
                if (success)
                    Sleep.sleepUntil(() -> TutorialIslandUtil.clickToContinue(ctx), 8500, 500);
                else
                    logError("Error mining rocks");
                break;
            }
            case SMELT: {
                if (ctx.getObjects().closest("Furnace").interact("Use")) {
                    Sleep.sleepUntil(() -> TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_TALKTO), 8500, 1000);
                } else {
                    logError("Error smelting ores");
                }
                break;
            }
            case SMITH: {
                if (ctx.getObjects().closest("Anvil").interact("Smith")) {
                    Sleep.sleepUntil(() -> ctx.getWidgets().isVisible(WIDGET_ROOT_SMITHING), 8000, 1000);
                    if (ctx.getWidgets().singleFilter(WIDGET_ROOT_SMITHING,f -> f.getItems() != null && Arrays.stream(f.getItems()).anyMatch(a -> a.getName().equals("Bronze dagger"))).interact()) {
                        Sleep.sleepUntil(() -> TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_MOVEON), 8500, 500);
                    }
                } else {
                    logError("Error smithing dagger");
                }
                break;
            }
            case MOVEON: {
                if (ctx.getWalking().walk(ctx.getHintArrow().getPosition())) {
                    Sleep.sleepUntil(() -> ctx.getMap().distance(ctx.getHintArrow().getPosition()) < 3, 8000,1000);
                    if (ctx.getObjects().closest("Gate").interact("Open"))
                        Sleep.sleepUntil(() -> TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_MOVEON), 5000, 800);
                    else
                        logError("Error exiting guidesmith");
                }
                break;
            }
            case HANDLED:
            break;
            case UNDEFINED:
            default: {
                logError("Undefined state");
            }
        }
        return MethodProvider.random(500, 4000);
    }

    private MineState getState() {
        if (TutorialIslandUtil.getClickToContinueWidget(ctx) != null) {
            TutorialIslandUtil.clickToContinue(ctx);
            return MineState.HANDLED;
        }
        if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_WALKTO, INSTRUCTIONS_TALKTO))
            return MineState.TALKTO;
        if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_MINE))
            return MineState.MINE;
        if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_SMELT))
            return MineState.SMELT;
        if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_SMITH, INSTRUCTIONS_SMITH_OTHER))
            return MineState.SMITH;
        if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_MOVEON)) {
            return MineState.MOVEON;
        }

        return MineState.UNDEFINED;
    }

    private enum MineState {
        UNDEFINED,
        HANDLED,
        TALKTO,
        MINE,
        SMELT,
        SMITH,
        MOVEON
    }

    /*
        Courtesy of Explv
     */
    private enum Rock {

        COPPER((short) 4645, (short) 4510),
        TIN((short) 53);

        private final short[] COLOURS;

        Rock(final short... COLOURS) {
            this.COLOURS = COLOURS;
        }

        public RS2Object getClosestWithOre(ScriptContext ctx) {
            //noinspection unchecked
            return ctx.getObjects().closest(obj -> {
                short[] colours = obj.getDefinition().getModifiedModelColors();
                if (colours != null) {
                    for (short c : colours) {
                        for (short col : COLOURS) {
                            if (c == col) return true;
                        }
                    }
                }
                return false;
            });
        }
    }
}
