package ir.ac.pvz.model.user;

import java.util.HashMap;
import java.util.Map;

public class Inventory {
    private Map<String, Integer> seedPackets;
    private int plantFoodCount;
    private int maxPlantFood;

    public Inventory() {
        this.seedPackets = new HashMap<>();
        this.plantFoodCount = 0;
        this.maxPlantFood = 3;
    }

    public void addSeedPackets(String plant, int count) {
        seedPackets.put(plant, getSeedPacketCount(plant) + count);
    }

    public boolean useSeedPacket(String plant) {
        int count = getSeedPacketCount(plant);
        if (count <= 0) {
            return false;
        }
        seedPackets.put(plant, count - 1);
        return true;
    }

    public int getSeedPacketCount(String plant) {
        return seedPackets.getOrDefault(plant, 0);
    }

    public int getPlantFoodCount() {
        return plantFoodCount;
    }

    public boolean addPlantFood() {
        if (plantFoodCount >= maxPlantFood) {
            return false;
        }
        plantFoodCount++;
        return true;
    }

    public boolean usePlantFood() {
        if (plantFoodCount <= 0) {
            return false;
        }
        plantFoodCount--;
        return true;
    }
}
