package nox.scripts.noxscape.tasks.combat;

import nox.scripts.noxscape.core.*;
import nox.scripts.noxscape.core.enums.*;
import nox.scripts.noxscape.tasks.base.BankingNode;
import nox.scripts.noxscape.tasks.base.CombatNode;
import nox.scripts.noxscape.tasks.base.NpcInteractionNode;
import nox.scripts.noxscape.tasks.base.WalkingNode;
import nox.scripts.noxscape.tasks.base.banking.BankAction;
import nox.scripts.noxscape.tasks.base.banking.BankItem;
import nox.scripts.noxscape.tasks.base.combat.CombatLocation;
import nox.scripts.noxscape.tasks.base.combat.CombatPreferenceProfile;
import nox.scripts.noxscape.util.Pair;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Skill;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CombatMasterNode extends NoxScapeMasterNode<CombatMasterNode.Configuration> {

    public CombatMasterNode(ScriptContext ctx) {
        super(ctx);
        nodeInformation = new MasterNodeInformation(
                "Combat",
                "Fighting various monster",
                Frequency.COMMON,
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
            configuration = new CombatMasterNode.Configuration();

        //Todo: Identify if our stopwatcher is magic/ranged, and if so, select an appropriate combatLocation
        //Todo: and build an appropriate CombatPreferenaceProfile, as well as getting the right BankItems
        if (configuration.getCombatLocation() == null) {
            configuration.setCombatLocation(getSuggestedCombatLocation());
            ctx.logClass(this, "Selected combat location of " + configuration.getCombatLocation().getName());
        }

        if (configuration.getStyleToUse() == null) {
            Skill toTrain = stopWatcher.getSkillToTrain();
            if (toTrain == Skill.STRENGTH || toTrain == Skill.ATTACK || toTrain == Skill.DEFENCE)
                configuration.setStyleToUse(CombatStyle.forSkill(toTrain));
            else if (toTrain == null || toTrain == Skill.HITPOINTS)
                configuration.setStyleToUse(getLoweestLevelCombatStyle());
            else {
                abort(String.format("Unsupported skill to train for CombatNode declared in StopWatcher (%s)", toTrain));
                return;
            }
        }

        if (!configuration.getStyleToUse().trainsSkill(stopWatcher.getSkillToTrain())) {
            abort(String.format("Mismatch between preferred combat style (%s) and stopwatched skill (%s)", configuration.getStyleToUse().name(), stopWatcher.getSkillToTrain()));
            return;
        }

        List<BankItem> combatItems = new ArrayList<>(getListOfCombatItems());

        CombatPreferenceProfile defaultCombatProfile = new CombatPreferenceProfile()
                .fightWithStyle(configuration.styleToUse)
                .fightWithin(configuration.getCombatLocation().getCombatArea())
                .setNpcsToFight(configuration.getCombatLocation().getAsCombatable());

        NoxScapeNode preExecutionWalkNode = new WalkingNode(ctx)
                .toArea(configuration.getCombatLocation().getBank().getBankArea())
                .isWebWalk(true)
                .forPipeline(NodePipeline.PRE_EXECUTION)
                .hasMessage("Walking to combat bank for first time");

        NoxScapeNode preExecutionBankNode = new BankingNode(ctx)
                .bankingAt(configuration.getCombatLocation().getBank())
                .handlingItems(combatItems)
                .forPipeline(NodePipeline.PRE_EXECUTION)
                .hasMessage("Handling first-time combat banking");

        NoxScapeNode toNpcNode = new WalkingNode(ctx)
                .isWebWalk(true)
                .toArea(configuration.getCombatLocation().getCombatArea())
                .hasMessage("Walking to " + configuration.getCombatLocation().getName());

        NoxScapeNode interactNpcNode = new NpcInteractionNode(ctx)
                .isCombat(defaultCombatProfile)
                .hasMessage("Attacking " + configuration.getCombatLocation().getMonsterName());

        NoxScapeNode combatNode = new CombatNode(ctx)
                .withProfile(defaultCombatProfile)
                .addListener(ctx.getScriptProgress())
                .hasMessage("Fighting " + configuration.getCombatLocation().getMonsterName());

        NoxScapeNode toBankNode = new WalkingNode(ctx)
                .toArea(configuration.getCombatLocation().getBank().getBankArea())
                .isWebWalk(true);

        NoxScapeNode bankNode = new BankingNode(ctx)
                .bankingAt(configuration.getCombatLocation().getBank())
                .depositAllBackpackItems()
                .hasMessage("Refreshing inventory for cmobat");

        NoxScapeNode postExecutionWalkNode = new WalkingNode(ctx)
                .isWebWalk(true)
                .toArea(configuration.getCombatLocation().getBank().getBankArea())
                .forPipeline(NodePipeline.POST_EXECUTION)
                .hasMessage("Walking to bank to wrap up Combat");

        NoxScapeNode postExecutionBankNode = new BankingNode(ctx)
                .bankingAt(configuration.getCombatLocation().getBank())
                .depositAllBackpackItems()
                .depositAllWornItems()
                .forPipeline(NodePipeline.POST_EXECUTION)
                .hasMessage("Wrapping up Combat");

        preExecutionWalkNode.setChildNode(preExecutionBankNode);
        preExecutionBankNode.setChildNode(toNpcNode);
        toNpcNode.setChildNode(interactNpcNode);
        interactNpcNode.setChildNode(combatNode);
        combatNode.setChildNodes(Arrays.asList(interactNpcNode, toBankNode));
        bankNode.setChildNode(toNpcNode);
        postExecutionWalkNode.setChildNode(postExecutionBankNode);

        setNodes(Arrays.asList(preExecutionWalkNode, preExecutionBankNode, toNpcNode, interactNpcNode, combatNode, toBankNode, bankNode, postExecutionWalkNode, postExecutionBankNode));

        ctx.getBot().addMessageListener(this);
        ctx.logClass(this, String.format("Initialized %d nodes.", getNodes().size()));
    }

    @Override
    public boolean requiresPreExecution() {
        Map<String, List<BankItem>> combatItems = getListOfCombatItems()
                .stream()
                .collect(Collectors.groupingBy(BankItem::getSet, Collectors.toList()));

        for (Map.Entry<String, List<BankItem>> kvp: combatItems.entrySet()) {
            String[] itemNames = kvp.getValue().stream().map(BankItem::getName).toArray(String[]::new);
            if (!ctx.getEquipment().contains(itemNames))
                return true;
        }

        return false;
    }

    @Override
    public void onMessage(Message message) throws InterruptedException {
        if (message.getType() == Message.MessageType.GAME) {
            String text = message.getMessage().toLowerCase();
            if (text.contains("advanced your hitpoints"))
                ctx.getScriptProgress().onLevelUp(Skill.HITPOINTS);
            else if (text.contains("advanced your"))
                ctx.getScriptProgress().onLevelUp(stopWatcher.getSkillToTrain());
        }
    }

    private List<BankItem> getListOfCombatItems() {
        List<BankItem> combatItems = new ArrayList<>();
        combatItems.addAll(getCombatItemsFromCachedItems(CombatItems.scimitar(), "Scimitars"));
        combatItems.addAll(getCombatItemsFromCachedItems(CombatItems.kiteshield(), "Kiteshields"));
        combatItems.addAll(getCombatItemsFromCachedItems(CombatItems.fullhelms(), "Helmets"));
        combatItems.addAll(getCombatItemsFromCachedItems(CombatItems.platebody(), "Platebodies"));
        combatItems.addAll(getCombatItemsFromCachedItems(CombatItems.platelegs(), "Platelegs"));
        combatItems.addAll(getCombatItemsFromCachedItems(CombatItems.amulets(), "Amulets"));
        return combatItems;
    }

    private List<BankItem> getCombatItemsFromCachedItems(List<CachedItem> items, String set) {
        return items.stream().filter(f -> f.canEquip(ctx)).map(m -> new BankItem(m.getName(), BankAction.WITHDRAW, 1, set, m.requiredLevelSum(), true).buyIfNecessary(1)).collect(Collectors.toList());
    }

    private CombatLocation getSuggestedCombatLocation() {
        return Arrays.stream(CombatLocation.values())
                .filter(f -> f.getSuggestedCombatLevel() <= ctx.myPlayer().getCombatLevel())
                .min(Comparator.comparingInt(loc -> ctx.myPosition().distance(loc.getCombatArea().getRandomPosition())))
                .orElse(null);
    }

    private CombatStyle getLoweestLevelCombatStyle() {
        return Stream.of(new Pair<>(CombatStyle.ATTACK_ACCURATE, ctx.getSkills().getStatic(Skill.ATTACK)), new Pair<>(CombatStyle.STRENGTH_RAPID, ctx.getSkills().getStatic(Skill.STRENGTH)), new Pair<>(CombatStyle.DEFENCE_LONGRANGE, ctx.getSkills().getStatic(Skill.DEFENCE)))
                .min(Comparator.comparingInt(Pair::getB))
                .get().a;
    }

    public static class Configuration {
        private CombatLocation combatLocation;

        private CombatStyle styleToUse = null;

        private boolean buyScimiatrs = true;
        private boolean buyShield = true;
        private boolean buyHelm = true;
        private boolean buyBody = true;
        private boolean buyLegs = true;

        public void setCombatLocation(CombatLocation combatLocation) {
            this.combatLocation = combatLocation;
        }

        public CombatLocation getCombatLocation() {
            return combatLocation;
        }

        public void setStyleToUse(CombatStyle styleToUse) {
            this.styleToUse = styleToUse;
        }

        public CombatStyle getStyleToUse() {
            return styleToUse;
        }
    }
}
