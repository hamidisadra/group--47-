package com.pvz.model.plants;

import com.pvz.model.core.Plant;
import com.pvz.model.enums.PlantCategory;
import com.pvz.model.enums.PlantTag;
import com.pvz.model.interfaces.ISunProducer;
import com.pvz.model.support.BalanceDefaults;

import java.util.Random;

public class SunProducerPlant extends Plant implements ISunProducer {

    public int sunAmount;
    public float productionInterval;

    protected float productionElapsedSeconds;
    private boolean producedOnce;
    private int queuedPlantFoodSun;
    private boolean productionPaused;
    private float doubleSunChance;
    private final Random random;

    public SunProducerPlant(int id, String name, int cost, int baseHp,
                            float rechargeTime, int sunAmount,
                            float productionInterval, PlantTag... tags) {
        super(id, name, cost, baseHp, rechargeTime, productionInterval, 0,
                PlantCategory.SUN_PRODUCER, tags);
        this.sunAmount = sunAmount;
        this.productionInterval = productionInterval;
        this.productionElapsedSeconds = 0f;
        this.producedOnce = false;
        this.queuedPlantFoodSun = 0;
        this.productionPaused = false;
        this.doubleSunChance = 0f;
        this.random = new Random();
    }

    @Override
    public void onTick() {
        super.onTick();
        if (!productionPaused && (!producedOnce || productionInterval > 0f)) {
            productionElapsedSeconds += 0.1f;
        }
    }

    @Override
    public int produceSun() {
        if (queuedPlantFoodSun > 0) {
            int produced = queuedPlantFoodSun;
            queuedPlantFoodSun = 0;
            return produced;
        }
        if (producedOnce && productionInterval == 0f) {
            return 0;
        }
        if (productionElapsedSeconds + 0.0001f < productionInterval) {
            return 0;
        }
        productionElapsedSeconds = 0f;
        producedOnce = true;
        if (productionInterval == 0f) {
            die();
        }
        if (doubleSunChance > 0f && random.nextFloat() < doubleSunChance) {
            return sunAmount * 2;
        }
        return sunAmount;
    }

    public void enableDoubleSunChance() {
        doubleSunChance = BalanceDefaults.DOUBLE_SUN_CHANCE;
    }

    public float getDoubleSunChance() {
        return doubleSunChance;
    }

    protected void queuePlantFoodSun(int amount) {
        queuedPlantFoodSun = amount;
    }

    public int consumeQueuedPlantFoodSun() {
        int amount = queuedPlantFoodSun;
        queuedPlantFoodSun = 0;
        return amount;
    }


    public void setProductionPaused(boolean paused) {
        this.productionPaused = paused;
    }

    public float getProductionInterval() {
        return productionInterval;
    }
}
