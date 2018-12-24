package nox.scripts.noxscape.tasks.tutorialisland.nodes;

import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.tasks.tutorialisland.TutorialIslandUtil;
import nox.scripts.noxscape.util.Sleep;
import nox.scripts.noxscape.util.WidgetActionFilter;
import org.osbot.rs07.api.HintArrow;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;

import java.util.List;
import java.util.Random;

public class ClickOptionsMenu extends NoxScapeNode {

    public final String TEXT_OPTIONS = "Options";
    public final String TEXT_OPTIONS_INSTRUCTIONS = "Options menu";

    public final int WIDGET_ROOT_GAMEICONS_RESIZABLE = 164;
    public final int WIDGET_ROOT_OPTIONS_FIXED = 261;

    public final int WIDGET_LOGOUT = 548;

    public WidgetActionFilter fixedFilter = new WidgetActionFilter("Fixed mode");

    public ClickOptionsMenu(List children, ScriptContext ctx, String message) {
        super(children, ctx, message);
    }

    @Override
    public boolean isValid() {
        HintArrow arrow = ctx.getHintArrow();
        return arrow != null && arrow.getType() == HintArrow.HintArrowType.NONE && TutorialIslandUtil.isInstructionVisible(ctx, "click on the flashing spanner icon");
    }

    @Override
    public int execute() throws InterruptedException {
        List<RS2Widget> settingsIcon = ctx.getWidgets().containingActions(WIDGET_ROOT_GAMEICONS_RESIZABLE, TEXT_OPTIONS);
        if (settingsIcon != null && settingsIcon.size() > 0) {
            if (settingsIcon.get(0).interact()) {
                Sleep.until(() -> ctx.getWidgets().isVisible(WIDGET_ROOT_OPTIONS_FIXED), 2000,300 );
            }

        } else {
            ctx.getTabs().open(Tab.SETTINGS);
        }

        MethodProvider.sleep(Script.random(500, 2000));

        boolean isFixed = ctx.getWidgets().isVisible(WIDGET_LOGOUT);
        if (!isFixed) {
            ctx.logClass(this, "Setting screen to fixed");
            RS2Widget fixedWidget = ctx.getWidgets().singleFilter(WIDGET_ROOT_OPTIONS_FIXED,fixedFilter);
            if (fixedWidget != null && fixedWidget.interact()) {
                ctx.logClass(this, "Screen set to fixed mode");
            }
        }
        return new Random().nextInt(1500) + 100;
    }
}
