package nox.scripts.noxscape.core.enums;

import nox.scripts.noxscape.core.interfaces.INameable;
import org.osbot.rs07.api.ui.Skill;

public enum CombatStyle {
    ATTACK_ACCURATE("Attack", "Accurate", Skill.ATTACK, 4),
    STRENGTH_RAPID("Strength", "Rapid", Skill.STRENGTH, 8),
    BALANCED("Balanced", null, null, 12),
    DEFENCE_LONGRANGE("Defence", "Long range", Skill.DEFENCE, 16);

    private final String meleeName;
    private final String rangedName;
    private final Skill skillTrained;
    private final int componentId;

    CombatStyle(String meleeName, String rangedName, Skill skillTrained, int componentId) {
        this.meleeName = meleeName;
        this.rangedName = rangedName;
        this.skillTrained = skillTrained;
        this.componentId = componentId;
    }

    public String getName(boolean isRanged) {
        return isRanged ? rangedName : meleeName;
    }

    public int getComponentId() {
        return componentId;
    }

    public Skill getSkillTrained() {
        return skillTrained;
    }

    public boolean trainsSkill(Skill skill) {
        return (skill == Skill.RANGED || skill == Skill.MAGIC) || skill  == this.skillTrained;
    }

    public static CombatStyle forSkill(Skill skill) {
        switch(skill) {
            case ATTACK:
                return ATTACK_ACCURATE;
            case STRENGTH:
                return STRENGTH_RAPID;
            case DEFENCE:
                return DEFENCE_LONGRANGE;
            default:
                throw new IllegalArgumentException("Selected skill " + skill.name() + " has no matching combat style");
        }
    }
}
