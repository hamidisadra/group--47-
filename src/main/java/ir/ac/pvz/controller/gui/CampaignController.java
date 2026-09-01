package ir.ac.pvz.controller.gui;

import ir.ac.pvz.model.chapter.AncientEgypt;
import ir.ac.pvz.model.chapter.BigWaveBeach;
import ir.ac.pvz.model.chapter.Chapter;
import ir.ac.pvz.model.chapter.DarkAges;
import ir.ac.pvz.model.chapter.FrostbiteCaves;
import ir.ac.pvz.model.enums.SeasonType;
import ir.ac.pvz.model.stage.BossStage;
import ir.ac.pvz.model.stage.ConveyorBeltStage;
import ir.ac.pvz.model.stage.DeadLineStage;
import ir.ac.pvz.model.stage.LockedPlantsStage;
import ir.ac.pvz.model.stage.NightOpsStage;
import ir.ac.pvz.model.stage.LoveYourPlantsStage;
import ir.ac.pvz.model.stage.NormalStage;
import ir.ac.pvz.model.stage.PlantWhatYouGetStage;
import ir.ac.pvz.model.stage.SaveOurSeedsStage;
import ir.ac.pvz.model.stage.Stage;
import ir.ac.pvz.model.stage.TimedWarStage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CampaignController {
    public static final int STAGES_PER_CHAPTER = 4;

    private static final String[] LOCKED_PLANTS = {
        "Repeater", "Snow Pea", "Cherry Bomb"
    };

    private static CampaignController instance;
    private final Map<String, Chapter> chapters = new LinkedHashMap<>();

    private CampaignController() {
        register(new AncientEgypt());
        register(new FrostbiteCaves());
        register(new BigWaveBeach());
        register(new DarkAges());
    }

    public static CampaignController getInstance() {
        if (instance == null) {
            instance = new CampaignController();
        }

        return instance;
    }

    private void register(Chapter chapter) {
        int chapterIndex = chapters.size();

        for (int number = 1; number <= STAGES_PER_CHAPTER; number++) {
            chapter.addStage(buildStage(chapterIndex, number));
        }

        chapters.put(chapter.getName(), chapter);
    }

    private Stage buildStage(int chapterIndex, int number) {
        int difficulty = 1 + chapterIndex;
        int waves = 3 + number;

        if (number == 1) {
            return new NormalStage(number, difficulty, waves);
        }

        if (number == STAGES_PER_CHAPTER) {
            return new BossStage(number, difficulty, waves);
        }

        return buildSpecialStage(chapterIndex, number, difficulty, waves);
    }

    private Stage lockedPlantsStage(int number, int difficulty, int waves) {
        LockedPlantsStage stage = new LockedPlantsStage(number, difficulty, waves);

        for (String plant : LOCKED_PLANTS) {
            stage.lockPlant(plant);
        }

        return stage;
    }

    private Stage buildSpecialStage(int chapterIndex, int number, int difficulty, int waves) {
        int specialIndex = chapterIndex * 2 + (number - 2);

        switch (specialIndex) {
            case 0:
                return new ConveyorBeltStage(number, difficulty, waves);
            case 1:
                return lockedPlantsStage(number, difficulty, waves);
            case 2:
                return new SaveOurSeedsStage(number, difficulty, waves);
            case 3:
                return new TimedWarStage(number, difficulty, waves, 90, 10, 1000);
            case 4:
                return new DeadLineStage(number, difficulty, waves, 1);
            case 5:
                return new NightOpsStage(number, difficulty, waves);
            case 6:
                return new LoveYourPlantsStage(number, difficulty, waves, 2);
            case 7:
                return new PlantWhatYouGetStage(number, difficulty, waves, 250);
            default:
                return new NormalStage(number, difficulty, waves);
        }
    }

    public List<Chapter> getChapters() {
        return new ArrayList<>(chapters.values());
    }

    public Chapter getChapter(String name) {
        return chapters.get(name);
    }

    public int indexOf(Chapter chapter) {
        int index = 0;
        for (Chapter candidate : chapters.values()) {
            if (candidate == chapter) {
                return index;
            }

            index++;
        }

        return 0;
    }

    public SeasonType seasonOf(Chapter chapter) {
        if (chapter instanceof FrostbiteCaves) {
            return SeasonType.FROSTBITE_CAVES;
        }

        if (chapter instanceof BigWaveBeach) {
            return SeasonType.BIG_WAVE_BEACH;
        }

        if (chapter instanceof DarkAges) {
            return SeasonType.DARK_AGES;
        }

        return SeasonType.ANCIENT_EGYPT;
    }

    public int completedStages(Chapter chapter, int gameProgress) {
        int offset = indexOf(chapter) * STAGES_PER_CHAPTER;
        return Math.max(0, Math.min(STAGES_PER_CHAPTER, gameProgress - offset));
    }

    public boolean isChapterUnlocked(Chapter chapter, int gameProgress) {
        return gameProgress >= indexOf(chapter) * STAGES_PER_CHAPTER;
    }

    public boolean isStageUnlocked(Chapter chapter, Stage stage, int gameProgress) {
        int offset = indexOf(chapter) * STAGES_PER_CHAPTER;
        return gameProgress >= offset + stage.getNumber() - 1;
    }

    public int globalIndex(Chapter chapter, Stage stage) {
        return indexOf(chapter) * STAGES_PER_CHAPTER + stage.getNumber();
    }

    public String describe(Stage stage) {
        if (stage instanceof BossStage) {
            return "Zomboss battle";
        }

        if (stage instanceof ConveyorBeltStage) {
            return "Conveyor belt";
        }

        if (stage instanceof SaveOurSeedsStage) {
            return "Save our seeds";
        }

        if (stage instanceof DeadLineStage) {
            return "Deadline";
        }

        if (stage instanceof TimedWarStage) {
            return "Timed battle";
        }

        if (stage instanceof LoveYourPlantsStage) {
            return "Don't lose your plants";
        }

        if (stage instanceof PlantWhatYouGetStage) {
            return "Plant what you get";
        }

        if (stage instanceof LockedPlantsStage) {
            return "Locked plants";
        }

        if (stage instanceof NightOpsStage) {
            return "Night ops";
        }

        return "Normal";
    }

    public List<String> objectives(Stage stage) {
        List<String> objectives = new ArrayList<>();
        objectives.add("Don't let the zombies reach your house");
        if (stage instanceof DeadLineStage) {
            objectives.add("Zombies must not cross the marked line at column "
                + (((DeadLineStage) stage).getDeadLineColumn() + 1));
        }

        if (stage instanceof SaveOurSeedsStage) {
            objectives.add("Protect the marked plants");
        }

        if (stage instanceof LoveYourPlantsStage) {
            objectives.add("Lose at most "
                + ((LoveYourPlantsStage) stage).getMaxPlantLosses() + " plants");
        }

        if (stage instanceof TimedWarStage) {
            TimedWarStage timed = (TimedWarStage) stage;
            objectives.add("Defeat " + timed.getKillTarget() + " zombies in "
                + timed.getTimeLimitSeconds() + " seconds");
            objectives.add("Produce " + timed.getSunTarget() + " sun");
        }

        if (stage instanceof PlantWhatYouGetStage) {
            objectives.add("Plant freely, then start the wave when you are ready");
        }

        if (stage instanceof ConveyorBeltStage) {
            objectives.add("Plants arrive on the conveyor belt");
        }

        if (stage instanceof LockedPlantsStage) {
            objectives.add("Some plants are locked for this level");
        }

        if (stage instanceof NightOpsStage) {
            objectives.add("No sun falls from the sky; rely on your producers");
        }

        if (stage instanceof BossStage) {
            objectives.add("Defeat Zomboss");
        }

        return objectives;
    }
}
