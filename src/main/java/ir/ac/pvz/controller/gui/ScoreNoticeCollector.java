package ir.ac.pvz.controller.gui;

import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.travel.ScoreEvent;
import ir.ac.pvz.model.travel.ScoreEventType;
import ir.ac.pvz.model.travel.ScoreTracker;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

final class ScoreNoticeCollector {
    private final List<String> notices = new ArrayList<>();
    private final Map<ScoreEventType, Integer> notifiedCounts =
        new EnumMap<>(ScoreEventType.class);

    private int lastKillCount;

    void collect(GameSession session) {
        int kills = session.getStatistics().getKilledZombies();

        if (kills == lastKillCount) {
            return;
        }

        lastKillCount = kills;

        Map<ScoreEventType, Integer> counts =
            new EnumMap<>(ScoreEventType.class);
        Map<ScoreEventType, Integer> points =
            new EnumMap<>(ScoreEventType.class);

        for (ScoreEvent event
            : new ScoreTracker().detect(session.getStatistics())) {
            counts.merge(event.getType(), 1, Integer::sum);
            points.put(event.getType(), event.getPoints());
        }

        appendNewNotices(counts, points);
    }

    private void appendNewNotices(Map<ScoreEventType, Integer> counts,
                                  Map<ScoreEventType, Integer> points) {
        for (Map.Entry<ScoreEventType, Integer> entry : counts.entrySet()) {
            int seen = notifiedCounts.getOrDefault(entry.getKey(), 0);

            if (entry.getValue() <= seen) {
                continue;
            }

            notifiedCounts.put(entry.getKey(), entry.getValue());
            notices.add(entry.getKey().name().replace('_', ' ')
                + "!  +" + points.get(entry.getKey()) + " MU");
        }
    }

    String poll() {
        if (notices.isEmpty()) {
            return null;
        }

        return notices.remove(0);
    }
}
