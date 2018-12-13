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

public class CookGuide extends NoxScapeNode {
    private final String NPC_CHEF_NAME = "Master Chef";

    private final String INSTRUCTIONS_MOVEON_PASTGATE = "Follow the path until you get to the door with the yellow arrow";
    private final String INSTRUCTIONS_TALKTOCHEF = "Talk to the chef indicated";
    private final String INSTRUCTIONS_MAKEDOUGH = "mix flour with water";
    private final String INSTRUCTIONS_COOKDOUGH = "you can bake it into some bread";
    private final String INSTRUCTIONS_MOVEON = "You've baked your first loaf of bread";
    private final String INSTRUCTIONS_MOVEON_PASTDOOR = "you can either run or walk";

    private final Position POS_EXIT_DOOR = new Position(3072, 3090, 0);

    public CookGuide(NoxScapeNode child, ScriptContext ctx, String message) {
        super(child, ctx, message);
    }

    @Override
    public boolean isValid() {
        HintArrow arrow = ctx.getHintArrow();
        NPC chef = ctx.getNpcs().closest(NPC_CHEF_NAME);
        boolean chefHinted = chef != null && arrow != null && arrow.getPosition() != null && arrow.getPosition().equals(chef.getPosition());
        boolean talkingToChef = chef != null && chef.isInteracting(ctx.myPlayer());
        boolean exitDoorHighlighted = arrow != null && arrow.getPosition() != null && arrow.getPosition().equals(new Position(POS_EXIT_DOOR));
        return talkingToChef || chefHinted || TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_MOVEON_PASTGATE, INSTRUCTIONS_TALKTOCHEF, INSTRUCTIONS_MAKEDOUGH, INSTRUCTIONS_COOKDOUGH, INSTRUCTIONS_MOVEON) || exitDoorHighlighted;
    }

    @Override
    public int execute() throws InterruptedException {
        NPC chef = ctx.getNpcs().closest(NPC_CHEF_NAME);
        switch(getState()) {
            case WALK_TO_DOOR: {
                HintArrow arrow = ctx.getHintArrow();
                if (ctx.getWalking().walk(arrow.getPosition())) {
                    if (ctx.getObjects().closest("Door").interact())
                        Sleep.sleepUntil(() -> TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_TALKTOCHEF), 4000, 500);
                    Sleep.sleepUntil(() -> TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_TALKTOCHEF), 7500, 800);
                    break;
                }
                ctx.logClass(this, "Error walking to cook door");
            }
            case TALK_TO_CHEF: {
                if (chef == null || !chef.interact())
                    logError("Unable to talk to chef");
                break;
            }
            case MAKE_DOUGH: {
                if (ctx.getInventory().contains("Pot of flour") && ctx.getInventory().contains("Bucket of water")) {
                    ctx.getInventory().interact("Use", "Pot of flour");
                    Sleep.sleepUntil(() -> ctx.getInventory().isItemSelected(), 2000, 400);
                    ctx.getInventory().interact("Use", "Bucket of water");
                    Sleep.sleepUntil(() -> ctx.getInventory().contains("Bread dough"), 5000, 500);
                } else if (ctx.getInventory().contains("Bread dough")) {
                    RS2Object range = ctx.getObjects().closest("Range");
                    if (range == null || !range.interact("Cook"))
                        logError("Unable to cook bread");
                    Sleep.sleepUntil(() -> ctx.getInventory().contains("Bread"),  5000, 500);
                } else {
                    if (chef == null || !chef.interact()) {
                        logError("Unable to reobtain flour/bread from chef");
                    }
                }
                break;
            }
            case MOVEON: {
                HintArrow arr = ctx.getHintArrow();
                if (arr != null && arr.getPosition().interact(ctx.getBot(),"Open"))
                    Sleep.sleepUntil(() -> TutorialIslandUtil.isInstructionVisible(ctx,INSTRUCTIONS_MOVEON_PASTDOOR), 8000, 1000);
                logError("Error opening door to leave chef");
                break;
            }
            case HANDLED: break;
            case UNDEFINED:
            default: {
                logError("Undefined state");
            }
        }
        return MethodProvider.random(800, 2000);
    }

    private CookState getState() {
        if (TutorialIslandUtil.getClickToContinueWidget(ctx) != null) {
            TutorialIslandUtil.clickToContinue(ctx);
            return CookState.HANDLED;
        }
        if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_MOVEON_PASTGATE))
            return CookState.WALK_TO_DOOR;
        if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_TALKTOCHEF))
            return CookState.TALK_TO_CHEF;
        if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_MAKEDOUGH, INSTRUCTIONS_COOKDOUGH))
            return CookState.MAKE_DOUGH;
        if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_MOVEON))
            return CookState.MOVEON;
        return CookState.UNDEFINED;
    }

    private enum CookState {
        UNDEFINED,
        HANDLED,
        WALK_TO_DOOR,
        TALK_TO_CHEF,
        MAKE_DOUGH,
        MOVEON,
    }
}
