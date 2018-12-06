package nox.scripts.noxscape.tasks.woodcutting;

import nox.scripts.noxscape.core.interfaces.ISkillable;
import org.osbot.rs07.api.ui.Skill;

public enum WoodcuttingEntity implements ISkillable {
    TREE("Tree", 1),
    OAK("Oak", 15),
    WILLOW("Willow", 30),
    MAPLE("Maple", 45),
    YEW("Yew", 60),
    MAGIC("Magic", 75);

    private String name;
    private int requiredLevel;

    WoodcuttingEntity(String name, int requiredLevel) {
        this.name = name;
        this.requiredLevel = requiredLevel;
    }

    @Override
    public Skill getSkill() {
        return Skill.WOODCUTTING;
    }

    @Override
    public int getRequiredLevel() {
        return this.requiredLevel;
    }

    @Override
    public String getInteractAction() {
        return "Chop-down";
    }

    @Override
    public String getName() {
        return this.name;
    }

}
