package ir.ac.pvz.model.stage;

public class LoveYourPlantsStage extends SpecialStage {
    private int maxPlantLosses;
    private int lostCount;

    public LoveYourPlantsStage(int number, int difficulty, int waveCount, int maxPlantLosses) {
        super(number, difficulty, waveCount);
        this.maxPlantLosses = maxPlantLosses;
    }

    @Override
    public void applySpecialRules() {
        System.out.println("Do not lose more than " + maxPlantLosses + " plants.");
    }

    public void onPlantDestroyed() {
        lostCount++;
    }

    public int getLostCount() {
        return lostCount;
    }

    @Override
    public boolean checkLoseCondition() {
        return lostCount >= maxPlantLosses;
    }
}
