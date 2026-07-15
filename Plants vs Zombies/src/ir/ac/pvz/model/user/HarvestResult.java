package ir.ac.pvz.model.user;

public class HarvestResult {
    private int coins;
    private String boostedPlant;

    public HarvestResult(int coins, String boostedPlant) {
        this.coins = coins;
        this.boostedPlant = boostedPlant;
    }

    public int getCoins() {
        return coins;
    }

    public String getBoostedPlant() {
        return boostedPlant;
    }
}
