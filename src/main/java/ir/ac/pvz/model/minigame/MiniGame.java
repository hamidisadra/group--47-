package ir.ac.pvz.model.minigame;

public abstract class MiniGame {
    protected String name;
    protected int stageNumber;
    protected boolean won;
    protected boolean finished;

    public MiniGame(String name, int stageNumber) {
        this.name = name;
        this.stageNumber = stageNumber;
    }

    public String getName() {
        return name;
    }

    public int getStageNumber() {
        return stageNumber;
    }

    public boolean isFinished() {
        return finished;
    }

    public void startGame() {
        System.out.println("Starting " + name + " stage " + stageNumber + ".");
    }

    public void finishGame(boolean won) {
        this.finished = true;
        this.won = won;
        System.out.println(won ? "You won " + name + "!" : "You lost " + name + ".");
    }

    public boolean isWon() {
        return won;
    }
}
