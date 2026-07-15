package ir.ac.pvz.model.stage;

public class PlantWhatYouGetStage extends SpecialStage {
    private int initialSun;

    public PlantWhatYouGetStage(int number, int difficulty, int waveCount, int initialSun) {
        super(number, difficulty, waveCount);
        this.initialSun = initialSun;
    }

    public int getInitialSun() {
        return initialSun;
    }

    @Override
    public void applySpecialRules() {
        System.out.println("You start with " + initialSun + " sun. No more sun will fall and sun producing plants are disabled.");
    }

    @Override
    public boolean checkLoseCondition() {
        return false;
    }
}
