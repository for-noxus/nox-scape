package nox.scripts.noxscape.tasks.base;

import nox.scripts.noxscape.core.*;
import nox.scripts.noxscape.tasks.base.NpcStore.NpcStoreLocation;
import nox.scripts.noxscape.tasks.base.banking.BankItem;
import nox.scripts.noxscape.tasks.base.banking.BankLocation;
import nox.scripts.noxscape.tasks.base.banking.PurchaseLocation;
import nox.scripts.noxscape.tasks.grand_exchange.GEAction;
import nox.scripts.noxscape.tasks.grand_exchange.GEItem;
import nox.scripts.noxscape.tasks.grand_exchange.GrandExchangeMasterNode;
import nox.scripts.noxscape.tasks.money_making.MoneyMakingMasterNode;
import nox.scripts.noxscape.tasks.npc_store.NpcStoreMasterNode;
import nox.scripts.noxscape.util.NRandom;
import nox.scripts.noxscape.util.Pair;
import nox.scripts.noxscape.util.Sleep;
import nox.scripts.noxscape.util.prices.RSBuddyExchangeOracle;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.model.Item;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BankingNode extends NoxScapeNode {

    private BankLocation bankLocation;
    private List<BankItem> items = new ArrayList<>();
    private boolean depositAllBackpackItems = false;
    private boolean depositAllWornItems = false;
    private boolean noted = false;
    private List<String> depositAllExceptItems;

    public BankingNode(ScriptContext ctx) {
        super(ctx);
    }

    public BankingNode bankingAt(BankLocation bankLocation) {
        this.bankLocation = bankLocation;
        return this;
    }

    public BankingNode handlingItems(BankItem... items) {
        this.items.addAll(Arrays.asList(items));
        return this;
    }

    public BankingNode handlingItems(List<BankItem> items) {
        this.items.addAll(items);
        return this;
    }

    public BankingNode asNoted() {
        this.noted = true;
        return this;
    }

    public BankingNode depositAllWornItems() {
        this.depositAllWornItems = true;
        return this;
    }

    public BankingNode depositAllBackpackItems() {
        this.depositAllBackpackItems = true;
        return this;
    }

    public BankingNode depositAllExcept(List<String> itemNames) {
        this.depositAllExceptItems = itemNames;
        return this;
    }

    protected boolean baseExecutionCondition() {
        if ((items == null || items.size() == 0) && (!depositAllBackpackItems && !depositAllWornItems)) {
            abort("Banking node added but no items were added for deposit/withdrawal");
            return false;
        }

        if (bankLocation == null) {
            abort("There was no destination set for this banking node!");
            return false;
        }

        return bankLocation.getBankArea().contains(ctx.myPosition()) || (ctx.getMap().getDestination() != null && bankLocation.getBankArea().contains(ctx.getMap().getDestination()) && ctx.getMap().canReach(ctx.getMap().getDestination()));
    }

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

        if (depositAllWornItems && !ctx.getEquipment().isEmpty()) {
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

        if (depositAllExceptItems != null && depositAllExceptItems.size() > 0) {
            if (bankLocation.isDepositBox())
                ctx.getDepositBox().depositAllExcept(this.depositAllExceptItems.toArray(new String[0]));
            else
                ctx.getBank().depositAllExcept(this.depositAllExceptItems.toArray(new String[0]));
        }

        if (items != null) {
            if (!noted && !bankLocation.isDepositBox()) {
                if (!ctx.getBank().enableMode(Bank.BankMode.WITHDRAW_ITEM)) {
                    abort("Unable to switch to normal item withdrawal");
                    return 5;
                }
            }

            // Split our items into two sets based on whether or not you can withdraw them
            Map<Boolean, List<BankItem>> belongsToSet = items.stream().collect(Collectors.partitioningBy(item -> item.getSet() != null));

            // Handle item sets
            BankItem[] setItemsToWithdraw = belongsToSet.get(true).stream().collect(Collectors.groupingBy(BankItem::getSet)).values().stream().map(this::filterItemsFromSet).filter(Objects::nonNull).toArray(BankItem[]::new);
            belongsToSet.get(false).addAll(Arrays.asList(setItemsToWithdraw));

            // Deposit all deposit-items
            belongsToSet.get(false).stream().filter(BankItem::isDeposit).forEach(this::depositItem);

            // Deposit all items in inventory that aren't specified to be withdrawn
            Set<String> bankItemNames = items.stream().map(BankItem::getName).collect(Collectors.toSet());
            if (bankLocation.isDepositBox())
                ctx.getDepositBox().depositAll(item -> !bankItemNames.contains(item.getName()));
            else
                ctx.getBank().depositAll(item -> !bankItemNames.contains(item.getName()));

            ScriptContext.sleep(NRandom.fuzzedBounds(50, 5, 150, 15));

            // Only check this section if we've not equipped our equip items and withdrawn our withdrawn items
            List<BankItem> itemsToWithdraw = belongsToSet.get(false).stream()
                    .filter(BankItem::isWithdraw)
                    .filter(a -> (a.shouldEquip() && !ctx.getEquipment().contains(a.getName())) || (!a.shouldEquip() && (!ctx.getInventory().contains(a.getName()) || ctx.getInventory().getAmount(a.getName()) < a.getAmount())))
                    .collect(Collectors.toList());

            List<BankItem> itemsToBuy = itemsToWithdraw.stream()
                    .filter(BankItem::shouldBuy)
                    .filter(f -> ctx.getBank().getAmount(f.getName()) < f.getAmount())
                    .collect(Collectors.toList());
            if (itemsToBuy.size() > 0)
                return handleItemsToBuy(itemsToBuy);

            if (itemsToWithdraw.size() > 0) {

                Map<Boolean, List<BankItem>> shouldEquip = itemsToWithdraw.stream().collect(Collectors.partitioningBy(BankItem::shouldEquip));

                // Withdraw and equip all equip-items first
                if (shouldEquip.get(true).size() > 0) {

                    shouldEquip.get(true).forEach(this::withdrawItem);

                    ctx.sleepHQuick();

                    if (ctx.getBank().close()) {
                        shouldEquip.get(true).stream().filter(f -> !ctx.getEquipment().contains(f.getName())).forEach(this::equipItem);
                    }
                }

                ctx.sleepHQuick();

                // Withdraw all withdraw-items
                if (shouldEquip.get(false).size() > 0) {
                    if (!ctx.getBank().isOpen()) {
                        if (!ctx.getBank().open()) {
                            abort("Error opening bank to withdraw items");
                        }
                    }
                    shouldEquip.get(false).stream().filter(BankItem::isDeposit).forEach(this::depositItem);

                    if (this.noted) {
                        if (!ctx.getBank().enableMode(Bank.BankMode.WITHDRAW_NOTE)) {
                            abort("Failed to set BankMode as noted");
                        }
                    }
                    shouldEquip.get(false).stream().filter(BankItem::isWithdraw).forEach(this::withdrawItem);
                }
            }
        }

        if ((bankLocation.isDepositBox() && !ctx.getDepositBox().close()) || !ctx.getBank().close()) {
            logError("Error closing bank;");
        } else {
            complete("Successfully handled banking at " + bankLocation.getName());
        }

        return NRandom.humanized();
    }

    private int handleItemsToBuy(List<BankItem> itemsToBuy) {
        ctx.log("Need to buy " + itemsToBuy.size() + " item(s), calculating prices..");
        try {
            RSBuddyExchangeOracle.retrievePriceGuide();
        } catch (IOException e) {
            abort("Failed to retrieve prices.");
            ctx.log(Arrays.toString(e.getStackTrace()));
        }

        long totalPrice = itemsToBuy.stream()
                .map(m -> m.getPurchaseLocation() == PurchaseLocation.GRAND_EXCHANGE ? RSBuddyExchangeOracle.getItemByName(m.getName()).getBuyPrice() * m.getAmount() :
                          m.getPurchaseLocation() == PurchaseLocation.NPC_STORE ? RSBuddyExchangeOracle.getItemByName(m.getName()).getStorePrice() * m.getAmount() : 0)
                .reduce(0, Integer::sum);
        long totalCoins = ctx.getInventory().getAmount("Coins") + ctx.getBank().getAmount("Coins");
        ctx.log(String.format("Total cost of items is %s, and we have %s", totalPrice, totalCoins));


        // If we need to buy from the GE, mark it as dependent on the MoneyMaking node. Otherwise, it is independent
        int moneyToMake = Math.max((int)((totalPrice - totalCoins) * 1.25), NRandom.fuzzedBounds(7_500, 200, 12_000, 1000)); // Let's always at least make a minimal amount
        boolean needsToMakeMoney = totalCoins <= (moneyToMake); // Play it safe with a 10% buffer
        DecisionMaker.addPriorityTask(ctx.getCurrentMasterNode().getClass(), ctx.getCurrentMasterNode().getConfiguration(), ctx.getCurrentMasterNode().getStopWatcher(), true);

        List<BankItem> itemsToBuyFromGe = itemsToBuy.stream().filter(f -> f.getPurchaseLocation() == PurchaseLocation.GRAND_EXCHANGE).collect(Collectors.toList());
        List<BankItem> itemsToBuyFromNpc = itemsToBuy.stream().filter(f -> f.getPurchaseLocation() == PurchaseLocation.NPC_STORE).collect(Collectors.toList());
        if (itemsToBuyFromGe.size() > 0) {
            GrandExchangeMasterNode.Configuration cfg = new GrandExchangeMasterNode.Configuration();
            cfg.setItemsToHandle(itemsToBuyFromGe.stream().map(m -> new GEItem(m.getName(), GEAction.BUY, m.shouldBuyAmount())).collect(Collectors.toList()));
            DecisionMaker.addPriorityTask(GrandExchangeMasterNode.class, cfg, null, needsToMakeMoney || itemsToBuyFromNpc.size() > 0);
        }
        if (itemsToBuyFromNpc.size() > 0) {
            if (itemsToBuyFromNpc.stream().anyMatch(a -> NpcStoreLocation.forItem(a.getName()) == null)) {
                abort(String.format("Unable to locate a store to purchase item from list %s", itemsToBuy.stream().map(BankItem::getName).collect(Collectors.joining(", "))));
                return 500;
            }
            Map<NpcStoreLocation, List<BankItem>> itemsByLocation =  itemsToBuyFromNpc.stream().collect(Collectors.groupingBy(g -> NpcStoreLocation.forItem(g.getName())));
            itemsByLocation.forEach((k, v) -> {
                NpcStoreMasterNode.Configuration cfg = new NpcStoreMasterNode.Configuration(k);
                cfg.setItemsToBuy(v.stream().map(m -> new Pair<>(m.getName(), m.getAmount())).collect(Collectors.toList()));
                DecisionMaker.addPriorityTask(GrandExchangeMasterNode.class, cfg, null, needsToMakeMoney);
            });
        }
        if (needsToMakeMoney) {
            DecisionMaker.addPriorityTask(MoneyMakingMasterNode.class, null, StopWatcher.create(ctx).stopAfter(moneyToMake).gpMade(), false);
        }
        abort(String.format("Needed to buy %s items from GE (%s) and %s items from NPCs (%s)",
                itemsToBuyFromGe.size(),
                itemsToBuyFromGe.stream().map(m -> String.format("%sx %s", m.getAmount(), m.getName())).collect(Collectors.joining(", ")),
                itemsToBuyFromNpc.size(),
                itemsToBuyFromNpc.stream().map(m -> String.format("%sx %s", m.getAmount(), m.getName())).collect(Collectors.joining(", "))));
        return 50;
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

    private BankItem filterItemsFromSet(List<BankItem> set) {
        Optional<BankItem> itemToWithdraw = set.stream()
                .sorted(Comparator.comparingInt(BankItem::getPriority).reversed())
                .filter(item -> ctx.getBank().contains(item.getName()) || ctx.getInventory().contains(item.getName()) || ctx.getEquipment().contains(item.getName()))
                .findFirst();

        Optional<BankItem> bestItemToBuy = set.stream().filter(BankItem::shouldBuy).sorted(Comparator.comparingInt(BankItem::getPriority).reversed()).findFirst();
        // If we have the item and we're not buying a better one, OR the best one IS our item
        if (itemToWithdraw.isPresent() && !bestItemToBuy.isPresent() || itemToWithdraw.isPresent() && itemToWithdraw.get().equals(bestItemToBuy.orElse(null))) {
            return itemToWithdraw.get();
        } else if (bestItemToBuy.isPresent() && !bestItemToBuy.get().equals(itemToWithdraw.orElse(null))) {
            // If we don't own our best item..
            bestItemToBuy.ifPresent(bankItem -> ctx.logClass(this, "Found best item to buy: " + bankItem.getName()));
            return bestItemToBuy.get();
        } else {
            abort(String.format("Unable to locate any items belonging to set (%s)", set.get(0).getSet()));
            return null;
        }
    }

    private void depositItem(BankItem item) {
        if (item.getName() != null && ctx.getInventory().contains(item.getName())) {
            Item inventoryItem = ctx.getInventory().getItem(item.getName());
            if (ctx.getInventory().getAmount(inventoryItem.getId()) <= item.getAmount()) {
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
        if (!ctx.getBank().contains(item.getName())) {
            abort(String.format("Bank does not contain item (%s)", item.getName()));
        }

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
