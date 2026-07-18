package ir.ac.pvz.model.support;

import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.core.Zombie;

public class ZombieAbility {

    public String name;
    public String description;
    public float cooldownSeconds;

    public ZombieAbility(String name, String description, float cooldownSeconds) {
        this.name = name;
        this.description = description;
        this.cooldownSeconds = cooldownSeconds;
    }

    public void execute(Zombie zombie, GameSession session) {
        zombie.specialBehavior();
    }
}
