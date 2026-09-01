package ir.ac.pvz.controller.gui;

import ir.ac.pvz.model.travel.DailyQuest;
import ir.ac.pvz.model.travel.EpicQuest;
import ir.ac.pvz.model.travel.Quest;
import ir.ac.pvz.model.user.QuestLog;
import ir.ac.pvz.model.travel.Reward;
import ir.ac.pvz.model.travel.RewardType;
import ir.ac.pvz.model.user.User;

public final class QuestSeeder {
    public static final String PAGE_ADVENTURE = "adventure";
    public static final String PAGE_SPECIAL = "special";
    public static final String PAGE_MINIGAMES = "minigames";
    public static final String PAGE_CHALLENGE = "challenge";
    public static final String PAGE_MYSTERY = "mystery";
    public static final String PAGE_DAILY = "daily";

    public static final int DAILY_SUN_TARGET = 3000;
    public static final int CHAPTER_HUNT_TARGET = 50;
    public static final int SOLO_PLANT_KILLS = 10;
    public static final int ECONOMIC_PLANT_LOSSES = 5;
    public static final int QUICK_KILL_TARGET = 10;
    public static final int QUICK_KILL_SECONDS = 30;
    public static final int EXPLOSIVE_TARGET = 3;
    public static final int STREAK_TARGET = 5;
    public static final int MOWER_KILL_TARGET = 10;
    public static final int SUN_PRODUCER_LIMIT = 3;
    public static final String SOLO_PLANT = "Peashooter";
    public static final String FAMILY_QUEST_TAG = "Pea";
    public static final int EMPTY_COLUMN = 4;
    public static final int EMPTY_ROW = 2;

    private QuestSeeder() {
    }

    public static void seed(User user) {
        if (user == null) {
            return;
        }

        QuestLog log = user.getQuestLog();
        if (log == null) {
            return;
        }

        if (!log.getPage(PAGE_ADVENTURE).isEmpty()) {
            return;
        }

        seedAdventure(log);
        seedSpecial(log);
        seedMinigames(log);
        seedChallenge(log);
        seedMystery(log);
        seedDaily(log);
    }

    private static void seedAdventure(QuestLog log) {
        add(log, new EpicQuest("hunt-Ancient Egypt", "Hunter of Ancient Egypt",
            "Defeat " + CHAPTER_HUNT_TARGET + " zombies in Ancient Egypt.",
            PAGE_ADVENTURE, CHAPTER_HUNT_TARGET, seeds(10)));

        add(log, new EpicQuest("hunt-Frostbite Caves", "Hunter of Frostbite Caves",
            "Defeat " + CHAPTER_HUNT_TARGET + " zombies in Frostbite Caves.",
            PAGE_ADVENTURE, CHAPTER_HUNT_TARGET, seeds(10)));

        add(log, new EpicQuest("hunt-Big Wave Beach", "Hunter of Big Wave Beach",
            "Defeat " + CHAPTER_HUNT_TARGET + " zombies in Big Wave Beach.",
            PAGE_ADVENTURE, CHAPTER_HUNT_TARGET, seeds(10)));

        add(log, new EpicQuest("hunt-Dark Ages", "Hunter of Dark Ages",
            "Defeat " + CHAPTER_HUNT_TARGET + " zombies in Dark Ages.",
            PAGE_ADVENTURE, CHAPTER_HUNT_TARGET, seeds(10)));

        add(log, new EpicQuest("eco-herbivore", "Thrifty Herbivore",
            "Win a stage without losing more than " + ECONOMIC_PLANT_LOSSES
                + " plants.", PAGE_ADVENTURE, 1,
            seeds(20 - ECONOMIC_PLANT_LOSSES)));

        add(log, new EpicQuest("quick-action", "Quick Off the Mark",
            "Defeat " + QUICK_KILL_TARGET + " zombies within "
                + QUICK_KILL_SECONDS + " seconds of the first wave.",
            PAGE_ADVENTURE, 1, coins(500)));
    }

    private static void seedSpecial(QuestLog log) {
        add(log, new EpicQuest("family-slaughter", "All in the Family",
            "Win a stage using only " + FAMILY_QUEST_TAG
                + " family plants to kill zombies.", PAGE_SPECIAL, 1, coins(1000)));

        add(log, new EpicQuest("constraint-bloom", "Blooming Under Limits",
            "Win a stage without planting any " + FAMILY_QUEST_TAG
                + " family plant.", PAGE_SPECIAL, 1, gems(100)));

        add(log, new EpicQuest("cloudy-day", "Cloudy Day",
            "Win a stage using at most " + SUN_PRODUCER_LIMIT
                + " sun producing plants.", PAGE_SPECIAL, 1, gems(10)));

        add(log, new EpicQuest("one-column-less", "One Column Short",
            "Win a stage without planting anything in column "
                + (EMPTY_COLUMN + 1) + ".", PAGE_SPECIAL, 1, gems(10)));

        add(log, new EpicQuest("undefended-row", "Undefended Row",
            "Win a stage without planting anything in row "
                + (EMPTY_ROW + 1) + ".", PAGE_SPECIAL, 1, gems(20)));

        add(log, new EpicQuest("undefended-cross", "Undefended Cross",
            "Win a stage leaving row " + (EMPTY_ROW + 1) + " and column "
                + (EMPTY_COLUMN + 1) + " empty.", PAGE_SPECIAL, 1, gems(25)));
    }

