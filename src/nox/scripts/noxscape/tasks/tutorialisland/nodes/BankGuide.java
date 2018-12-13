package nox.scripts.noxscape.tasks.tutorialisland.nodes;

import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.tasks.tutorialisland.TutorialIslandUtil;
import nox.scripts.noxscape.util.Sleep;
import nox.scripts.noxscape.util.WidgetActionFilter;
import org.osbot.rs07.api.HintArrow;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.script.MethodProvider;

public class BankGuide extends NoxScapeNode {

    private final int WIDGET_ROOT_CLOSE = 310;
    private final int WIDGET_ROOT_BOTTOMTABS = 548;

    private final String NPC_NAME_ACCOUNTGUIDE = "Account Guide";

    private final String INSTRUCTIONS_OPENBANK = "just click on the indicated booth";
    private final String INSTRUCTIONS_POLL = "click on the indicated poll booth";
    private final String INSTRUCTIONS_MOVEON = "Polls are run periodically to let the";
    private final String INSTRUCTIONS_TALKTO_ACCOUNTGUIDE = "will tell you all about your account";
    private final String INSTRUCTIONS_ACCOUNT_CLICK = "to open your Account Management menu";
    private final String INSTRUCTIONS_TALKTO_ACCOUNTGUIDE2 = "Talk to the Account Guide";
    private final String INSTRUCTIONS_MOVEON2 = "Continue through the next door.";

    private final Position POSITION_BANK = new Position(3121, 3123, 0);
    private final Position POSITION_DOOR_ACCOUNTGUIDE = new Position(3125, 3124, 0);
    private final Position POSITION_DOOR_ACCOUNTGUIDE_MOVEON = new Position(3130, 3124, 0);

    private final WidgetActionFilter closeFilter = new WidgetActionFilter("Close");
    private final WidgetActionFilter accountManagementFilter = new WidgetActionFilter("Account Management");

    public BankGuide(NoxScapeNode child, ScriptContext ctx, String message) {
        super(child, ctx, message);
    }

    @Override
    public boolean isValid() {
        HintArrow arrow = ctx.getHintArrow();
        RS2Object pollBooth = ctx.getObjects().closest("Poll booth");
        NPC accountGuide = ctx.getNpcs().closest(NPC_NAME_ACCOUNTGUIDE);

        boolean isArrowOverPoll = arrow != null && pollBooth != null && arrow.getPosition() != null && arrow.getPosition().equals(pollBooth.getPosition());
        boolean isArrowOverGuide = arrow != null && accountGuide != null && arrow.getPosition() != null && arrow.getPosition().equals(accountGuide.getPosition());
        boolean talkingToAccountGuide = accountGuide != null && accountGuide.isInteracting(ctx.myPlayer());

        return (accountGuide != null && TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_MOVEON2)) || isArrowOverGuide || talkingToAccountGuide || isArrowOverPoll ||
                TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_OPENBANK, INSTRUCTIONS_POLL, INSTRUCTIONS_MOVEON, INSTRUCTIONS_TALKTO_ACCOUNTGUIDE, INSTRUCTIONS_ACCOUNT_CLICK, INSTRUCTIONS_TALKTO_ACCOUNTGUIDE2);
    }

    @Override
    public int execute() throws InterruptedException {
        BankState state = getState();
        ctx.logClass(this, "Executing state: " + state.name());
        switch (state) {
            case BANKPOLL: {
                if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_POLL)) {
                    if (ctx.getBank().isOpen()) {
                        ctx.getBank().close();
                        ctx.sleep(MethodProvider.random(200, 1200));
                    }
                    if (ctx.getObjects().closest("Poll booth").interact()) {
                        break;
                    }
                }
                if (ctx.getMap().distance(POSITION_BANK) > 3) {
                    if (ctx.getWalking().walk(POSITION_BANK)) {
                        Sleep.sleepUntil(() -> ctx.getMap().distance(POSITION_BANK) <= 3, 5000, 500);
                    }
                }
                RS2Object booth = ctx.getObjects().closest("Bank booth");
                if (booth != null && booth.interact("Use")) {
                    Sleep.sleepUntil(() -> ctx.getBank().isOpen(), 500, 500);
                } else {
                    logError("Error opening bank booth");
                }
                break;
            }
            case MOVEON: {
                if (ctx.getWidgets().singleFilter(WIDGET_ROOT_CLOSE, closeFilter).interact("Close")) {
                    ctx.sleep(1000);
                    if (ctx.getObjects().closest(f -> f.getPosition().equals(POSITION_DOOR_ACCOUNTGUIDE)).interact("Open")) {
                        Sleep.sleepUntil(() -> TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_TALKTO_ACCOUNTGUIDE), 6000, 600);
                    }
                }
                break;
            }
            case TALKTO_ACCOUNTGUIDE: {
                if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_ACCOUNT_CLICK)) {
                    RS2Widget accountTab = ctx.getWidgets().singleFilter(WIDGET_ROOT_BOTTOMTABS, accountManagementFilter);
                    if (accountTab != null && accountTab.interact()) {
                        Sleep.sleepUntil(() -> ctx.getHintArrow() != null, 5000, 1000);
                    } else {
                        logError("Error clicking account management tab");
                    }
                } else {
                    NPC accountGuide = ctx.getNpcs().closest(NPC_NAME_ACCOUNTGUIDE);
                    if (accountGuide != null) {
                        if (accountGuide.interact("Talk-to")) {
                            Sleep.sleepUntil(() -> TutorialIslandUtil.getClickToContinueWidget(ctx) != null, 5000, 500);
                        }
                    }
                }
                break;
            }
            case MOVEON2: {
                RS2Object moveOnDoor = ctx.getObjects().closest(f -> f.getPosition().equals(POSITION_DOOR_ACCOUNTGUIDE_MOVEON));
                if (moveOnDoor != null && moveOnDoor.interact("Open")) {
                    Sleep.sleepUntil(() -> !TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_MOVEON2), 8000, 800);
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

    private BankState getState() {
        if (TutorialIslandUtil.getClickToContinueWidget(ctx) != null) {
            TutorialIslandUtil.clickToContinue(ctx);
            return BankState.HANDLED;
        }
        if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_OPENBANK, INSTRUCTIONS_POLL)) {
            return BankState.BANKPOLL;
        }
        if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_MOVEON)) {
            return BankState.MOVEON;
        }
        if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_TALKTO_ACCOUNTGUIDE, INSTRUCTIONS_TALKTO_ACCOUNTGUIDE2, INSTRUCTIONS_ACCOUNT_CLICK)) {
            return BankState.TALKTO_ACCOUNTGUIDE;
        }
        if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_MOVEON2)) {
            return BankState.MOVEON2;
        }
        return BankState.UNDEFINED;
    }

    private enum BankState {
        UNDEFINED,
        HANDLED,
        BANKPOLL,
        MOVEON,
        TALKTO_ACCOUNTGUIDE,
        MOVEON2
    }

}
