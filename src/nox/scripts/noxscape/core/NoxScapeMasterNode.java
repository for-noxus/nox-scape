package nox.scripts.noxscape.core;

import nox.api.graphscript.Node;
import nox.scripts.noxscape.NoxScape;
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
        this.currentNode = getEntryPoint();
    }

    protected NoxScapeNode getEntryPoint() {
        return nodes.stream().filter(Node::isValid).findFirst().orElse(null);
    }

    public int continueExecution() throws InterruptedException {
        // Store the current node in a tmp
        NoxScapeNode lastNode = currentNode;

        // Track how many times we attempt to find a new node
        int attempts = 0;
        // Our current node is invalid or completed. Find a new one
        while (!currentNode.isValid() || currentNode.isCompleted()) {
            // If we've completed our current node gracefully..
            if (currentNode.isCompleted()) {
                ctx.logClass(this, String.format("Node (%s) has completed successfully. Finding next node", lastNode.getClass().getSimpleName()));
            } else if (attempts == 0) {
                ctx.logClass(this, String.format("Node (%s) is invalid. Scanning for next node", lastNode.getClass().getSimpleName()));
            }
            attempts++;
            // Attempt to find the next node
            currentNode = currentNode.getNext();
            // If we found a new node..
            if (currentNode != null) {
                ctx.logClass(this, String.format("Checking node (%s) for validity..%s", currentNode.getClass().getSimpleName(), currentNode.isValid()));
            } else {
                // Unable to find any new nodes to execute from our current node
                ctx.logClass(this, String.format("%s had no valid children. Attempting to find a valid entrypoint in MasterNode..", lastNode.getClass().getSimpleName()));
                // Attempt to cycle through all nodes to find a new point of entry
                setEntryPoint();
                if (currentNode == null) {
                    ctx.logClass(this, String.format("There were no more nodes. MasterNode (%s) will abort.", nodeInformation.getFriendlyName()));
                    return 0;
                } else {
                    ctx.logClass(this, String.format("We were able to locate a valid entrypoint (%s).", currentNode.getClass().getSimpleName()));
                }
            }
            // No nodes in our children were valid to execute
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
