package nox.scripts.noxscape.core;

import nox.scripts.noxscape.core.api.QuickExchange;
import nox.scripts.noxscape.core.api.ScriptProgress;
import nox.scripts.noxscape.util.NRandom;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.script.MethodProvider;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class ScriptContext extends MethodProvider {

    private NoxScapeMasterNode currentMasterNode;

    private Entity targetEntity;
    public final String logDir;

    private QuickExchange quickExchange;
    private ScriptProgress scriptProgress;

    public ScriptContext(MethodProvider api, String logDir) {
        this.logDir = logDir;
        exchangeContext(api.getBot());
    }

    public QuickExchange getQuickExchange() {
        if (quickExchange == null)
            quickExchange = new QuickExchange( this);

        return quickExchange;
    }

    public ScriptProgress getScriptProgress() {
        if (scriptProgress == null)
            scriptProgress = new ScriptProgress(this);

        return scriptProgress;
    }

    public NoxScapeMasterNode<?> getCurrentMasterNode() {
        return currentMasterNode;
    }

    public void setCurrentMasterNode(NoxScapeMasterNode currentNode) {
        this.currentMasterNode = currentNode;
    }

    public NoxScapeNode getCurrentNode() { return currentMasterNode.getCurrentNode(); }

    public void sleepH() throws InterruptedException {
        MethodProvider.sleep((long) NRandom.humanized());
    }

    public void sleepHQuick() throws InterruptedException {
        MethodProvider.sleep((long) (NRandom.humanized() * 0.7));
    }

    public void sleep(int lowerBounds, int higherBounds) throws InterruptedException {
        MethodProvider.sleep(new Random().nextInt(higherBounds - lowerBounds) + lowerBounds);
    }

    public void logClass(Object o, String message) {
        log(o.getClass().getSimpleName() + ": " + message);
    }

    public String currentNodeMessage() {
        if (currentMasterNode != null && currentMasterNode.getCurrentNode() != null && currentMasterNode.getCurrentNode().getMessage() != null)
            return currentMasterNode.getCurrentNode().getMessage();
        return "No message given";
    }

    public Entity getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(Entity targetEntity) {
        this.targetEntity = targetEntity;
    }
}
