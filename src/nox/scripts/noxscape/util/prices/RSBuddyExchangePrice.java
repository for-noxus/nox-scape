package nox.scripts.noxscape.util.prices;

public class RSBuddyExchangePrice implements Comparable<RSBuddyExchangePrice> {

    private int id;
    private String name;
    private boolean isMembers;
    private int storePrice;
    private int buyPrice;
    private int buyQuantity;
    private int sellPrice;
    private int sellQuantity;
    private int overallPrice;

    public RSBuddyExchangePrice() { }

    public RSBuddyExchangePrice(int id, String name, boolean isMembers, int storePrice, int buyPrice, int buyQuantity, int sellPrice, int sellQuantity, int overallPrice, int overallQuantity) {
        this.id = id;
        this.name = name;
        this.isMembers = isMembers;
        this.storePrice = storePrice;
        this.buyPrice = buyPrice;
        this.buyQuantity = buyQuantity;
        this.sellPrice = sellPrice;
        this.sellQuantity = sellQuantity;
        this.overallPrice = overallPrice;
        this.overallQuantity = overallQuantity;
    }

    private int overallQuantity;

    @Override
    public int compareTo(RSBuddyExchangePrice o) {
        return Integer.compare(id, o.id);
    }

    @Override
    public String toString() {
        return String.format("{ \"id\":%s, \"name\":\"%s\", \"members\":%s, \"storePrice\":%s, \"buyPrice\":%s, \"buyQuantity\":%s, \"sellPrice\":%s, \"sellQuantity\":%s, \"overallPrice\":%s, \"overallQuantity\":%s }",
                getId(),
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

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isMembers() {
        return isMembers;
    }

    public int getStorePrice() {
        return storePrice;
    }

    public int getBuyPrice() {
        return buyPrice;
    }

    public int getBuyQuantity() {
        return buyQuantity;
    }

    public int getSellPrice() {
        return sellPrice;
    }

    public int getSellQuantity() {
        return sellQuantity;
    }

    public int getOverallPrice() {
        return overallPrice;
    }

    public int getOverallQuantity() {
        return overallQuantity;
    }
}

