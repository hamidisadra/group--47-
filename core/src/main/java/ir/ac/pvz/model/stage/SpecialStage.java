package ir.ac.pvz.model.stage;

public abstract class SpecialStage extends Stage {

    public SpecialStage(int number, int difficulty, int waveCount) {
        super(number, difficulty, waveCount);
    }

    @Override
    public void startStage() {
        System.out.println("Special stage " + number + " started.");
        applySpecialRules();
    }

    public abstract void applySpecialRules();

    public abstract boolean checkLoseCondition();
}
