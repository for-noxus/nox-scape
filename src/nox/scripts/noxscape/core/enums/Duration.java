package nox.scripts.noxscape.core.enums;

import nox.scripts.noxscape.util.NRandom;

public enum Duration {
    SHORT(25, 2, 40, 4),
    MEDIUM(30, 3, 50, 5),
    LONG(40, 5, 80, 5),
    COMPLETION(0, 0, 0, 0);

    private final int lowerMean;
    private final int lowerDev;
    private final int upperMean;
    private final int upperDev;

    Duration(int lowerMean, int lowerDev, int upperMean, int upperDev) {
        this.lowerMean = lowerMean;
        this.lowerDev = lowerDev;
        this.upperMean = upperMean;
        this.upperDev = upperDev;
    }

    public int getMinutes() {
        return NRandom.fuzzedBounds(lowerMean, lowerDev, upperMean, upperDev);
    }
}
