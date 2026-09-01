package ir.ac.pvz.controller.gui;

import ir.ac.pvz.model.chapter.Chapter;
import ir.ac.pvz.model.enums.PlantCategory;
import ir.ac.pvz.model.enums.PlantTag;
import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.others.GameStatistics;
import ir.ac.pvz.model.stage.Stage;
import ir.ac.pvz.model.user.QuestLog;
import ir.ac.pvz.model.user.User;

import java.util.Map;

public final class QuestProgressService {
    private static final int TICKS_PER_SECOND = 10;

    private QuestProgressService() {
    }

    public static void advance(User user, Chapter chapter, Stage stage,
                               String minigame, GameSession session) {
        if (user == null || session == null) {
            return;
        }

        QuestLog log = user.getQuestLog();
        if (log == null) {
            return;
        }

        GameStatistics stats = session.getStatistics();
        boolean won = session.status == ir.ac.pvz.model.enums.GameStatus.WON;

        if (minigame != null) {
            if (won) {
                log.advance(minigameQuestId(minigame), 1);
            }
            return;
        }

        advanceCounters(log, chapter, stats);
        advanceSoloPlantQuests(log, stats);
        advanceStreak(log, won);

        if (won) {
            advanceWinConditions(log, session, stats);
        }
    }

    private static void advanceCounters(QuestLog log, Chapter chapter,
                                        GameStatistics stats) {
        if (chapter != null) {
            log.advance("hunt-" + chapter.getName(), stats.getKilledZombies());
        }

        log.advance("day-sun", stats.getCollectedSun());
        log.advance("demolisher", stats.getExplosivePlantsUsed());
        log.advance("mowing-time", stats.getLawnMowerKills());
        log.advance("almost-won", firstColumnKillsWithoutMower(stats));

        if (killedWithinOpeningWindow(stats) >= QuestSeeder.QUICK_KILL_TARGET) {
            log.advance("quick-action", 1);
        }
    }

    private static void advanceSoloPlantQuests(QuestLog log, GameStatistics stats) {
        Map<String, Integer> kills = stats.getKillsByPlantType();
        log.advance("solo-plant", soloKills(kills, QuestSeeder.SOLO_PLANT));
        log.advance("only-cactus", soloKills(kills, "Cactus"));
    }

    private static int soloKills(Map<String, Integer> kills, String type) {
        int mine = 0;
        for (Map.Entry<String, Integer> entry : kills.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(type)) {
                mine += entry.getValue();
            }
            else if (entry.getValue() > 0) {
                return 0;
            }
        }

