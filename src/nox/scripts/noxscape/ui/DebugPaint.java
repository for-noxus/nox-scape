package nox.scripts.noxscape.ui;

import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.StopWatcher;
import nox.scripts.noxscape.core.enums.StopCondition;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.canvas.paint.Painter;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DebugPaint implements Painter {

    private final Color color1 = new Color(0, 0, 0, 140);
    private final Color color2 = new Color(0, 0, 0);
    private final Color color3 = new Color(255, 255, 255);

    private final BasicStroke stroke1 = new BasicStroke(1);

    private final Font stopWatchFont = new Font("Arial", 0, 12);
    private final Font stopWatchTitleFont = new Font("Arial", 1, 14);

    private ScriptContext ctx;
    private StopWatcher watcher;

    private StopCondition stopCondition;
    private int stopConditionAmount = 0;
    private String runtime = "";
    private String masterNodeName = "";

    private long lastUpdateTime = 0;

    public DebugPaint(ScriptContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void onPaint(Graphics2D g) {
        if (ctx != null) {
            g.setColor(Color.white);
            g.drawString(ctx.currentNodeMessage(), 10, 325);

            Entity ent = ctx.getTargetEntity();
            if (ent != null) {
                g.setColor(Color.red.brighter());
                g.draw(ctx.getDisplay().getModelArea(ent.getGridX(), ent.getGridY(), ent.getZ(), ent.getModel()));
            }

            if (System.currentTimeMillis() - lastUpdateTime > 1000)
                updateVariables();

            g.setColor(color1);
            g.fillRect(322, 251, 195, 85);
            g.setColor(color2);
            g.setStroke(stroke1);
            g.drawRect(322, 251, 195, 85);
            g.setColor(color3);
            g.setFont(stopWatchTitleFont);
            g.drawString("StopWatcher (" + masterNodeName + ")", 330, 270);
            g.setFont(stopWatchFont);
            g.drawString("MasterNode Runtime: " + runtime, 300, 285);
            g.drawString("Stop Condition: " + stopCondition.getName(), 330, 300);
            g.drawString("Condition Amount: " + watcher.getTrackedAmount(), 330, 330);
        }
    }

    private void updateVariables() {
        if (watcher == null) {
            if (ctx != null && ctx.getCurrentMasterNode() != null && ctx.getCurrentMasterNode().getStopWatcher() != null) {
                this.watcher = ctx.getCurrentMasterNode().getStopWatcher();
            } else {
                return;
            }
        }

        stopCondition = watcher.getStopCondition();
        stopConditionAmount = watcher.getTrackedAmount();
        runtime = formatTime(watcher.getRunTime());
    }

    private String formatTime(long time) {
        Date date = new Date(time);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(date);
    }
}
