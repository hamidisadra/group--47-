package ir.ac.pvz.model.stage;

public class NormalStage extends Stage {

    public NormalStage(int number, int difficulty, int waveCount) {
        super(number, difficulty, waveCount);
    }

    @Override
    public void startStage() {
        System.out.println("Stage " + number + " started.");
    }
}
