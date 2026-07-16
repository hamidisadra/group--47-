package com.pvz.game;

import com.pvz.model.core.Plant;
import com.pvz.model.core.Zombie;
import com.pvz.model.enums.PlantCategory;
import com.pvz.model.enums.PlantTag;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class GameStatistics {

    private int collectedSun;
    private int plantedPlants;
    private int lostPlants;
    private int killedZombies;
    private int explosivePlantsUsed;
    private int lawnMowerKills;
    private int firstWaveStartTick;
    private int lastZombieKillTick;
    private final Map<String, Integer> plantedByPlantType;
    private final Map<PlantCategory, Integer> plantedByPlantCategory;
    private final Map<PlantTag, Integer> plantedByPlantTag;
    private final Map<String, Integer> plantPlacementsByCell;
    private final Map<String, Integer> lostByPlantType;
    private final Map<String, Integer> killsByPlantType;
    private final Map<PlantCategory, Integer> killsByPlantCategory;
    private final Map<PlantTag, Integer> killsByPlantTag;
    private final Map<String, Integer> zombieKillsByCell;
    private final Map<String, Integer> killsByZombieType;
    private final List<Integer> zombieKillTicks;

    public GameStatistics() {
        collectedSun = 0;
        plantedPlants = 0;
        lostPlants = 0;
        killedZombies = 0;
        explosivePlantsUsed = 0;
        lawnMowerKills = 0;
        firstWaveStartTick = -1;
        lastZombieKillTick = -1;
        plantedByPlantType = new LinkedHashMap<>();
        plantedByPlantCategory = new LinkedHashMap<>();
        plantedByPlantTag = new LinkedHashMap<>();
        plantPlacementsByCell = new LinkedHashMap<>();
        lostByPlantType = new LinkedHashMap<>();
        killsByPlantType = new LinkedHashMap<>();
        killsByPlantCategory = new LinkedHashMap<>();
        killsByPlantTag = new LinkedHashMap<>();
        zombieKillsByCell = new LinkedHashMap<>();
        killsByZombieType = new LinkedHashMap<>();
        zombieKillTicks = new ArrayList<>();
    }

    public void recordSunCollected(int amount) {
        collectedSun += Math.max(0, amount);
    }

    public void recordPlantPlaced(Plant plant) {
        plantedPlants++;
        if (plant == null) {
            return;
        }
        plantedByPlantType.merge(plant.type, 1, Integer::sum);
        plantedByPlantCategory.merge(plant.category, 1, Integer::sum);
        for (PlantTag tag : plant.plantTags) {
            plantedByPlantTag.merge(tag, 1, Integer::sum);
        }
        if (plant.location != null) {
            plantPlacementsByCell.merge(cellKey(plant.location.x,
                    plant.location.y), 1, Integer::sum);
        }
        if (plant.category == PlantCategory.EXPLOSIVE) {
            explosivePlantsUsed++;
        }
    }

    public void recordPlantLost() {
        recordPlantLost(null);
    }

    public void recordPlantLost(Plant plant) {
        lostPlants++;
        if (plant != null) {
            lostByPlantType.merge(plant.type, 1, Integer::sum);
        }
    }

    public void recordZombieKilled(Zombie zombie, int currentTick) {
        killedZombies++;
        lastZombieKillTick = currentTick;
        zombieKillTicks.add(currentTick);
        if (zombie != null) {
            killsByZombieType.merge(zombie.getClass().getSimpleName(),
                    1, Integer::sum);
        }
        if (zombie != null && zombie.currentPosition != null) {
            zombieKillsByCell.merge(cellKey(
                    (int) Math.floor(zombie.currentPosition.x),
                    zombie.lane), 1, Integer::sum);
        }
        if (zombie == null || zombie.lastDamageSource == null) {
            return;
        }
        Plant source = zombie.lastDamageSource;
        killsByPlantType.merge(source.type, 1, Integer::sum);
        killsByPlantCategory.merge(source.category, 1, Integer::sum);
        for (PlantTag tag : source.plantTags) {
            killsByPlantTag.merge(tag, 1, Integer::sum);
        }
    }

    public void recordLawnMowerKills(int count) {
        lawnMowerKills += Math.max(0, count);
    }

    public void recordFirstWaveStart(int currentTick) {
        if (firstWaveStartTick < 0) {
            firstWaveStartTick = currentTick;
        }
    }

    public int getCollectedSun() { return collectedSun; }
    public int getPlantedPlants() { return plantedPlants; }
    public int getLostPlants() { return lostPlants; }
    public int getKilledZombies() { return killedZombies; }
    public int getExplosivePlantsUsed() { return explosivePlantsUsed; }
    public int getLawnMowerKills() { return lawnMowerKills; }
    public int getFirstWaveStartTick() { return firstWaveStartTick; }
    public int getLastZombieKillTick() { return lastZombieKillTick; }

    public Map<String, Integer> getPlantedByPlantType() {
        return Collections.unmodifiableMap(plantedByPlantType);
    }

    public Map<PlantCategory, Integer> getPlantedByPlantCategory() {
        return Collections.unmodifiableMap(plantedByPlantCategory);
    }

    public Map<PlantTag, Integer> getPlantedByPlantTag() {
        return Collections.unmodifiableMap(plantedByPlantTag);
    }

    public Map<String, Integer> getPlantPlacementsByCell() {
        return Collections.unmodifiableMap(plantPlacementsByCell);
    }

    public Map<String, Integer> getLostByPlantType() {
        return Collections.unmodifiableMap(lostByPlantType);
    }

    public Map<String, Integer> getKillsByPlantType() {
        return Collections.unmodifiableMap(killsByPlantType);
    }

    public Map<PlantCategory, Integer> getKillsByPlantCategory() {
        return Collections.unmodifiableMap(killsByPlantCategory);
    }

    public Map<PlantTag, Integer> getKillsByPlantTag() {
        return Collections.unmodifiableMap(killsByPlantTag);
    }

    public Map<String, Integer> getKillsByZombieType() {
        return Collections.unmodifiableMap(killsByZombieType);
    }

    public List<Integer> getZombieKillTicks() {
        return Collections.unmodifiableList(zombieKillTicks);
    }

    public Map<String, Integer> getZombieKillsByCell() {
        return Collections.unmodifiableMap(zombieKillsByCell);
    }

    private String cellKey(int x, int y) {
        return x + "," + y;
    }
}
