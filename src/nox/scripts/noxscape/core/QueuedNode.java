package nox.scripts.noxscape.core;

public class QueuedNode {
    public Class<? extends NoxScapeMasterNode> clazz;
    public Object configuration;
    public StopWatcher stopWatcher;
}
