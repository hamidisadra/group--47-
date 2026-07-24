package com.pvz.model.core;

import com.pvz.game.LootDropService;
import com.pvz.model.armor.ArmorDecorator;
import com.pvz.model.enums.DamageMode;
import com.pvz.model.enums.LootType;
import com.pvz.model.enums.ProjectileTrajectory;
import com.pvz.model.enums.ProjectileType;
import com.pvz.model.enums.ZombieEffectType;
import com.pvz.model.interfaces.IMovable;
import com.pvz.model.interfaces.IWall;
import com.pvz.model.support.ArmorPiece;
import com.pvz.model.support.ContinuousPosition;
import com.pvz.model.support.Projectile;
import com.pvz.model.support.ZombieAbility;
import com.pvz.model.support.EatingAttackStrategy;
import com.pvz.model.support.WalkingMovementStrategy;
import com.pvz.model.support.ZombieAttackStrategy;
import com.pvz.model.support.ZombieDeathEvent;
import com.pvz.model.support.ZombieDeathListener;
import com.pvz.model.support.ZombieMovementStrategy;
import com.pvz.model.support.ZombieEffect;
import com.pvz.model.support.ZombieBaseStats;
import com.pvz.model.support.ZombieDataRepository;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class Zombie extends GameObject implements IMovable {
    public float speed;
    public int attackDamage;
    public ArmorDecorator armor;
    public int waveCost;
    public boolean isHypnotized;
    public ContinuousPosition currentPosition;
    public int lane;
    public int currentHealth;
    public int damageToPlant;
    public List<ZombieAbility> abilities;
    public List<ZombieEffect> effects;
    public boolean isGlowing;
    public boolean isBoss;
    public int initialWaveCost;
    public List<ArmorPiece> armorPieces;
    public Plant lastDamageSource;
    public int selectionWeight;
    public boolean canSpawnPlantFood;
    public String type;
    public String displayName;
    public ZombieMovementStrategy movementStrategy;
    public ZombieAttackStrategy attackStrategy;
    public Projectile incomingProjectile;
    private float chillSlowFactor;
    private float attackProgress;
    private int poisonDamagePerSecond;
    private float poisonDamageAccumulator;
    private boolean deathEventPublished;
    private final List<ZombieDeathListener> deathListeners;
    protected Zombie(float speed, int health, int attackDamage, int waveCost) {
        super(8f, 0, health);
        this.speed = speed;
        this.attackDamage = attackDamage;
        this.armor = null;
        this.waveCost = waveCost;
        this.isHypnotized = false;
        this.currentPosition = new ContinuousPosition(8f, 0);
        this.lane = 0;
        this.currentHealth = health;
        this.damageToPlant = attackDamage / 10;
        this.abilities = new ArrayList<>();
        this.effects = new ArrayList<>();
        this.isGlowing = Math.random() < 0.05;
        this.isBoss = false;
        this.initialWaveCost = waveCost;
        this.armorPieces = new ArrayList<>();
        this.lastDamageSource = null;
        this.selectionWeight = 0;
        this.canSpawnPlantFood = true;
        this.type = "Zombie";
        this.displayName = this.type;
        this.movementStrategy = new WalkingMovementStrategy();
        this.attackStrategy = new EatingAttackStrategy();
        this.incomingProjectile = null;
        this.chillSlowFactor = 1f;
        this.attackProgress = 0f;
        this.poisonDamagePerSecond = 0;
        this.poisonDamageAccumulator = 0f;
        this.deathEventPublished = false;
        this.deathListeners = new ArrayList<>();
        this.deathListeners.add(event -> System.out.println(
                "Zombie of type " + event.type + " is dead at ("
                        + (event.position.x + 1f) + ", "
                        + (event.position.y + 1) + ")"));
    }
    protected Zombie(String zombieType) {
        this(ZombieBaseStats.fromRepository(zombieType));
        ZombieDataRepository.getInstance().applyTo(this, zombieType);
        setIdentity(zombieType, zombieType);
    }
    private Zombie(ZombieBaseStats stats) {
        this(stats.speed, stats.health, stats.eatDamagePerSecond,
                stats.waveCost);
    }
    protected double requiredDataNumber(String key) {
        com.pvz.model.support.ZombieDefinition definition =
                ZombieDataRepository.getInstance().getByZombieType(type);
        if (definition == null
                || !definition.numericProperties.containsKey(key)) {
            throw new IllegalStateException(
                    "Missing zombie value " + key + " for " + type);
        }
        return definition.numericProperties.get(key);
    }
    public void applyBaseData(float movementSpeed, int baseHealth,
                              int eatDamagePerSecond, int cost,
                              int weight, boolean plantFoodEligible) {
        speed = movementSpeed;
        health = baseHealth;
        currentHealth = baseHealth;
        attackDamage = eatDamagePerSecond;
        damageToPlant = eatDamagePerSecond / 10;
        waveCost = cost;
        initialWaveCost = cost;
        selectionWeight = weight;
        canSpawnPlantFood = plantFoodEligible;
        isGlowing = Math.random() < 0.05;
        isAlive = true;
        deathEventPublished = false;
    }
    @Override
    public void move(float deltaX) {
        if (frozen || isStunned() || !isAlive) {
            return;
        }
        movementStrategy.move(this, deltaX);
    }
    public void onReachPlant(Plant plant) {
        attackPlant(plant);
    }
    public void onDeath() {
        publishDeathEvent();
    }
    public void specialBehavior() {
    }
    public void receiveProjectile(Projectile projectile) {
        if (projectile == null || !isAlive) {
            return;
        }
        for (ZombieAbility ability : new ArrayList<>(abilities)) {
            if (ability.blocksProjectile(this, projectile)) {
                return;
            }
        }
        for (ZombieAbility ability : new ArrayList<>(abilities)) {
            ability.onProjectileReceived(this, projectile, null);
        }
        incomingProjectile = projectile;
        try {
            if (projectile.type == ProjectileType.FIRE) {
                melt();
                clearChill();
                effects.removeIf(effect -> effect.type == ZombieEffectType.FROZEN
                        || effect.type == ZombieEffectType.CHILLED);
            }
            if (projectile.damageMode == DamageMode.INSTANT_KILL) {
                if (!blocksAbilityDamage(projectile.damageAmount)) {
                    die();
                }
            }
            else if (projectile.type == ProjectileType.POISON
                    || projectile.damageMode == DamageMode.IGNORE_ARMOR) {
                if (!blocksAbilityDamage(projectile.damageAmount)) {
                    super.takeDamage(projectile.damageAmount);
                    currentHealth = health;
                }
            }
            else {
                takeDamage(projectile.damageAmount);
            }
            if (projectile.type == ProjectileType.ICE) {
                chill(0.5f, 3f);
            }
        } finally {
            incomingProjectile = null;
        }
    }
    public void attackPlant(Plant plant) {
        attackStrategy.attack(this, plant);
    }
    public LootType dropLoot() {
        return new LootDropService().rollLoot(this);
    }
    public boolean isDead() {
        return !isAlive || currentHealth <= 0;
    }
    @Override
    public void takeDamage(int amount) {
        if (blocksAbilityDamage(amount)) {
            return;
        }
        int armorPiecesBefore = armorPieces.size();
        int remaining = amount;
        Iterator<ArmorPiece> iterator = armorPieces.iterator();
        while (iterator.hasNext() && remaining > 0) {
            ArmorPiece piece = iterator.next();
            remaining = piece.absorbDamage(remaining);
            if (piece.isBroken()) {
                iterator.remove();
            }
        }
        if (armorPieces.isEmpty()) {
            armor = null;
        } else {
            armor = armorPieces.get(0);
        }
        super.takeDamage(remaining);
        currentHealth = health;
        for (ZombieAbility ability : new ArrayList<>(abilities)) {
            ability.onDamaged(this, armorPiecesBefore, armorPieces.size());
        }
    }
    public void receiveInstantKill(ProjectileTrajectory trajectory) {
        die();
    }
    public final void forceDie() {
        if (!isAlive) {
            return;
        }
        super.die();
        currentHealth = 0;
        for (ZombieAbility ability : new ArrayList<>(abilities)) {
            ability.onDeath(this, null);
        }
        onDeath();
    }
    @Override
    public void die() {
        forceDie();
    }
    @Override
    public void update(int tickCount) {
        super.update(tickCount);
        float elapsedSeconds = tickCount / 10f;
        Iterator<ZombieEffect> iterator = effects.iterator();
        while (iterator.hasNext()) {
            ZombieEffect effect = iterator.next();
            effect.remainingSeconds -= elapsedSeconds;
            if (effect.remainingSeconds <= 0f) {
                effect.expire();
                iterator.remove();
            }
        }
        applyPoisonDamage(elapsedSeconds);
        if (findEffect(ZombieEffectType.CHILLED) == null) {
            chillSlowFactor = 1f;
        }
        if (findEffect(ZombieEffectType.POISONED) == null) {
            poisonDamagePerSecond = 0;
            poisonDamageAccumulator = 0f;
        }
    }
    @Override
    public void freeze(int duration) {
        for (ZombieAbility ability : new ArrayList<>(abilities)) {
            if (ability.blocksFreeze(this)) {
                return;
            }
        }
        super.freeze(duration);
        ZombieEffect frozenEffect = findEffect(ZombieEffectType.FROZEN);
        if (frozenEffect == null) {
            effects.add(new ZombieEffect(ZombieEffectType.FROZEN, duration / 10f));
        }
        else {
            frozenEffect.remainingSeconds = Math.max(frozenEffect.remainingSeconds,
                    duration / 10f);
        }
    }
    public void chill(float slowFactor, float seconds) {
        if (!isAlive || slowFactor <= 0f || slowFactor >= 1f || seconds <= 0f) {
            return;
        }
        chillSlowFactor = Math.min(chillSlowFactor, slowFactor);
        ZombieEffect chilled = findEffect(ZombieEffectType.CHILLED);
        if (chilled == null) {
            effects.add(new ZombieEffect(ZombieEffectType.CHILLED, seconds));
        }
        else {
            chilled.remainingSeconds = Math.max(chilled.remainingSeconds, seconds);
        }
    }
    public void clearChill() {
        chillSlowFactor = 1f;
        effects.removeIf(effect -> effect.type == ZombieEffectType.CHILLED);
    }
    public void stun(float seconds) {
        if (!isAlive || seconds <= 0f) {
            return;
        }
        ZombieEffect stunned = findEffect(ZombieEffectType.STUNNED);
        if (stunned == null) {
            effects.add(new ZombieEffect(ZombieEffectType.STUNNED, seconds));
        }
        else {
            stunned.remainingSeconds = Math.max(stunned.remainingSeconds, seconds);
        }
    }
    public boolean isStunned() {
        return findEffect(ZombieEffectType.STUNNED) != null;
    }
    public void poison(int damagePerSecond, float seconds) {
        if (!isAlive || damagePerSecond <= 0 || seconds <= 0f) {
            return;
        }
        poisonDamagePerSecond = Math.max(poisonDamagePerSecond, damagePerSecond);
        ZombieEffect poisoned = findEffect(ZombieEffectType.POISONED);
        if (poisoned == null) {
            effects.add(new ZombieEffect(ZombieEffectType.POISONED, seconds));
        }
        else {
            poisoned.remainingSeconds = Math.max(poisoned.remainingSeconds, seconds);
        }
    }
    private void applyPoisonDamage(float elapsedSeconds) {
        if (poisonDamagePerSecond <= 0 || !isAlive
                || findEffect(ZombieEffectType.POISONED) == null) {
            return;
        }
        poisonDamageAccumulator += poisonDamagePerSecond * elapsedSeconds;
        int damage = (int) poisonDamageAccumulator;
        if (damage <= 0) {
            return;
        }
        poisonDamageAccumulator -= damage;
        super.takeDamage(damage);
        currentHealth = health;
    }
    public boolean canAttackThisTick() {
        if (frozen || isStunned() || !isAlive) {
            return false;
        }
        attackProgress += chillSlowFactor;
        if (attackProgress + 0.0001f < 1f) {
            return false;
        }
        attackProgress -= 1f;
        return true;
    }
    public float getChillSlowFactor() {
        return chillSlowFactor;
    }
    public void equipArmor(ArmorDecorator armor) {
        this.armor = armor;
    }
    public final void addArmorPiece(ArmorPiece piece) {
        if (piece == null) {
            return;
        }
        armorPieces.add(piece);
        armor = armorPieces.get(0);
    }
    public int getRemainingArmorHealth() {
        int total = 0;
        for (ArmorPiece piece : armorPieces) {
            total += Math.max(0, piece.health);
        }
        return total;
    }
    public ArmorDecorator getArmor() {
        return armor;
    }
    public String getName() {
        return displayName;
    }
    private boolean blocksAbilityDamage(int amount) {
        for (ZombieAbility ability : new ArrayList<>(abilities)) {
            if (ability.blocksDamage(this, amount)) {
                return true;
            }
        }
        return false;
    }
    public String getType() {
        return type;
    }
    public void setIdentity(String zombieType, String zombieName) {
        if (zombieType != null && !zombieType.isBlank()) {
            type = zombieType;
        }
        if (zombieName == null || zombieName.isBlank()) {
            displayName = type;
        } else {
            displayName = zombieName;
        }
    }
    public void addDeathListener(ZombieDeathListener listener) {
        if (listener != null && !deathListeners.contains(listener)) {
            deathListeners.add(listener);
        }
    }
    private void publishDeathEvent() {
        if (deathEventPublished) {
            return;
        }
        deathEventPublished = true;
        ZombieDeathEvent event = new ZombieDeathEvent(this);
        for (ZombieDeathListener listener
                : new ArrayList<>(deathListeners)) {
            listener.onZombieDeath(event);
        }
    }
    public int getAttackDamage() {
        return attackDamage;
    }
    public int getWaveCost() {
        return waveCost;
    }
    public boolean isHypnotized() {
        return isHypnotized;
    }
    public void setHypnotized(boolean hypnotized) {
        isHypnotized = hypnotized;
        if (hypnotized && findEffect(ZombieEffectType.HYPNOTIZED) == null) {
            effects.add(new ZombieEffect(ZombieEffectType.HYPNOTIZED,
                    Float.POSITIVE_INFINITY));
        }
    }
    private ZombieEffect findEffect(ZombieEffectType type) {
        for (ZombieEffect effect : effects) {
            if (effect.type == type) {
                return effect;
            }
        }
        return null;
    }
}
