package com.pvz.model.support;

public class PlantStatusView {

    public String plantType;
    public int requiredSun;
    public boolean plantableNow;
    public float cooldownRemainingSeconds;

    public PlantStatusView(String plantType, int requiredSun, boolean plantableNow,
                           float cooldownRemainingSeconds) {
        this.plantType = plantType;
        this.requiredSun = requiredSun;
        this.plantableNow = plantableNow;
        this.cooldownRemainingSeconds = cooldownRemainingSeconds;
    }

    @Override
    public String toString() {
        return plantType + ": sun=" + requiredSun + ", plantable=" + plantableNow
                + ", cooldown=" + cooldownRemainingSeconds + "s";
    }
}
