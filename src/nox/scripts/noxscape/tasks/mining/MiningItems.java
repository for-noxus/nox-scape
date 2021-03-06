package nox.scripts.noxscape.tasks.mining;

import nox.scripts.noxscape.core.CachedItem;
import org.osbot.rs07.api.ui.Skill;

import java.util.List;
import java.util.stream.Collectors;

public final class MiningItems {

    public static List<CachedItem> pickaxes() {
        List<CachedItem> items = CachedItem.generateFromBaseMetals("pickaxe", Skill.MINING, 1, 1, 6, -1, 21, 31, 41, 61);

        for (CachedItem i: items) {
            i.addLevelRequirement(Skill.ATTACK, i.getLevelRequirement(Skill.MINING) - 1);
        }

        return items.stream().filter(f -> f.requiredLevelSum() > 0).collect(Collectors.toList());
    }
}
