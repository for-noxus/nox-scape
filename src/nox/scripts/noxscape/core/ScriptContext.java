package nox.scripts.noxscape.core;

import nox.scripts.noxscape.util.NRandom;
import org.osbot.rs07.script.MethodProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class ScriptContext extends MethodProvider {

    private NoxScapeMasterNode currentMasterNode;

    private File logFile;

    public ScriptContext(MethodProvider api, File logFile) throws IOException {
        exchangeContext(api.getBot());
        if (!logFile.exists()) {
            logFile.mkdirs();
            log("Creating logfile at: " + logFile.getAbsolutePath());
            logFile.createNewFile();
        }
        logClass(this, "Script context initialized.");
    }

    public NoxScapeMasterNode getCurrentMasterNode() {
        return currentMasterNode;
    }

    public void setCurrentMasterNode(NoxScapeMasterNode currentNode) {
        this.currentMasterNode = currentNode;
    }

    public NoxScapeNode getCurrentNode() { return currentMasterNode.getCurrentNode(); }

    public void logToFile(String text) {
        try {
            FileWriter out = new FileWriter(logFile, true);
            out.append(text + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
}
