package nox.scripts.noxscape.util;

import java.util.Random;

public final class NRandom {

    private static int BASE_REACTION_TIME = 300;
    private static double DEFAULT_EFFICIENCY = 0.8;

    public static int exact(int lowerBounds, int higherBounds) {
        int generated = new Random().nextInt(higherBounds - lowerBounds);
        return lowerBounds + generated;
    }

    public static int fuzzed(int mean, int deviation) {
        Random r = new Random();
        return (int) Math.round(r.nextGaussian() * deviation + mean);
    }

    public static int fuzzedBounds(int mean, int lowerDeviation, int upperDeviation) {
        Random r = new Random();
        int lowerNum = (int) Math.round(mean - lowerDeviation * r.nextGaussian());
        int upperNum = (int) Math.round(mean + upperDeviation * r.nextGaussian());
        upperNum = lowerNum >= upperNum ? lowerNum +1 : upperNum;

        return exact(lowerNum, upperNum);
    }

    public static int fuzzedBounds(int lowerMean, int lowerDeviation, int upperMean, int upperDeviation) {
        Random r = new Random();
        int lowerNum = (int) Math.round((lowerDeviation * r.nextGaussian()) + lowerMean);
        int upperNum = (int) Math.max(lowerNum + 1, Math.round((upperDeviation * r.nextGaussian()) + upperMean));

        return exact(lowerNum, upperNum);
    }

    public static int humanized() {
        return humanized(DEFAULT_EFFICIENCY);
    }

    public static int humanized(Double efficiency) {
        double boundedCoefficient = Math.max(0.1, efficiency);
        double stdCoefficient = getStandardizedCoefficient(boundedCoefficient);
        double lambda = 10/stdCoefficient;
        int poiss = poisson(lambda);
        return exact((int)Math.floor(0.9*BASE_REACTION_TIME), BASE_REACTION_TIME + 10*poiss);
    }

    public static <T> T fromArray(T[] array) {
        int generated = new Random().nextInt(array.length);
        return array[generated];
    }

    private static int poisson(double lambda) {
        double L = Math.exp(-lambda);
        double p = 1.0;
        int k = 0;

        do {
            k++;
            p *= Math.random();
        } while (p > L);

        return k - 1;
    }

    private static double getStandardizedCoefficient(double coeff) {
        return Math.pow(coeff, 2);
    }
}
