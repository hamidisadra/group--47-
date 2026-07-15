package ir.ac.pvz.model.minigame;

import java.util.ArrayList;
import java.util.List;

public class Zombotany extends MiniGame {
    private List<PlantZombieType> availablePlantZombies;

    public Zombotany(int stageNumber) {
        super("Zombotany", stageNumber);
        this.availablePlantZombies = new ArrayList<>();
        availablePlantZombies.add(PlantZombieType.PEASHOOTER_ZOMBIE);
        availablePlantZombies.add(PlantZombieType.WALLNUT_ZOMBIE);
        availablePlantZombies.add(PlantZombieType.JALAPENO_ZOMBIE);
        availablePlantZombies.add(PlantZombieType.SQUASH_ZOMBIE);
    }

    public List<PlantZombieType> getAvailablePlantZombies() {
        return availablePlantZombies;
    }

    public void spawnPlantZombie(PlantZombieType type, int row) {
        switch (type) {
            case PEASHOOTER_ZOMBIE:
                System.out.println("A peashooter zombie spawns in row " + row + " and shoots peas at plants ahead.");
                break;
            case WALLNUT_ZOMBIE:
                System.out.println("A wall-nut zombie spawns in row " + row + ". It is slow but very tanky.");
                break;
            case JALAPENO_ZOMBIE:
                System.out.println("A jalapeno zombie spawns in row " + row + ". It will explode after 10 seconds if it does not reach the house.");
                break;
            case SQUASH_ZOMBIE:
                System.out.println("A squash zombie spawns in row " + row + ". It moves fast and squashes the first plant it reaches.");
                break;
            default:
                break;
        }
    }

    public boolean checkJalapenoExplodes(int secondsInLawn) {
        return secondsInLawn >= 10;
    }
}
