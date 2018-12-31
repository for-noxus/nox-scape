package nox.scripts.noxscape.core;

import nox.scripts.noxscape.core.enums.StopCondition;
import nox.scripts.noxscape.core.interfaces.IAmountable;
import nox.scripts.noxscape.core.interfaces.IConditionable;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.listener.MessageListener;

import java.util.function.Consumer;

public class StopWatcher implements MessageListener {

    protected transient ScriptContext ctx;
    private Builder builder;
    private int trackedAmount;
    private long initTime;
    private StopWatcher(ScriptContext ctx) {
        this.ctx = ctx;
    }

    public static IAmountable create(ScriptContext ctx) {
        StopWatcher watcher = new StopWatcher(ctx);
        return new Builder(watcher);
    }

    public static StopWatcher createDefault(ScriptContext ctx) {
        StopWatcher watcher = new StopWatcher(ctx);
        Builder bld = new Builder(watcher);
        bld.condition = StopCondition.UNSET;
        return watcher;
    }

    @Override
    public void onMessage(Message message) {
        if (message.getType() == Message.MessageType.GAME && message.getMessage().toLowerCase().contains(builder.actionsMessage))
            trackedAmount++;
    }

    public void addTrackedAmount(int amount) {
        this.trackedAmount += amount;
    }

    public boolean shouldStop() {
        return getTrackedAmount() >= builder.amount;
    }

    public void begin() {
        trackedAmount = 0;
        initTime = System.currentTimeMillis();
        if (builder.skill != null)
            ctx.getExperienceTracker().start(builder.skill);
    }

    public long getRunTime() {
        return System.currentTimeMillis() - initTime;
    }

    public int getGoalAmount() {
        return builder.amount;
    }

    public int getTrackedAmount() {
        switch(builder.condition) {
            case XP_GAINED:
                return ctx.getExperienceTracker().getGainedXP(builder.skill);
            case LEVELS_GAINED:
                return ctx.getExperienceTracker().getGainedLevels(builder.skill);
            case MONEY_MADE:
            case RESOURCES_ACTIONED:
                return trackedAmount;
            case TIME_ELAPSED:
                return (int)((System.currentTimeMillis() - initTime) / 1000 / 60);
            default:
                return -1;
        }
    }

    public StopCondition getStopCondition() {
        return builder.condition;
    }

    @Override
    public String toString() {
        return "StopWatcher{" +
                "trackedAmount=" + trackedAmount +
                "builder=" + builder.toString() +
                '}';
    }

    private static class Builder implements IConditionable, IAmountable {
        private StopCondition condition;
        private Skill skill;
        private String actionsMessage;
        private int amount;
        private transient StopWatcher stopWatcher;

        public Builder(StopWatcher stopWatcher) {
            this.stopWatcher = stopWatcher;
            this.stopWatcher.builder = this;
        }

        @Override
        public StopWatcher levelsGained() {
            this.condition = StopCondition.LEVELS_GAINED;
            return stopWatcher;
        }

        @Override
        public StopWatcher xpGained() {
            this.condition = StopCondition.XP_GAINED;
            return stopWatcher;
        }

        @Override
        public StopWatcher gpMade() {
            this.condition = StopCondition.MONEY_MADE;
            return stopWatcher;
        }

        @Override
        public StopWatcher minutesRan() {
            this.condition = StopCondition.TIME_ELAPSED;
            return stopWatcher;
        }

        @Override
        public StopWatcher actionsPerformed() {
            this.condition = StopCondition.RESOURCES_ACTIONED;
            return stopWatcher;
        }

        @Override
        public StopWatcher messagesContaining(String message) {
            this.condition = StopCondition.RESOURCES_ACTIONED;
            this.actionsMessage = message;
            stopWatcher.ctx.getBot().addMessageListener(stopWatcher);
            return stopWatcher;
        }

        @Override
        public IConditionable stopAfter(int amount) {
            this.amount = amount;
            return this;
        }

        @Override
        public IConditionable stopAfter(int amount, Skill skill) {
            this.amount = amount;
            this.skill = skill;
            return this;
        }

        @Override
        public String toString() {
            return "Builder{" +
                    ", condition=" + condition +
                    ", skill=" + skill +
                    ", actionsMessage='" + actionsMessage + '\'' +
                    ", amount=" + amount +
                    '}';
        }
    }
}

