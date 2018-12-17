package nox.scripts.noxscape.core.items;

import org.osbot.rs07.api.ui.Skill;

import java.util.List;

public final class MiningItems {

    public static List<CachedItem> pickaxes() {
        List<CachedItem> items = CachedItem.generateFromBaseMetals("pickaxe", Skill.MINING, 1, 1, 6, 11, 21, 31, 41, 61);

        for (CachedItem i: items) {
            i.addLevelRequirement(Skill.ATTACK, i.getLevelRequirement(Skill.MINING) - 1);
        }

        return items;
    }
}
