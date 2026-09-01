package ir.ac.pvz.model.stage;

public class DeadLineStage extends SpecialStage {
    private int deadLineColumn;
    private boolean crossed;

    public DeadLineStage(int number, int difficulty, int waveCount, int deadLineColumn) {
        super(number, difficulty, waveCount);
        this.deadLineColumn = deadLineColumn;
    }

    public int getDeadLineColumn() {
        return deadLineColumn;
    }

    @Override
    public void applySpecialRules() {
        System.out.println("A dead line is drawn at column " + deadLineColumn + ". No zombie may cross it.");
    }

    public void checkZombieCrossed(int zombieColumn) {
        if (zombieColumn <= deadLineColumn) {
            crossed = true;
        }
    }

    @Override
    public boolean checkLoseCondition() {
        return crossed;
    }
}
