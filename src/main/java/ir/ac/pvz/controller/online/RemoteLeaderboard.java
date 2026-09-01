package ir.ac.pvz.controller.online;

import ir.ac.pvz.model.travel.Leaderboard;
import ir.ac.pvz.model.travel.LeaderboardBuilder;
import ir.ac.pvz.model.travel.LeaderboardEntry;

import network.client.GameClient;
import network.protocol.NetworkMessage;

public final class RemoteLeaderboard {
    private RemoteLeaderboard() {
    }

    public static Leaderboard load() {
        if (!OnlineAccounts.isOnlineMode()
            || !GameClient.getInstance().isSignedIn()) {
            return LeaderboardBuilder.build();
        }

        NetworkMessage response = GameClient.getInstance().fetchLeaderboard();

        if (response == null || response.isError()) {
            return LeaderboardBuilder.build();
        }

        return parse(response);
    }

    public static Leaderboard parse(NetworkMessage response) {
        Leaderboard board = new Leaderboard();
        int count = response.getInt("count", 0);

        for (int index = 0; index < count; index++) {
            readRow(board, response, "row" + index + ".");
        }

        return board;
    }

    private static void readRow(Leaderboard board, NetworkMessage response,
                                String prefix) {
        String username = response.get(prefix + "username");

        if (username == null || username.isBlank()) {
            return;
        }

        LeaderboardEntry entry = board.getOrCreateEntry(username);

        entry.setLastStage(response.getInt(prefix + "stage", 0));
        entry.setMinigamesCompleted(response.getInt(prefix + "minigames", 0));
        entry.setDailyQuestsCompleted(response.getInt(prefix + "daily", 0));
        entry.setNonDailyQuestsCompleted(response.getInt(prefix + "other", 0));
        entry.updateHighScore(response.getInt(prefix + "score", 0));
        entry.setRankedScore(response.getBoolean(prefix + "ranked"));
    }
}
