package nox.scripts.noxscape.core.interfaces;

import org.osbot.rs07.api.ui.Skill;

public interface IActionListener {
    void onActionPerformed(String action);
    void onActionPerformed(String action, int amount);
    void onLevelUp(Skill skill);
    void onItemAcquired(int id, int amount);
}
