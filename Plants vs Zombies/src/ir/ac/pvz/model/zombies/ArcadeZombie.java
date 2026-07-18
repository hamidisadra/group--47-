package ir.ac.pvz.model.zombies;

import ir.ac.pvz.model.support.ArcadeMachine;
import ir.ac.pvz.model.support.ContinuousPosition;
import ir.ac.pvz.model.core.GameObject;
import ir.ac.pvz.model.core.Zombie;

public class ArcadeZombie extends Zombie {

    public ArcadeMachine arcadeMachine;

    public ArcadeZombie() {
        super(0.19f, 1290, 100, 600);
        this.arcadeMachine = new ArcadeMachine(1290,
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
