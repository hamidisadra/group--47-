package ir.ac.pvz.model.core;

import ir.ac.pvz.controller.game_core.LootDropService;
import ir.ac.pvz.model.armor.ArmorDecorator;
import ir.ac.pvz.model.enums.DamageMode;
import ir.ac.pvz.model.enums.LootType;
import ir.ac.pvz.model.enums.ProjectileTrajectory;
import ir.ac.pvz.model.enums.ProjectileType;
import ir.ac.pvz.model.enums.ZombieEffectType;
import ir.ac.pvz.model.interfaces.IMovable;
import ir.ac.pvz.model.interfaces.IWall;
import ir.ac.pvz.model.support.ArmorPiece;
import ir.ac.pvz.model.support.ContinuousPosition;
import ir.ac.pvz.model.support.Projectile;
import ir.ac.pvz.model.support.ZombieAbility;
import ir.ac.pvz.model.support.EatingAttackStrategy;
import ir.ac.pvz.model.support.WalkingMovementStrategy;
import ir.ac.pvz.model.support.ZombieAttackStrategy;
import ir.ac.pvz.model.support.ZombieDeathEvent;
import ir.ac.pvz.model.support.ZombieDeathListener;
import ir.ac.pvz.model.support.ZombieMovementStrategy;
import ir.ac.pvz.model.support.ZombieEffect;
import ir.ac.pvz.model.support.ZombieBaseStats;
import ir.ac.pvz.model.support.ZombieDataRepository;
import ir.ac.pvz.model.support.ZombieDefinition;
import ir.ac.pvz.model.support.ZombieAbilityRegistry;
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
    public int lastDamageProjectileId;
    public int selectionWeight;
    public boolean canSpawnPlantFood;
    public String type;
    public String displayName;
    public ZombieMovementStrategy movementStrategy;
    public ZombieAttackStrategy attackStrategy;
    public Projectile incomingProjectile;
    private boolean freezeImmune;
    float chillSlowFactor;
    private float attackProgress;
    int poisonDamagePerSecond;
    float poisonDamageAccumulator;
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
        this.damageToPlant = Math.max(0, Math.round(attackDamage / 10f));
        this.abilities = new ArrayList<>();
        this.effects = new ArrayList<>();
        this.isGlowing = false;
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
        this.freezeImmune = false;
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
        initializeDefinitionData(zombieType);
    }

    private Zombie(ZombieBaseStats stats) {
        this(stats.speed, stats.health, stats.eatDamagePerSecond,
                stats.waveCost);
    }

    private void initializeDefinitionData(String zombieType) {
        ZombieDefinitionSupport.initialize(this, zombieType);
    }

    protected final double requiredDataNumber(String key) {
        return ZombieDefinitionSupport.requiredNumber(type, key);
    }

    public void applyBaseData(float movementSpeed, int baseHealth,
                              int eatDamagePerSecond, int cost,
                              int weight, boolean plantFoodEligible) {
        speed = movementSpeed;
        health = baseHealth;
        currentHealth = baseHealth;
        attackDamage = eatDamagePerSecond;
        damageToPlant = Math.max(0, Math.round(eatDamagePerSecond / 10f));
        waveCost = cost;
        initialWaveCost = cost;
        selectionWeight = weight;
        canSpawnPlantFood = plantFoodEligible;
        isGlowing = false;
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
        if (projectile != null) {
            lastDamageProjectileId = projectile.projectileId;
        }

        if (projectile == null || !isAlive || isProjectileBlocked(projectile)) {
            return;
        }

        for (ZombieAbility ability : new ArrayList<>(abilities)) {
            ability.onProjectileReceived(this, projectile, null);
        }

        incomingProjectile = projectile;

        try {
            applyProjectileEffects(projectile);
        }

        finally {
            incomingProjectile = null;
        }
    }

    private boolean isProjectileBlocked(Projectile projectile) {
        for (ZombieAbility ability : new ArrayList<>(abilities)) {
            if (ability.blocksProjectile(this, projectile)) {
                return true;
            }
        }

        return false;
    }

    private void applyProjectileEffects(Projectile projectile) {
        if (projectile.type == ProjectileType.FIRE) {
            melt();
            clearChill();
            effects.removeIf(effect -> effect.type == ZombieEffectType.FROZEN
                    || effect.type == ZombieEffectType.CHILLED);
        }

        applyProjectileDamage(projectile);

        if (projectile.type == ProjectileType.ICE) {
            chill(0.5f, 3f);
        }
    }

    private void applyProjectileDamage(Projectile projectile) {
        boolean ignoresArmor = projectile.type == ProjectileType.POISON
                || projectile.damageMode == DamageMode.IGNORE_ARMOR;

        if (projectile.damageMode == DamageMode.INSTANT_KILL) {
            if (!blocksAbilityDamage(projectile.damageAmount)) {
                die();
            }
        }

        else if (ignoresArmor) {
            if (!blocksAbilityDamage(projectile.damageAmount)) {
                super.takeDamage(projectile.damageAmount);
                currentHealth = health;
            }
        }

        else {
            takeDamage(projectile.damageAmount);
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

    public void takeFireDamage(int amount) {
        for (ZombieAbility ability : new ArrayList<>(abilities)) {
            if (ability.blocksFireDamage(this)) {
                return;
            }
        }
        melt();
        clearChill();
        effects.removeIf(effect -> effect.type == ZombieEffectType.FROZEN
                || effect.type == ZombieEffectType.CHILLED);
        takeDamage(amount);
    }

    @Override
    public void takeDamage(int amount) {
        if (blocksAbilityDamage(amount)) {
            return;
        }

        int armorPiecesBefore = armorPieces.size();
        int remaining = ZombieArmorSupport.absorb(this, amount);

        super.takeDamage(remaining);
        currentHealth = health;

        ZombieArmorSupport.notifyDamaged(this, armorPiecesBefore);
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

    public void die() {
        forceDie();
    }

    public void update(int tickCount) {
        super.update(tickCount);

        float elapsedSeconds = tickCount / 10f;

        ZombieEffectSupport.expire(this, elapsedSeconds);
        applyPoisonDamage(elapsedSeconds);
        ZombieEffectSupport.clearExpiredState(this);
    }

    public void freeze(int duration) {
        if (freezeImmune) {
            return;
        }

        for (ZombieAbility ability : new ArrayList<>(abilities)) {
            if (ability.blocksFreeze(this)) {
                return;
            }
        }

        encaseInIce(duration);
    }

    public void encaseInIce(int duration) {
        super.freeze(duration);
        ZombieEffectSupport.refresh(this, ZombieEffectType.FROZEN, duration / 10f);
    }

    public void setFreezeImmune(boolean freezeImmune) {
        this.freezeImmune = freezeImmune;
    }

    public boolean isFreezeImmune() {
        return freezeImmune;
    }

    public void chill(float slowFactor, float seconds) {
        if (!isAlive || slowFactor <= 0f || slowFactor >= 1f || seconds <= 0f) {
            return;
        }

        chillSlowFactor = Math.min(chillSlowFactor, slowFactor);
        ZombieEffectSupport.refresh(this, ZombieEffectType.CHILLED, seconds);
    }

    public void clearChill() {
        chillSlowFactor = 1f;
        effects.removeIf(effect -> effect.type == ZombieEffectType.CHILLED);
    }

    public void stun(float seconds) {
        if (!isAlive || seconds <= 0f) {
            return;
        }

        ZombieEffectSupport.refresh(this, ZombieEffectType.STUNNED, seconds);
    }

    public boolean isStunned() {
        return findEffect(ZombieEffectType.STUNNED) != null;
    }

    public void poison(int damagePerSecond, float seconds) {
        if (!isAlive || damagePerSecond <= 0 || seconds <= 0f) {
            return;
        }

        poisonDamagePerSecond = Math.max(poisonDamagePerSecond, damagePerSecond);
        ZombieEffectSupport.refresh(this, ZombieEffectType.POISONED, seconds);
    }

    private void applyPoisonDamage(float elapsedSeconds) {
        int damage = ZombieEffectSupport.poisonDamageThisStep(this,
                elapsedSeconds);

        if (damage <= 0) {
            return;
        }

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
        ZombieArmorSupport.refreshTopPiece(this);
    }

    public int getRemainingArmorHealth() {
        return ZombieArmorSupport.remainingHealth(this);
    }

    public ArmorDecorator getArmor() {
        return armor;
    }

    public String getName() {
        return displayName;
    }

    private boolean blocksAbilityDamage(int amount) {
        return ZombieArmorSupport.blocksAbilityDamage(this, amount);
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
        }

        else {
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

        for (ZombieDeathListener listener : new ArrayList<>(deathListeners)) {
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
        return ZombieEffectSupport.find(this, type);
    }
}
