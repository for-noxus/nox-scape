package nox.scripts.noxscape.core.items;

import org.osbot.rs07.api.ui.Skill;

import java.util.List;

public final class WoodcuttingItems {

    public static List<CachedItem> axes() {
        List<CachedItem> items = CachedItem.generateFromBaseMetals("axe", Skill.WOODCUTTING, 1, 1, 6, 11, 21, 31, 41, 61);

        for (CachedItem i: items) {
            i.addLevelRequirement(Skill.ATTACK,  i.getLevelRequirement(Skill.WOODCUTTING) + 1);
        }

        return items;
    }
}
