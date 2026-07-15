package ir.ac.pvz.model.board;

public class GraveTile extends Tile {
    private int health;
    private boolean hasSun;
    private boolean hasPlantFood;
    private boolean necromancy;

    public GraveTile(Position position, int health) {
        super(position);
        this.health = health;
        this.plantable = false;
    }

    public int getHealth() {
        return health;
    }

    public boolean hasSun() {
        return hasSun;
    }

    public void setHasSun(boolean hasSun) {
        this.hasSun = hasSun;
    }

    public boolean hasPlantFood() {
        return hasPlantFood;
    }

    public void setHasPlantFood(boolean hasPlantFood) {
        this.hasPlantFood = hasPlantFood;
    }

    public boolean isNecromancy() {
        return necromancy;
    }

    public void setNecromancy(boolean necromancy) {
        this.necromancy = necromancy;
    }

    public void takeDamage(int amount) {
        health -= amount;
        if (health < 0) {
            health = 0;
        }
        if (isDestroyed()) {
            plantable = true;
        }
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

    @Override
    public boolean canPlant() {
        return isDestroyed() && plant == null;
    }

    public String spawnZombieUnderneath() {
        if (!necromancy) {
            return null;
        }
        return "regular";
    }
}
