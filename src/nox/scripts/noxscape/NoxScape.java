package nox.scripts.noxscape;

import nox.scripts.noxscape.core.DecisionMaker;
import nox.scripts.noxscape.core.NoxScapeMasterNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.tasks.mining.MiningEntity;
import nox.scripts.noxscape.tasks.mining.MiningMasterNode;
import nox.scripts.noxscape.util.Sleep;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.awt.*;
import java.util.Arrays;

@ScriptManifest(name = "NoxScape", author = "Nox", version = 1.0, info = "", logo = "")
public class NoxScape extends Script {

    private ScriptContext ctx;

    @Override
    public void onStart() {
        try {
            ctx = new ScriptContext(this, getDirectoryData());
            DecisionMaker.init(ctx);

            MiningMasterNode.Configuration cfg = new MiningMasterNode.Configuration();
            cfg.setRockToMine(MiningEntity.COAL);

            DecisionMaker.addPriorityTask(MiningMasterNode.class, cfg, null);
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
                if (newNode == null) {
                    log("We can't find a new node to execute. Exiting script");
                    stop(false);
                    return 1;
                }
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
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (ctx != null) {
            g.setColor(Color.white);
            g.drawString(ctx.currentNodeMessage(), 10, 325);

            Entity ent = ctx.getTargetEntity();
            if (ent != null) {
                g.setColor(Color.red.brighter());
                g.draw(ctx.getDisplay().getModelArea(ent.getGridX(), ent.getGridY(), ent.getZ(), ent.getModel()));
            }}

        g.setColor(Color.green.darker());
        // Get current mouse position
        Point mP = getMouse().getPosition();

        // Draw a line from top of screen (0), to bottom (500), with mouse x coordinate
        g.drawLine(mP.x, 0, mP.x, 500);

        // Draw a line from left of screen (0), to right (800), with mouse y coordinate
        g.drawLine(0, mP.y, 800, mP.y);
    }
}