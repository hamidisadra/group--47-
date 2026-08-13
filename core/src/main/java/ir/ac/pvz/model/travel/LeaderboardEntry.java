package ir.ac.pvz.model.travel;

public class LeaderboardEntry {
    private String username;
    private String lastChapter;
    private int lastStage;
    private int minigamesCompleted;
    private int dailyQuestsCompleted;
    private int nonDailyQuestsCompleted;
    private int highScore;

    public LeaderboardEntry(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public String getLastChapter() {
        return lastChapter;
    }

    public int getLastStage() {
        return lastStage;
    }

    public int getMinigamesCompleted() {
        return minigamesCompleted;
    }

    public int getDailyQuestsCompleted() {
        return dailyQuestsCompleted;
    }

    public int getNonDailyQuestsCompleted() {
        return nonDailyQuestsCompleted;
    }

    public int getHighScore() {
        return highScore;
    }

    public void setLastProgress(String chapter, int stage) {
        this.lastChapter = chapter;
        this.lastStage = stage;
    }

    public void addMinigameCompleted() {
        minigamesCompleted++;
    }

    public void addDailyQuestCompleted() {
        dailyQuestsCompleted++;
    }

    public void addNonDailyQuestCompleted() {
        nonDailyQuestsCompleted++;
    }

    public void updateHighScore(int score) {
        if (score > highScore) {
            highScore = score;
        }
    }
}
