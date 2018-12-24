package nox.scripts.noxscape.tasks.tutorialisland.nodes;

import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.tasks.tutorialisland.TutorialIslandUtil;
import nox.scripts.noxscape.util.Sleep;
import org.osbot.rs07.api.HintArrow;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.MethodProvider;

public class QuestGuide extends NoxScapeNode {

    private final String NPC_NAME_QUESTGUIDE = "Quest Guide";

    private final String INSTRUCTIONS_TOGGLERUN = "you can either run or walk";
    private final String INSTRUCTIONS_TOGUIDE = "Follow the path to the next guide. When you get there, click on the door to pass through it.";
    private final String INSTRUCTIONS_TALKTOGUIDE = "time to learn about quests!";
    private final String INSTRUCTIONS_CLICK_QUEST = "Click on the flashing icon to the left of your inventory.";
    private final String INSTRUCTIONS_QUEST_JOURNAL = "Talk to the quest guide again";
    private final String INSTRUCTIONS_MOVEON = "time to enter some caves.";

    public QuestGuide(NoxScapeNode child, ScriptContext ctx, String message) {
        super(child, ctx, message);
    }

    @Override
    public boolean isValid() {
        NPC questGuide = ctx.getNpcs().closest(NPC_NAME_QUESTGUIDE);
        HintArrow arrow = ctx.getHintArrow();
        boolean talkingToGuide = questGuide != null && questGuide.isInteracting(ctx.myPlayer());
        boolean arrowOverGuide = arrow != null && arrow.getNPC() != null && arrow.getNPC().equals(questGuide);

        return arrowOverGuide || talkingToGuide || TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_TOGGLERUN, INSTRUCTIONS_TOGUIDE, INSTRUCTIONS_TALKTOGUIDE, INSTRUCTIONS_CLICK_QUEST, INSTRUCTIONS_QUEST_JOURNAL, INSTRUCTIONS_MOVEON);
    }

    @Override
    public int execute() throws InterruptedException {
        NPC questGuide = ctx.getNpcs().closest(NPC_NAME_QUESTGUIDE);
        switch (getState()) {
            case WALK_TO_DOOR: {
                if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_TOGGLERUN)) {
                    if (ctx.getSettings().isRunning()) {
                        if (ctx.getSettings().setRunning(false))
                            MethodProvider.sleep(MethodProvider.random(50, 500));
                    }
                    ctx.getSettings().setRunning(true);
                }
                if (ctx.getWalking().walk(ctx.getHintArrow().getPosition())) {
                    RS2Object door = ctx.getObjects().closest("Door");
                    if (door != null) {
                        if (door.interact()) {
                            Sleep.until(() -> ctx.getMap().canReach(questGuide), 5000, 500);
                        } else {
                            logError("Error opening questguide door");
                            ctx.getCamera().toEntity(door);
                        }
                    } else {
                        logError("Couldn't find questguide door");
                    }

                } else {
                    logError("Unable to walk to questguide door");
                }
                break;
            }
            case TALK_TO_GUIDE: {
                RS2Widget cct = TutorialIslandUtil.getClickToContinueWidget(ctx);
                if (cct != null) {
                    if (cct.interact())
                        Sleep.until(() -> TutorialIslandUtil.getClickToContinueWidget(ctx) != null, 5000, 1000);
                        break;
                } else {
                    if (questGuide.interact()) {
                        break;
                    } else {
                        logError("Unable to talk to quest guide");
                    }
                }
                break;
            }
            case CLICK_QUEST_ICON: {
                ctx.getTabs().open(Tab.QUEST);
                break;
            }
            case CLIMB_LADDER: {
                ctx.getObjects().closest("Ladder").interact("Climb-down");
                break;
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

    private QuestState getState() {
        if (TutorialIslandUtil.getClickToContinueWidget(ctx) != null) {
            TutorialIslandUtil.clickToContinue(ctx);
            return QuestState.HANDLED;
        }
        if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_TOGGLERUN, INSTRUCTIONS_TOGUIDE)) {
            return QuestState.WALK_TO_DOOR;
        }
        if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_TALKTOGUIDE, INSTRUCTIONS_QUEST_JOURNAL)) {
            return QuestState.TALK_TO_GUIDE;
        }
        if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_CLICK_QUEST)) {
            return QuestState.CLICK_QUEST_ICON;
        }
        if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_MOVEON)) {
            return QuestState.CLIMB_LADDER;
        }
        return QuestState.UNDEFINED;
    }

    private enum QuestState {
        UNDEFINED,
        HANDLED,
        WALK_TO_DOOR,
        TALK_TO_GUIDE,
        CLICK_QUEST_ICON,
        CLIMB_LADDER
    }

}
