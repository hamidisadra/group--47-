package ir.ac.pvz.model.travel;

public class EpicQuest extends Quest {

    public EpicQuest(String id, String title, String description, String page, int targetProgress, Reward reward) {
        super(id, title, description, page, QuestCategory.SPECIAL, QuestPriority.HIGH, targetProgress, reward);
    }
}
