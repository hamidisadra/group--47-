package ir.ac.pvz.model.stage;

public class BossStage extends Stage {

    public BossStage(int number, int difficulty, int waveCount) {
        super(number, difficulty, waveCount);
    }

    @Override
    public void startStage() {
        System.out.println("Stage " + number + " started.");
    }

    public void startBossFight() {
        System.out.println("The boss fight begins!");
    }
}
