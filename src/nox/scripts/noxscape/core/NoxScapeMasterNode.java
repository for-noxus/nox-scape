package nox.scripts.noxscape.core;

import nox.api.graphscript.Node;
import nox.scripts.noxscape.core.enums.Duration;
import nox.scripts.noxscape.core.enums.Frequency;
import nox.scripts.noxscape.core.enums.MasterNodeType;

import javax.swing.plaf.ColorUIResource;
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

    public int continueExecution() throws InterruptedException {
        NoxScapeNode lastNode = currentNode;
        int attempts = 0;
        while (!currentNode.isValid()) {
            if (attempts == 0)
                ctx.logClass(this, "Node (" + lastNode.getClass().getSimpleName() + ") is invalid. Scanning for next node");
            attempts++;
            currentNode = currentNode.getNext();
            if (currentNode != null) {
                ctx.logClass(this, "Checking node (" + currentNode.getClass().getSimpleName() + ") for validity.." + currentNode.isValid());
            } else {
                ctx.logClass(this, "There were no more nodes. MasterNode (" + nodeInformation.getFriendlyName() +") will abort.");
            }
            if (attempts > nodes.size() || currentNode == null) {
                this.abort(nodeInformation.getFriendlyName() + ": Unable to locate a valid node to continue execution. Last node that was valid: " + lastNode.getClass().getSimpleName());
                return 0;
            }
        }
        ctx.log(String.format("%s: Executing Node (%s)", nodeInformation.getFriendlyName(), currentNode.getClass().getSimpleName()));
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
                "\nnodeInformation=" + nodeInformation +
                "\n, isAborted=" + isAborted +
                "\n, abortedReason='" + abortedReason + '\'' +
                "\n, childNodes=(" + getChildNodeNames() + ")" +
                '}';
    }

    private String getChildNodeNames() {
        if (this.nodes == null || this.nodes.size() == 0)
            return "";
        return this.nodes.stream().map(m -> m.getClass().getSimpleName() + ": " + m.toDebugString()).reduce("", (a, b) -> a + b) + "\n\n";
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
