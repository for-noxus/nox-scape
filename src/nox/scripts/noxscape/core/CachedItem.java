package nox.scripts.noxscape.core;

import nox.scripts.noxscape.util.Pair;
import nox.scripts.noxscape.core.interfaces.INameable;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.MethodProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class CachedItem implements INameable {
    private String name;

    private Predicate<MethodProvider> addititionalConditions;

    private List<Pair<Skill, Integer>> requiredLevels;

    public CachedItem(String name, Pair<Skill, Integer>... requiredLevels) {
        this(name, null, requiredLevels);
    }

    public CachedItem(String name, Predicate<MethodProvider> addititionalConditions, Pair<Skill, Integer>... requiredLevels) {
        this.name = name;
        this.addititionalConditions = addititionalConditions;
        this.requiredLevels = requiredLevels == null ? null : new ArrayList<>(Arrays.asList(requiredLevels));
    }

    public String getName() {
        return name;
    }

    public Predicate<MethodProvider> getAddititionalConditions() {
        return addititionalConditions;
    }

    public void setAddititionalConditions(Predicate<MethodProvider> addititionalConditions) {
        this.addititionalConditions = addititionalConditions;
    }

    public int getLevelRequirement(Skill skill) {
        if (requiredLevels.size() == 0)
            return 0;
        else if (requiredLevels.size() == 1 && requiredLevels.get(0).a.equals(skill))
            return requiredLevels.get(0).b;
        else
            return requiredLevels.stream().filter(f -> f.a.equals(skill)).findFirst().orElse(new Pair<>(Skill.AGILITY, 0)).b;
    }

    public void addLevelRequirement(Skill skill, int req) {
        this.requiredLevels.add(new Pair<>(skill, req));
    }

    public boolean canEquip(MethodProvider api) {
        List<Skill> equipSkills = Arrays.asList(Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE, Skill.RANGED);
        return (addititionalConditions == null || addititionalConditions.test(api)) &&
                (requiredLevels == null || requiredLevels.stream().filter(f -> equipSkills.contains(f.a)).noneMatch(pair -> api.getSkills().getDynamic(pair.a) < pair.b));
    }

    public boolean canUse(MethodProvider api) {
        List<Skill> equipSkills = Arrays.asList(Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE, Skill.RANGED);

        return (addititionalConditions == null || addititionalConditions.test(api)) &&
                (requiredLevels == null || requiredLevels.stream().filter(f -> !equipSkills.contains(f.a)).noneMatch(pair -> api.getSkills().getDynamic(pair.a) < pair.b));
    }

    public int requiredLevelSum() {
        return requiredLevels == null ? 0 : requiredLevels.stream().map(m -> m.b).reduce(Integer::sum).get();
    }

    public static List<CachedItem> generateFromBaseMetals(String itemSuffix, Skill skill, int bronzeReq, int ironReq, int steelReq, int blackReq,
                                                          int mithReq, int adamantReq, int runeReq, int dragonReq) {
        return Arrays.asList(
                new CachedItem("Bronze " + itemSuffix, new Pair<>(skill, bronzeReq)),
                new CachedItem("Iron " + itemSuffix, new Pair<>(skill, ironReq)),
                new CachedItem("Steel " + itemSuffix, new Pair<>(skill, steelReq)),
                new CachedItem("Black " + itemSuffix, new Pair<>(skill, blackReq)),
                new CachedItem("Mithril " + itemSuffix, new Pair<>(skill, mithReq)),
                new CachedItem("Adamant " + itemSuffix, new Pair<>(skill, adamantReq)),
                new CachedItem("Rune " + itemSuffix, new Pair<>(skill, runeReq)),
                new CachedItem("Dragon " + itemSuffix, api -> api.getWorlds().isMembersWorld(), new Pair<>(skill, dragonReq))
        );
    }
}
