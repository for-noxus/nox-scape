package nox.scripts.noxscape.core;

import nox.api.graphscript.Node;
import nox.scripts.noxscape.core.enums.Duration;
import nox.scripts.noxscape.core.enums.Frequency;
import nox.scripts.noxscape.core.enums.MasterNodeType;

import java.util.ArrayList;
import java.util.Objects;

public abstract class NoxScapeMasterNode<k extends Tracker> {

    protected k tracker;
    protected ScriptContext ctx;
    protected ArrayList<NoxScapeNode> nodes;

    private long expirationTime;

    private NoxScapeNode currentNode;

    protected MasterNodeInformation nodeInformation;

    private boolean isAborted;
    private String abortedReason;

    public NoxScapeMasterNode(ScriptContext ctx) {
        this.ctx = ctx;
        nodeInformation = getMasterNodeInformation();
    }

    public abstract boolean canExecute();

    public abstract MasterNodeInformation getMasterNodeInformation();

    public abstract void initializeNodes();

    protected void setEntryPoint() {
        this.currentNode = nodes.stream().filter(Node::isValid).findFirst().orElse(null);
    }

    public int continueExecution() {
        int attempts = 0;
        while (!currentNode.isValid()) {
            attempts++;
            currentNode = currentNode.getNext();
            if (attempts > nodes.size()) {
                this.abort(nodeInformation.getFriendlyName() + ": Unable to locate a valid node to continue execution.");
            }
        }
        return currentNode.execute();
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public void setCurrentNode(NoxScapeNode currentNode) {
        this.currentNode = currentNode;
    }

    public NoxScapeNode getCurrentNode() {
        return currentNode;
    }

    public boolean isCompleted() {
        return System.currentTimeMillis() > expirationTime;
    }

    public boolean isAborted() {
        return nodes.stream().anyMatch(Node::isAborted) || isAborted;
    }

    public void abort(String abortedReason) {
        isAborted = true;
        this.abortedReason = abortedReason;
    }

    public String getAbortedReason() {
        return abortedReason == null ?
                nodes.stream().filter(Node::isAborted).map(Node::getAbortedReason).findFirst().orElse(null) :
                abortedReason;
    }

    @Override
    public String toString() {
        return "NoxScapeMasterNode{" +
                "nodeInformation=" + nodeInformation +
                ", isAborted=" + isAborted +
                ", abortedReason='" + abortedReason + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoxScapeMasterNode<?> that = (NoxScapeMasterNode<?>) o;
        return Objects.equals(nodeInformation, that.nodeInformation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeInformation);
    }
}
