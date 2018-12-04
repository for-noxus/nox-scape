package nox.scripts.noxscape.core;

import nox.api.graphscript.Node;
import nox.scripts.noxscape.NoxScape;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class NoxScapeNode<k extends Tracker> extends Node<NoxScapeNode> {

    protected ScriptContext ctx;

    private k tracker;

    public NoxScapeNode(List<NoxScapeNode> children, ScriptContext ctx, String message, k tracker) {
        super(children, message);
        this.ctx = ctx;
        this.tracker = tracker;
    }

    public NoxScapeNode(NoxScapeNode child, ScriptContext ctx, String message, k tracker) {
        super(child, message);
        this.ctx = ctx;
        this.tracker = tracker;
    }

    public NoxScapeNode() {
        super((NoxScapeNode) null, "");
    }

    public NoxScapeNode<k> trackedBy(k tracker) { this.tracker = tracker; return this; }
    public NoxScapeNode<k> withContext(ScriptContext ctx) { this.ctx = ctx; return this; }

    public String toDebugString() {
        String ret = "Message: " + this.getMessage();
        if (this.getChildNodes() != null && this.getChildNodes().size() > 0) {
            ret += "\nChildren: " +  this.getChildNodes().stream().filter(Objects::nonNull).map(m -> m.getClass().getSimpleName()).reduce("", (a, b) -> a + b + ", ");
        }
        return ret;
    }

    @Override
    public NoxScapeNode getNext() {
        return super.getNext();
    }

    protected void logError(String error) {
        ctx.logClass(this, error);
    }
}
