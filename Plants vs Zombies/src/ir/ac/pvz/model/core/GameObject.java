package ir.ac.pvz.model.core;

import ir.ac.pvz.model.interfaces.IFreezable;

public abstract class GameObject implements IFreezable {
    public float positionX;
    public int positionY;
    public int health;
    public boolean isAlive;
    protected boolean frozen;
    protected int freezeTicksRemaining;
    protected GameObject(float positionX, int positionY, int health) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.health = health;
        this.isAlive = true;
        this.frozen = false;
        this.freezeTicksRemaining = 0;
    }
    public void takeDamage(int amount) {
        if (!isAlive || amount <= 0) {
            return;
        }
        health -= amount;
        if (health <= 0) {
            health = 0;
            die();
        }
    }
    public void die() {
        isAlive = false;
    }
    public void update(int tickCount) {
        if (!frozen) {
            return;
        }
        freezeTicksRemaining -= tickCount;
        if (freezeTicksRemaining <= 0) {
            melt();
        }
    }
    @Override
    public void freeze(int duration) {
        frozen = true;
        freezeTicksRemaining = duration;
    }
    @Override
    public void melt() {
        frozen = false;
        freezeTicksRemaining = 0;
    }
    public float getPositionX() {
        return positionX;
    }
    public void setPositionX(float positionX) {
        this.positionX = positionX;
    }
    public int getPositionY() {
        return positionY;
    }
    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }
    public int getHealth() {
        return health;
    }
    public boolean isAlive() {
        return isAlive;
    }
    public boolean isFrozen() {
        return frozen;
    }
}
