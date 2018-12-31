package nox.scripts.noxscape.util;

import org.osbot.rs07.utility.ConditionalSleep;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class Sleep extends ConditionalSleep {

    private final BooleanSupplier condition;

    public Sleep(final BooleanSupplier condition, final int timeout) {
        super(timeout);
        this.condition = condition;
    }

    public Sleep(final BooleanSupplier condition, final int timeout, final int interval) {
        super(timeout, interval);
        this.condition = condition;
    }

    @Override
    public final boolean condition() throws InterruptedException {
        return condition.getAsBoolean();
    }

    public static boolean until(final BooleanSupplier condition, final int timeout) {
        return new Sleep(condition, timeout).sleep();
    }

    public static boolean until(final BooleanSupplier condition, final int timeout, final int interval) {
        return new Sleep(condition, timeout, interval).sleep();
    }

    public static <k> k untilNotNull(final Supplier<k> locator, final int timeout, final int interval) {
        Sleep.until(() -> locator.get() != null, timeout, interval);
        return locator.get();
    }
}