    private static void seedMinigames(QuestLog log) {
        add(log, new ir.ac.pvz.model.travel.RepeatableQuest("mini-vasebreaker",
            "Vase Breaker", "Complete a round of Vasebreaker.",
            PAGE_MINIGAMES, 3, coins(750)));

        add(log, new ir.ac.pvz.model.travel.RepeatableQuest("mini-bowling",
            "Wall-nut Bowling", "Complete a round of Wall-nut Bowling.",
            PAGE_MINIGAMES, 3, coins(750)));

        add(log, new ir.ac.pvz.model.travel.RepeatableQuest("mini-izombie",
            "I, Zombie", "Win a round of I, Zombie.",
            PAGE_MINIGAMES, 3, gems(6)));
    }

    private static void seedChallenge(QuestLog log) {
        add(log, new EpicQuest("defence-master", "Master of Defence",
            "Finish a stage with exactly zero sun left.",
            PAGE_CHALLENGE, 1, gems(200)));

        add(log, new EpicQuest("win-streak", "Win After Win",
            "Win " + STREAK_TARGET + " stages in a row on the hardest difficulty.",
            PAGE_CHALLENGE, STREAK_TARGET, coins(5000)));

        add(log, new EpicQuest("mowing-time", "Mowing Time",
            "Defeat " + MOWER_KILL_TARGET + " zombies with lawn mowers.",
            PAGE_CHALLENGE, MOWER_KILL_TARGET, gems(MOWER_KILL_TARGET)));

        add(log, new EpicQuest("night-or-morning", "Night or Morning",
            "Win a daytime stage using mushroom plants.",
            PAGE_CHALLENGE, 1, gems(20)));
    }

    private static void seedMystery(QuestLog log) {
        add(log, new EpicQuest("symmetry", "Symmetry",
            "Win a stage with a vertically symmetric garden.",
            PAGE_MYSTERY, 1, coins(500)));

        add(log, new EpicQuest("ocd", "No Symmetry Allowed",
            "Win a stage with no symmetry outside the middle row.",
            PAGE_MYSTERY, 1, coins(800)));

        add(log, new EpicQuest("almost-won", "Almost Lost",
            "Defeat 10 zombies in the first column of a row with no mower.",
            PAGE_MYSTERY, 10, coins(300)));
    }

    private static void seedDaily(QuestLog log) {
        add(log, new DailyQuest("day-sun", "Daily Sun Catcher",
            "Collect " + DAILY_SUN_TARGET + " sun today.",
            PAGE_DAILY, DAILY_SUN_TARGET, coins(DAILY_SUN_TARGET / 100)));

        add(log, new DailyQuest("solo-plant", "Professional " + SOLO_PLANT,
            "Defeat " + SOLO_PLANT_KILLS + " zombies using only "
                + SOLO_PLANT + ".", PAGE_DAILY, SOLO_PLANT_KILLS, seeds(5)));

        add(log, new DailyQuest("only-cactus", "Only Cactus",
            "Defeat " + SOLO_PLANT_KILLS + " zombies using only Cactus.",
            PAGE_DAILY, SOLO_PLANT_KILLS, gems(20)));

        add(log, new DailyQuest("demolisher", "Professional Demolisher",
            "Use " + EXPLOSIVE_TARGET + " explosive plants in one stage.",
            PAGE_DAILY, EXPLOSIVE_TARGET, coins(100)));

        add(log, new DailyQuest("day-greenhouse", "Daily Harvest",
            "Harvest a plant from the greenhouse.", PAGE_DAILY, 1, gems(3)));
    }

    private static void add(QuestLog log, Quest quest) {
        log.addQuest(quest);
    }

    private static Reward coins(int amount) {
        return new Reward(RewardType.CURRENCY_COINS, amount, null);
    }

    private static Reward gems(int amount) {
        return new Reward(RewardType.CURRENCY_GEMS, amount, null);
    }

    private static Reward seeds(int amount) {
        return new Reward(RewardType.INVENTORY, amount, "seed packet");
    }
}
