package nox.scripts.noxscape.tasks.tutorialisland.nodes;

import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.tasks.tutorialisland.TutorialIslandUtil;
import nox.scripts.noxscape.util.Sleep;
import nox.scripts.noxscape.util.WidgetActionFilter;
import org.osbot.rs07.api.HintArrow;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.MethodProvider;

public class CombatGuide extends NoxScapeNode {

    private final int WIDGET_ROOT_EQUIPMENTSTATS = 387;

    private final String NPC_NAME_COMBATGUIDE = "Combat Instructor";

    private final String INSTRUCTIONS_TALKTOGUIDE = "you will find out about melee and ranged combat";
    private final String INSTRUCTIONS_TALKTOGUIDE2 = "Speak to the combat instructor";
    private final String INSTRUCTIONS_TALKTOGUIDE3 = "talk to the combat instructor";
    private final String INSTRUCTIONS_CLICK_EQUIPMENT = "flashing icon of a man";
    private final String INSTRUCTIONS_CLICK_WORNITEMS = "flashing button with a shield and helmet on it";
    private final String INSTRUCTIONS_EQUIP_DAGGER = "Click your dagger to equip it";
    private final String INSTRUCTIONS_EQUIP_SWORDSHIELD = "swapping your dagger for the sword and shield";
    private final String INSTRUCTIONS_CLICK_COMBAT = "flashing crossed swords icon";
    private final String INSTRUCTIONS_GOTO_RAT = "Click on the gates to continue";
    private final String INSTRUCTIONS_ATTACK_RAT = "time to slay some rats";
    private final String INSTRUCTIONS_ATTACK_RAT_RANGED = "Once equipped with the ranging";
    private final String INSTRUCTIONS_MOVEON = "To move on, click on the indicated ladder.";
    private final String INSTRUCTIONS_HEALTHBAR = "you will see a bar over your head.";

    private final Position POSITION_GATE_1 = new Position(3111, 9518, 0);
    private final Position POSITION_GATE_2 = new Position(3111, 9519, 0);
    private final Position POSITION_LADDER = new Position(3111, 9525, 0);

    private final WidgetActionFilter equipmentFilter = new WidgetActionFilter("View equipment stats");

    public CombatGuide(NoxScapeNode child, ScriptContext ctx, String message) {
        super(child, ctx, message);
    }

