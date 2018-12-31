package nox.scripts.noxscape.ui;

import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.StopWatcher;
import nox.scripts.noxscape.core.enums.StopCondition;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.canvas.paint.Painter;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

public class DebugPaint implements Painter {

    private final Color color1 = new Color(0, 0, 0, 140);
    private final Color color2 = new Color(0, 0, 0);
    private final Color color3 = new Color(255, 255, 255);

    private final BasicStroke stroke1 = new BasicStroke(1);

    private final Font font1 = new Font("Arial", 1, 14);
    private final Font font2 = new Font("Arial", 0, 12);
    private final Font font3 = new Font("Arial", 1, 12);
    private final Font font4 = new Font("Arial", 0, 10);

    private ScriptContext ctx;
    private StopWatcher watcher;

    private int stopConditionAmount = 0;
    private int goalAmount = 0;
    private String stopCondition = "";
    private String runtime = "";
    private String masterNodeName = "";
    private String scriptProgress = "";
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
            g.fillRect(290, 251, 226, 84);
            g.setColor(color2);
            g.setStroke(stroke1);
            g.drawRect(290, 251, 226, 84);
            g.setColor(color1);
            g.fillRoundRect(551, 209, 183, 256, 16, 16);
            g.setColor(color2);
            g.drawRoundRect(551, 209, 183, 256, 16, 16);
            g.setFont(font1);
            g.setColor(color3);
            g.drawString("StopWatcher - " +masterNodeName , 300, 270);
            g.setFont(font2);
            g.drawString("Stop Condition: " + stopCondition, 300, 315);
            g.drawString(String.format("Condition Amount: %d (%d)", stopConditionAmount, goalAmount), 300, 330);
            g.drawString("MasterNode Runtime: " + runtime, 300, 285);
            g.setFont(font3);
            g.drawString("Progress", 616, 224);
            g.setFont(font4);
            FontMetrics metrics = g.getFontMetrics();
            int vspace = metrics.getHeight();
            int idx = 0;
            for (String f : scriptProgress.split("\n")) {
                g.drawString(f, 560, 230 + (vspace * idx++));
            }
        }
    }

    private void updateVariables() {
        scriptProgress = ctx.getScriptProgress().toString();

        if (ctx == null || ctx.getCurrentMasterNode() == null)
            return;

        if (watcher == null || ctx.getCurrentMasterNode().getStopWatcher() != watcher) {
            this.watcher = ctx.getCurrentMasterNode().getStopWatcher();
        }

        stopCondition = watcher.getStopCondition().getName();
        stopConditionAmount = watcher.getTrackedAmount();
        goalAmount = watcher.getGoalAmount();
        runtime = formatTime(watcher.getRunTime());
        masterNodeName = ctx.getCurrentMasterNode().getMasterNodeInformation().getFriendlyName();
        lastUpdateTime = System.currentTimeMillis();
    }

    private String formatTime(long time) {
        Date date = new Date(time);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(date);
    }
}
