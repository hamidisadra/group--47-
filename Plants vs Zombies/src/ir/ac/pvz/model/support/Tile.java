package ir.ac.pvz.model.support;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.PlantTag;
import ir.ac.pvz.model.enums.TileType;
import ir.ac.pvz.model.plants.ExplodeONut;
import java.util.ArrayList;
import java.util.List;

public class Tile {
    public GridPosition position;
    public TileType type;
    public TileType nativeGroundType;
    public boolean canPlant;
    public boolean canStackPlants;
    public int slipDeltaRow;
    public boolean isWater;
    public boolean isLowTideSpawn;
    public TileObstacle obstacle;
    private final List<Plant> plants;
    private final List<Zombie> zombies;
    public Tile(GridPosition position, TileType type, boolean canPlant) {
        this.position = position;
        this.type = type;
        this.nativeGroundType = type;
        this.canPlant = canPlant;
        this.canStackPlants = false;
        this.slipDeltaRow = slipDirection(type);
        this.isWater = type == TileType.WATER;
        this.isLowTideSpawn = type == TileType.LOW_TIDE || type == TileType.NECROMANCY;
        this.obstacle = null;
        this.plants = new ArrayList<>();
        this.zombies = new ArrayList<>();
    }
    public boolean addPlant(Plant plant) {
        if (plant == null || !plant.canPlantOn(this)) {
            return false;
        }
        if (!plants.isEmpty() && !canStackPlants && !plant.canStack
                && !plants.get(plants.size() - 1).canStack) {
            return false;
        }
        plants.add(plant);
        plant.location = new GridPosition(position.x, position.y);
        plant.positionX = position.x;
        plant.positionY = position.y;
        if (plant.plantTags.contains(PlantTag.STACK)) {
            canStackPlants = true;
        }
        return true;
    }
    public Plant removePlant() {
        if (plants.isEmpty()) {
            return null;
        }
        Plant removed = plants.remove(plants.size() - 1);
        canStackPlants = plants.stream().anyMatch(plant -> plant.canStack);
        return removed;
    }
    public void addZombie(Zombie zombie) {
        if (zombie != null && !zombies.contains(zombie)) {
            zombies.add(zombie);
            zombie.positionY = position.y;
            zombie.lane = position.y;
        }
    }
    public void removeZombie(Zombie zombie) {
        zombies.remove(zombie);
    }
    public boolean hasObstacle() {
        return obstacle != null && obstacle.isAlive;
    }
    public String getStatus() {
        StringBuilder builder = new StringBuilder();
        builder.append("position: ").append(position.toUserString())
                .append(System.lineSeparator());
        builder.append("terrain: ").append(type)
                .append(", canPlant: ").append(canPlant)
                .append(", water: ").append(isWater)
                .append(System.lineSeparator());
        for (Plant plant : plants) {
            builder.append("plant: ").append(plant.type)
                    .append(", category: ").append(plant.category)
                    .append(", health: ").append(plant.currentHp)
                    .append('/').append(plant.baseHp)
                    .append(", level: ").append(plant.level)
                    .append(", tags: ").append(plant.plantTags);
            if (plant.isPermanentlyFrozen()) {
                builder.append(", frozen: true");
            }
            if (plant.isCatTransformed) {
                builder.append(", cat-transformed: true");
            }
            if (plant.isOctopusBlocked) {
                builder.append(", octopus-blocked: true");
            }
            if (plant instanceof ExplodeONut) {
                builder.append(", metal-armor: ")
                        .append(((ExplodeONut) plant).metalArmorHealth);
            }
            builder.append(System.lineSeparator());
        }
        for (Zombie zombie : zombies) {
            builder.append("zombie: ")
                    .append(zombie.getType())
                    .append(", health: ").append(zombie.currentHealth)
                    .append(", armor: ").append(zombie.getRemainingArmorHealth())
                    .append(", effects: ").append(zombie.effects)
                    .append(System.lineSeparator());
        }
        if (obstacle != null) {
            builder.append("obstacle: ")
                    .append(obstacle.getClass().getSimpleName())
                    .append(", health: ").append(obstacle.health)
                    .append(System.lineSeparator());
        }
        return builder.toString();
    }
    public boolean hasAdjacentFirePlant(Board board) {
        for (int deltaY = -2; deltaY <= 2; deltaY++) {
            for (int deltaX = -2; deltaX <= 2; deltaX++) {
                if (deltaX == 0 && deltaY == 0) {
                    continue;
                }
                Tile adjacent = board.getTile(new GridPosition(
                        position.x + deltaX, position.y + deltaY));
                if (adjacent != null && containsWarmingPlant(adjacent,
                        Math.max(Math.abs(deltaX), Math.abs(deltaY)))) {
                    return true;
                }
            }
        }
        return false;
    }
    private boolean containsWarmingPlant(Tile tile, int distance) {
        for (Plant plant : tile.plants) {
            if (!plant.plantTags.contains(PlantTag.FIRE)) {
                continue;
            }
            if (distance <= 1 || plant.getNormalizedType().equals("pepperpult")
                    && plant.level >= 3) {
                return true;
            }
        }
        return false;
    }
    public void moveZombieBySlip(Zombie zombie) {
        if (zombie == null || slipDeltaRow == 0) {
            return;
        }
        zombie.lane += slipDeltaRow;
        zombie.positionY = zombie.lane;
        zombie.currentPosition.y = zombie.lane;
    }
    public void setNativeGroundType(TileType nativeGroundType) {
        if (nativeGroundType != null) {
            this.nativeGroundType = nativeGroundType;
        }
    }
    public void restoreNativeGround() {
        type = nativeGroundType;
        slipDeltaRow = slipDirection(type);
        isWater = type == TileType.WATER;
        isLowTideSpawn = type == TileType.LOW_TIDE
                || type == TileType.NECROMANCY;
        canPlant = type != TileType.SLIPPERY_UP
                && type != TileType.SLIPPERY_DOWN;
    }
    public void addFrozenPlant(Plant plant) {
        if (plant == null || plants.contains(plant)) {
            return;
        }
        plants.add(plant);
        plant.location = new GridPosition(position.x, position.y);
        plant.positionX = position.x;
        plant.positionY = position.y;
    }
    public void addFrozenZombie(Zombie zombie) {
        addZombie(zombie);
        if (zombie != null) {
            zombie.currentPosition = new ContinuousPosition(position.x, position.y);
            zombie.positionX = position.x;
        }
    }
    public GridPosition getPosition() {
        return position;
    }
    public TileType getType() {
        return type;
    }
    public Plant getPlant() {
        if (plants.isEmpty()) {
            return null;
        }
        return plants.get(plants.size() - 1);
    }

    private static int slipDirection(TileType type) {
        if (type == TileType.SLIPPERY_UP) {
            return -1;
        }
        if (type == TileType.SLIPPERY_DOWN) {
            return 1;
        }
        return 0;
    }
    public boolean hasLilyPad() {
        return plants.stream().anyMatch(plant ->
                plant.getNormalizedType().equals("lilypad"));
    }
    public Plant getLilyPad() {
        return plants.stream().filter(plant ->
                plant.getNormalizedType().equals("lilypad")).findFirst().orElse(null);
    }
    public List<Plant> getPlants() {
        return plants;
    }
    public List<Zombie> getZombies() {
        return zombies;
    }
}
