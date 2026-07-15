package ir.ac.pvz.model.stage;

import java.util.ArrayList;
import java.util.List;

public class LockedPlantsStage extends SpecialStage {
    private List<String> lockedPlants;

    public LockedPlantsStage(int number, int difficulty, int waveCount) {
        super(number, difficulty, waveCount);
        this.lockedPlants = new ArrayList<>();
    }

    public List<String> getLockedPlants() {
        return lockedPlants;
    }

    public void lockPlant(String plantType) {
        if (!lockedPlants.contains(plantType)) {
            lockedPlants.add(plantType);
        }
    }

    public boolean isLocked(String plantType) {
        return lockedPlants.contains(plantType);
    }

    @Override
    public void applySpecialRules() {
        System.out.println("Some plant slots are locked in this stage.");
    }

    @Override
    public boolean checkLoseCondition() {
        return false;
    }
}
