package com.pvz.model.support;

import com.pvz.game.GameSession;
import com.pvz.model.core.Plant;
import com.pvz.model.core.Zombie;

public class ZombieAbility {
    public String name;
    public String description;
    public float cooldownSeconds;
    public float elapsedSeconds;
    public ZombieAbility(String name, String description, float cooldownSeconds) {
        this.name = name;
        this.description = description;
        this.cooldownSeconds = cooldownSeconds;
        this.elapsedSeconds = 0f;
    }
    public void onTick(Zombie zombie, GameSession session, float elapsed) {
        elapsedSeconds += Math.max(0f, elapsed);
    }
    public boolean isReady() {
        return cooldownSeconds <= 0f || elapsedSeconds >= cooldownSeconds;
    }
    public void resetCooldown() {
        elapsedSeconds = 0f;
    }
    public boolean onPlantContact(Zombie zombie, Plant plant,
                                  GameSession session) {
        return false;
    }

    public void onProjectileReceived(Zombie zombie, Projectile projectile,
                                     GameSession session) {
    }
    public boolean blocksProjectile(Zombie zombie, Projectile projectile) {
        return false;
    }
    public boolean blocksFreeze(Zombie zombie) {
        return false;
    }
    public void onDamaged(Zombie zombie, int armorPiecesBefore,
                          int armorPiecesAfter) {
    }
    public boolean blocksDamage(Zombie zombie, int amount) {
        return false;
    }
    public void onDeath(Zombie zombie, GameSession session) {
    }

    public void execute(Zombie zombie, GameSession session) {
    }
}
