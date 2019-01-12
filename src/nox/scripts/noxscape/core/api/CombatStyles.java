package nox.scripts.noxscape.core.api;

import nox.scripts.noxscape.core.enums.CombatStyle;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.MethodProvider;

import java.util.concurrent.locks.StampedLock;

public class CombatStyles extends MethodProvider {

    private final int _combatconfig = 43;

    private final int _attackWidgetRootId = 593;

    public CombatStyles(MethodProvider api) {
        exchangeContext(api.getBot());
    }

    public CombatStyle getCurrentStyle() {
        switch(getCombatConfig()) {
            case 0: return CombatStyle.ATTACK_ACCURATE;
            case 1: return CombatStyle.STRENGTH_RAPID;
            case 2: return CombatStyle.BALANCED;
            case 3: return CombatStyle.DEFENCE_LONGRANGE;
            default: return null;
        }
    }

    public boolean setStyle(CombatStyle style) {
        if (style == null)
            throw new IllegalArgumentException("Unable to set a null combat style");

        if (!getTabs().open(Tab.ATTACK)) {
            log(String.format("Failed to open attack tab to set style (%s)", style.name()));
            return false;
        }

        RS2Widget styleWidget = getWidgets().get(_attackWidgetRootId, style.getComponentId());

        if (!styleWidget.isVisible()) {
            log(String. format("Couldn't find widget for style (%s)", style.name()));
            return false;
        }

        return styleWidget.interact();
    }

    private int getCombatConfig() {
        return getConfigs().get(_combatconfig);
    }
}
