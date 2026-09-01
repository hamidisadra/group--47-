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

    private boolean rankedScore;

    public boolean hasRankedScore() {
        return rankedScore;
    }

    public void setRankedScore(boolean rankedScore) {
        this.rankedScore = rankedScore;
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

    public void setMinigamesCompleted(int count) {
        this.minigamesCompleted = Math.max(0, count);
    }

    public void setDailyQuestsCompleted(int count) {
        this.dailyQuestsCompleted = Math.max(0, count);
    }

    public void setNonDailyQuestsCompleted(int count) {
        this.nonDailyQuestsCompleted = Math.max(0, count);
    }

    public void setLastStage(int stage) {
        this.lastStage = Math.max(0, stage);
    }
}
