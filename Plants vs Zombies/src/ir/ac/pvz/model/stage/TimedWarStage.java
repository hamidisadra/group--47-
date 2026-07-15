package ir.ac.pvz.model.stage;

public class TimedWarStage extends SpecialStage {
    private int timeLimitSeconds;
    private int killTarget;
    private int sunTarget;
    private int elapsedSeconds;
    private int killedCount;
    private int sunProduced;

    public TimedWarStage(int number, int difficulty, int waveCount, int timeLimitSeconds, int killTarget, int sunTarget) {
        super(number, difficulty, waveCount);
        this.timeLimitSeconds = timeLimitSeconds;
        this.killTarget = killTarget;
        this.sunTarget = sunTarget;
    }

    @Override
    public void applySpecialRules() {
        System.out.println("You have " + timeLimitSeconds + " seconds to reach the target.");
    }

    public void updateTimer(int seconds) {
        elapsedSeconds += seconds;
    }

    public void addKill() {
        killedCount++;
    }

    public void addSun(int amount) {
        sunProduced += amount;
    }

    public boolean checkWinCondition() {
        if (killTarget > 0) {
            return killedCount >= killTarget;
        }
        return sunProduced >= sunTarget;
    }

    @Override
    public boolean checkLoseCondition() {
        return elapsedSeconds >= timeLimitSeconds && !checkWinCondition();
    }
}
