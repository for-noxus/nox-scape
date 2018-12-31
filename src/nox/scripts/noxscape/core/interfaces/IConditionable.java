package nox.scripts.noxscape.core.interfaces;

import nox.scripts.noxscape.core.StopWatcher;

public interface IConditionable {
    StopWatcher levelsGained();
    StopWatcher xpGained();
    StopWatcher gpMade();
    StopWatcher minutesRan();

    StopWatcher actionsPerformed();

    StopWatcher messagesContaining(String message);
}