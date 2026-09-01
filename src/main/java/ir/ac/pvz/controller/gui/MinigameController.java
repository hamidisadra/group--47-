package ir.ac.pvz.controller.gui;

import ir.ac.pvz.controller.managers.MenuManager;
import ir.ac.pvz.model.minigame.BowlingNut;
import ir.ac.pvz.model.minigame.BowlingNutType;
import ir.ac.pvz.model.minigame.IZombie;
import ir.ac.pvz.model.minigame.MiniGame;
import ir.ac.pvz.model.minigame.Vase;
import ir.ac.pvz.model.minigame.Vasebreaker;
import ir.ac.pvz.model.minigame.WallnutBowling;
import ir.ac.pvz.model.minigame.Zombotany;
import ir.ac.pvz.model.user.User;

import java.util.ArrayList;
import java.util.List;

public class MinigameController {
    private final String name;
    private final MiniGame game;
    private static final float SEED_PACKET_LIFETIME_SECONDS = 12f;

    private final List<String> harvestedPlants = new ArrayList<>();
    private final List<Float> harvestedAge = new ArrayList<>();

    public static final int LEVELS_PER_MINIGAME = 3;

    private final int level;

    public MinigameController(String name, int rows, int columns) {
        this(name, rows, columns, 1);
    }

    public MinigameController(String name, int rows, int columns, int level) {
        this.name = name;
        this.level = Math.max(1, Math.min(LEVELS_PER_MINIGAME, level));
        switch (name) {
            case "Vasebreaker":
                game = new Vasebreaker(this.level, rows, columns);
                break;
            case "Wall-nut Bowling":
                game = new WallnutBowling(this.level, 1 + this.level);
                break;
            case "I, Zombie":
                game = new IZombie(this.level);
                break;
            case "Zombotany":
                game = new Zombotany(this.level);
                break;
            default:
                game = null;
                break;
        }
        if (game != null) {
            game.startGame();
        }
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public MiniGame getGame() {
        return game;
    }

    public boolean isVasebreaker() {
        return game instanceof Vasebreaker;
    }

    public boolean isBowling() {
        return game instanceof WallnutBowling;
    }

    public boolean isIZombie() {
        return game instanceof IZombie;
    }

    public List<Vase> getVases() {
        return isVasebreaker() ? ((Vasebreaker) game).getVases() : new ArrayList<>();
    }

    public List<String> getHarvestedPlants() {
        return harvestedPlants;
    }

    public static final class VaseResult {
        public final String kind;
        public final String value;

        VaseResult(String kind, String value) {
            this.kind = kind;
            this.value = value;
        }
    }

    public VaseResult breakVase(int column, int row) {
        if (!isVasebreaker()) {
            return null;
        }
        Vasebreaker vasebreaker = (Vasebreaker) game;
        Vase vase = vasebreaker.getVase(column, row);
        if (vase == null || vase.isBroken()) {
            return null;
        }
        User user = MenuManager.getInstance().getActiveUser();
        List<String> unlocked = user == null
                ? new ArrayList<>() : user.getCollection().getUnlockedPlants();
        String content = vasebreaker.breakVase(vase, unlocked);
        if (content == null) {
            return new VaseResult("empty", null);
        }
        if (content.startsWith("seedpacket:")) {
            String plant = content.substring("seedpacket:".length());
            harvestedPlants.add(plant);
            harvestedAge.add(0f);
            return new VaseResult("plant", plant);
        }
        if (content.startsWith("zombie:")) {
            String kind = content.substring("zombie:".length());
            return new VaseResult("zombie",
                    kind.equals("gargantuar") ? "Gargantuar" : "BasicZombie");
        }
        return new VaseResult("empty", null);
    }

    public String vaseAnimation(Vase vase) {
        switch (vase.getType()) {
            case PLANT_VASE:
                return "VASE_GREEN";
            case GHOUL_VASE:
                return "VASE_GARGANTUAR";
            default:
                return "VASE_BROWN";
        }
    }

    public boolean allVasesBroken() {
        return isVasebreaker() && ((Vasebreaker) game).allVasesBroken();
    }

    public List<BowlingNut> getNuts() {
        return isBowling() ? ((WallnutBowling) game).getNuts() : new ArrayList<>();
    }

    public int getRedLineColumn() {
        return isBowling() ? ((WallnutBowling) game).getRedLineCol() : -1;
    }

    public BowlingNut launchNut(int row, int col, String type) {
        if (!isBowling()) {
            return null;
        }
        BowlingNutType nutType;
        switch (type) {
            case "Explode-o-nut":
                nutType = BowlingNutType.EXPLODE_O_NUT;
                break;
            case "Tall-nut":
                nutType = BowlingNutType.GIANT;
                break;
            default:
                nutType = BowlingNutType.NORMAL;
                break;
        }
        return ((WallnutBowling) game).launchNut(row, col, nutType);
    }

    public boolean expireHarvestedPlants(float delta) {
        boolean expired = false;

        for (int index = harvestedAge.size() - 1; index >= 0; index--) {
            float age = harvestedAge.get(index) + delta;
            if (age >= SEED_PACKET_LIFETIME_SECONDS) {
                harvestedAge.remove(index);
                harvestedPlants.remove(index);
                expired = true;
            }

            else {
                harvestedAge.set(index, age);
            }
        }

        return expired;
    }

    public void consumeHarvestedPlant(String plantType) {
        int index = harvestedPlants.indexOf(plantType);
        if (index >= 0) {
            harvestedPlants.remove(index);
            harvestedAge.remove(index);
        }
    }

    public void advanceNuts(int rows, int columns) {
        for (BowlingNut nut : new ArrayList<>(getNuts())) {
            if (nut.isAlive()) {
                nut.move(rows);
            }
        }

        if (isBowling()) {
            ((WallnutBowling) game).removeFinishedNuts(columns);
        }
    }


    public boolean isFinished() {
        return game != null && game.isFinished();
    }

    public boolean isWon() {
        return game != null && game.isWon();
    }
}
