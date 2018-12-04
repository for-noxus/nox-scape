package nox.scripts.noxscape.tasks.base.banking;

public class BankItem {

    private String name;
    protected BankAction action;
    protected int amount;
    protected int id;

    public BankItem(int id, BankAction action, int amount) {
        this.id = id;
        this.action = action;
        this.amount = amount;
    }

    public BankItem(String name, BankAction action, int amount) {
        this.name = name;
        this.action = action;
        this.amount = amount;
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

    public int getId() {
        return id;
    }

    public boolean isDeposit() {
        return this.action == BankAction.DEPOSIT;
    }

    public boolean isWithdraw() {
        return !this.isDeposit();
    }
}
