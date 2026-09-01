package network.service;

import ir.ac.pvz.model.travel.LeaderboardEntry;
import ir.ac.pvz.model.travel.LeaderboardBuilder;

import network.protocol.MessageType;
import network.protocol.NetworkMessage;

import java.util.List;

public final class LeaderboardService {
    public NetworkMessage buildResponse() {
        List<LeaderboardEntry> entries = LeaderboardBuilder.build().getEntries();
        NetworkMessage response =
            new NetworkMessage(MessageType.LEADERBOARD_RESPONSE);

        response.put("count", entries.size());

        for (int index = 0; index < entries.size(); index++) {
            LeaderboardEntry entry = entries.get(index);
            String prefix = "row" + index + ".";

            response.put(prefix + "username", entry.getUsername());
            response.put(prefix + "stage", entry.getLastStage());
            response.put(prefix + "minigames", entry.getMinigamesCompleted());
            response.put(prefix + "daily", entry.getDailyQuestsCompleted());
            response.put(prefix + "other", entry.getNonDailyQuestsCompleted());
            response.put(prefix + "score", entry.getHighScore());
            response.put(prefix + "ranked", entry.hasRankedScore());
        }

        return response;
    }
}
