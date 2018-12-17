package nox.scripts.noxscape.core;

import nox.scripts.noxscape.util.NRandom;
import org.osbot.rs07.script.MethodProvider;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class ScriptContext extends MethodProvider {

    private NoxScapeMasterNode currentMasterNode;

    public final String logDir;

    public ScriptContext(MethodProvider api, String logDir) {
        this.logDir = logDir;
        exchangeContext(api.getBot());
        logClass(this, "Script context initialized.");
    }

    public NoxScapeMasterNode getCurrentMasterNode() {
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

    public void logSafe(Object clazz, Object... objects) {
        logClass(clazz, objects == null ? "null" : (String) Arrays.stream(objects).reduce("", (a, b) -> a + (b == null ? " null" : " " + b.toString())));
    }

    public void logClass(Object o, String message) {
        log(o.getClass().getSimpleName() + ": " + message);
    }

    public String currentNodeMessage() {
        if (currentMasterNode != null && currentMasterNode.getCurrentNode() != null && currentMasterNode.getCurrentNode().getMessage() != null)
            return currentMasterNode.getCurrentNode().getMessage();
        return "No message given";
    }
}
