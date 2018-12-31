package nox.scripts.noxscape.core.interfaces;

public interface IActionListener {
    void onActionPerformed(String action);
    void onActionPerformed(String action, int amount);
    void onItemAcquired(int id, int amount);
}
