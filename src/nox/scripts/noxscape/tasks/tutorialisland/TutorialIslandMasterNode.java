package nox.scripts.noxscape.tasks.tutorialisland;

import nox.scripts.noxscape.core.MasterNodeInformation;
import nox.scripts.noxscape.core.NoxScapeMasterNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.enums.Duration;
import nox.scripts.noxscape.core.enums.Frequency;
import nox.scripts.noxscape.core.enums.MasterNodeType;
import nox.scripts.noxscape.tasks.tutorialisland.nodes.*;

import java.util.ArrayList;
import java.util.Arrays;

public class TutorialIslandMasterNode extends NoxScapeMasterNode {

    public final static int WIDGET_ROOT_PROGRESS = 614;

    public TutorialIslandMasterNode(ScriptContext ctx) {
        super(ctx);
        this.nodeInformation = getMasterNodeInformation();
    }

    @Override
    public boolean canExecute() {
        return ctx.getWidgets().isVisible(WIDGET_ROOT_PROGRESS);
    }

    @Override
    public void initializeNodes() {
        MagicGuide magicGuide = new MagicGuide(null, ctx, "Handling magic guide");
        PrayerGuide  prayerGuide = new PrayerGuide(magicGuide, ctx, "Handling prayer guide");
        BankGuide bankGuide = new BankGuide(prayerGuide, ctx, "Handling bank guide");
        CombatGuide combatGuide = new CombatGuide(bankGuide, ctx, "Handling combat guide");
        MiningGuide miningGuide = new MiningGuide(combatGuide, ctx, "Handling mining guide");
        QuestGuide questGuide = new QuestGuide(miningGuide, ctx, "Handling quest guide");
        CookGuide cookGuide = new CookGuide(questGuide, ctx, "Handling cooking guide");
        FishingGuide fishingGuide = new FishingGuide(cookGuide, ctx, "Handling fishing guide");
        WalkToFishingGuide walkToFishingGuide = new WalkToFishingGuide(fishingGuide, ctx, "Walking to fishing guide");
        ClickOptionsMenu clickOptionsMenu = new ClickOptionsMenu(null, ctx, "Clicking options menu");
        TalkToGuide talkToGuide = new TalkToGuide(Arrays.asList(clickOptionsMenu, walkToFishingGuide), ctx, "Talking to Guide");
        CreateCharacter createCharacter = new CreateCharacter(talkToGuide, ctx, "Creating your character.");

        clickOptionsMenu.setChildNode(talkToGuide);

        setNodes(new ArrayList<>(Arrays.asList(createCharacter, talkToGuide, clickOptionsMenu, walkToFishingGuide, fishingGuide, cookGuide, questGuide, miningGuide, combatGuide, bankGuide, prayerGuide, magicGuide)));

        ctx.logClass(this, String.format("Initialized %d nodes.", getNodes().size()));
    }

    @Override
    public boolean isCompleted() {
        return !canExecute();
    }

    @Override
    public boolean requiresPreExecution() {
        return false;
    }

    @Override
    public MasterNodeInformation getMasterNodeInformation() {
        if (nodeInformation != null)
            return nodeInformation;

        nodeInformation = new MasterNodeInformation(
                "Tutorial Island",
                "Completes Tutorial Island",
                Frequency.MANUAL,
                Duration.COMPLETION,
                MasterNodeType.QUEST);

        return nodeInformation;
    }
}
