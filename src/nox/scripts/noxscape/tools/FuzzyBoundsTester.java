package nox.scripts.noxscape.tools;

import nox.scripts.noxscape.util.NRandom;

import java.util.stream.IntStream;

public class FuzzyBoundsTester {
    public static final int ITERATIONS = 100;
    public static final int LOWER_MEAN = 30;
    public static final int LOWER_DEV = 3;
    public static final int UPPER_MEAN = 80;
    public static final int UPPER_DEV = 5;

    public static void main(String[] args) {
        System.out.println(String.format("Starting %s iterations of FuzzedBounds with lower:upper (mean/dev) of (%s/%s):(%s/%s)", ITERATIONS, LOWER_MEAN, LOWER_DEV, UPPER_MEAN, UPPER_DEV));
        IntStream.range(0, ITERATIONS).forEach(f -> System.out.println(NRandom.fuzzedBounds(LOWER_MEAN, LOWER_DEV, UPPER_MEAN, UPPER_DEV)));
    }
}
