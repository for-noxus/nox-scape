package nox.scripts.noxscape;

import nox.scripts.noxscape.core.DecisionMaker;
import nox.scripts.noxscape.core.NoxScapeMasterNode;
import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

@ScriptManifest(name = "NoxScape", author = "Nox", version = 1.0, info = "", logo = "")
public class NoxScape extends Script {

    private ScriptContext ctx;

    private DecisionMaker decisionMaker;

    @Override
    public void onStart() {
        try {
            ctx = new ScriptContext(this, new File(getDirectoryData()+getName()+File.separator+"log.txt"));
            decisionMaker = new DecisionMaker(ctx);
        } catch (Exception e) {
            log("Script failed to start.");
            logException(e);
        }
    }

    @Override
    public int onLoop() {
        try {
            NoxScapeMasterNode cmn = ctx.getCurrentMasterNode();
            if (cmn == null || cmn.isCompleted()) {
                NoxScapeMasterNode newNode = decisionMaker.getNextMasterNode();
                log("Starting new MasterNode: " + newNode.getMasterNodeInformation().getFriendlyName());
                ctx.setCurrentMasterNode(newNode);
                cmn = newNode;
            }
            if (cmn.isAborted()) {
                log(String.format("Node %s requested script abortion.\nReason: %s", cmn.getClass().getSimpleName(), cmn.getAbortedReason()));
                stop();
            }
            if (!cmn.isCompleted()) {
                return cmn.continueExecution();
            }
            return 1001;
        } catch (Exception e) {
            logException(e);
            stop();
        }
        return -1;
    }

    private void logException(Exception e) {
        if (e.getMessage() != null)
            log(e.getMessage());
        Arrays.stream(e.getStackTrace()).forEach(f -> log(f.toString()));
    }

    @Override
    public void onPaint(Graphics2D g) {
        //This is where you will put your code for paint(s)
    }
}