package ir.ac.pvz.model.travel;

public class StoryQuest extends Quest {
    private String targetChapter;
    private int targetStage;

    public StoryQuest(String id, String title, String description, String page, int targetProgress, Reward reward, String targetChapter, int targetStage) {
        super(id, title, description, page, QuestCategory.ADVENTURE, QuestPriority.CRITICAL, targetProgress, reward);
        this.targetChapter = targetChapter;
        this.targetStage = targetStage;
    }

    public String getTargetChapter() {
        return targetChapter;
    }

    public int getTargetStage() {
        return targetStage;
    }
}
