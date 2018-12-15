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

    private StopWatcher(ScriptContext ctx) {
        this.ctx = ctx;
    }

    public static IAmountable create(ScriptContext ctx) {
        StopWatcher watcher = new StopWatcher(ctx);
        return new Builder(watcher);
    }

    @Override
    public void onMessage(Message message) throws InterruptedException {
        if (message.getType() == Message.MessageType.GAME && message.getMessage().toLowerCase().contains(builder.actionsMessage))
            trackedAmount++;
    }

    public boolean shouldStop() {
        switch(builder.condition) {
            case XP_GAINED:
                return ctx.getExperienceTracker().getGainedXP(builder.skill) >= builder.amount;
            case LEVELS_GAINED:
                return ctx.getExperienceTracker().getGainedLevels(builder.skill) >= builder.amount;
            case MONEY_MADE:
            case RESOURCES_ACTIONED:
                return trackedAmount >= builder.amount;
            case TIME_ELAPSED:
                return ((System.currentTimeMillis() - builder.initTime) / 1000 / 60) >= builder.amount;
            default:
                return true;
        }
    }

    public void begin() {
        trackedAmount = 0;
        builder.initTime = System.currentTimeMillis();
    }

    protected static class Builder implements IConditionable, IAmountable {
        private long initTime;
        private StopCondition condition;
        private Skill skill;
        private String actionsMessage;
        private int amount;
        private transient StopWatcher stopWatcher;

        public Builder(StopWatcher stopWatcher) {
            this.stopWatcher = stopWatcher;
            this.stopWatcher.builder = this;
            initTime = System.currentTimeMillis();
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

    }
}

