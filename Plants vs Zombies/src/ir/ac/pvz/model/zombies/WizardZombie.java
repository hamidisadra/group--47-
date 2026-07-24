package ir.ac.pvz.model.zombies;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.CatCurse;
import ir.ac.pvz.model.support.TransformPlantAbility;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WizardZombie extends Zombie {
    public float spellCooldownSeconds;
    public CatCurse catCurse;
    private final Random random;
    private final TransformPlantAbility transformAbility;
    public WizardZombie() {
        this(new Random());
    }
    public WizardZombie(Random random) {
        super("WizardZombie");
        if (random == null) {
            this.random = new Random();
        } else {
            this.random = random;
        }
        this.spellCooldownSeconds = randomBetween(7f, 9f);
        this.catCurse = null;
        this.transformAbility = new TransformPlantAbility(
                spellCooldownSeconds, this.random);
        this.abilities.add(transformAbility);
    }
    public boolean transformRandomPlantToCat(Board board) {
        if (board == null) {
            return false;
        }
        List<Plant> plants = new ArrayList<>();
        for (Plant plant : board.getAllPlants()) {
            if (plant.isAlive && !plant.isCatTransformed) {
                plants.add(plant);
            }
        }
        if (plants.isEmpty()) {
            return false;
        }
        getCatCurse().transform(plants.get(random.nextInt(plants.size())));
        return true;
    }
    public void scheduleNextSpell() {
        spellCooldownSeconds = randomBetween(11f, 14f);
    }
    @Override
    public void onReachPlant(Plant plant) {
        transformAbility.onPlantContact(this, plant, null);
    }
    @Override
    public void onDeath() {
        super.onDeath();
    }
    private float randomBetween(float minimum, float maximum) {
        return minimum + random.nextFloat() * (maximum - minimum);
    }
    private CatCurse getCatCurse() {
        if (catCurse == null) {
            catCurse = new CatCurse(this);
        }
        return catCurse;
    }
}
