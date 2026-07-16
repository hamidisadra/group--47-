package com.pvz.model.support;

import com.pvz.model.core.GameObject;
import com.pvz.model.core.Zombie;

public class ArcadeMachine {

    public int health;
    public ContinuousPosition position;

    public ArcadeMachine(int health, ContinuousPosition position) {
        this.health = health;
        this.position = position;
    }

    public void push(float deltaX) {
        position.x -= deltaX;
    }

    public void instantKill(GameObject target) {
        if (target == null || !target.isAlive) {
            return;
        }
        if (target instanceof Zombie) {
            ((Zombie) target).forceDie();
        } else {
            target.die();
        }
    }
}
