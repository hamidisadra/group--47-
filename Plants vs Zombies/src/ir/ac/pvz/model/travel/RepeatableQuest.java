package ir.ac.pvz.model.travel;

public class RepeatableQuest extends Quest {
    private int completionCount;

    public RepeatableQuest(String id, String title, String description, String page, int targetProgress, Reward reward) {
        super(id, title, description, page, QuestCategory.MINIGAME, QuestPriority.MEDIUM, targetProgress, reward);
    }

    public int getCompletionCount() {
        return completionCount;
    }

    public void resetForRepeat() {
        completionCount++;
        currentProgress = 0;
        completed = false;
    }
}
