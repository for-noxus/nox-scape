package nox.scripts.noxscape;

import nox.scripts.noxscape.core.DecisionMaker;
import nox.scripts.noxscape.core.NoxScapeMasterNode;
import nox.scripts.noxscape.core.ScriptContext;
import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.awt.*;
import java.io.File;
import java.util.Arrays;

@ScriptManifest(name = "NoxScape", author = "Nox", version = 1.0, info = "", logo = "")
public class NoxScape extends Script {

    private ScriptContext ctx;

    @Override
    public void onStart() {
        try {
            ctx = new ScriptContext(this, new File(getDirectoryData()+getName()+File.separator+"log.txt"));
            DecisionMaker.init(ctx);
        } catch (Exception e) {
            log("Script failed to start.");
            logException(e);
        }
    }

    @Override
    public int onLoop() throws InterruptedException {
        try {
            NoxScapeMasterNode cmn = ctx.getCurrentMasterNode();
            // We either need a first node, or we need to move on to the next one
            if (cmn == null || cmn.isCompleted()) {
                NoxScapeMasterNode newNode = DecisionMaker.getNextMasterNode();
                log("Starting new MasterNode: " + newNode.getMasterNodeInformation().getFriendlyName());
                ctx.setCurrentMasterNode(newNode);
                cmn = newNode;
            }

            // If any nodes in our CMN, or our CMN itself is requesting abortion
            if (cmn.isAborted()) {
                log(String.format("Node %s requested script abortion.\nReason: %s", cmn.getClass().getSimpleName(), cmn.getAbortedReason()));
                ctx.setCurrentMasterNode(null);
                // Loop back to the top to get assigned a new node
                return 3000;
            }
            // Watch our current node's stopwatcher
            if (cmn.getStopWatcher() != null && cmn.getStopWatcher().shouldStop()) {
                return cmn.continuePostExecution();
            } else {
                // Carry on as usual
                return cmn.continueExecution();
            }
        } catch (Exception e) {
            logException(e);
            sleep(5000);
            stop(false);
        }
        // This should never happen..........
        log("Captain we've got no fkin clue why we exited");
        stop();
        return 11;
    }

    private void logException(Exception e) {
        log(e);
        Arrays.stream(e.getStackTrace()).forEach(f -> log(f.toString()));
    }

    @Override
    public void onPaint(Graphics2D g) {
        //This is where you will put your code for paint(s)
    }
}