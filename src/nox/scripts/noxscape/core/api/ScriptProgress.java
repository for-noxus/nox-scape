package nox.scripts.noxscape.core.api;

import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.enums.StopCondition;
import nox.scripts.noxscape.core.interfaces.IActionListener;
import org.osbot.rs07.api.def.ItemDefinition;
import org.osbot.rs07.script.MethodProvider;

import java.util.HashMap;
import java.util.Map;

public class ScriptProgress extends MethodProvider implements IActionListener {

    private Map<String, Integer> actionsMap = new HashMap<>();
    private ScriptContext ctx;

    public ScriptProgress(ScriptContext ctx) {
        this.ctx = ctx;
        exchangeContext(ctx.getBot());
    }

    @Override
    public void onActionPerformed(String action) {
        onActionPerformed(action, 1);
    }

    @Override
    public void onActionPerformed(String action, int amount) {
        if (action == null || action.equals(""))
            return;

        actionsMap.computeIfPresent(action, (k, v) -> v + amount);
        actionsMap.putIfAbsent(action, amount);

        if (ctx.getCurrentMasterNode() != null && ctx.getCurrentMasterNode().getStopWatcher() != null) {
            ctx.getCurrentMasterNode().getStopWatcher().addTrackedAmount(1);
        }
    }

    @Override
    public void onItemAcquired(int id, int amount) {
        if (id <= 0 || amount <= 0)
            return;

        ItemDefinition def = ItemDefinition.forId(id);

        if (def == null) {
            log(String.format("Unable to identify acquired item (%d)", id));
            return;
        }

        if (def.getName() == null) {
            log("Found definition for item id " + id + ", but name was null?");
        }

        actionsMap.computeIfPresent("Acquired " + def.getName(), (k, v) -> v + 1);
        actionsMap.putIfAbsent("Acquired " + def.getName(), 1);

        if (ctx.getCurrentMasterNode() != null && ctx.getCurrentMasterNode().getStopWatcher() != null) {
            if (ctx.getCurrentMasterNode().getStopWatcher().getStopCondition() == StopCondition.MONEY_MADE) {
                int dosh = ctx.getGrandExchange().getOverallPrice(id) * amount;
                ctx.getCurrentMasterNode().getStopWatcher().addTrackedAmount(dosh);
            } else {
                ctx.getCurrentMasterNode().getStopWatcher().addTrackedAmount(amount);
            }
        }
    }

    @Override
    public String toString() {
        return actionsMap.entrySet().stream().map(m -> String.format("%s (x%d)", padRight(m.getKey(), 40), m.getValue())).reduce("", (prev, next) -> prev + "\n" + next);
    }

    public static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }
}
