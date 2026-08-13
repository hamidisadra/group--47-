package ir.ac.pvz.model.zombies;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.support.TransformPlantAbility;
import java.util.Random;

public class WizardZombie extends Zombie {
    public float spellCooldownSeconds;
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
        this.transformAbility = new TransformPlantAbility(
                spellCooldownSeconds, this.random);
        this.abilities.add(transformAbility);
    }
    @Override
    public void onReachPlant(Plant plant) {
        transformAbility.onPlantContact(this, plant, null);
    }
    private float randomBetween(float minimum, float maximum) {
        return minimum + random.nextFloat() * (maximum - minimum);
    }
}
