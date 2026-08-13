package ir.ac.pvz.model.user;

import ir.ac.pvz.model.travel.Quest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestLog {
    private Map<String, List<Quest>> quests;
    private int dailyCompleted;
    private int nonDailyCompleted;

    public QuestLog() {
        this.quests = new HashMap<>();
    }

    public void addQuest(Quest quest) {
        quests.computeIfAbsent(quest.getPage(), k -> new ArrayList<>()).add(quest);
    }

    public List<Quest> getPage(String page) {
        return quests.getOrDefault(page, new ArrayList<>());
    }

    public Quest findQuest(String id) {
        for (List<Quest> pageQuests : quests.values()) {
            for (Quest quest : pageQuests) {
                if (quest.getId().equals(id)) {
                    return quest;
                }
            }
        }
        return null;
    }

    public boolean completeQuest(String id, PlayerWallet wallet, Collection collection, Inventory inventory) {
        Quest quest = findQuest(id);
        if (quest == null || !quest.checkCompletion()) {
            return false;
        }
        quest.grantReward(wallet, collection, inventory);
        if (quest instanceof ir.ac.pvz.model.travel.DailyQuest) {
            dailyCompleted++;
        } else {
            nonDailyCompleted++;
        }
        return true;
    }

    public int getDailyCompleted() {
        return dailyCompleted;
    }

    public int getNonDailyCompleted() {
        return nonDailyCompleted;
    }
}
