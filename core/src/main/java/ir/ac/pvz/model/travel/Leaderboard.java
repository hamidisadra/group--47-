package ir.ac.pvz.model.travel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Leaderboard {
    private List<LeaderboardEntry> entries;

    public Leaderboard() {
        this.entries = new ArrayList<>();
    }

    public List<LeaderboardEntry> getEntries() {
        return entries;
    }

    public LeaderboardEntry getEntry(String username) {
        for (LeaderboardEntry entry : entries) {
            if (entry.getUsername().equals(username)) {
                return entry;
            }
        }
        return null;
    }

    public LeaderboardEntry getOrCreateEntry(String username) {
        LeaderboardEntry entry = getEntry(username);
        if (entry == null) {
            entry = new LeaderboardEntry(username);
            entries.add(entry);
        }
        return entry;
    }

    public void sortBy(String column, boolean ascending) {
        Comparator<LeaderboardEntry> comparator;

        switch (column) {
            case "minigames":
                comparator = Comparator.comparingInt(LeaderboardEntry::getMinigamesCompleted);
                break;
            case "quests":
                comparator = Comparator.comparingInt(e -> e.getDailyQuestsCompleted() + e.getNonDailyQuestsCompleted());
                break;
            case "score":
                comparator = Comparator.comparingInt(LeaderboardEntry::getHighScore);
                break;
            default:
                comparator = Comparator.comparingInt(LeaderboardEntry::getLastStage);
                break;
        }

        if (!ascending) {
            comparator = comparator.reversed();
        }

        entries.sort(comparator);
    }

    public void display() {
        System.out.println("========== Leaderboard ==========");
        for (LeaderboardEntry entry : entries) {
            System.out.println(entry.getUsername() + " | " + entry.getLastChapter() + " stage " + entry.getLastStage()
                    + " | minigames: " + entry.getMinigamesCompleted()
                    + " | quests: " + entry.getDailyQuestsCompleted() + "/" + entry.getNonDailyQuestsCompleted()
                    + " | score: " + entry.getHighScore());
        }
    }
}
