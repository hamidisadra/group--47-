package ir.ac.pvz.model.user;

import ir.ac.pvz.model.support.PlantDataRepository;
import ir.ac.pvz.model.support.PlantDefinition;

import java.util.HashMap;
import java.util.Map;

public class Inventory {
    private Map<String, Integer> seedPackets;
    private int plantFoodCount;
    private int maxPlantFood;

    public Inventory() {
        seedPackets = new HashMap<>();
        plantFoodCount = 0;
        maxPlantFood = 3;
    }

    public void addSeedPackets(String plant, int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Seed packet count cannot be negative.");
        }
        if (count == 0) {
            return;
        }
        String canonical = canonicalPlantName(plant);
        if (canonical == null) {
            throw new IllegalArgumentException("Unknown plant type.");
        }
        int current = getSeedPacketCount(canonical);
        long result = (long) current + count;
        if (result > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Seed packet count exceeds the supported limit.");
        }
        removeAliases(canonical);
        seedPackets.put(canonical, (int) result);
    }

    public boolean useSeedPacket(String plant) {
        return useSeedPackets(plant, 1);
    }

    public boolean useSeedPackets(String plant, int count) {
        if (count <= 0) {
            return false;
        }
        String canonical = canonicalPlantName(plant);
        if (canonical == null) {
            return false;
        }
        int current = getSeedPacketCount(canonical);
        if (current < count) {
            return false;
        }
        removeAliases(canonical);
        seedPackets.put(canonical, current - count);
        return true;
    }

    public int getSeedPacketCount(String plant) {
        ensureSeedPackets();
        String normalized = normalize(plant);
        if (normalized.isEmpty()) {
            return 0;
        }
        for (Map.Entry<String, Integer> entry : seedPackets.entrySet()) {
            if (normalize(entry.getKey()).equals(normalized)) {
                return Math.max(0, entry.getValue());
            }
        }
        return 0;
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

    private String canonicalPlantName(String plant) {
        if (plant == null || plant.isBlank()) {
            return null;
        }
        PlantDefinition definition = PlantDataRepository.getInstance().get(plant);
        return definition == null ? null : definition.name;
    }

    private void removeAliases(String canonical) {
        ensureSeedPackets();
        seedPackets.keySet().removeIf(key -> normalize(key).equals(normalize(canonical)));
    }

    private void ensureSeedPackets() {
        if (seedPackets == null) {
            seedPackets = new HashMap<>();
        }
        if (maxPlantFood < 1) {
            maxPlantFood = 3;
        }
        if (plantFoodCount < 0) {
            plantFoodCount = 0;
        }
        if (plantFoodCount > maxPlantFood) {
            plantFoodCount = maxPlantFood;
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("-", "")
                .replace("_", "")
                .replace(" ", "")
                .trim()
                .toLowerCase();
    }
}
