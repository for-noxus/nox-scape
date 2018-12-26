package nox.scripts.noxscape.tasks.base;

import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.tasks.base.banking.BankItem;
import nox.scripts.noxscape.tasks.base.banking.BankLocation;
import nox.scripts.noxscape.util.NRandom;
import nox.scripts.noxscape.util.Sleep;

import java.util.*;
import java.util.stream.Collectors;

public class BankingNode extends NoxScapeNode {

    private BankLocation bankLocation;
    private BankItem[] items;
    private boolean depositAllBackpackItems = false;
    private boolean depositallWornItems = false;

    public BankingNode(ScriptContext ctx) {
        super(ctx);
    }

    public BankingNode bankingAt(BankLocation bankLocation) {
        this.bankLocation = bankLocation;
        return this;
    }

    public BankingNode handlingItems(BankItem... items) {
        this.items = items;
        return this;
    }

    public BankingNode handlingItems(List<BankItem> items) {
        this.items = new BankItem[items.size()];
        this.items = items.toArray(this.items);
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

        if (bankLocation == null) {
            abort("There was no destination set for this banking node!");
            return false;
        }

        return bankLocation.getBankArea().contains(ctx.myPosition());
    }

    // Deposit-all iron ore
    // Deposit inventory
    // Withdraw-all iron ore
    // Withdraw-all adamant pickaxe
    @Override
    public int execute() throws InterruptedException {
        // Ensure bank screen is open
        if (bankLocation.isDepositBox()) {
            if (!ctx.getDepositBox().isOpen()) {
                if (!ctx.getDepositBox().open())
                    logError("Error opening deposit box at location " + bankLocation.getName());
            }
            Sleep.until(() -> ctx.getDepositBox().isOpen(), 6000, 600);
        } else if (!ctx.getBank().isOpen()) {
            if (!ctx.getBank().open()) {
                logError("Error opening bank at location " + bankLocation.getName());
            }
            Sleep.until(() -> ctx.getBank().isOpen(), 6000, 600);
        }

        if (depositallWornItems && !ctx.getEquipment().isEmpty()) {
            if (bankLocation.isDepositBox())
                ctx.getDepositBox().depositWornItems();
            else
                ctx.getBank().depositWornItems();
            ctx.sleepHQuick();
        }

        if (depositAllBackpackItems && !ctx.getInventory().isEmpty()) {
            if (bankLocation.isDepositBox())
                ctx.getDepositBox().depositAll();
            else
                ctx.getBank().depositAll();
            ctx.sleepHQuick();
        }

        if (items != null) {

            // Split our items into two sets based on whether or not you can withdraw them
            Map<Boolean, List<BankItem>> belongsToSet = Arrays.stream(items).collect(Collectors.partitioningBy(item -> item.getSet() != null));

            // Handle item sets
            BankItem[] setItemsToWithdraw = belongsToSet.get(true).stream().filter(f -> f.getSet() != null).collect(Collectors.groupingBy(BankItem::getSet)).values().stream().map(this::getOwnedItemFromSet).toArray(BankItem[]::new);
            belongsToSet.get(false).addAll(Arrays.asList(setItemsToWithdraw));

            // Deposit all deposit-items
            belongsToSet.get(false).stream().filter(BankItem::isDeposit).forEach(this::depositItem);

            // Deposit all items in inventory that aren't specified to be withdrawn
            Set<String> bankItemNames = Arrays.stream(items).map(BankItem::getName).collect(Collectors.toSet());
            if (bankLocation.isDepositBox())
                ctx.getDepositBox().depositAll(item -> !bankItemNames.contains(item.getName()));
            else
                ctx.getBank().depositAll(item -> !bankItemNames.contains(item.getName()));

            ctx.sleep(0, 80);

            Map<Boolean, List<BankItem>> shouldEquip = belongsToSet.get(false).stream().collect(Collectors.partitioningBy(BankItem::shouldEquip));

            // Withdraw and equip all equip-items first
            if (shouldEquip.get(true).size() > 0) {

                shouldEquip.get(true).forEach(this::withdrawItem);

                ctx.sleepHQuick();

                if (ctx.getBank().close()) {
                    shouldEquip.get(true).stream().filter(f -> !ctx.getEquipment().contains(f.getName())).forEach(this::equipItem);
                }

                ctx.sleepHQuick();
            }

            // Withdraw all withdraw-items
            if (shouldEquip.get(false).size() > 0) {
                if (!ctx.getBank().isOpen()) {
                    if (!ctx.getBank().open()) {
                        abort("Error opening bank to withdraw items");
                    }
                }
                shouldEquip.get(false).stream().filter(BankItem::isDeposit).forEach(this::depositItem);
                shouldEquip.get(false).stream().filter(BankItem::isWithdraw).forEach(this::withdrawItem);
            }
        }

        if ((bankLocation.isDepositBox() && !ctx.getDepositBox().close()) || !ctx.getBank().close()) {
            logError("Error closing bank;");
        } else {
            complete("Successfully handled banking at " + bankLocation.getName());
        }

        return NRandom.humanized();
    }

