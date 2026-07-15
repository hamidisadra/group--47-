package ir.ac.pvz.model.travel;

import java.time.LocalDate;

public class DailyQuest extends Quest {
    private String date;

    public DailyQuest(String id, String title, String description, String page, int targetProgress, Reward reward) {
        super(id, title, description, page, QuestCategory.CHALLENGE, QuestPriority.LOW, targetProgress, reward);
        this.date = LocalDate.now().toString();
    }

    public boolean isExpired() {
        return !date.equals(LocalDate.now().toString());
    }
}
