package nox.scripts.noxscape.tasks.grand_exchange;

public class GEItem {
    private String name;
    private GEAction action;
    private int amount;
    private boolean isOptional;

    public GEItem(String name, GEAction action, int amount, boolean isOptional) {
        this.name = name;
        this.action = action;
        this.amount = amount;
        this.isOptional = isOptional;
    }

    public GEItem(String name, GEAction action, int amount) {
        this(name, action, amount, false);
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

    public boolean isOptional() {
        return isOptional;
    }

    @Override
    public String toString() {
        return "GEItem{" +
                "name='" + name + '\'' +
                ", action=" + action +
                ", amount=" + amount +
                ", isOptional=" + isOptional +
                '}';
    }
}
