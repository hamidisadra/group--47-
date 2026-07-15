package ir.ac.pvz.model.travel;

import ir.ac.pvz.model.user.QuestLog;

import java.util.List;

public class TravelLog {
    private String currentPage;

    public TravelLog() {
        this.currentPage = "adventure";
    }

    public String getCurrentPage() {
        return currentPage;
    }

    public void changePage(String pageName) {
        this.currentPage = pageName;
    }

    public void showPage(QuestLog questLog) {
        System.out.println("========== Travel Log: " + currentPage + " ==========");
        List<Quest> quests = questLog.getPage(currentPage);
        if (quests.isEmpty()) {
            System.out.println("Nothing to show on this page.");
            return;
        }
        for (Quest quest : quests) {
            String status = quest.isCompleted() ? "completed" : quest.getCurrentProgress() + "/" + quest.getTargetProgress();
            System.out.println("[" + quest.getPriority() + "] " + quest.getTitle() + " - " + status);
        }
    }
}
