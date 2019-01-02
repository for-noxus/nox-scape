package nox.scripts.noxscape.util.prices;

import java.util.Map;

public class RSBuddyExchangePrice implements Comparable<RSBuddyExchangePrice> {

    private final Map<String, Object> itemJson;

    public RSBuddyExchangePrice(Map<String, Object> itemJson) {
        super();
        this.itemJson = itemJson;
    }

    @Override
    public int compareTo(RSBuddyExchangePrice o) {
        return Integer.compare(getID(), o.getID());
    }

    @Override
    public String toString() {
        return String.format("{ \"id\":%s, \"name\":\"%s\", \"members\":%s, \"storePrice\":%s, \"buyPrice\":%s, \"buyQuantity\":%s, \"sellPrice\":%s, \"sellQuantity\":%s, \"overallPrice\":%s, \"overallQuantity\":%s }",
                getID(),
                getName(),
                isMembers(),
                getStorePrice(),
                getBuyPrice(),
                getBuyQuantity(),
                getSellPrice(),
                getSellQuantity(),
                getOverallPrice(),
                getOverallQuantity());
    }

    public int getID() {
        return (Integer) itemJson.getOrDefault("id", -1);
    }

    public String getName() {
        return (String) itemJson.getOrDefault("name", "undefined");
    }

    public boolean isMembers() {
        return (Boolean) itemJson.getOrDefault("members", Boolean.FALSE);
    }

    public long getStorePrice() {
        return (Long) itemJson.getOrDefault("sp", 0L);
    }

    public long getBuyPrice() {
        return (Long) itemJson.getOrDefault("buy_average", 0L);
    }

    public long getBuyQuantity() {
        return (Long) itemJson.getOrDefault("buy_quantity", 0L);
    }

    public long getSellPrice() {
        return (Long) itemJson.getOrDefault("sell_average", 0L);
    }

    public long getSellQuantity() {
        return (Long) itemJson.getOrDefault("sell_quantity", 0L);
    }

    public long getOverallPrice() {
        return (Long) itemJson.getOrDefault("overall_average", 0L);
    }

    public long getOverallQuantity() {
        return (Long) itemJson.getOrDefault("overall_quantity", 0L);
    }
}

