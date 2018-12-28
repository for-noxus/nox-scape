package nox.scripts.noxscape.tasks.grand_exchange;

public class GEItem {
    private String name;
    private GEAction action;
    private int amount;

    public GEItem(String name, GEAction action, int amount) {
        this.name = name;
        this.action = action;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public GEAction getAction() {
        return action;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "GEItem{" +
                "name='" + name + '\'' +
                ", action=" + action +
                ", amount=" + amount +
                '}';
    }
}
