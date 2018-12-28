package nox.scripts.noxscape.tasks.mining;

import nox.scripts.noxscape.core.MasterNodeInformation;
import nox.scripts.noxscape.core.NoxScapeMasterNode;
import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.enums.Duration;
import nox.scripts.noxscape.core.enums.Frequency;
import nox.scripts.noxscape.core.enums.MasterNodeType;
import nox.scripts.noxscape.core.CachedItem;
import nox.scripts.noxscape.tasks.base.BankingNode;
import nox.scripts.noxscape.tasks.base.EntitySkillingNode;
import nox.scripts.noxscape.tasks.base.WalkingNode;
import nox.scripts.noxscape.tasks.base.banking.BankAction;
import nox.scripts.noxscape.tasks.base.banking.BankItem;
import nox.scripts.noxscape.tasks.base.banking.BankLocation;
import nox.scripts.noxscape.util.LocationUtils;
import nox.scripts.noxscape.util.NRandom;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.event.webwalk.PathPreferenceProfile;

import java.util.*;
import java.util.stream.Collectors;

public class MiningMasterNode extends NoxScapeMasterNode<MiningMasterNode.Configuration> {

    public MiningMasterNode(ScriptContext ctx) {
        super(ctx);
        nodeInformation = new MasterNodeInformation(
                "Mining",
                "Mines ores at various locations",
                Frequency.COMMON,
                Duration.COMPLETION,
                MasterNodeType.SKILLING);
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    @Override
    public void initializeNodes() {
        ctx.logClass(this, "Initializing Mining Nodes");

        // Get the highest level ore we can currently mine
        if (configuration == null)
            configuration = new Configuration();

        if (configuration.rockToMine == null) {
            configuration.rockToMine = Arrays.stream(MiningEntity.values())
                .filter(f -> f.getRequiredLevel() <= ctx.getSkills().getStatic(Skill.MINING))
                .max(Comparator.comparingInt(MiningEntity::getRequiredLevel))
                .get();
            ctx.log(Arrays.toString(Thread.currentThread().getStackTrace()));

        } else if (configuration.rockToMine.getRequiredLevel() > ctx.getSkills().getStatic(Skill.MINING)) {
            abort("Unable to mine chosen rock: " + configuration.rockToMine.getName());
            return;
        }

        BankItem[] axesToWithdraw = MiningItems.pickaxes().stream().filter(f -> f.canUse(ctx)).map(m -> new BankItem(m.getName(), BankAction.WITHDRAW, 1, "Mining", m.requiredLevelSum(), m.canEquip(ctx))).toArray(BankItem[]::new);
        BankItem oreToBank = new BankItem(configuration.rockToMine.producesItemName(), BankAction.DEPOSIT, 100);
        List<BankItem> bankItems = new ArrayList<>();
        bankItems.addAll(Arrays.asList(axesToWithdraw));
        bankItems.add(oreToBank);

        // Get the closest Mining location to ours
        final Position curPos = ctx.myPosition();
        MiningLocation location = Arrays.stream(MiningLocation.values())
                .filter(f -> f.rock == configuration.rockToMine)
                .min(Comparator.comparingInt(a -> a.positions[0].distance(curPos)))
                .get();

        PathPreferenceProfile ppp = new PathPreferenceProfile()
                .checkBankForItems(true)
                .checkEquipmentForItems(true)
                .checkInventoryForItems(true)
                .setAllowTeleports(true);

        NoxScapeNode preExecutionWalkNode = new WalkingNode(ctx)
                .isWebWalk(true)
                .setPathProfile(ppp)
                .toArea(location.getBank().getBankArea())
                .hasMessage("Walking to " + location.getBank().getName());

        NoxScapeNode preExecutionBankNode = new BankingNode(ctx)
                .bankingAt(location.getBank())
                .depositAllWornItems()
                .depositAllBackpackItems()
                .handlingItems(axesToWithdraw)
                .hasMessage(String.format("Banking at %s for the first time", location.getBank().getName()));

        Position orePos = NRandom.fromArray(location.positions);
        NoxScapeNode toOreNode = new WalkingNode(ctx)
                .toPosition(orePos)
                .isExactWebWalk(true)
                .hasMessage("Walking to Ore (" + configuration.rockToMine.getName() + ")");

        BankLocation depositLocation = location.getDepositBox() != null ? location.getDepositBox() : location.getBank();
        NoxScapeNode toBankNode = new WalkingNode(ctx)
                .toArea(depositLocation.getBankArea())
                .isWebWalk(true)
                .hasMessage("Walking to bank at " + depositLocation.getName());

        NoxScapeNode bankNode = new BankingNode(ctx)
                .bankingAt(depositLocation)
                .handlingItems(bankItems)
                .hasMessage(String.format("Banking %s at %s", configuration.rockToMine.producesItemName(), depositLocation.getName()));

        NoxScapeNode interactNode = new EntitySkillingNode(ctx)
                .interactWith(configuration.rockToMine)
                .boundedBy(orePos, location.shouldStayInTile() ? 1 : 16)
                .findEntityWith(ent -> ent.getName() != null && ent.getName().equals("Rocks") && configuration.rockToMine.hasOre(ent) && (!location.shouldStayInTile() || LocationUtils.manhattenDistance(ctx.myPosition(), ent.getPosition()) == 1))
                .entityInvalidWhen(ent -> !configuration.rockToMine.hasOre(ent), 30000, 200)
                .hasMessage("Mining " + configuration.rockToMine.getName());

        toOreNode.setChildNode(interactNode);
        interactNode.setChildNode(toBankNode);
        toBankNode.setChildNode(bankNode);
        bankNode.setChildNode(toOreNode);
        preExecutionBankNode.setChildNode(toOreNode);
        preExecutionWalkNode.setChildNode(preExecutionBankNode);

        setNodes(Arrays.asList(bankNode, interactNode, toOreNode, toBankNode, preExecutionBankNode, preExecutionWalkNode));
        setPreExecutionNode(preExecutionWalkNode);
        setReturnToBankNode(toBankNode);

        ctx.logClass(this, String.format("Initialized %d nodes.", getNodes().size()));
    }

    @Override
    public boolean requiresPreExecution() {
        String[] pickaxeNames = MiningItems.pickaxes().stream().filter(f -> f.canUse(ctx)).map(CachedItem::getName).toArray(String[]::new);
        Set<String> axeset = Arrays.stream(pickaxeNames).collect(Collectors.toSet());

        boolean inventoryHasAxe = ctx.getInventory().contains(pickaxeNames);
        boolean wieldingAxe = ctx.getEquipment().isWieldingWeaponThatContains(pickaxeNames);
        boolean hasStuffInInventory = !ctx.getInventory().isEmpty() && !Arrays.stream(ctx.getInventory().getItems()).allMatch(a -> a == null || a.getName() == null || (axeset.contains(a.getName()) || a.getName().equals(configuration.rockToMine.producesItemName())));

        return (!inventoryHasAxe && !wieldingAxe) || ctx.getInventory().isFull() || hasStuffInInventory;
    }

    public static class Configuration {
        MiningEntity rockToMine;

        @Override
        public String toString() {
            return "Configuration{" +
                    "rockToMine=" + rockToMine +
                    '}';
        }

        public void setRockToMine(MiningEntity rockToMine) {
            this.rockToMine = rockToMine;
        }
    }
}
