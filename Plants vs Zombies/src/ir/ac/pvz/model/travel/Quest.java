package ir.ac.pvz.model.travel;

import ir.ac.pvz.model.user.Collection;
import ir.ac.pvz.model.user.Inventory;
import ir.ac.pvz.model.user.PlayerWallet;

public abstract class Quest {
    protected String id;
    protected String title;
    protected String description;
    protected String page;
    protected QuestCategory category;
    protected QuestPriority priority;
    protected Reward reward;
    protected int currentProgress;
    protected int targetProgress;
    protected boolean completed;

    public Quest(String id, String title, String description, String page, QuestCategory category, QuestPriority priority, int targetProgress, Reward reward) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.page = page;
        this.category = category;
        this.priority = priority;
        this.targetProgress = targetProgress;
        this.reward = reward;
        this.currentProgress = 0;
        this.completed = false;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPage() {
        return page;
    }

    public QuestCategory getCategory() {
        return category;
    }

    public QuestPriority getPriority() {
        return priority;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public int getTargetProgress() {
        return targetProgress;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void updateProgress(int amount) {
        if (completed) {
            return;
        }
        currentProgress += amount;
        if (currentProgress >= targetProgress) {
            currentProgress = targetProgress;
            completed = true;
        }
    }

    public boolean checkCompletion() {
        return completed;
    }

    public void grantReward(PlayerWallet wallet, Collection collection, Inventory inventory) {
        if (completed && reward != null) {
            reward.apply(wallet, collection, inventory);
        }
    }
}
