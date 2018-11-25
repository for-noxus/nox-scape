package nox.scripts.noxscape.core;

import org.osbot.rs07.script.MethodProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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

    public void logClass(Object o, String message) {
        log(o.getClass().getSimpleName() + ": " + message);
    }
}
