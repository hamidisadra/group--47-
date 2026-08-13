package ir.ac.pvz.model.zombies;

import ir.ac.pvz.model.core.GameObject;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.support.ArcadeMachine;
import ir.ac.pvz.model.support.ContinuousPosition;
import ir.ac.pvz.model.support.ZombieDataRepository;

public class ArcadeZombie extends Zombie {
    public ArcadeMachine arcadeMachine;
    public ArcadeZombie() {
        super("ArcadeZombie");
        int machineHealth = (int) Math.round(
                ZombieDataRepository.getInstance().getNumber(
                        "ArcadeZombie", "ArcadeMachineHealth", 0d));
        if (machineHealth <= 0) {
            throw new IllegalStateException(
                    "ArcadeMachineHealth must be positive for ArcadeZombie");
        }
        this.arcadeMachine = new ArcadeMachine(machineHealth,
                new ContinuousPosition(currentPosition.x, currentPosition.y));
    }
    @Override
    public void move(float deltaX) {
        super.move(deltaX);
        arcadeMachine.push(deltaX * speed);
    }
    public void collideWith(GameObject target) {
        arcadeMachine.instantKill(target);
    }
}
