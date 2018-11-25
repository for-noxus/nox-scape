package nox.scripts.noxscape.tasks.tutorialisland;

import nox.scripts.noxscape.core.Tracker;

public class TutorialIslandTracker extends Tracker {
    public int progressPercent;
    public String stage;

    @Override
    public String getMessage() {
        if (progressPercent == 100) {
            return "Tutorial island successfully completed.";
        } else {
            return String.format("We are on stage %s, and are %s percent done.",stage, progressPercent);
        }
    }
}