    protected boolean baseExecutionCondition() {
        NPC questGuide = ctx.getNpcs().closest(NPC_NAME_COMBATGUIDE);
        HintArrow arrow = ctx.getHintArrow();
        boolean talkingToGuide = questGuide != null && questGuide.isInteracting(ctx.myPlayer());
        boolean arrowOverGuide = arrow != null && arrow.getNPC() != null && arrow.getNPC().equals(questGuide);

        return ctx.getCombat().isFighting() || arrowOverGuide || talkingToGuide ||
                TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_MOVEON, INSTRUCTIONS_ATTACK_RAT_RANGED, INSTRUCTIONS_ATTACK_RAT, INSTRUCTIONS_TALKTOGUIDE3, INSTRUCTIONS_CLICK_WORNITEMS, INSTRUCTIONS_HEALTHBAR,
                        INSTRUCTIONS_EQUIP_DAGGER, INSTRUCTIONS_TALKTOGUIDE, INSTRUCTIONS_CLICK_EQUIPMENT, INSTRUCTIONS_TALKTOGUIDE2, INSTRUCTIONS_EQUIP_SWORDSHIELD, INSTRUCTIONS_GOTO_RAT, INSTRUCTIONS_CLICK_COMBAT);
    }

    @Override
    public int execute() throws InterruptedException {
        NPC combatGuide = ctx.getNpcs().closest(NPC_NAME_COMBATGUIDE);
        CombatState state = getState();
        ctx.logClass(this, "Executing state: " + state.name());
        switch (state) {
            case TALK_TO_GUIDE: {
                RS2Widget cct = TutorialIslandUtil.getClickToContinueWidget(ctx);
                if (cct != null) {
                    if (cct.interact()) {
                        Sleep.until(() -> TutorialIslandUtil.getClickToContinueWidget(ctx) != null, 5000, 1000);
                        break;
                    }
                } else if (!ctx.getMap().canReach(combatGuide)) {
                    ctx.getObjects().closest("Gate").interact("Open");
                }
                else {
                    if (combatGuide.interact()) {
                        break;
                    } else {
                        logError("Unable to talk to combat guide");
                    }
                }
                break;
            }
            case CLICK_EQUIPMENT: {
                if (ctx.getTabs().getOpen() != Tab.EQUIPMENT) {
                    if (ctx.getTabs().open(Tab.EQUIPMENT))
                        Sleep.until(() -> TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_CLICK_WORNITEMS, INSTRUCTIONS_EQUIP_DAGGER), 5000, 1000);
                }
                RS2Widget equippedItemsWidget = ctx.getWidgets().singleFilter(WIDGET_ROOT_EQUIPMENTSTATS, equipmentFilter);
                if (equippedItemsWidget != null && equippedItemsWidget.interact()) {
                    Sleep.until(() -> TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_EQUIP_DAGGER), 5000, 800);
                }
                if (ctx.getInventory().contains("Bronze dagger")) {
                    if (ctx.getInventory().interact("Equip", "Bronze dagger"))
                        Sleep.until(() -> TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_TALKTOGUIDE2), 5000, 800);
                    else
                        logError("Unable to equip dagger");
                }
                break;
            }
            case CLICK_SWORDSHIELD: {
                if (ctx.getInventory().contains("Bronze sword")) {
                    ctx.getInventory().interact("Wield","Bronze sword");
                    MethodProvider.sleep(MethodProvider.random(50, 800));
                }
                if (ctx.getInventory().contains("Wooden shield")) {
                    ctx.getInventory().interact("Wield","Wooden shield");
                }
                Sleep.until(() -> TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_CLICK_COMBAT), 6500, 800);
                ctx.getTabs().open(Tab.ATTACK);
                Sleep.until(() -> TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_GOTO_RAT), 8000, 800);
                break;
            }
            case HANDLE_RATS: {
                if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_ATTACK_RAT_RANGED)) {
                    if (ctx.getInventory().contains("Shortbow")) {
                        ctx.getInventory().interact("Wield","Shortbow");
                        MethodProvider.sleep(MethodProvider.random(50, 800));
                    }
                    if (ctx.getInventory().contains("Bronze arrow")) {
                        ctx.getInventory().interact("Wield", "Bronze arrow");
                    }
                    if (ctx.getNpcs().closest(f -> !f.isUnderAttack() && f.getName().equals("Giant rat")).interact()) {
                        Sleep.until(() -> TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_MOVEON), 15000, 1000);
                    }
                } else {
                    HintArrow arr = ctx.getHintArrow();
                    if (arr != null && arr.getPosition() != null) {
                        if (ctx.getObjects().closest(f -> f.getPosition().equals(POSITION_GATE_2)).interact("Open")) {
                            if (ctx.getObjects().closest(f -> f.getName().equals("Gate") && f.getPosition().distance(POSITION_GATE_1) <= 4).interact("Open")) {
                                Sleep.until(() -> TutorialIslandUtil.isInstructionVisible(ctx,INSTRUCTIONS_ATTACK_RAT), 8500, 800);
                            }
                        }
                    }
                    if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_ATTACK_RAT) || (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_HEALTHBAR) && !ctx.getCombat().isFighting())) {
                        if (ctx.getNpcs().closest(f -> !f.isUnderAttack() && f.getName().equals("Giant rat")).interact("Attack")) {
                            Sleep.until(() -> !ctx.getCombat().isFighting(), 15000, 1000);
                        }
                    }
                }
                break;
            }
            case MOVEON: {
                if (ctx.getWalking().walk(POSITION_LADDER)) {
                    if (ctx.getObjects().closest("Ladder").interact("Climb-up")) {
                        Sleep.until(() -> !TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_MOVEON), 5000, 500);
                        break;
                    } else {
                        logError("Error climbing up combat ladder");
                    }
                } else {
                    logError("Error walking to combat ladder");
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

    private CombatState getState() {
        if (TutorialIslandUtil.getClickToContinueWidget(ctx) != null) {
            TutorialIslandUtil.clickToContinue(ctx);
            return CombatState.HANDLED;
        }
        if (ctx.getCombat().isFighting()) {
            return CombatState.HANDLED;
        }
        if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_MOVEON)) {
            return CombatState.MOVEON;
        }
        if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_TALKTOGUIDE, INSTRUCTIONS_TALKTOGUIDE2, INSTRUCTIONS_TALKTOGUIDE3)) {
            return CombatState.TALK_TO_GUIDE;
        }
        if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_CLICK_EQUIPMENT, INSTRUCTIONS_CLICK_WORNITEMS, INSTRUCTIONS_EQUIP_DAGGER)) {
            return CombatState.CLICK_EQUIPMENT;
        }
        if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_EQUIP_SWORDSHIELD, INSTRUCTIONS_CLICK_COMBAT)) {
            return CombatState.CLICK_SWORDSHIELD;
        }
        if (TutorialIslandUtil.isInstructionVisible(ctx, INSTRUCTIONS_GOTO_RAT, INSTRUCTIONS_ATTACK_RAT, INSTRUCTIONS_ATTACK_RAT_RANGED, INSTRUCTIONS_HEALTHBAR)) {
            return CombatState.HANDLE_RATS;
        }
        return CombatState.UNDEFINED;
    }

    private enum CombatState {
        UNDEFINED,
        HANDLED,
        TALK_TO_GUIDE,
        CLICK_EQUIPMENT,
        CLICK_SWORDSHIELD,
        HANDLE_RATS,
        MOVEON
    }

}
