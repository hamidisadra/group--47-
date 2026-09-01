package ir.ac.pvz.model.user;

import ir.ac.pvz.model.support.PlantDataRepository;
import ir.ac.pvz.model.support.PlantDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Collection {
    private Map<String, Integer> plantLevels = new HashMap<>();
    private List<String> unlockedPlants;
    private List<String> seenZombies;
    private List<String> selectedPlants;
    private List<String> boostedPlants;
    private List<String> earnedBoostedPlants;
    private int capacity;

    public Collection() {
        unlockedPlants = new ArrayList<>();
        seenZombies = new ArrayList<>();
        selectedPlants = new ArrayList<>();
        boostedPlants = new ArrayList<>();
        earnedBoostedPlants = new ArrayList<>();
        capacity = 8;
    }

    public List<String> getUnlockedPlants() {
        ensureLists();
        return Collections.unmodifiableList(unlockedPlants);
    }

    public List<String> getSeenZombies() {
        ensureLists();
        return Collections.unmodifiableList(seenZombies);
    }

    public List<String> getSelectedPlants() {
        ensureLists();
        return Collections.unmodifiableList(selectedPlants);
    }

    public List<String> getBoostedPlants() {
        ensureLists();
        return Collections.unmodifiableList(boostedPlants);
    }

    public List<String> getEarnedBoostedPlants() {
        ensureLists();
        return Collections.unmodifiableList(earnedBoostedPlants);
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("Selection capacity must be positive.");
        }
        this.capacity = capacity;
    }

    public void addSeenZombies(String zombieName) {
        ensureLists();
        if (zombieName != null && !zombieName.isBlank()
                && !containsNormalized(seenZombies, zombieName)) {
            seenZombies.add(zombieName.trim());
        }
    }

    public CollectionStatus unlockPlant(String plantName) {
        ensureLists();
        String canonical = canonicalPlantName(plantName);
        if (canonical == null) {
            return CollectionStatus.PLANT_NOT_FOUND;
        }
        if (containsNormalized(unlockedPlants, canonical)) {
            return CollectionStatus.PLANT_ALREADY_UNLOCKED;
        }
        unlockedPlants.add(canonical);
        return CollectionStatus.SUCCESS;
    }

    public CollectionStatus selectPlant(String plantName) {
        ensureLists();
        String canonical = canonicalPlantName(plantName);
        if (canonical == null) {
            return CollectionStatus.PLANT_NOT_FOUND;
        }
        if (!containsNormalized(unlockedPlants, canonical)) {
            return CollectionStatus.PLANT_NOT_UNLOCKED;
        }
        if (containsNormalized(selectedPlants, canonical)) {
            return CollectionStatus.PLANT_ALREADY_SELECTED;
        }
        if (selectedPlants.size() >= capacity) {
            return CollectionStatus.CAPACITY_IS_FULL;
        }
        selectedPlants.add(canonical);
        return CollectionStatus.SUCCESS;
    }

    public CollectionStatus removePlant(String plantName) {
        ensureLists();
        int index = indexOfNormalized(selectedPlants, plantName);
        if (index < 0) {
            return CollectionStatus.PLANT_NOT_SELECTED;
        }
        selectedPlants.remove(index);
        return CollectionStatus.SUCCESS;
    }

    public CollectionStatus boostPlant(String plantName) {
        ensureLists();
        String canonical = canonicalPlantName(plantName);
        if (canonical == null) {
            return CollectionStatus.PLANT_NOT_FOUND;
        }
        if (!containsNormalized(unlockedPlants, canonical)) {
            return CollectionStatus.PLANT_NOT_UNLOCKED;
        }
        if (containsNormalized(boostedPlants, canonical)) {
            return CollectionStatus.PLANT_ALREADY_BOOSTED;
        }
        boostedPlants.add(canonical);
        return CollectionStatus.SUCCESS;
    }

    public CollectionStatus removeBoost(String plantName) {
        ensureLists();
        int index = indexOfNormalized(boostedPlants, plantName);
        if (index < 0) {
            return CollectionStatus.PLANT_NOT_SELECTED;
        }
        boostedPlants.remove(index);
        return CollectionStatus.SUCCESS;
    }

    public CollectionStatus earnPlantBoost(String plantName) {
        ensureLists();
        String canonical = canonicalPlantName(plantName);
        if (canonical == null) {
            return CollectionStatus.PLANT_NOT_FOUND;
        }
        if (!containsNormalized(unlockedPlants, canonical)) {
            return CollectionStatus.PLANT_NOT_UNLOCKED;
        }
        if (containsNormalized(earnedBoostedPlants, canonical)) {
            return CollectionStatus.PLANT_ALREADY_BOOSTED;
        }
        earnedBoostedPlants.add(canonical);
        return CollectionStatus.SUCCESS;
    }

    public List<String> getEffectiveBoostedPlants() {
        ensureLists();
        List<String> result = new ArrayList<>();
        for (String plant : boostedPlants) {
            if (!containsNormalized(result, plant)) {
                result.add(plant);
            }
        }
        for (String plant : earnedBoostedPlants) {
            if (!containsNormalized(result, plant)) {
                result.add(plant);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public void consumeEarnedBoosts(List<String> usedPlantTypes) {
        ensureLists();
        if (usedPlantTypes == null) {
            return;
        }
        for (String plantType : usedPlantTypes) {
            int index = indexOfNormalized(earnedBoostedPlants, plantType);
            if (index >= 0) {
                earnedBoostedPlants.remove(index);
            }
        }
    }

    public void clearSelection() {
        ensureLists();
        selectedPlants.clear();
        boostedPlants.clear();
    }

    public int getPlantLevel(String plantName) {
        ensureLevels();
        String canonical = canonicalPlantName(plantName);
        if (canonical == null) {
            return 1;
        }
        Integer level = findLevel(canonical);
        return level == null ? 1 : level;
    }

    public void setPlantLevel(String plantName, int level) {
        ensureLevels();
        String canonical = canonicalPlantName(plantName);
        if (canonical == null) {
            throw new IllegalArgumentException("Unknown plant type.");
        }
        if (level < 1 || level > 4) {
            throw new IllegalArgumentException("Plant level must be between 1 and 4.");
        }
        removeLevelAliases(canonical);
        plantLevels.put(canonical, level);
    }

    public String getCanonicalUnlockedPlant(String plantName) {
        ensureLists();
        int index = indexOfNormalized(unlockedPlants, plantName);
        return index < 0 ? null : unlockedPlants.get(index);
    }

    public String getCanonicalSelectedPlant(String plantName) {
        ensureLists();
        int index = indexOfNormalized(selectedPlants, plantName);
        return index < 0 ? null : selectedPlants.get(index);
    }

    private Integer findLevel(String canonical) {
        for (Map.Entry<String, Integer> entry : plantLevels.entrySet()) {
            if (normalize(entry.getKey()).equals(normalize(canonical))) {
                return entry.getValue();
            }
        }
        return null;
    }

    private void removeLevelAliases(String canonical) {
        plantLevels.keySet().removeIf(key -> normalize(key).equals(normalize(canonical)));
    }

    private String canonicalPlantName(String plantName) {
        if (plantName == null || plantName.isBlank()) {
            return null;
        }
        PlantDefinition definition = PlantDataRepository.getInstance().get(plantName.trim());
        return definition == null ? null : definition.name;
    }

    private boolean containsNormalized(List<String> values, String value) {
        return indexOfNormalized(values, value) >= 0;
    }

    private int indexOfNormalized(List<String> values, String value) {
        String normalized = normalize(value);
        if (normalized.isEmpty()) {
            return -1;
        }
        for (int index = 0; index < values.size(); index++) {
            if (normalize(values.get(index)).equals(normalized)) {
                return index;
            }
        }
        return -1;
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

    private void ensureLists() {
        if (unlockedPlants == null) {
            unlockedPlants = new ArrayList<>();
        }
        if (seenZombies == null) {
            seenZombies = new ArrayList<>();
        }
        if (selectedPlants == null) {
            selectedPlants = new ArrayList<>();
        }
        if (boostedPlants == null) {
            boostedPlants = new ArrayList<>();
        }
        if (earnedBoostedPlants == null) {
            earnedBoostedPlants = new ArrayList<>();
        }
        if (capacity < 1) {
            capacity = 8;
        }
    }

    private void ensureLevels() {
        if (plantLevels == null) {
            plantLevels = new HashMap<>();
        }
    }
}
