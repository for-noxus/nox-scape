package nox.scripts.noxscape.tasks.core;

import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.tasks.core.banking.BankItem;
import nox.scripts.noxscape.util.Sleep;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.script.MethodProvider;

import java.util.Arrays;

public class BankingNode extends NoxScapeNode {

    private final Area bankArea;
    private final BankItem[] items;


    public BankingNode(NoxScapeNode child, ScriptContext ctx, String message, Area bankArea, BankItem... items) {
        super(child, ctx, message, null);
        this.bankArea = bankArea;
        this.items = items;
    }

    @Override
    public boolean isValid() {
        if (items == null || items.length == 0) {
            abort("Banking node added but no items were added for deposit/withdrawal");
            return false;
        }

        return bankArea.contains(ctx.myPosition());
    }

    @Override
    public int execute() throws InterruptedException {
        // Ensure bank screen is open
        if (!ctx.getBank().isOpen()) {
            if (!ctx.getBank().open()) {
                logError("Error opening bank at location");
            }
            Sleep.sleepUntil(() -> ctx.getBank().isOpen(), 6000, 600);
        }

        // Deposit all deposit-items
        Arrays.stream(items).filter(BankItem::isDeposit).forEach(this::depositItem);
        ctx.sleep(0, 80);
        // Withdraw all withdraw-items
        Arrays.stream(items).filter(BankItem::isWithdraw).forEach(this::withdrawItem);

        if (!ctx.getBank().close())
            logError("Error closing bank;");
        else
            this.complete();

        return MethodProvider.random(50, 800);
    }

    private void depositItem(BankItem item) {
        if (item.getName() != null) {
            if (!ctx.getBank().deposit(item.getName(), item.getAmount()))
                logBankError(item);
        } else if (item.getId() == 0) {
            if (!ctx.getBank().deposit(item.getId(), item.getAmount()))
                logBankError(item);
        }
    }

    private void withdrawItem(BankItem item) {
        if (item.getName() != null) {
            if (!ctx.getBank().contains(item.getName()))
                abort(String.format("Bank does not contain item (%s)", item.getName()));
            else if (!ctx.getBank().withdraw(item.getName(), item.getAmount()))
                logBankError(item);
        } else if (item.getId() == 0) {
            if (!ctx.getBank().contains(item.getId()))
                abort(String.format("Bank does not contain item (%s)", item.getId()));
            else if (!ctx.getBank().withdraw(item.getId(), item.getAmount()))
                logBankError(item);
        }
    }

    private void logBankError(BankItem item) {
        logError(String.format("Error handling banking action (%s) for item %s", item.getAction().name(), item.getName() != null ? item.getName() : item.getId() + "."));
    }
}
