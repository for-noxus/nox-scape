package nox.scripts.noxscape.tasks.base.combat;

import nox.scripts.noxscape.core.enums.CombatStyle;
import nox.scripts.noxscape.core.interfaces.ICombatable;
import nox.scripts.noxscape.tools.FuzzyBoundsTester;
import nox.scripts.noxscape.util.NRandom;
import org.osbot.rs07.api.map.Area;

import java.util.Arrays;
import java.util.List;

public class CombatPreferenceProfile {

    private List<Food> foods;

    private CombatStyle style;

    private int healthToEat;
    private boolean isExact;
    private boolean IsPercentHealthToEat;
    private List<ICombatable> npcsToFight;
    private Area areaToFight;

    public CombatPreferenceProfile consumeFood(List<Food> foods) {
        this.foods = foods;
        return this;
    }

    public CombatPreferenceProfile consumeFood(Food... foods) {
        this.foods = Arrays.asList(foods);
        return this;
    }

    public CombatPreferenceProfile eatWhenBelow(int health, boolean isExact) {
        this.healthToEat = health;
        this.isExact = isExact;
        return this;
    }

    public CombatPreferenceProfile eatWhenBelowPercent(int percent, boolean isExact) {
        this.IsPercentHealthToEat = true;
        healthToEat = percent;
        return this;
    }

    public CombatPreferenceProfile fightWithStyle(CombatStyle style) {
        this.style = style;
        return this;
    }

    public CombatPreferenceProfile fightWithin(Area area) {
        this.areaToFight = area;
        return this;
    }

    public CombatPreferenceProfile setNpcsToFight(List<ICombatable> npcs) {
        if (npcs.stream().anyMatch(a -> a.getLevel() <= 0 || a.getName() == null || a.getName() == ""))
            throw new IllegalArgumentException("ICombatable NPC submitted with either a blank name or invalid level");

        npcsToFight = npcs;
        return this;
    }

    public boolean shouldEat(int currentHealth, int maxHealth) {
        if (healthToEat == 0)
            return false;

        int cutoff = IsPercentHealthToEat ? maxHealth * healthToEat : healthToEat;

        if (isExact && currentHealth <= cutoff)
            return true;

        int lowerdev = (int) (Math.floor(cutoff * 0.4));
        int upperdev = (int) (Math.floor(cutoff * 0.2));
        cutoff = cutoff = NRandom.fuzzedBounds(cutoff, lowerdev, upperdev);

        return currentHealth <= cutoff;
    }

    public List<Food> getFoods() {
        return foods;
    }

    public CombatStyle getStyle() {
        return style;
    }

    public List<ICombatable> getNpcsToFight() {
        return npcsToFight;
    }

    public Area getAreaToFight() {
        return areaToFight;
    }
}
