package nox.scripts.noxscape.tasks.combat;

import nox.scripts.noxscape.core.MasterNodeInformation;
import nox.scripts.noxscape.core.NoxScapeMasterNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.enums.Duration;
import nox.scripts.noxscape.core.enums.Frequency;
import nox.scripts.noxscape.core.enums.MasterNodeType;
import nox.scripts.noxscape.tasks.base.banking.BankLocation;
import nox.scripts.noxscape.tasks.base.combat.CombatLocation;
import org.osbot.rs07.api.ui.Message;

import java.util.Arrays;
import java.util.Comparator;

public class CombatMasterNode<Configuration> extends NoxScapeMasterNode<CombatMasterNode.Configuration> {

    public CombatMasterNode(ScriptContext ctx) {
        super(ctx);
        nodeInformation = new MasterNodeInformation(
                "Combat",
                "Fighting various monster",
                Frequency.COMMON,
                Duration.MEDIUM,
                MasterNodeType.SKILLING);
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    @Override
    public void initializeNodes() {
        if (configuration == null)
            configuration = new CombatMasterNode.Configuration();

        if (configuration.getCombatLocation() == null)
            configuration.setCombatLocation(getSuggestedCombatLocation());
    }

    @Override
    public boolean requiresPreExecution() {
        return false;
    }

    @Override
    public void onMessage(Message message) throws InterruptedException {

    }

    private CombatLocation getSuggestedCombatLocation() {
        return Arrays.stream(CombatLocation.values())
                .filter(f -> f.getSuggestedCombatLevel() <= ctx.myPlayer().getCombatLevel())
                .min(Comparator.comparingInt(loc -> ctx.myPosition().distance(loc.getCombatArea().getRandomPosition())))
                .orElse(null);
    }

    public static class Configuration {
        private CombatLocation combatLocation;

        public void setCombatLocation(CombatLocation combatLocation) {
            this.combatLocation = combatLocation;
        }

        public CombatLocation getCombatLocation() {
            return combatLocation;
        }
    }
}
