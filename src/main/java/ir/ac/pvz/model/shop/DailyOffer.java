package ir.ac.pvz.model.shop;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

public class DailyOffer {
    private String offerDate;
    private String plantType;
    private int priceCoins;
    private boolean purchased;

    public DailyOffer() {
        this.offerDate = null;
        this.priceCoins = 1600;
    }

    public String getPlantType() {
        return plantType;
    }

    public int getPriceCoins() {
        return priceCoins;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public void refreshIfNeeded(List<String> unlockedPlants) {
        String today = LocalDate.now().toString();
        if (today.equals(offerDate)) {
            return;
        }
        offerDate = today;
        purchased = false;
        if (!unlockedPlants.isEmpty()) {
            plantType = unlockedPlants.get(new Random().nextInt(unlockedPlants.size()));
        }
    }

    public boolean isAvailableToday() {
        return !purchased && plantType != null;
    }

    public void purchase() {
        purchased = true;
    }
}
