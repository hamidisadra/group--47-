package ir.ac.pvz.model.user;

import java.util.ArrayList;
import java.util.List;

public class Collection {
    private List<String> unlockedPlants;
    private List<String> seenZombies;

    private List<String> selectedPlants;
    private List<String> boostedPlants;
    private int capacity;

    public Collection() {
        unlockedPlants = new ArrayList<>();
        seenZombies = new ArrayList<>();
        selectedPlants = new ArrayList<>();
        boostedPlants = new ArrayList<>();
        this.capacity = 8;
    }


    //Getters


    public List<String> getUnlockedPlants() {
        return unlockedPlants;
    }

    public List<String> getSeenZombies() {
        return seenZombies;
    }

    public List<String> getSelectedPlants() {
        return selectedPlants;
    }

    public List<String> getBoostedPlants() {
        return boostedPlants;
    }

    public int getCapacity() {
        return capacity;
    }

    //Setters

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void addSeenZombies(String zombieName) {
        if (!seenZombies.contains(zombieName)) {
            seenZombies.add(zombieName);
        }
    }

    public CollectionStatus unlockPlant(String plantName) {
        if (unlockedPlants.contains(plantName)) {
            return CollectionStatus.PLANT_ALREADY_UNLOCKED;
        }
        unlockedPlants.add(plantName);
        return CollectionStatus.SUCCESS;
    }

    public CollectionStatus selectPlant(String plantName) {
        if (!unlockedPlants.contains(plantName)) return CollectionStatus.PLANT_NOT_UNLOCKED;
        if (selectedPlants.contains(plantName)) return CollectionStatus.PLANT_ALREADY_SELECTED;
        if (selectedPlants.size() >= this.capacity) return CollectionStatus.CAPACITY_IS_FULL;

        selectedPlants.add(plantName);
        return CollectionStatus.SUCCESS;
    }

    public CollectionStatus removePlant(String plantName) {
        if (!selectedPlants.contains(plantName)) return CollectionStatus.PLANT_NOT_SELECTED;

        selectedPlants.remove(plantName);
        return CollectionStatus.SUCCESS;
    }

    public CollectionStatus boostPlant(String plantName) {
        if (!unlockedPlants.contains(plantName)) return CollectionStatus.PLANT_NOT_UNLOCKED;
        if (boostedPlants.contains(plantName)) return CollectionStatus.PLANT_ALREADY_BOOSTED;

        boostedPlants.add(plantName);
        return CollectionStatus.SUCCESS;
    }

    public void clearSelection() {
        selectedPlants.clear();
        boostedPlants.clear();
    }


}
