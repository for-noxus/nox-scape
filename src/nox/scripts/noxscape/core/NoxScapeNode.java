package nox.scripts.noxscape.core;

import nox.api.graphscript.Node;
import nox.scripts.noxscape.core.interfaces.IActionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class NoxScapeNode extends Node<NoxScapeNode> {

    protected ScriptContext ctx;

    private ArrayList<IActionListener> listeners;

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

    public NoxScapeNode addListener(IActionListener listener) {
        if (this.listeners == null)
            this.listeners = new ArrayList<>();

        if (listener != null)
            this.listeners.add(listener);

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

    protected void notifyItemAcquired(int itemId, int amount) {
        listeners.forEach(f -> f.onItemAcquired(itemId, amount));
    }

    protected void notifyAction(String action) {
        listeners.forEach(f -> f.onActionPerformed(action));
    }

    protected void notifyAction(String action, int amount) {
        listeners.forEach(f -> f.onActionPerformed(action, amount));
    }

    protected void logError(String error) {
        ctx.logClass(this, error);
    }
}
