package ir.ac.pvz.model.travel;

import ir.ac.pvz.model.others.GameStatistics;

import java.util.*;

public class ScoreTracker {
    public static final int A_SECOND = 10;
    public static final int MIN_CHAIN_LENGTH = 3;
    private final List<ScoreEvent> events = new ArrayList<>();


    public List<ScoreEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public List<ScoreEvent> detect(GameStatistics gameStatistics) {
        events.clear();

        if (gameStatistics == null) {
            return getEvents();
        }

        detectSimultaneousKills(gameStatistics);
        detectMultiHits(gameStatistics);
        detectSpeedKills(gameStatistics);
        detectChainKills(gameStatistics);
        detectFlawlessDefence(gameStatistics);

        return getEvents();
    }


    private void detectSimultaneousKills(GameStatistics gameStatistics) {
        Map<Integer, Integer> killsPerTick = countKillsPerTick(gameStatistics);

        for (Map.Entry<Integer, Integer> entry : killsPerTick.entrySet()) {
            if (entry.getValue() >= 2) {
                events.add(new ScoreEvent(ScoreEventType.SIMULTANEOUS_KILLS, entry.getKey(), entry.getValue()));
            }
        }
    }

    private void detectMultiHits(GameStatistics gameStatistics) {
        for (Map.Entry<Integer, Integer> entry : gameStatistics.getKillsByProjectile().entrySet()) {
            if (entry.getValue() >= 2) {
                events.add(new ScoreEvent(ScoreEventType.MULTI_HIT, 0, entry.getValue()));
            }
        }
    }

    private Map<Integer, Integer> countKillsPerTick(GameStatistics gameStatistics) {
        Map<Integer, Integer> killsPerTick = new LinkedHashMap<>();

        for (Integer tick : gameStatistics.getZombieKillTicks()) {
            if (killsPerTick.containsKey(tick)) {
                killsPerTick.put(tick, killsPerTick.get(tick) + 1);
            }
            else {
                killsPerTick.put(tick, 1);
            }
        }

        return killsPerTick;
    }

    private void detectSpeedKills(GameStatistics gameStatistics) {

        List<Integer> ticks = new ArrayList<>(gameStatistics.getZombieKillTicks());
        Collections.sort(ticks);

        for (int i = 1; i < ticks.size(); i++) {
            int space = ticks.get(i) - ticks.get(i - 1);

            if (space > 0 && space <= A_SECOND) {
                events.add(new ScoreEvent(ScoreEventType.SPEED_KILL, ticks.get(i), 1));
            }
        }
    }

    private void detectChainKills(GameStatistics gameStatistics) {
        List<Integer> ticks = new ArrayList<>(gameStatistics.getZombieKillTicks());
        Collections.sort(ticks);

        int chainLength = 1;
        int chainStart = ticks.isEmpty() ? 0 : ticks.get(0);

        for (int i = 1; i < ticks.size(); i++) {
            if (ticks.get(i) - ticks.get(i - 1) <= A_SECOND) {
                chainLength++;
            }
            else {
                addChain(chainStart, chainLength);

                chainLength = 1;
                chainStart = ticks.get(i);
            }
        }

        addChain(chainStart, chainLength);
    }

    private void addChain(int chainStart, int chainLength) {
        if (chainLength >= MIN_CHAIN_LENGTH) {
            events.add(new ScoreEvent(ScoreEventType.CHAIN_KILL, chainStart, chainLength));
        }
    }

    private void detectFlawlessDefence(GameStatistics gameStatistics) {
        if (gameStatistics.getLostPlants() == 0 &&  gameStatistics.getKilledZombies() > 0) {
            events.add(new ScoreEvent(ScoreEventType.FLAWLESS_DEFENSE, gameStatistics.getLastZombieKillTick(), gameStatistics.getKilledZombies()));
        }
    }
}
