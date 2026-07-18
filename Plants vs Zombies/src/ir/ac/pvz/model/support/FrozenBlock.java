package ir.ac.pvz.model.support;


import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;

public class FrozenBlock extends TileObstacle {

    public int initialHealth;
    public int meltRatePerSecond;
    public boolean containsPlantOrZombie;
    public int adjacentFireMeltRatePerSecond;
    public boolean blocksPlantsAndZombiesUntilBroken;

    private Plant containedPlant;
    private Zombie containedZombie;

    public FrozenBlock() {
        super(600, true, false);
        this.initialHealth = 600;
        this.meltRatePerSecond = 60;
        this.containsPlantOrZombie = false;
        this.adjacentFireMeltRatePerSecond = 60;
        this.blocksPlantsAndZombiesUntilBroken = true;
    }

    public FrozenBlock(Plant plant) {
        this();
        this.containedPlant = plant;
        this.containsPlantOrZombie = plant != null;
        if (plant != null) {
            plant.freeze(Integer.MAX_VALUE);
        }
    }


    public FrozenBlock(Plant plant, int health) {
        this(plant);
        this.health = Math.max(1, health);
        this.initialHealth = this.health;
    }

    public FrozenBlock(Zombie zombie) {
        this();
        this.containedZombie = zombie;
        this.containsPlantOrZombie = zombie != null;
        if (zombie != null) {
            zombie.freeze(Integer.MAX_VALUE);
        }
    }

    public void meltByFire() {
        takeDamage(meltRatePerSecond);
    }

    public void takeProjectileDamage(int amount) {
        takeDamage(amount);
    }

    @Override
    public void destroy() {
        super.destroy();
        releaseContent();
    }

    public void releaseContent() {
        if (containedPlant != null) {
            containedPlant.releaseFromIce();
        }
        if (containedZombie != null) {
            containedZombie.melt();
        }
        containsPlantOrZombie = false;
        blocksPlantsAndZombiesUntilBroken = false;
    }
}
