package com.pvz.model.zombies;

import com.pvz.model.core.GameObject;
import com.pvz.model.core.Zombie;
import com.pvz.model.support.ArcadeMachine;
import com.pvz.model.support.ContinuousPosition;

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
