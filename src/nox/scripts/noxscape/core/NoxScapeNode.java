package nox.scripts.noxscape.core;

import nox.api.graphscript.Node;

import java.util.List;
import java.util.Objects;

public abstract class NoxScapeNode extends Node<NoxScapeNode> {

    protected ScriptContext ctx;

    public NoxScapeNode(ScriptContext ctx) {
        this.ctx = ctx;
    }

    public NoxScapeNode(List<NoxScapeNode> children, ScriptContext ctx, String message) {
        super(children, message);
        this.ctx = ctx;
    }

    public NoxScapeNode(NoxScapeNode child, ScriptContext ctx, String message) {
        super(child, message);
        this.ctx = ctx;
    }

    public NoxScapeNode() {
        super((NoxScapeNode) null, "");
    }

    @Override
    public void setChildNodes(List<NoxScapeNode> nodes) {
        super.setChildNodes(nodes);
    }

    @Override
    public void setChildNode(NoxScapeNode node) {
        super.setChildNode(node);
    }

    @Override
    public NoxScapeNode hasChild(NoxScapeNode child) {
        super.hasChild(child);
        return this;
    }

    @Override
    public NoxScapeNode hasChildren(List<NoxScapeNode> children) {
        super.hasChildren(children);
        return this;
    }

    @Override
    public NoxScapeNode hasMessage(String message) {
         super.hasMessage(message);
         return this;
    }

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
