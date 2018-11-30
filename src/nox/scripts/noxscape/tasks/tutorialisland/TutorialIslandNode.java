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

public class TutorialIslandNode extends NoxScapeMasterNode {

    public final static int WIDGET_ROOT_PROGRESS = 614;

    public TutorialIslandNode(ScriptContext ctx) {
        super(ctx);
        this.tracker = new TutorialIslandTracker();
        this.nodeInformation = getMasterNodeInformation();
    }

    @Override
    public boolean canExecute() {
        return ctx.getWidgets().isVisible(WIDGET_ROOT_PROGRESS);
    }

    @Override
    public void initializeNodes() {
        MagicGuide magicGuide = new MagicGuide(null, ctx, "Handling magic guide", tracker);
        PrayerGuide  prayerGuide = new PrayerGuide(magicGuide, ctx, "Handling prayer guide", tracker);
        BankGuide bankGuide = new BankGuide(prayerGuide, ctx, "Handling bank guide", tracker);
        CombatGuide combatGuide = new CombatGuide(bankGuide, ctx, "Handling combat guide", tracker);
        MiningGuide miningGuide = new MiningGuide(combatGuide, ctx, "Handling mining guide", tracker);
        QuestGuide questGuide = new QuestGuide(miningGuide, ctx, "Handling quest guide", tracker);
        CookGuide cookGuide = new CookGuide(questGuide, ctx, "Handling cooking guide", tracker);
        FishingGuide fishingGuide = new FishingGuide(cookGuide, ctx, "Handling fishing guide", tracker);
        WalkToFishingGuide walkToFishingGuide = new WalkToFishingGuide(fishingGuide, ctx, "Walking to fishing guide", tracker);
        ClickOptionsMenu clickOptionsMenu = new ClickOptionsMenu(null, ctx, "Clicking options menu", tracker);
        TalkToGuide talkToGuide = new TalkToGuide(Arrays.asList(clickOptionsMenu, walkToFishingGuide), ctx, "Talking to Guide", tracker);
        CreateCharacter createCharacter = new CreateCharacter(talkToGuide, ctx, "Creating your character.", tracker);

        clickOptionsMenu.setChildNode(talkToGuide);

        nodes = new ArrayList(Arrays.asList(createCharacter, talkToGuide, clickOptionsMenu, walkToFishingGuide, fishingGuide, cookGuide, questGuide, miningGuide, combatGuide, bankGuide, prayerGuide, magicGuide));

        setEntryPoint();

        if (this.getCurrentNode() == null) {
            this.abort("Unable to find a valid entrypoint.");
        }

        ctx.logClass(this, String.format("Initialized %d nodes.", nodes.size()));
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

    @Override
    public boolean isCompleted() {
        return !canExecute();
    }
}
