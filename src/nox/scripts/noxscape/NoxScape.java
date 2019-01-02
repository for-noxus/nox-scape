package nox.scripts.noxscape;

import javafx.scene.paint.Stop;
import nox.scripts.noxscape.core.DecisionMaker;
import nox.scripts.noxscape.core.NoxScapeMasterNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.StopWatcher;
import nox.scripts.noxscape.tasks.base.banking.BankLocation;
import nox.scripts.noxscape.tasks.mining.MiningEntity;
import nox.scripts.noxscape.tasks.mining.MiningMasterNode;
import nox.scripts.noxscape.tasks.money_making.MoneyMakingMasterNode;
import nox.scripts.noxscape.ui.DebugPaint;
import nox.scripts.noxscape.util.Sleep;
import nox.scripts.noxscape.util.prices.RSBuddyExchangeOracle;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.event.WalkingEvent;
import org.osbot.rs07.event.WebWalkEvent;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.Condition;

import java.awt.*;
import java.util.Arrays;
import java.util.function.Supplier;

@ScriptManifest(name = "NoxScape", author = "Nox", version = 1.0, info = "", logo = "")
public class NoxScape extends Script {

    private ScriptContext ctx;

    @Override
    public void onStart() {
        try {
            ctx = new ScriptContext(this, getDirectoryData());
            DecisionMaker.init(ctx);

            MiningMasterNode.Configuration cfg = new MiningMasterNode.Configuration();
            cfg.setRockToMine(MiningEntity.IRON);
            cfg.setPurchaseNewPick(true);
            StopWatcher sw = StopWatcher.create(ctx).stopAfter(0, Skill.MINING).xpGained();

            DecisionMaker.addPriorityTask(MiningMasterNode.class, cfg, sw, false);
//            DecisionMaker.addPriorityTask(MoneyMakingMasterNode.class, null, StopWatcher.create(ctx).stopAfter(500).gpMade(), false);
        } catch (Exception e) {
            log("Script failed to start.");
            logException(e);
        }
        getBot().addPainter(new DebugPaint(ctx));
    }

    @Override
    public int onLoop() throws InterruptedException {
        try {
            NoxScapeMasterNode cmn = ctx.getCurrentMasterNode();
            // We either need a first node, or we need to move on to the next one
            if (cmn == null || cmn.isCompleted()) {
                if (cmn != null && cmn.isCompleted()) {
                    log("Successfully completed MasterNode " + cmn.getMasterNodeInformation().getFriendlyName());
                    cmn.reset();
                }
                NoxScapeMasterNode newNode = DecisionMaker.getNextMasterNode();
                if (newNode == null) {
                    log("We can't find a new node to execute. Exiting script");
                    stop(false);
                    return 1;
                }
                log("Starting new MasterNode: " + newNode.getMasterNodeInformation().getFriendlyName());
                ctx.setCurrentMasterNode(newNode);
                ctx.getCurrentMasterNode().getStopWatcher().begin();
                cmn = ctx.getCurrentMasterNode();
            }

            // If any nodes in our CMN, or our CMN itself is requesting abortion
            if (cmn.isAborted()) {
                log(String.format("Node %s aborted (%s)", cmn.getMasterNodeInformation().getFriendlyName(), cmn.getAbortedReason()));
                // Clear all nodes that were dependent on this one successfully executing
                DecisionMaker.clearDependentNodeStack();
                ctx.setCurrentMasterNode(null);
                // Loop back to the top to get assigned a new node
                return 3000;
            }

            // Watch our current node's stopwatcher
            if (cmn.getStopWatcher().shouldStop()) {
                return cmn.continuePostExecution();
            } else {
                // Carry on as usual
                return cmn.continueExecution();
            }
        } catch (Exception e) {
            logException(e);
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


        g.setColor(Color.green.darker());
        // Get current mouse position
        Point mP = getMouse().getPosition();

        // Draw a line from top of screen (0), to bottom (500), with mouse x coordinate
        g.drawLine(mP.x, 0, mP.x, 500);

        // Draw a line from left of screen (0), to right (800), with mouse y coordinate
        g.drawLine(0, mP.y, 800, mP.y);
    }
}