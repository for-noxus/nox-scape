package nox.scripts.noxscape.core.interfaces;

import org.osbot.rs07.api.ui.Skill;

public interface ISkillable extends INameable {
    Skill getSkill();

    int getRequiredLevel();

    String getInteractAction();

    String producesItemName();
}
