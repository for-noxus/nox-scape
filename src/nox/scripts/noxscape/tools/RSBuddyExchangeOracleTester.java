package nox.scripts.noxscape.tools;

import nox.scripts.noxscape.util.prices.RSBuddyExchangeOracle;
import nox.scripts.noxscape.util.prices.RSBuddyExchangePrice;
import org.osbot.rs07.api.def.ItemDefinition;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RSBuddyExchangeOracleTester {
    private static final String[] NAMES_TO_TEST = new String[] { "Clay", "Adamant pickaxe", "Rune axe", "Mithril platebody" };
    private static final int[] IDS_TO_TEST = new int[] { 434,  532 }; // clay, big bones
    public static void main(String[] args) throws IOException {
        System.out.println("Testing RSBuddy Exchange Oracle");
        long start = System.currentTimeMillis();
        RSBuddyExchangeOracle.retrievePriceGuide();
        System.out.println("Took " + (System.currentTimeMillis() - start) + "ms to load prices");
        for(int i : IDS_TO_TEST) {
            System.out.println("Testing " + i);
            RSBuddyExchangePrice price = RSBuddyExchangeOracle.getItemByID(i);
            System.out.println(price);
        }
        for(String s: NAMES_TO_TEST) {
            System.out.println("Testing " + s);
            RSBuddyExchangePrice price = RSBuddyExchangeOracle.getItemByName(s);
            System.out.println(price);
        }

        System.out.println("Total time taken: " + (System.currentTimeMillis() - start) + "ms");
    }
}
