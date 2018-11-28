package nox.scripts.noxscape.tasks.tutorialisland;

import nox.scripts.noxscape.core.ScriptContext;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.util.CachedWidget;
import org.osbot.rs07.script.MethodProvider;

public final class TutorialIslandUtil {

    private final static int WIDGET_INSTRUCTIONS_ROOT = 263;

    private final static String TEXT_LOOKUP_CLICKTOCONTINUE = "Click to continue";
    private final static String TEXT_LOOKUP_CLICKHERETOCONTINUE = "Click here to continue";

    public static RS2Widget getClickToContinueWidget(ScriptContext ctx) {
        return ctx.getWidgets()
                .getAll()
                .stream()
                .filter(f -> f.isVisible() && f.getMessage() != null && (f.getMessage().contains(TEXT_LOOKUP_CLICKHERETOCONTINUE) || f.getMessage().contains(TEXT_LOOKUP_CLICKTOCONTINUE)))
                .findFirst()
                .orElse(null);
    }

    public static boolean clickToContinue(ScriptContext ctx) {
        RS2Widget cct = getClickToContinueWidget(ctx);
        return cct != null && cct.interact();
    }

    public static boolean isInstructionVisible(ScriptContext ctx, String... instructionsTexts) {
        RS2Widget widget = ctx.getWidgets().getWidgetContainingText(WIDGET_INSTRUCTIONS_ROOT, instructionsTexts);
        return widget != null && widget.isVisible();
    }
}
