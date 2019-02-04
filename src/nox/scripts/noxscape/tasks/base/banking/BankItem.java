package nox.scripts.noxscape.tasks.base.banking;

import nox.scripts.noxscape.core.interfaces.INameable;

import java.util.Objects;

public class BankItem implements INameable {

    private BankAction action;
    private String name;
    private String set;
    private int amount;
    private int shouldBuyAmount;
    private int priority;
    private boolean equip;
    private boolean shouldBuy;
    private PurchaseLocation purchaseLocation;

    public BankItem(String name, BankAction action, int amount) {
        this(name, action, amount, null, 0);
    }

    public BankItem(String name, BankAction action, int amount, String set, int priority) {
        this(name, action, amount, set, priority, false);
    }

    public BankItem(String name, BankAction action, int amount, String set, int priority, boolean equip) {
        this.name = name;
        this.action = action;
        this.amount = amount;
        this.set = set;
        this.priority = priority;
        this.equip = equip;
    }

    public BankItem buyIfNecessary(int amount, PurchaseLocation purchaseLocation) {
        return buyIfNecessary(true, amount, purchaseLocation);
    }

    public BankItem buyIfNecessary(boolean shouldBuy, int amount, PurchaseLocation purchaseLocation) {
        this.shouldBuy = shouldBuy;
        this.shouldBuyAmount = amount;
        this.purchaseLocation = purchaseLocation;
        return this;
    }

    public boolean shouldBuy() {
        return shouldBuy;
    }

    public int shouldBuyAmount() {
        return shouldBuyAmount;
    }

    public String getName() {
        return name;
    }

    public BankAction getAction() {
        return action;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isDeposit() {
        return this.action == BankAction.DEPOSIT;
    }

    public boolean isWithdraw() {
        return !this.isDeposit();
    }

    public String getSet() {
        return set;
    }

    public int getPriority() {
        return priority;
    }

    public boolean shouldEquip() {
        return equip;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BankItem bankItem = (BankItem) o;
        return Objects.equals(name, bankItem.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public PurchaseLocation getPurchaseLocation() {
        return purchaseLocation;
    }

    @Override
    public String toString() {
        return "BankItem{" +
                "name='" + name + '\'' +
                ", action=" + action +
                ", amount=" + amount +
                ", set='" + set + '\'' +
                ", priority=" + priority +
                ", equip=" + equip +
                ", shouldBuyAmount=" + shouldBuyAmount +
                ", shouldBuy=" + shouldBuy +
                '}';
    }
}
