package nox.scripts.noxscape.util.prices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RSBuddyExchangeOracle {

    private static final Map<Integer, RSBuddyExchangePrice> JSON_BY_IDS = new HashMap<>();
    private static final Map<String, RSBuddyExchangePrice> JSON_BY_NAMES = new WeakHashMap<>();

    private RSBuddyExchangeOracle() {
        super();
    }

    public static void retrievePriceGuide() throws IOException {
        retrievePriceGuide(0L);
    }

    public static void retrievePriceGuide(long timestamp) throws IOException {

        String url = "https://rsbuddy.com/exchange/summary.json";
        String json;

        if (timestamp >= 0L) {
            url += ("?ts=" + timestamp);
        }

        JSON_BY_IDS.clear();
        JSON_BY_NAMES.clear();

        json = downloadSummary(url);
        json = json.replaceAll("\\\\u0027", "'");
        json = json.replaceAll("\\\\u0026", "&");

        processIntoJSONMap(json);
    }

    private static String downloadSummary(String urlAddress) throws IOException {

        StringBuilder sb = new StringBuilder();
        URLConnection connection;
        URL url = new URL(urlAddress);
        connection = url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {

            br.lines().forEach(sb::append);
        }

        return sb.toString();
    }

    private static void processIntoJSONMap(String jsonStr) {

        RSBuddyExchangePrice item;

        Pattern p = Pattern.compile("\"\\d+\":(\\{.*?\\})", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(jsonStr);

        while (m.find()) {
            item = new RSBuddyExchangePrice(parse(m.group(1)));
            JSON_BY_IDS.put(item.getID(), item);
            JSON_BY_NAMES.put(item.getName(), item);
        }
    }

    public static RSBuddyExchangePrice getItemByID(int itemId) {
        return JSON_BY_IDS.get(itemId);
    }

    public static RSBuddyExchangePrice getItemByName(String itemName) {
        return JSON_BY_NAMES.get(itemName);
    }

    public static List<RSBuddyExchangePrice> getItemsByNameContaining(String needle) {
        return JSON_BY_NAMES.keySet().stream()
                .filter(str -> str.contains(needle))
                .map(RSBuddyExchangeOracle::getItemByName)
                .collect(Collectors.toList());
    }

    public static RSBuddyExchangePrice getItemByNameContaining(String needle) {
        return JSON_BY_NAMES.keySet().stream()
                .filter(str -> str.contains(needle))
                .findFirst()
                .map(RSBuddyExchangeOracle::getItemByName)
                .orElse(null);
    }

    public static List<RSBuddyExchangePrice> getItemsByNameMatching(String regexNeedle) {
        final String regexNeedleCaseInsensitive = "(?i:" + regexNeedle + ")";
        return JSON_BY_NAMES.keySet().stream()
                .filter(str -> str.matches(regexNeedleCaseInsensitive))
                .map(RSBuddyExchangeOracle::getItemByName)
                .collect(Collectors.toList());
    }

    public static RSBuddyExchangePrice getItemByNameMatching(String regexNeedle) {
        final String regexNeedleCaseInsensitive = "(?i:" + regexNeedle + ")";
        return JSON_BY_NAMES.keySet().stream()
                .filter(str -> str.matches(regexNeedleCaseInsensitive))
                .findFirst()
                .map(RSBuddyExchangeOracle::getItemByName)
                .orElse(null);
    }

    private static Map<String, Object> parse(String jsonStr) {

        Map<String, Object> item = new HashMap<>(10);
        Pattern p = Pattern.compile("\\\"id\\\":(\\d+),\\\"name\\\":\\\"(.*?)\\\",\\\"members\\\":(\\w+),\\\"sp\\\":(\\d+),\\\"buy_average\\\":(\\d+),\\\"buy_quantity\\\":(\\d+),\\\"sell_average\\\":(\\d+),\\\"sell_quantity\\\":(\\d+),\\\"overall_average\\\":(\\d+),\\\"overall_quantity\\\":(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(jsonStr);

        if (m.find()) {
            item.put("id", Integer.parseInt(m.group(1)));
            item.put("name", m.group(2));
            item.put("members", Boolean.parseBoolean(m.group(3)));
            item.put("sp", Long.parseLong(m.group(4)));
            item.put("buy_average", Long.parseLong(m.group(5)));
            item.put("buy_quantity", Long.parseLong(m.group(6)));
            item.put("sell_average", Long.parseLong(m.group(7)));
            item.put("sell_quantity", Long.parseLong(m.group(8)));
            item.put("overall_average", Long.parseLong(m.group(9)));
            item.put("overall_quantity", Long.parseLong(m.group(10)));
        }

        return item;
    }

}