        return mine;
    }

    private static void advanceStreak(QuestLog log, boolean won) {
        boolean hardest = StageConfigFactory.activeDifficulty() >= 5;
        if (won && hardest) {
            log.advance("win-streak", 1);
        }
        else if (!won) {
            log.resetProgress("win-streak");
        }
    }

    private static void advanceWinConditions(QuestLog log, GameSession session,
                                             GameStatistics stats) {
        if (stats.getLostPlants() <= QuestSeeder.ECONOMIC_PLANT_LOSSES) {
            log.advance("eco-herbivore", 1);
        }

        if (session.currentSunAmount == 0) {
            log.advance("defence-master", 1);
        }

        if (countCategory(stats, PlantCategory.SUN_PRODUCER)
            <= QuestSeeder.SUN_PRODUCER_LIMIT) {
            log.advance("cloudy-day", 1);
        }

        if (countTag(stats, PlantTag.SHROOM) > 0) {
            log.advance("night-or-morning", 1);
        }

        advanceFamilyQuests(log, stats);
        advanceLayoutQuests(log, session, stats);
    }

    private static void advanceFamilyQuests(QuestLog log, GameStatistics stats) {
        PlantTag family = familyTag();
        if (family == null) {
            return;
        }

        int familyKills = valueOf(stats.getKillsByPlantTag(), family);
        int totalKills = total(stats.getKillsByPlantType());
        if (familyKills > 0 && familyKills >= totalKills) {
            log.advance("family-slaughter", 1);
        }

        if (valueOf(stats.getPlantedByPlantTag(), family) == 0) {
            log.advance("constraint-bloom", 1);
        }
    }

    private static void advanceLayoutQuests(QuestLog log, GameSession session,
                                            GameStatistics stats) {
        Map<String, Integer> cells = stats.getPlantPlacementsByCell();
        int rows = session.getBoard().rows;
        int columns = session.getBoard().columns;

        boolean columnEmpty = columnIsEmpty(cells, QuestSeeder.EMPTY_COLUMN, rows);
        boolean rowEmpty = rowIsEmpty(cells, QuestSeeder.EMPTY_ROW, columns);

        if (columnEmpty) {
            log.advance("one-column-less", 1);
        }

        if (rowEmpty) {
            log.advance("undefended-row", 1);
        }

        if (columnEmpty && rowEmpty) {
            log.advance("undefended-cross", 1);
        }

        if (isSymmetric(cells, rows, columns)) {
            log.advance("symmetry", 1);
        }
        else if (hasNoSymmetryOutsideMiddle(cells, rows, columns)) {
            log.advance("ocd", 1);
        }
    }

    private static boolean columnIsEmpty(Map<String, Integer> cells, int column,
                                         int rows) {
        for (int row = 0; row < rows; row++) {
            if (planted(cells, column, row)) {
                return false;
            }
        }

        return true;
    }

    private static boolean rowIsEmpty(Map<String, Integer> cells, int row,
                                      int columns) {
        for (int column = 0; column < columns; column++) {
            if (planted(cells, column, row)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isSymmetric(Map<String, Integer> cells, int rows,
                                       int columns) {
        for (int row = 0; row < rows / 2; row++) {
            int mirror = rows - 1 - row;
            for (int column = 0; column < columns; column++) {
                if (planted(cells, column, row) != planted(cells, column, mirror)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean hasNoSymmetryOutsideMiddle(Map<String, Integer> cells,
                                                      int rows, int columns) {
        for (int row = 0; row < rows / 2; row++) {
            int mirror = rows - 1 - row;
            for (int column = 0; column < columns; column++) {
                if (planted(cells, column, row) == planted(cells, column, mirror)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean planted(Map<String, Integer> cells, int column, int row) {
        Integer value = cells.get(column + "," + row);
        return value != null && value > 0;
    }

    private static int firstColumnKillsWithoutMower(GameStatistics stats) {
        int total = 0;
        for (Map.Entry<String, Integer> entry
            : stats.getZombieKillsByCell().entrySet()) {
            if (entry.getKey().startsWith("0,")) {
                total += entry.getValue();
            }
        }

        return Math.max(0, total - stats.getLawnMowerKills());
    }

    private static int killedWithinOpeningWindow(GameStatistics stats) {
        int start = stats.getFirstWaveStartTick();
        if (start <= 0) {
            return 0;
        }

        int limit = start + QuestSeeder.QUICK_KILL_SECONDS * TICKS_PER_SECOND;
        int killed = 0;
        for (Integer tick : stats.getZombieKillTicks()) {
            if (tick != null && tick >= start && tick <= limit) {
                killed++;
            }
        }

        return killed;
    }

    private static PlantTag familyTag() {
        for (PlantTag tag : PlantTag.values()) {
            if (tag.name().equalsIgnoreCase(QuestSeeder.FAMILY_QUEST_TAG)) {
                return tag;
            }
        }

        return null;
    }

    private static int countCategory(GameStatistics stats, PlantCategory category) {
        Integer value = stats.getPlantedByPlantCategory().get(category);
        return value == null ? 0 : value;
    }

    private static int countTag(GameStatistics stats, PlantTag tag) {
        return valueOf(stats.getPlantedByPlantTag(), tag);
    }

    private static int valueOf(Map<PlantTag, Integer> map, PlantTag tag) {
        Integer value = map.get(tag);
        return value == null ? 0 : value;
    }

    private static int total(Map<String, Integer> map) {
        int sum = 0;
        for (Integer value : map.values()) {
            if (value != null) {
                sum += value;
            }
        }

        return sum;
    }

    private static String minigameQuestId(String name) {
        String lower = name.toLowerCase();

        if (lower.contains("vase")) {
            return "mini-vasebreaker";
        }

        if (lower.contains("bowling")) {
            return "mini-bowling";
        }

        return "mini-izombie";
    }
}
