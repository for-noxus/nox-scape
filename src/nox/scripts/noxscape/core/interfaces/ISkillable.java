package nox.scripts.noxscape.core.interfaces;

import org.osbot.rs07.api.ui.Skill;

public interface ISkillable extends INameable, IInteractable {
    Skill getSkill();

    int getRequiredLevel();

    String producesItemName();
}
