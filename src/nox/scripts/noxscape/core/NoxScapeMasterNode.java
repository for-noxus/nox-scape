package nox.scripts.noxscape.core;

import nox.api.graphscript.Node;
import nox.scripts.noxscape.NoxScape;

import java.util.List;
import java.util.Objects;

public abstract class NoxScapeMasterNode<k extends Tracker> {

    protected k tracker;
    protected ScriptContext ctx;
    protected MasterNodeInformation nodeInformation;

    private long expirationTime;

    private NoxScapeNode currentNode;
    private NoxScapeNode postExecutionNode;
    private NoxScapeNode preExecutionNode;
    private NoxScapeNode returnToBankNode;

    private List<NoxScapeNode> nodes;

    private boolean completedPreExecution = false;
    private boolean isAborted;
    private String abortedReason;

    public NoxScapeMasterNode(ScriptContext ctx) {
        this.ctx = ctx;
        nodeInformation = getMasterNodeInformation();
    }

    public abstract boolean canExecute();

    public abstract MasterNodeInformation getMasterNodeInformation();

    public abstract void initializeNodes();

    protected void setDefaultEntryPoint() {
        this.currentNode = getEntryPoint();

        if (this.getCurrentNode() == null) {
            this.abort("Unable to find a valid entrypoint. All child nodes were null");
        }
    }

    public void setNodes(List<NoxScapeNode> nodes) {
        this.nodes = nodes;
    }

    public List<NoxScapeNode> getNodes() {
        return this.nodes;
    }

    protected NoxScapeNode getEntryPoint() {
        return nodes.stream().filter(Node::isValid).findFirst().orElse(null);
    }

    public abstract boolean requiresPreExecution();

    protected void setPreExecutionNode(NoxScapeNode node) {
        this.preExecutionNode = node;
    }

    public int continueExecution() throws InterruptedException {
        // Store the current node in a tmp
        NoxScapeNode lastNode = currentNode;

        if (currentNode == null) {
            ctx.logClass(this, String.format("Assigning entrpoiny to MasterNode (%s)", getMasterNodeInformation().getFriendlyName()));

            if (!completedPreExecution && requiresPreExecution()) {
                currentNode = preExecutionNode;
                if (currentNode == null)  {
                    abort("MasterNode requires PreExecution, but there was none specified");
                }
                ctx.logClass(this, String.format("Executing PreExecution pipeline for MasterNode (%s)", getMasterNodeInformation().getFriendlyName()));
            } else {
                // Attempt to find the next node from our current node's children
                currentNode = currentNode.getNext();
            }
        } else if (!currentNode.isValid() || currentNode.isCompleted()) {
            // If we've completed our current node gracefully..
            if (currentNode.isCompleted()) {
                ctx.logClass(this, String.format("Node (%s) has completed successfully. Finding next node", lastNode.getClass().getSimpleName()));
            } else {
                ctx.logClass(this, String.format("Node (%s) is invalid. Scanning for next node", lastNode.getClass().getSimpleName()));
            }

            currentNode = currentNode.getNext();

            // If we found a new node..
            if (currentNode != null) {
                currentNode.reactivate();
                ctx.logClass(this, String.format("Node (%s) found as a valid next node", currentNode.getClass().getSimpleName()));
            } else {
                // Unable to find any new nodes to execute from our current node
                ctx.logClass(this, String.format("%s had no valid children. Attempting to find a valid entrypoint in MasterNode..", lastNode.getClass().getSimpleName()));
                // Attempt to cycle through all nodes to find a new point of entry
                setDefaultEntryPoint();
                if (currentNode == null) {
                    ctx.logClass(this, String.format("There were no more valid nodes. MasterNode (%s) will abort.", nodeInformation.getFriendlyName()));
                    this.abort(nodeInformation.getFriendlyName() + ": Unable to locate a valid node to continue execution. Last node that was valid: " + lastNode.getClass().getSimpleName());
                    return 0;
                } else {
                    ctx.logClass(this, String.format("We were able to locate a valid entrypoint (%s).", currentNode.getClass().getSimpleName()));
                }
            }
        }

        ctx.log(String.format("%s: Executing Node (%s)", nodeInformation.getFriendlyName(), currentNode.getClass().getSimpleName()));
        return currentNode.execute();
    }

    public int continuePostExecution() throws InterruptedException {
        if (postExecutionNode != null && !postExecutionNode.isCompleted()) {
            return postExecutionNode.execute();
        } else {
            return returnToBankNode.execute();
        }
    }

    protected void setReturnToBankNode(NoxScapeNode node) {
        this.returnToBankNode = node;
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
        // Nodes aren't required to have a PostExecutionNode, but they are required to have a Node to return to bank
        return (postExecutionNode == null || postExecutionNode.isCompleted()) && returnToBankNode.isCompleted();
    }

    public boolean shouldComplete() {
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
