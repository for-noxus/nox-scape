package nox.scripts.noxscape.tasks.woodcutting;

import nox.scripts.noxscape.core.interfaces.ISkillable;
import org.osbot.rs07.api.ui.Skill;

public enum WoodcuttingEntity implements ISkillable {
    TREE("Tree", 1, "Logs"),
    OAK("Oak", 15, "Oak logs"),
    WILLOW("Willow", 30, "Willow logs"),
    MAPLE("Maple", 45, "Maple logs"),
    YEW("Yew", 60, "Yew logs"),
    MAGIC("Magic", 75, "Magic logs");

    private String name;
    private int requiredLevel;
    private String producedItem;

    WoodcuttingEntity(String name, int requiredLevel, String producedItem) {
        this.name = name;
        this.requiredLevel = requiredLevel;
        this.producedItem = producedItem;
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
    public String producesItemName() {
        return producedItem;
    }

    @Override
    public String getName() {
        return this.name;
    }

}
