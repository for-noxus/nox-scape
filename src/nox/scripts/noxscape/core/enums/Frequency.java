package nox.scripts.noxscape.core.enums;

public enum Frequency {
    COMMON(0.5),
    UNCOMMON(0.3),
    RARE(0.1),
    MYTHICAL(0.02),
    MANUAL(0.0);

    private double weight;

    public double getWeight() {
        return weight;
    }

    Frequency(double weight) {
        this.weight = weight;
    }
}
