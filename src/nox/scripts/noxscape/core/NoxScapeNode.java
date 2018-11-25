package nox.scripts.noxscape.core;

import nox.api.graphscript.Node;

public abstract class NoxScapeNode<k extends Tracker> extends Node<NoxScapeNode> {

    protected final ScriptContext ctx;

    private k tracker;

    public NoxScapeNode(ScriptContext ctx, k tracker) {
        this(null, ctx, null, tracker);
    }

    public NoxScapeNode(NoxScapeNode next, ScriptContext ctx, String message, k tracker) {
        super(next, message);
        this.ctx = ctx;
        this.tracker = tracker;
    }

    @Override
    public NoxScapeNode getNext() {
        return super.getNext();
    }
}
