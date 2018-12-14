package nox.scripts.noxscape.core.interfaces;

import org.osbot.rs07.api.ui.Skill;

public interface IAmountable {
    IConditionable stopAfter(int amount);
    IConditionable stopAfter(int amount, Skill skill);
}