    private void equipItem(BankItem i) {
        if (!ctx.getInventory().getItem(i.getName()).interact())
            abort(String.format("Failed to equip item %s", i.getName()));

        try {
            ctx.sleepHQuick();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private BankItem getOwnedItemFromSet(List<BankItem> set) {
        Optional<BankItem> itemToWithdraw = set.stream()
                .sorted(Comparator.comparingInt(BankItem::getPriority).reversed())
                .filter(item -> ctx.getBank().contains(item.getName()) || ctx.getInventory().contains(item.getName()) || ctx.getEquipment().contains(item.getName()))
                .findFirst();

        if (itemToWithdraw.isPresent()) {
            return itemToWithdraw.get();
        } else {
            abort(String.format("Unable to locate any items belonging to set (%s)", set.get(0).getSet()));
            return null;
        }
    }

    private void depositItem(BankItem item) {
        if (item.getName() != null && ctx.getInventory().contains(item.getName())) {
            if (ctx.getInventory().getAmount(item.getName()) <= item.getAmount()) {
                if (bankLocation.isDepositBox()) {
                    if (!ctx.getDepositBox().depositAll(item.getName()))
                        logBankError(item);
                } else if (!ctx.getBank().depositAll(item.getName()))
                    logBankError(item);
            } else {
                if ((bankLocation.isDepositBox() && !ctx.getDepositBox().deposit(item.getName(), item.getAmount())) || !ctx.getBank().deposit(item.getName(), item.getAmount()))
                    logBankError(item);
            }
        }
    }

    private boolean withdrawItem(BankItem item) {
        if (item.getName() == null)
            abort("Attempted to withdraw a null BankItem");

        int amountToWithdraw = item.getAmount() - ((int) ctx.getInventory().getAmount(item.getName()));

        // It appears this item already exists in the inventory
        if (amountToWithdraw <= 0) {
            ctx.logClass(this, String.format("Not withdrawing (%s), inventory contains sufficient amount", item.getName()));
            return true;
        }

        // We already have the required item equipped
        if (ctx.getEquipment().contains(item.getName())) {
            ctx.logClass(this, String.format("Not withdrawing (%s), item already equipped", item.getName()));
            return true;
        }

        // We don't have the item we're looking for
        if (!ctx.getBank().contains(item.getName()))
            abort(String.format("Bank does not contain item (%s)", item.getName()));

        // We're unable to withdraw the item we're looking for
        if (ctx.getInventory().getEmptySlotCount() == 0)
            abort("Had no inventory space to withdraw " + item.getName());

        if (ctx.getInventory().getEmptySlotCount() <=  amountToWithdraw) {
            if (!ctx.getBank().withdrawAll(item.getName())) {
                logBankError(item);
                return false;
            }
        } else {
            if (!ctx.getBank().withdraw(item.getName(), amountToWithdraw)) {
                logBankError(item);
                return false;
            }
        }

        return !isAborted();
    }

    private void logBankError(BankItem item) {
        logError(String.format("Error handling banking action (%s) for item %s.", item.getAction().name(), item.getName()));
    }
}
