package ir.ac.pvz.model.support;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public final class BalanceDefaults {
    private static final Properties DATA = load();
    public static final float BARREL_ROLLER_SPEED = floatValue("barrelRoller.speed");
    public static final int BARREL_ROLLER_HEALTH = intValue("barrelRoller.health");
    public static final int BARREL_ROLLER_EAT_DPS = intValue("barrelRoller.eatDps");
    public static final int BARREL_ROLLER_WAVE_COST = intValue("barrelRoller.waveCost");
    public static final int BARREL_HEALTH = intValue("barrel.health");
    public static final float PIANIST_MUSIC_LOOP_SECONDS =
            floatValue("pianist.musicLoopSeconds");
    public static final int HUNTER_ICE_HEALTH = intValue("hunter.iceHealth");
    public static final int OCTOPUS_BLOCK_HEALTH = intValue("octopus.blockHealth");
    public static final int BASE_POISON_DPS = intValue("poison.baseDps");
    public static final int UPGRADED_POISON_DPS = intValue("poison.upgradedDps");
    public static final float POISON_DURATION_SECONDS =
            floatValue("poison.durationSeconds");
    public static final float DOUBLE_SUN_CHANCE = floatValue("sun.doubleChance");
    public static final float ENLIGHTEN_MINT_DURATION_SECONDS =
            floatValue("mint.enlightenDurationSeconds");
    public static final float APPEASE_MINT_DURATION_SECONDS =
            floatValue("mint.appeaseDurationSeconds");
    public static final float ARMA_MINT_DURATION_SECONDS =
            floatValue("mint.armaDurationSeconds");
    public static final float BOMBARD_MINT_DURATION_SECONDS =
            floatValue("mint.bombardDurationSeconds");
    public static final float ENFORCE_MINT_DURATION_SECONDS =
            floatValue("mint.enforceDurationSeconds");
    public static final float REINFORCE_MINT_DURATION_SECONDS =
            floatValue("mint.reinforceDurationSeconds");
    public static final float ENCHANT_MINT_DURATION_SECONDS =
            floatValue("mint.enchantDurationSeconds");
    public static final float PIERCE_MINT_DURATION_SECONDS =
            floatValue("mint.pierceDurationSeconds");
    public static final float CATTAIL_MINT_DURATION_SECONDS =
            floatValue("mint.cattailDurationSeconds");
    public static final int SHORT_PROJECTILE_RANGE =
            intValue("projectile.shortRange");
    public static final int MEDIUM_PROJECTILE_RANGE =
            intValue("projectile.mediumRange");
    public static final float KERNEL_BUTTER_CHANCE =
            floatValue("kernel.butterChance");
    public static final float KERNEL_LEVEL_TWO_BUTTER_CHANCE =
            floatValue("kernel.levelTwoButterChance");
    public static final float KERNEL_BUTTER_STUN_SECONDS =
            floatValue("kernel.butterStunSeconds");
    public static final float MEGA_GATLING_PLANT_FOOD_CHANCE =
            floatValue("megaGatling.plantFoodChance");
    public static final float MEGA_GATLING_LEVEL_THREE_PLANT_FOOD_CHANCE =
            floatValue("megaGatling.levelThreePlantFoodChance");
    public static final int GRAPESHOT_GRAPE_COUNT =
            intValue("grapeshot.grapeCount");
    public static final int GRAPESHOT_SECONDARY_DAMAGE =
            intValue("grapeshot.secondaryDamage");
    public static final int GRAPESHOT_BASE_BOUNCES =
            intValue("grapeshot.baseBounces");
    public static final int HOT_POTATO_FINISH_DAMAGE =
            intValue("hotPotato.finishDamage");
    public static final int GRAVE_BUSTER_FINISH_DAMAGE =
            intValue("graveBuster.finishDamage");
    public static final int TORCHWOOD_LEVEL_THREE_DEATH_DAMAGE =
            intValue("torchwood.levelThreeDeathDamage");
    public static final int THREEPEATER_PLANT_FOOD_SHOTS =
            intValue("threepeater.plantFoodShots");
    public static final int CACTUS_PLANT_FOOD_DAMAGE_MULTIPLIER =
            intValue("cactus.plantFoodDamageMultiplier");
    public static final int BOWLING_BULB_PLANT_FOOD_DAMAGE =
            intValue("bowlingBulb.plantFoodDamage");
    public static final int WALL_NUT_PLANT_FOOD_ARMOR =
            intValue("wallNut.plantFoodArmor");
    public static final int TALL_NUT_PLANT_FOOD_ARMOR =
            intValue("tallNut.plantFoodArmor");
    public static final int ENDURIAN_PLANT_FOOD_ARMOR =
            intValue("endurian.plantFoodArmor");
    public static final int EXPLODE_O_NUT_PLANT_FOOD_ARMOR =
            intValue("explodeONut.plantFoodArmor");
    public static final int PUMPKIN_PLANT_FOOD_ARMOR =
            intValue("pumpkin.plantFoodArmor");
    public static final int SUN_BEAN_PLANT_FOOD_ARMOR =
            intValue("sunBean.plantFoodArmor");
    private BalanceDefaults() {
    }
    private static Properties load() {
        Properties values = new Properties();
        try (InputStream input = DataFileLocator.open("balance.properties")) {
            values.load(input);
            return values;
        }
        catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
    private static int intValue(String key) {
        String value = requiredValue(key);
        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException exception) {
            throw invalidValue(key, value, exception);
        }
    }
    private static float floatValue(String key) {
        String value = requiredValue(key);
        try {
            return Float.parseFloat(value);
        }
        catch (NumberFormatException exception) {
            throw invalidValue(key, value, exception);
        }
    }
    private static String requiredValue(String key) {
        String value = DATA.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing balance value: " + key);
        }
        return value.trim();
    }
    private static IllegalStateException invalidValue(
            String key, String value, NumberFormatException cause) {
        return new IllegalStateException(
                "Invalid balance value for " + key + ": " + value, cause);
    }
}
