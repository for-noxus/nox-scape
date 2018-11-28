package nox.scripts.noxscape.tasks.tutorialisland.nodes;

import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.Tracker;
import nox.scripts.noxscape.tasks.tutorialisland.TutorialIslandUtil;
import nox.scripts.noxscape.util.Sleep;
import org.osbot.rs07.api.HintArrow;
import org.osbot.rs07.api.NPCS;
import org.osbot.rs07.api.Widgets;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.MethodProvider;

import javax.print.attribute.standard.MediaSize;
import java.util.List;

public class FishingGuide extends NoxScapeNode {

    private final int WIDGET_INSTRUCTIONS_ROOT = 263;

    private final String NPC_EXPERT_NAME = "Survival Expert";

    private final String INSTRUCTIONS_WALK = "on the ground will walk you";
    private final String INSTRUCTIONS_INSTRUCTOR = "Speak to the survival expert to continue";
    private final String INSTRUCTIONS_INVEN_CLICK = "click on the flashing backpack icon";
    private final String INSTRUCTIONS_FISH = "Let's use it to catch some shrimp";
    private final String INSTRUCTIONS_SKILLS_CLICK = "flashing bar graph icon";
    private final String INSTRUCTIONS_CHOP = "time to cook your shrimp";
    private final String INSTRUCTIONS_LIGHT_FIRE = "time to light a fire";
    private final String INSTRUCTIONS_COOK = "time to get cooking";
    private final String INSTRUCTIONS_MOVEON = "Click on the gate shown";
    private final String INSTRUCTIONS_MOVEON_PASTGATE = "Follow the path until you get to the door with the yellow arrow";

    public FishingGuide(NoxScapeNode child, ScriptContext ctx, String message, Tracker tracker) {
        super(child, ctx, message, tracker);
    }

    @Override
    public boolean isValid() {
        HintArrow arrow = ctx.getHintArrow();
        boolean isExpertHinted = arrow.getNPC() != null && arrow.getNPC().getName().equals(NPC_EXPERT_NAME);
        boolean shrimpsCaught = ctx.getWidgets().getWidgetContainingText("manage to catch some shrimp") != null;
        return shrimpsCaught || isExpertHinted || TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_WALK, INSTRUCTIONS_INSTRUCTOR, INSTRUCTIONS_INVEN_CLICK, INSTRUCTIONS_FISH, INSTRUCTIONS_SKILLS_CLICK,
                INSTRUCTIONS_CHOP, INSTRUCTIONS_LIGHT_FIRE, INSTRUCTIONS_COOK, INSTRUCTIONS_MOVEON);
    }

    @Override
    public int execute() throws InterruptedException {
        NPC expert = ctx.getNpcs().closest(NPC_EXPERT_NAME);
        if (expert == null) {
            abort("Can't find Survival Expert NPC.");
            return 0;
        }
        switch(getState()) {
            case TALKTO: {
                RS2Widget cct = TutorialIslandUtil.getClickToContinueWidget(ctx);
                if (cct != null) {
                    cct.interact();
                    break;
                }
                if (ctx.getWalking().walk(expert)) {
                    expert.interact();
                    break;
                }
            }
            case INVEN_CLICK: {
                ctx.getTabs().open(Tab.INVENTORY);
                break;
            }
            case FISH: {
                if (!ctx.getInventory().contains("Raw shrimps")) {
                    ctx.getNpcs().closest("Fishing spot").interact("Net");
                    Sleep.sleepUntil(() -> TutorialIslandUtil.getClickToContinueWidget(ctx) != null, 4000, 500);
                    TutorialIslandUtil.clickToContinue(ctx);
                }
                Sleep.sleepUntil(() -> TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_SKILLS_CLICK), 3000, 500);
                ctx.getTabs().open(Tab.SKILLS);
                break;
            }
            case CHOPCOOK: {
                if (!ctx.getInventory().contains("Logs")) {
                    ctx.getObjects().closest("Tree").interact("Chop down");
                    Sleep.sleepUntil(() -> TutorialIslandUtil.getClickToContinueWidget(ctx) != null, 4000, 500);
                    TutorialIslandUtil.clickToContinue(ctx);
                    Sleep.sleepUntil(() -> TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_LIGHT_FIRE, INSTRUCTIONS_COOK), 3000, 500);
                }
                if (ctx.getInventory().interact("Use", "Logs") && ctx.getInventory().interact("Use", "Tinderbox")) {
                    Sleep.sleepUntil(() -> TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_COOK) && ctx.getObjects().closest("Fire") != null, 9000, 500);
                    RS2Object fire = ctx.getObjects().closest("Fire");
                    if (fire == null || !(ctx.getInventory().interact("Use", "Raw shrimps") && fire.interact()))
                        ctx.logClass(this, "Unable to cook shrimps..");
                    Sleep.sleepUntil(() -> TutorialIslandUtil.getClickToContinueWidget(ctx) != null, 4000, 500);
                    TutorialIslandUtil.clickToContinue(ctx);
                }
                break;
            }
            case MOVEON: {
                HintArrow arrow = ctx.getHintArrow();
                if (arrow == null || !ctx.getWalking().walk(arrow.getPosition()))
                    ctx.logClass(this, "Error moving on from fishing uide");
                Sleep.sleepUntil(() -> arrow.getPosition().distance(ctx.myPosition()) < 4, 8000, 800);
                if (arrow.getPosition().interact(ctx.getBot(), "Open")) {
                    Sleep.sleepUntil(() -> TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_MOVEON_PASTGATE), 3000, 500);
                    break;
                }
            }
            default:
            case NA: {
                return 2000;
            }
        }
        return MethodProvider.random(500, 3000);
    }

    private FishState getState() {
        if (TutorialIslandUtil.getClickToContinueWidget(ctx) != null)
            TutorialIslandUtil.clickToContinue(ctx);
        if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_WALK, INSTRUCTIONS_INSTRUCTOR) || TutorialIslandUtil.getClickToContinueWidget(ctx) != null)
            return FishState.TALKTO;
         else if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_INVEN_CLICK))
            return FishState.INVEN_CLICK;
         else if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_FISH, INSTRUCTIONS_SKILLS_CLICK))
             return FishState.FISH;
         else if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_CHOP, INSTRUCTIONS_COOK))
             return FishState.CHOPCOOK;
         else if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_MOVEON))
            return FishState.MOVEON;
        return FishState.NA;
    }

    private enum FishState {
        NA,
        TALKTO,
        INVEN_CLICK,
        FISH,
        CHOPCOOK,
        MOVEON
    }
}
