package ir.ac.pvz.model.stage;

public class NightOpsStage extends SpecialStage {
    private boolean skySunBlocked;

    public NightOpsStage(int number, int difficulty, int waveCount) {
        super(number, difficulty, waveCount);
        this.skySunBlocked = true;
    }

    public boolean isSkySunBlocked() {
        return skySunBlocked;
    }

    @Override
    public void applySpecialRules() {
        System.out.println("It is night; no sun falls from the sky.");
    }

    @Override
    public boolean checkLoseCondition() {
        return false;
    }
}
