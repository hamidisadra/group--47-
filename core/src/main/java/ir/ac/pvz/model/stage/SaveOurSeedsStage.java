package ir.ac.pvz.model.stage;

import java.util.ArrayList;
import java.util.List;

public class SaveOurSeedsStage extends SpecialStage {
    private List<String> protectedPositions;
    private boolean failed;

    public SaveOurSeedsStage(int number, int difficulty, int waveCount) {
        super(number, difficulty, waveCount);
        this.protectedPositions = new ArrayList<>();
        this.failed = false;
    }

    public void addProtectedPosition(int x, int y) {
        protectedPositions.add(x + "," + y);
    }

    @Override
    public void applySpecialRules() {
        System.out.println("Protect the marked plants; losing any of them ends the stage.");
    }

    public void onPlantDestroyed(int x, int y) {
        if (protectedPositions.contains(x + "," + y)) {
            failed = true;
        }
    }

    @Override
    public boolean checkLoseCondition() {
        return failed;
    }
}
