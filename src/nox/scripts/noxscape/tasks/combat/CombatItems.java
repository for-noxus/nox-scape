package nox.scripts.noxscape.tasks.combat;

import nox.scripts.noxscape.core.CachedItem;
import org.osbot.rs07.api.Quests;
import org.osbot.rs07.api.ui.Skill;

import java.util.Collections;
import java.util.List;

public final class CombatItems {

    public static List<CachedItem> platebody() {
        List<CachedItem> platebodies = CachedItem.generateFromBaseMetals("platebody", Skill.DEFENCE, 0, 1, 5, 10, 20, 30, 40, 60);
        platebodies.stream().filter(f -> f.getLevelRequirement(Skill.DEFENCE) == 40).findFirst().get().setAddititionalConditions(ctx -> ctx.getQuests().isComplete(Quests.Quest.DRAGON_SLAYER));
        return platebodies;
    }

    public static List<CachedItem> platelegs() {
        return CachedItem.generateFromBaseMetals("platelegs", Skill.DEFENCE, 0, 1, 5, 10, 20, 30, 40, 60);
    }

    public static List<CachedItem> fullhelms() {
        return CachedItem.generateFromBaseMetals("full helm", Skill.DEFENCE, 0, 1, 5, 10, 20, 30, 40, 60);
    }

    public static List<CachedItem> kiteshield() {
        return CachedItem.generateFromBaseMetals("kiteshield", Skill.DEFENCE, 0, 1, 5, 10, 20, 30, 40, 60);
    }

    public static List<CachedItem> scimitar() {
        return CachedItem.generateFromBaseMetals("scimitar", Skill.DEFENCE, 0, 1, 5, 10, 20, 30, 40, 60);
    }

    public static List<CachedItem> amulets() {
        return Collections.singletonList(new CachedItem("Amulet of power", null));
    }
}
