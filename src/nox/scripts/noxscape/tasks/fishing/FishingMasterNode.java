package nox.scripts.noxscape.tasks.fishing;

import nox.scripts.noxscape.core.MasterNodeInformation;
import nox.scripts.noxscape.core.NoxScapeMasterNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.enums.Duration;
import nox.scripts.noxscape.core.enums.Frequency;
import nox.scripts.noxscape.core.enums.MasterNodeType;
import nox.scripts.noxscape.tasks.base.banking.BankAction;
import nox.scripts.noxscape.tasks.base.banking.BankItem;
import nox.scripts.noxscape.tasks.base.banking.PurchaseLocation;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Skill;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class FishingMasterNode extends NoxScapeMasterNode<FishingMasterNode.Configuration> {

    public FishingMasterNode(ScriptContext ctx) {
        super(ctx);
        nodeInformation = new MasterNodeInformation("Fishing",
                "Fishing fishies",
                Frequency.UNCOMMON,
                Duration.LONG,
                MasterNodeType.SKILLING);
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    @Override
    public void initializeNodes() {
        if (configuration == null)
            configuration = new Configuration(getDefaultFishingLocation());

        if (configuration.fishingLocation == null) {
            abort("No FishingLocation selected");
            return;
        }

        if (configuration.fishingLocation.getFishingTool().getMinLevel() > ctx.getSkills().getStatic(Skill.FISHING)) {
            abort(String.format("Not high enough fishing for chosen fishing spot (%s) ", configuration.fishingLocation.getName()));
            return;
        }

        BankItem fishingTool = new BankItem(configuration.fishingLocation.getFishingTool().getPrimaryItemName(), BankAction.WITHDRAW, 1).buyIfNecessary(1, PurchaseLocation.NPC_STORE);
        BankItem fishingSupplement = new BankItem(configuration.fishingLocation.getFishingTool().getSecondaryItemName(), BankAction.WITHDRAW, 1000).buyIfNecessary(2000, PurchaseLocation.GRAND_EXCHANGE);
    }

    @Override
    public boolean requiresPreExecution() {
        return false;
    }

    @Override
    public void onMessage(Message message) throws InterruptedException {

    }

    private FishingLocation getDefaultFishingLocation() {
        List<FishingTool> toolsToUse = Arrays.asList(FishingTool.NET, FishingTool.FLY, FishingTool.POT, FishingTool.HARPOON);
        return Arrays.stream(FishingLocation.values())
                .filter(f -> toolsToUse.contains(f.getFishingTool()))
                .filter(f -> f.getFishingTool().getMinLevel() <= ctx.getSkills().getStatic(Skill.FISHING))
                .filter(f -> f.meetsCondition(ctx))
                .max(Comparator.comparingInt(i -> i.getFishingTool().getMinLevel()))
                .orElse(null);
    }

    public static class Configuration {
        final FishingLocation fishingLocation;

        public Configuration(FishingLocation fishingLocation) {
            this.fishingLocation = fishingLocation;
        }

        public FishingLocation getFishingLocation() {
            return fishingLocation;
        }
    }
}
