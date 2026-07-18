package ir.ac.pvz.model.others;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.enums.FallingSunType;
import ir.ac.pvz.model.enums.SeasonType;
import ir.ac.pvz.model.interfaces.ISunProducer;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.GridPosition;
import ir.ac.pvz.model.support.Sun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SunManager {

    public int currentSunAmount;
    public int lastSkyDropTick;
    public float normalSunChance;
    public float specialSunChance;
    public float radioactiveSunChance;
    public float skySunFallDurationSeconds;

    private final List<Sun> suns;
    private final Map<Plant, Sun> plantSuns;
    private final Random random;
    private Board board;

    public SunManager(int startingSun) {
        this(startingSun, null);
    }

    public SunManager(int startingSun, Board board) {
        this.currentSunAmount = startingSun;
        this.lastSkyDropTick = 0;
        this.normalSunChance = 0.80f;
        this.specialSunChance = 0.15f;
        this.radioactiveSunChance = 0.05f;
        this.skySunFallDurationSeconds = 5f;
        this.suns = new ArrayList<>();
        this.plantSuns = new HashMap<>();
        this.random = new Random();
        this.board = board;
    }

    public float calculateDropIntervalSeconds(float elapsedSeconds) {
        return Math.max(6f + 0.05f * elapsedSeconds, 12f);
    }

    public Sun dropSkySun(Board board) {
        this.board = board;
        GridPosition position = new GridPosition(random.nextInt(board.columns), random.nextInt(board.rows));
        float roll = random.nextFloat();
        FallingSunType type;
        int amount;
        if (roll < normalSunChance) {
            type = FallingSunType.NORMAL;
            amount = 25;
        }
        else if (roll < normalSunChance + specialSunChance) {
            type = FallingSunType.SPECIAL;
            amount = 100;
        }
        else {
            type = FallingSunType.RADIOACTIVE;
            amount = 0;
        }
        Sun sun = new Sun(type, amount, position, skySunFallDurationSeconds, true);
        suns.add(sun);
        System.out.println("New " + type + " sun is dropping at position "
                + position.toUserString());
        return sun;
    }

    public Sun producePlantSun(Plant plant) {
        if (!(plant instanceof ISunProducer) || plantSuns.containsKey(plant)) {
            return null;
        }
        int amount = ((ISunProducer) plant).produceSun();
        if (amount <= 0) {
            return null;
        }
        Sun sun = new Sun(FallingSunType.NORMAL, amount, plant.location, 0f, false);
        suns.add(sun);
        plantSuns.put(plant, sun);
        System.out.println("plant " + plant.type + " produced a sun at "
                + plant.location.toUserString());
        return sun;
    }



    public Sun dropGroundSun(GridPosition position, int amount) {
        if (position == null || amount <= 0) {
            return null;
        }
        Sun sun = new Sun(FallingSunType.NORMAL, amount, position, 0f, false);
        suns.add(sun);
        return sun;
    }

    public Sun produceBonusPlantSun(Plant plant, int amount) {
        if (plant == null || plant.location == null || amount <= 0) {
            return null;
        }
        Sun sun = new Sun(FallingSunType.NORMAL, amount,
                plant.location, 0f, false);
        suns.add(sun);
        System.out.println("plant " + plant.type + " produced a sun at "
                + plant.location.toUserString());
        return sun;
    }

    public boolean collectSun(GridPosition position) {
        for (Sun sun : new ArrayList<>(suns)) {
            if (!sun.isAlive || !sun.groundPosition.equals(position)) {
                continue;
            }
            if (sun.type == FallingSunType.RADIOACTIVE && sun.isFalling) {
                sun.explode(board);
            }
            else {
                addSuns(sun.amount);
            }
            sun.die();
            removeSun(sun);
            return true;
        }
        return false;
    }

    public void addSuns(int count) {
        currentSunAmount += count;
    }

    public int showSunAmount() {
        return currentSunAmount;
    }


    public int pullGroundSunsToward(GridPosition target, int maximumAmount) {
        if (target == null || maximumAmount <= 0) {
            return 0;
        }
        int stolen = 0;
        for (Sun sun : new ArrayList<>(suns)) {
            if (sun.isFalling || plantSuns.containsValue(sun) || !sun.isAlive
                    || stolen + sun.amount > maximumAmount) {
                continue;
            }
            moveSunToward(sun, target);
            if (sun.groundPosition.equals(target)) {
                stolen += sun.amount;
                sun.die();
                removeSun(sun);
            }
        }
        return stolen;
    }

    private void moveSunToward(Sun sun, GridPosition target) {
        int x = sun.groundPosition.x;
        int y = sun.groundPosition.y;
        if (x != target.x) {
            x += Integer.compare(target.x, x);
        }
        else if (y != target.y) {
            y += Integer.compare(target.y, y);
        }
        sun.groundPosition = new GridPosition(x, y);
        sun.positionX = x;
        sun.positionY = y;
    }

    public int stealGroundSuns(int maximumAmount) {
        int stolen = 0;
        for (Sun sun : new ArrayList<>(suns)) {
            if (sun.isFalling || plantSuns.containsValue(sun) || !sun.isAlive) {
                continue;
            }
            if (stolen + sun.amount > maximumAmount) {
                continue;
            }
            stolen += sun.amount;
            sun.die();
            removeSun(sun);
            if (stolen >= maximumAmount) {
                break;
            }
        }
        return stolen;
    }

    public List<Sun> getActiveSuns() {
        return new ArrayList<>(suns);
    }

    public boolean isSkyDropAllowed(SeasonType seasonType) {
        return seasonType != SeasonType.DARK_AGES;
    }

    public boolean hasPendingPlantSun(Plant plant) {
        return plantSuns.containsKey(plant);
    }

    public boolean spendSuns(int count) {
        if (currentSunAmount < count) {
            return false;
        }
        currentSunAmount -= count;
        return true;
    }

    public void update(int currentTick, float elapsedSeconds) {
        for (Sun sun : new ArrayList<>(suns)) {
            sun.update(1);
        }
        if (board != null && isSkyDropAllowed(board.seasonType)) {
            float intervalTicks = calculateDropIntervalSeconds(elapsedSeconds) * 10f;
            if (currentTick - lastSkyDropTick >= intervalTicks) {
                dropSkySun(board);
                lastSkyDropTick = currentTick;
            }
        }
        Iterator<Sun> iterator = suns.iterator();
        while (iterator.hasNext()) {
            Sun sun = iterator.next();
            if (!sun.isAlive) {
                iterator.remove();
                plantSuns.values().removeIf(value -> value == sun);
            }
        }
    }

    private void removeSun(Sun sun) {
        suns.remove(sun);
        plantSuns.values().removeIf(value -> value == sun);
    }
}
