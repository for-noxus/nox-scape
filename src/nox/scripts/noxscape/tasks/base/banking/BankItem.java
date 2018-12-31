package nox.scripts.noxscape.tasks.base.banking;

import nox.scripts.noxscape.core.interfaces.INameable;

public class BankItem implements INameable {

    private String name;
    private BankAction action;
    private int amount;
    private String set;
    private int priority;
    private boolean equip;
    private int shouldBuyAmount;
    private boolean shouldBuy;

    public BankItem(String name, BankAction action, int amount) {
        this(name, action, amount, null, 0);
    }

    public BankItem(String name, BankAction action, int amount, String set, int priority) {
        this(name, action, amount, null, priority, false);
    }

    public BankItem(String name, BankAction action, int amount, String set, int priority, boolean equip) {
        this.name = name;
        this.action = action;
        this.amount = amount;
        this.set = set;
        this.priority = priority;
        this.equip = equip;
    }

    public BankItem buyIfNecessary(int amount) {
        this.shouldBuy = true;
        this.shouldBuyAmount = amount;
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
    public String toString() {
        return "BankItem{" +
                "name='" + name + '\'' +
                ", action=" + action +
                ", amount=" + amount +
                ", set='" + set + '\'' +
                ", priority=" + priority +
                ", equip=" + equip +
                '}';
    }
}
