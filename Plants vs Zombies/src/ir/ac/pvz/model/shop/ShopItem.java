package ir.ac.pvz.model.shop;

public class ShopItem {
    private String id;
    private String name;
    private int priceCoins;
    private int priceGems;
    private int buyUnit;

    public ShopItem(String id, String name, int priceCoins, int priceGems, int buyUnit) {
        this.id = id;
        this.name = name;
        this.priceCoins = priceCoins;
        this.priceGems = priceGems;
        this.buyUnit = buyUnit;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPriceCoins() {
        return priceCoins;
    }

    public int getPriceGems() {
        return priceGems;
    }

    public int getBuyUnit() {
        return buyUnit;
    }
}
