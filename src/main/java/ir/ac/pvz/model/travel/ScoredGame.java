package ir.ac.pvz.model.travel;

import java.time.LocalDate;
import java.util.List;

public class ScoredGame {
    private String date;
    private long sunSeed;
    private int score;

    public ScoredGame(long sunSeed) {
        this.date = LocalDate.now().toString();
        this.sunSeed = sunSeed;
    }

    public String getDate() {
        return date;
    }

    public long getSunSeed() {
        return sunSeed;
    }

    public int getScore() {
        return score;
    }

    public int calculateScore(List<ScoreEvent> events) {
        score = 0;
        for (ScoreEvent event : events) {
            score += event.getPoints();
        }
        return score;
    }
}
