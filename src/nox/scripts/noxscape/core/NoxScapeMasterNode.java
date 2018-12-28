package nox.scripts.noxscape.core;

import nox.api.graphscript.Node;
import nox.scripts.noxscape.NoxScape;
import nox.scripts.noxscape.core.interfaces.IAmountable;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class NoxScapeMasterNode<k> {

    protected ScriptContext ctx;

    protected MasterNodeInformation nodeInformation;
    protected k configuration;
    protected StopWatcher stopWatcher;

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
    }

    public int continueExecution() throws InterruptedException {
        // Store the current node in a tmp
        NoxScapeNode lastNode = currentNode;

        // If we don't yet have a node..
        if (currentNode == null) {

            ctx.logClass(this, String.format("Assigning entrypoint to MasterNode (%s)", getMasterNodeInformation().getFriendlyName()));

            // If we haven't completed our PreExecution yet
            if (!completedPreExecution) {
                // Sometimes we don't need to
                if (!requiresPreExecution()) {
                    ctx.logClass(this, String.format("MasterNode (%s) already satisfies PreExecution condition, moving right along..", getMasterNodeInformation().getFriendlyName()));
                    completedPreExecution = true;
                    setDefaultEntryPoint();
                } else {
                    currentNode = preExecutionNode;
                    if (currentNode == null) {
                        abort("MasterNode requires PreExecution, but there was none specified");
                    }
                    ctx.logClass(this, String.format("Executing PreExecution pipeline for MasterNode (%s)", getMasterNodeInformation().getFriendlyName()));
                }
            } else {
                // Attempt to find the next node from our current node's children
                currentNode = currentNode.getNext();
            }
        } else if (!currentNode.isValid() || currentNode.isCompleted()) {
            // If we've completed our current node gracefully..
            if (currentNode.isCompleted())
                ctx.logClass(this, String.format("Node (%s) has completed successfully (%s). Finding next node", lastNode.getClass().getSimpleName(), lastNode.getCompletedMessage()));
            else
                ctx.logClass(this, String.format("Node (%s) is invalid. Scanning for next node", lastNode.getClass().getSimpleName()));

            currentNode = currentNode.getNext();

            // If we found a new node..
            if (currentNode != null) {
                currentNode.reactivate();
                ctx.logClass(this, String.format("Node (%s) found as a valid next node", currentNode.getClass().getSimpleName()));
            } else {
                // Unable to find any new nodes to execute from our current node
                ctx.logClass(this, String.format("%s had no valid children. Attempting to find a valid entrypoint in MasterNode..", lastNode.getClass().getSimpleName()));

                // Attempt to cycle through all nodes to find a new point of entry
                if (!setDefaultEntryPoint(lastNode))
                    return 0;
                else
                    ctx.logClass(this, String.format("We were able to locate a valid entrypoint (%s).", currentNode.getClass().getSimpleName()));
            }
        }

        if (currentNode != null) {
            ctx.log(String.format("%s: Executing Node (%s)", nodeInformation.getFriendlyName(), currentNode.getClass().getSimpleName()));
            return currentNode.execute();
        }

        return 0;
    }

    public int continuePostExecution() throws InterruptedException {
        if (postExecutionNode != null && !postExecutionNode.isCompleted()) {
            return postExecutionNode.execute();
        } else {
            return returnToBankNode.execute();
        }
    }

    public abstract boolean canExecute();

    public abstract void initializeNodes();

    public abstract boolean requiresPreExecution();

    public void setNodes(List<NoxScapeNode> nodes) {
        this.nodes = nodes;
    }

    protected void setReturnToBankNode(NoxScapeNode node) {
        this.returnToBankNode = node;
    }

    protected void setPreExecutionNode(NoxScapeNode node) {
        this.preExecutionNode = node;
    }

    protected NoxScapeNode getCurrentNode() {
        return currentNode;
    }

    public List<NoxScapeNode> getNodes() {
        return this.nodes;
    }

    public MasterNodeInformation getMasterNodeInformation() {
        return nodeInformation;
    }

    public StopWatcher getStopWatcher() {
        return stopWatcher;
    }

    public void setStopWatcher(StopWatcher stopWatcher) {
        this.stopWatcher = stopWatcher;
    }

    public NoxScapeMasterNode configureStopWatcher(Function<IAmountable, StopWatcher> config) {
        stopWatcher = config.apply(StopWatcher.create(ctx));
        return this;
    }

    public boolean isCompleted() {
        // Nodes aren't required to have a PostExecutionNode, but they are required to have a Node to return to bank
        return (postExecutionNode == null || postExecutionNode.isCompleted()) && returnToBankNode.isCompleted() && nodes.stream().allMatch(NoxScapeNode::isCompleted);
    }

    public boolean isAborted() {
        return nodes == null || nodes.stream().anyMatch(Node::isAborted) || isAborted;
    }


    public void reset() {
        if (ctx.getBot().getMessageListeners().contains(stopWatcher)) {
            ctx.getBot().removeMessageListener(stopWatcher);
        }
        this.completedPreExecution = false;
        this.nodes = null;
        this.configuration = null;
        this.stopWatcher = null;
        this.isAborted = false;
        this.abortedReason = null;
        this.preExecutionNode = null;
        this.postExecutionNode = null;
        this.returnToBankNode = null;
    }

    public k getConfiguration() {
        return configuration;
    }

    protected void abort(String abortedReason) {
        isAborted = true;
        this.abortedReason = abortedReason;
    }

    public String getAbortedReason() {
        return abortedReason == null ?
                nodes.stream().filter(Node::isAborted).map(Node::getAbortedReason).findFirst().orElse(null) :
                abortedReason;
    }

    private String getChildNodeNames() {
        if (this.nodes == null || this.nodes.size() == 0)
            return "";
        return this.nodes.stream().map(m -> m.getClass().getSimpleName() + ": " + m.toDebugString()).reduce("", (a, b) -> a + b) + "\n\n";
    }

    private boolean setDefaultEntryPoint(NoxScapeNode lastNode) {
        this.currentNode = getEntryPoint();

        if (this.currentNode == null) {
            ctx.logClass(this, String.format("There were no more valid nodes. MasterNode (%s) will abort.", nodeInformation.getFriendlyName()));

            if (lastNode != null)
                ctx.logClass(this, "Last node that was valid: " + lastNode.getClass().getSimpleName());

            this.abort(nodeInformation.getFriendlyName() + ": Unable to locate a valid node to continue execution.");
            return false;
        }

        return true;
    }

    private boolean setDefaultEntryPoint() {
        return setDefaultEntryPoint(null);
    }

    private NoxScapeNode getEntryPoint() {
        return nodes.stream().filter(Node::isValid).findFirst().orElse(null);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoxScapeMasterNode that = (NoxScapeMasterNode) o;
        return Objects.equals(nodeInformation, that.nodeInformation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeInformation);
    }
}
