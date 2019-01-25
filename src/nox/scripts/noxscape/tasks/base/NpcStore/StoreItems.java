package nox.scripts.noxscape.tasks.base.NpcStore;

import nox.scripts.noxscape.core.CachedItem;
import nox.scripts.noxscape.util.Pair;
import org.osbot.rs07.api.ui.Skill;

import java.util.Arrays;
import java.util.List;

public class StoreItems {

    public static List<CachedItem> fishing() {
        return Arrays.asList(
                new CachedItem("Small fishing net"),
                new CachedItem("Fishing rod", new Pair<>(Skill.FISHING, 5)),
                new CachedItem("Fly fishing rod", new Pair<>(Skill.FISHING, 5)),
                new CachedItem("Harpoon", new Pair<>(Skill.FISHING, 35)),
                new CachedItem("Lobster pot", new Pair<>(Skill.FISHING, 40)),
                new CachedItem("Feather"),
                new CachedItem("Bait")
        );
    }

    public static List<CachedItem> general() {
        return Arrays.asList(
                new CachedItem("Pot"),
                new CachedItem("Jug"),
                new CachedItem("Shears"),
                new CachedItem("Bucket"),
                new CachedItem("Bowl"),
                new CachedItem("Cake tin"),
                new CachedItem("Tinderbox"),
                new CachedItem("Chisel"),
                new CachedItem("Hammer")
        );
    }
}
