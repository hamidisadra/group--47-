package ir.ac.pvz.model.stage;

import java.util.List;
import java.util.Random;

public class ConveyorBeltStage extends SpecialStage {
    private int beltIntervalSeconds;
    private Random random;

    public ConveyorBeltStage(int number, int difficulty, int waveCount) {
        super(number, difficulty, waveCount);
        this.beltIntervalSeconds = 12;
        this.random = new Random();
    }

    public int getBeltIntervalSeconds() {
        return beltIntervalSeconds;
    }

    @Override
    public void applySpecialRules() {
        System.out.println("Plants arrive automatically on a conveyor belt every " + beltIntervalSeconds + " seconds.");
    }

    public String deliverNextPlant(List<String> availablePlants) {
        if (availablePlants.isEmpty()) {
            return null;
        }
        return availablePlants.get(random.nextInt(availablePlants.size()));
    }

    @Override
    public boolean checkLoseCondition() {
        return false;
    }
}
