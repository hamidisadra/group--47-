package ir.ac.pvz.model.core;

import ir.ac.pvz.model.enums.PlantCategory;
import ir.ac.pvz.model.enums.PlantTag;
import ir.ac.pvz.model.interfaces.IUpgradable;
import ir.ac.pvz.model.plants.PlantFactory;
import ir.ac.pvz.model.support.FrozenBlock;
import ir.ac.pvz.model.support.GridPosition;
import ir.ac.pvz.model.support.Tile;
import ir.ac.pvz.model.support.Tombstone;
import ir.ac.pvz.model.support.OctopusBlock;
import ir.ac.pvz.model.support.Upgrade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Plant extends GameObject implements IUpgradable {

    public int id;
    public String name;
    public int cost;
    public int baseHp;
    public int currentHp;
    public float rechargeTime;
    public float actionInterval;
    public int level;
    public float cooldownRemaining;
    public List<PlantTag> plantTags;
    public List<Upgrade> levelUpgrades;
    public String type;
    public int sunCost;
    public int attackPower;
    public GridPosition location;
    public PlantCategory category;
    public boolean canStack;
    public boolean isBoostedByPlantFood;
    public boolean isCatTransformed;
    public boolean isOctopusBlocked;
    public OctopusBlock blockingOctopus;

    private int hunterIceHitCount;
    private int activeCatCurses;
    private float ageSeconds;
    protected float lifeSpanSeconds;
    private boolean permanentlyFrozen;
    private boolean destroyedByDamage;

    protected Plant(int id, String name, int cost, int baseHp, float rechargeTime,
                    float actionInterval, int attackPower, PlantCategory category,
                    PlantTag... tags) {
        super(0f, 0, baseHp);
        this.id = id;
        this.name = name;
        this.cost = cost;
        this.baseHp = baseHp;
        this.currentHp = baseHp;
        this.rechargeTime = rechargeTime;
        this.actionInterval = actionInterval;
        this.level = 1;
        this.cooldownRemaining = 0f;
        this.plantTags = new ArrayList<>(Arrays.asList(tags));
        this.levelUpgrades = new ArrayList<>();
        this.type = name;
        this.sunCost = cost;
        this.attackPower = attackPower;
        this.location = new GridPosition(0, 0);
        this.category = category;
        this.canStack = plantTags.contains(PlantTag.STACK);
        this.isBoostedByPlantFood = false;
        this.isCatTransformed = false;
        this.isOctopusBlocked = false;
        this.blockingOctopus = null;
        this.hunterIceHitCount = 0;
        this.activeCatCurses = 0;
        this.ageSeconds = 0f;
        this.lifeSpanSeconds = resolveInitialLifeSpan();
        this.permanentlyFrozen = false;
        this.destroyedByDamage = false;
    }

    public void onPlantFood() {
        isBoostedByPlantFood = true;
    }

    public void onTick() {
        ageSeconds += 0.1f;
        if (cooldownRemaining > 0f) {
            cooldownRemaining = Math.max(0f, cooldownRemaining - 0.1f);
        }
        if (lifeSpanSeconds > 0f && ageSeconds + 0.0001f >= lifeSpanSeconds) {
            die();
        }
    }

    @Override
    public void update(int tickCount) {
        super.update(tickCount);
        if (frozen || permanentlyFrozen || isCatTransformed || isOctopusBlocked) {
            return;
        }
        for (int i = 0; i < tickCount; i++) {
            onTick();
        }
    }

    @Override
    public void takeDamage(int amount) {
        boolean aliveBeforeDamage = isAlive;
        super.takeDamage(amount);
        currentHp = health;
        if (aliveBeforeDamage && !isAlive) {
            destroyedByDamage = true;
        }
    }

    @Override
    public void die() {
        if (!isAlive) {
            return;
        }
        super.die();
        currentHp = 0;
        if (location != null) {
            System.out.println("Plant " + type + " at "
                    + location.toUserString() + " is destroyed.");
        }
    }

    @Override
    public void upgrade() {
        int nextLevel = level + 1;
        for (Upgrade upgrade : levelUpgrades) {
            if (upgrade.level == nextLevel) {
                upgrade.applyTo(this);
                return;
            }
        }
    }

    public boolean canPlantOn(Tile tile) {
        if (tile == null) {
            return false;
        }
        String normalized = getNormalizedType();
        if (normalized.equals("hotpotato") && tile.obstacle instanceof FrozenBlock) {
            return true;
        }
        if (normalized.equals("gravebuster") && tile.obstacle instanceof Tombstone) {
            return true;
        }
        if (tile.hasObstacle()) {
            return false;
        }
        if (tile.isWater) {
            return plantTags.contains(PlantTag.WATER) || tile.hasLilyPad();
        }
        if (!tile.canPlant && !canStack && !tile.canStackPlants) {
            return false;
        }
        if (tile.getPlants().isEmpty()) {
            return tile.canPlant;
        }
        return canStack || tile.canStackPlants || tile.getPlants().stream()
                .anyMatch(plant -> plant.canStack);
    }

    public void applyPlantFoodEffect() {
        onPlantFood();
    }

    public void resetCooldown() {
        cooldownRemaining = rechargeTime;
    }

    public void addCatCurse() {
        activeCatCurses++;
        isCatTransformed = true;
        if (activeCatCurses == 1) {
            freeze(Integer.MAX_VALUE);
        }
    }

    public void removeCatCurse() {
        activeCatCurses = Math.max(0, activeCatCurses - 1);
        isCatTransformed = activeCatCurses > 0;
        if (activeCatCurses == 0 && !permanentlyFrozen
                && !isOctopusBlocked) {
            melt();
        }
    }

    public int getActiveCatCurseCount() {
        return activeCatCurses;
    }

    public void receiveHunterIceHit(int requiredHits) {
        hunterIceHitCount++;
        if (hunterIceHitCount >= requiredHits) {
            permanentlyFrozen = true;
            freeze(Integer.MAX_VALUE);
        }
    }

    public void releaseFromIce() {
        hunterIceHitCount = 0;
        permanentlyFrozen = false;
        melt();
    }

    public boolean isPermanentlyFrozen() {
        return permanentlyFrozen;
    }

    public boolean canAct() {
        return isAlive && !frozen && !permanentlyFrozen
                && !isCatTransformed && !isOctopusBlocked;
    }

    public void receiveInstantKill() {
        die();
    }

    public boolean wasDestroyedByDamage() {
        return destroyedByDamage;
    }

    public void resetAge() {
        ageSeconds = 0f;
    }

    public float getAgeSeconds() {
        return ageSeconds;
    }

    public void setLifeSpanSeconds(float seconds) {
        lifeSpanSeconds = Math.max(0f, seconds);
    }

    public float getLifeSpanSeconds() {
        return lifeSpanSeconds;
    }

    public String getNormalizedType() {
        return PlantFactory.normalize(type);
    }

    public static String[] getSpreadsheetTypes() {
        return PlantFactory.getPlantTypes();
    }

    public static Plant createSpreadsheetPlant(int id, String type) {
        return PlantFactory.create(id, type);
    }

    private float resolveInitialLifeSpan() {
        String normalized = PlantFactory.normalize(name);
        if (normalized.equals("seashroom") || normalized.equals("puffshroom")) {
            return 60f;
        }
        return 0f;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCost() {
        return sunCost;
    }

    public int getBaseHp() {
        return baseHp;
    }

    public PlantCategory getCategory() {
        return category;
    }

    public float getCooldownRemaining() {
        return cooldownRemaining;
    }

    public List<PlantTag> getPlantTags() {
        return plantTags;
    }

    public int getLevel() {
        return level;
    }
}
