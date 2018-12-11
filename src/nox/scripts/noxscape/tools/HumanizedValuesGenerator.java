package nox.scripts.noxscape.tools;

import nox.scripts.noxscape.util.NRandom;

import java.util.Arrays;
import java.util.stream.Stream;

public class HumanizedValuesGenerator {

    private static int ITERATIONS = 100;

    public static void main(String[] cheese) {
        double coefficienct = 0.0;
        if (cheese.length == 0) {
            log("Please pass in an efficiency coefficient decimal.");
            return;
        }
        try {
            coefficienct = Double.parseDouble(cheese[0]);
        } catch (NumberFormatException e) {
            log("Error parsing efficiency decimal");
            return;
        }

        log("Running %s iterations of humanized wait times with Efficiency Coefficient (%s)", ITERATIONS, coefficienct);
        log("------------------------------------------------------------------------------------------------");

        int[] generatedValues = new int[ITERATIONS];

        for (int i = 0; i < ITERATIONS; i++) {
            int generated = NRandom.humanized(coefficienct);
            generatedValues[i] = generated;
            log("%s", generated);
        }

        log("------------------------------------------------------------------------------------------------");
        log("Mean: %s", mean(generatedValues));
        log("Min: %s", Arrays.stream(generatedValues).min().getAsInt());
        log("Max: %s", Arrays.stream(generatedValues).max().getAsInt());
        log("StdDev: %s", calculateSD(generatedValues));
    }

    private static void log(String s, Object... args) {
        System.out.println(String.format(s,args));
    }

    private static double mean(int[] values) {
        return ((double)Arrays.stream(values).sum()) / (double)values.length;
    }

    public static double calculateSD(int numArray[])
    {
        double sum = 0.0, standardDeviation = 0.0;
        int length = numArray.length;

        for (int num : numArray) {
            sum += num;
        }

        double mean = sum/length;

        for (double num: numArray) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation/length);
    }
}
