package nox.scripts.noxscape.tasks.base;

import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.tasks.base.banking.BankItem;
import nox.scripts.noxscape.util.Sleep;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.script.MethodProvider;

import java.util.*;
import java.util.stream.Collectors;

public class BankingNode extends NoxScapeNode {

    private Area bankArea;
    private BankItem[] items;
    private boolean depositAllBackpackItems = false;
    private boolean depositallWornItems = false;

    public BankingNode(ScriptContext ctx) {
        super(ctx);
    }

    public BankingNode bankingAt(Area bankArea) {
        this.bankArea = bankArea;
        return this;
    }

    public BankingNode handlingItems(BankItem... items) {
        this.items = items;
        return this;
    }

    public BankingNode depositAllWornItems() {
        this.depositallWornItems = true;
        return this;
    }

    public BankingNode depositAllBackpackItems() {
        this.depositAllBackpackItems = true;
        return this;
    }

    @Override
    public boolean isValid() {
        if (items == null || items.length == 0) {
            abort("Banking node added but no items were added for deposit/withdrawal");
            return false;
        }

        if (bankArea == null) {
            abort("There was no destination set for this banking node!");
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

        if (depositallWornItems)
            ctx.getBank().depositWornItems();

        if (depositAllBackpackItems)
            ctx.getBank().depositAll();

        if (items != null) {

            // Split our items into two sets based on whether or not you can withdraw them
            Map<Boolean, List<BankItem>> belongsToSet = Arrays.stream(items).collect(Collectors.partitioningBy(item -> item.getSet() != null));

            // Handle item sets
            belongsToSet.get(true).stream().filter(f -> f.getSet() != null).collect(Collectors.groupingBy(BankItem::getSet)).forEach(this::withdrawItemSet);

            // Deposit all deposit-items
            belongsToSet.get(false).stream().filter(BankItem::isDeposit).forEach(this::depositItem);

            ctx.sleep(0, 80);
            // Withdraw all withdraw-items
            belongsToSet.get(false).stream().filter(BankItem::isWithdraw).forEach(this::withdrawItem);
        }

        if (!ctx.getBank().close())
            logError("Error closing bank;");
        else
            complete("Successfully handled banking");

        return MethodProvider.random(50, 800);
    }

    private void withdrawItemSet(String key, List<BankItem> set) {
        set.sort(Comparator.comparingInt(o -> o.getPriority()));

        Optional<BankItem> itemToWithdraw = set.stream().filter(item -> ctx.getBank().contains(item.getName())).findFirst();

        if (itemToWithdraw.isPresent()) {
            if (!ctx.getBank().withdraw(itemToWithdraw.get().getName(), itemToWithdraw.get().getAmount()))
                logError(String.format("Error withdrawing item (%s) belonging to set (%s).", itemToWithdraw.get().getName(), itemToWithdraw.get().getSet()));
        } else {
            abort(String.format("Unable to locate any items belonging to set (%s)", itemToWithdraw.get().getSet()));
        }
    }

    private void depositItem(BankItem item) {
        if (item.getName() != null && ctx.getInventory().contains(item.getName())) {
            if (ctx.getInventory().getAmount(item.getName()) <= item.getAmount()) {
                if (!ctx.getBank().depositAll(item.getName())) {
                    logBankError(item);
                }
            } else if (!ctx.getBank().deposit(item.getName(), item.getAmount())) {
                logBankError(item);
            }
        }
    }

    private void withdrawItem(BankItem item) {
        if (item.getName() != null) {
            if (!ctx.getBank().contains(item.getName())) {
                abort(String.format("Bank does not contain item (%s)", item.getName()));
            } else {
                if (ctx.getInventory().getEmptySlotCount() <= item.getAmount()) {
                    if (!ctx.getBank().withdrawAll(item.getName()))
                        logBankError(item);
                } else {
                    if (!ctx.getBank().withdraw(item.getName(), item.getAmount()))
                        logBankError(item);
                }
            }
        }
    }

    private void logBankError(BankItem item) {
        logError(String.format("Error handling banking action (%s) for item %s.", item.getAction().name(), item.getName()));
    }
}
