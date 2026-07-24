package ir.ac.pvz.model.support;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.plants.ExplosivePlant;
import ir.ac.pvz.model.plants.LobberPlant;
import ir.ac.pvz.model.plants.MeleePlant;
import ir.ac.pvz.model.plants.MintPlant;
import ir.ac.pvz.model.plants.ShooterPlant;
import ir.ac.pvz.model.plants.StrikeThroughPlant;
import ir.ac.pvz.model.plants.SunProducerPlant;
import ir.ac.pvz.model.plants.WallPlant;

public class Upgrade {
    public static final int UNSPECIFIED_COST = -1;
    public int level;
    public int coinCost;
    public int seedPacketCost;
    public int damageBonus;
    public int healthBonus;
    public int sunCostReduction;
    public Upgrade(int level, int coinCost, int seedPacketCost, int damageBonus,
                   int healthBonus, int sunCostReduction) {
        this.level = level;
        this.coinCost = coinCost;
        this.seedPacketCost = seedPacketCost;
        this.damageBonus = damageBonus;
        this.healthBonus = healthBonus;
        this.sunCostReduction = sunCostReduction;
    }
    public boolean hasConfiguredCost() {
        return coinCost >= 0 && seedPacketCost >= 0;
    }
    public void setCost(int coins, int seedPackets) {
        coinCost = coins;
        seedPacketCost = seedPackets;
    }
    public void applyTo(Plant plant) {
        plant.attackPower += damageBonus;
        plant.baseHp += healthBonus;
        plant.currentHp += healthBonus;
        plant.health += healthBonus;
        plant.sunCost = Math.max(0, plant.sunCost - sunCostReduction);
        plant.cost = plant.sunCost;
        plant.level = level;
        synchronizeDamageFields(plant);
        applySpecialUpgrade(plant);
    }
    public static void configureFor(Plant plant, String type) {
        if (addUpgradeGroupOne(plant, type)) return;
        if (addUpgradeGroupTwo(plant, type)) return;
        if (addUpgradeGroupThree(plant, type)) return;
        addUpgradeGroupFour(plant, type);
    }
    private void synchronizeDamageFields(Plant plant) {
        if (plant instanceof ShooterPlant) {
            ((ShooterPlant) plant).damage = plant.attackPower;
        }
        if (plant instanceof LobberPlant) {
            ((LobberPlant) plant).damage = plant.attackPower;
        }
        if (plant instanceof ExplosivePlant) {
            ((ExplosivePlant) plant).explosionDamage = plant.attackPower;
        }
        if (plant instanceof WallPlant && plant.getNormalizedType().equals("endurian")) {
            ((WallPlant) plant).reflectDamage = plant.attackPower;
        }
    }
    private void applySpecialUpgrade(Plant plant) {
        String type = plant.getNormalizedType();
        applyTimingUpgrade(plant, type);
        applyCategoryUpgrade(plant, type);
        applySpecialValueUpgrade(plant, type);
    }
    private void applyTimingUpgrade(Plant plant, String type) {
        if (level == 2 && isOneOf(type, "sunflower", "twinsunflower", "primalsunflower")) {
            reduceProductionInterval(plant, 2f);
        }
        else if (level == 2 && type.equals("goldbloom")) {
            reduceRecharge(plant, 5f);
        }
        else if (level == 2 && type.equals("citron")) {
            plant.actionInterval = Math.max(0.1f, plant.actionInterval - 1f);
        }
        else if (level == 2 && isOneOf(type, "caulipower", "electricblueberry")) {
            plant.actionInterval = Math.max(0.1f, plant.actionInterval - 2f);
        }
        else if (level == 2 && type.equals("bowlingbulb")) {
            plant.actionInterval = Math.max(0.1f, plant.actionInterval - 1f);
        }
        else if (level == 2 && type.equals("starfruit")) {
            plant.actionInterval *= 0.9f;
        }
        else if (level == 3 && isOneOf(type, "bonkchoy", "phatbeet")) {
            plant.actionInterval *= 0.9f;
        }
        else if (level == 3 && type.equals("cabbagepult")) {
            plant.actionInterval *= 0.85f;
        }
        else if (level == 2 && type.equals("puffshroom")) {
            increaseLifeSpan(plant, 10f);
        }
        else if (level == 2 && type.equals("gravebuster")
                && plant instanceof ExplosivePlant) {
            ((ExplosivePlant) plant).reduceGraveEatingTime(1f);
        }
        else if (level == 4 && type.equals("seashroom")) {
            increaseLifeSpan(plant, 10f);
        }
        else if (level == 2 && type.equals("imitater")) {
            reduceRecharge(plant, 2f);
        }
        else if (level == 4 && type.equals("lilypad")) {
            reduceRecharge(plant, 2f);
        }
    }

    private void applyCategoryUpgrade(Plant plant, String type) {
        if (level == 2 && type.equals("cactus") && plant instanceof StrikeThroughPlant) {
            ((StrikeThroughPlant) plant).pierceCount++;
        }
        else if (level == 2 && isOneOf(type, "cherrybomb", "jalapeno", "doomshroom",
                "tanglekelp")) {
            reduceRecharge(plant, 5f);
        }
        else if (level == 2 && isOneOf(type, "squash", "iceberglettuce", "hotpotato")) {
            float reduction = 2f;
            if (type.equals("squash")) {
                reduction = 3f;
            }
            reduceRecharge(plant, reduction);
        }
        else if (level == 3 && type.equals("primalpotatomine")) {
            reduceRecharge(plant, 3f);
        }
        else if (level == 3 && type.equals("potatomine")) {
            reduceRecharge(plant, 5f);
        }
        else if (level == 3 && isOneOf(type, "wallnut", "tallnut", "sweetpotato",
                "pumpkin", "magnetshroom", "iceshroom")) {
            reduceRecharge(plant, 5f);
        }
        else if (level == 3 && type.equals("garlic")) {
            reduceRecharge(plant, 3f);
        }
        else if (level == 3 && isOneOf(type, "gravebuster")) {
            reduceRecharge(plant, 2f);
        }
    }

    private void applySpecialValueUpgrade(Plant plant, String type) {
        if (level == 4 && isOneOf(type, "sunflower", "sunshroom")
                && plant instanceof SunProducerPlant) {
            ((SunProducerPlant) plant).enableDoubleSunChance();
        }
        else if (level == 3 && type.equals("goldbloom")
                && plant instanceof SunProducerPlant) {
            ((SunProducerPlant) plant).sunAmount += 50;
        }
        else if (level == 2 && type.equals("endurian") && plant instanceof WallPlant) {
            ((WallPlant) plant).reflectDamage += 5;
        }
        else if (level == 2 && type.equals("sunbean")) {
            plant.attackPower += 5;
        }
        else if (level == 3 && type.equals("explodeonut")) {
            plant.attackPower += 200;
        }
        else if (level == 2 && type.equals("chomper")) {
            plant.actionInterval = Math.max(0f, plant.actionInterval - 2f);
        }
        else if (level == 4 && type.equals("chomper")) {
            plant.actionInterval = Math.max(0f, plant.actionInterval - 3f);
        }
        else if (level == 2 && isMint(type) && plant instanceof MintPlant) {
            ((MintPlant) plant).durationSeconds += 1f;
        }
        else if (level == 3 && isMint(type)) {
            plant.rechargeTime = Math.max(0f, plant.rechargeTime - 5f);
        }
    }
    private void increaseLifeSpan(Plant plant, float amount) {
        if (plant.getLifeSpanSeconds() > 0f) {
            plant.setLifeSpanSeconds(plant.getLifeSpanSeconds() + amount);
        }
    }
    private void reduceRecharge(Plant plant, float amount) {
        plant.rechargeTime = Math.max(0f, plant.rechargeTime - amount);
    }
    private void reduceProductionInterval(Plant plant, float amount) {
        if (!(plant instanceof SunProducerPlant)) {
            return;
        }
        SunProducerPlant producer = (SunProducerPlant) plant;
        producer.productionInterval = Math.max(0f, producer.productionInterval - amount);
        producer.actionInterval = producer.productionInterval;
    }
    private boolean isOneOf(String value, String... choices) {
        for (String choice : choices) {
            if (value.equals(choice)) {
                return true;
            }
        }
        return false;
    }
    private boolean isMint(String type) {
        return type.endsWith("mint");
    }
    private static boolean addUpgradeGroupOne(Plant p, String type) {
        switch (type) {
            case "sunflower": addUpgrades(p, 0,0,0, 0,150,0, 0,0,0);
            return true;
            case "twinsunflower": addUpgrades(p, 0,0,0, 0,150,0, 0,0,25);
            return true;
            case "sunshroom": addUpgrades(p, 0,0,0, 0,150,0, 0,0,0);
            return true;
            case "primalsunflower": addUpgrades(p, 0,0,0, 0,150,0, 0,0,25);
            return true;
            case "goldbloom": addUpgrades(p, 0,0,0, 0,0,0, 0,0,25);
            return true;
            case "peashooter": addUpgrades(p, 10,0,0, 0,150,0, 0,0,25);
            return true;
            case "repeater": addUpgrades(p, 10,0,0, 0,200,0, 0,0,25);
            return true;
            case "threepeater": addUpgrades(p, 0,0,25, 10,0,0, 0,200,0);
            return true;
            case "snowpea": addUpgrades(p, 10,0,0, 0,0,0, 0,0,25);
            return true;
            case "rotobaga": addUpgrades(p, 10,0,0, 0,150,0, 0,0,25);
            return true;
            case "peapod": addUpgrades(p, 10,0,0, 0,200,0, 0,0,25);
            return true;
            case "splitpea": addUpgrades(p, 10,0,0, 0,200,0, 0,0,25);
            return true;
            case "citron": addUpgrades(p, 0,0,0, 150,0,0, 0,0,50);
            return true;
            case "caulipower": addUpgrades(p, 0,0,0, 0,150,0, 0,0,50);
            return true;
            case "electricblueberry": addUpgrades(p, 0,0,0, 0,0,0, 0,0,25);
            return true;
            case "bowlingbulb": addUpgrades(p, 0,0,0, 15,0,0, 0,0,25);
            return true;
            case "cactus": addUpgrades(p, 0,0,0, 10,0,0, 0,0,25);
            return true;
            case "firepeashooter": addUpgrades(p, 10,0,0, 0,200,0, 0,0,25);
            return true;
            default:
                return false;
        }
    }
    private static boolean addUpgradeGroupTwo(Plant p, String type) {
        switch (type) {
            case "starfruit": addUpgrades(p, 0,0,0, 10,0,0, 0,0,25);
            return true;
            case "goopeashooter": addUpgrades(p, 0,0,0, 0,150,0, 0,0,25);
            return true;
            case "megagatlingpea": addUpgrades(p, 10,0,0, 0,0,0, 0,0,50);
            return true;
            case "seashroom": addUpgrades(p, 0,0,0, 5,0,0, 0,0,0);
            return true;
            case "puffshroom": addUpgrades(p, 0,0,0, 10,0,0, 0,0,0);
            return true;
            case "fumeshroom": addUpgrades(p, 0,0,0, 10,0,0, 0,0,25);
            return true;
            case "cabbagepult": addUpgrades(p, 10,0,0, 0,0,0, 0,150,0);
            return true;
            case "kernelpult": addUpgrades(p, 0,0,0, 10,0,0, 0,150,0);
            return true;
            case "melonpult": addUpgrades(p, 0,0,25, 0,0,0, 30,0,0);
            return true;
            case "wintermelon": addUpgrades(p, 0,0,50, 0,0,0, 0,0,25);
            return true;
            case "pepperpult": addUpgrades(p, 15,0,0, 0,0,0, 0,0,25);
            return true;
            case "potatomine": addUpgrades(p, 0,0,0, 0,0,0, 600,0,0);
            return true;
            case "primalpotatomine": addUpgrades(p, 0,0,0, 0,0,0, 400,0,0);
            return true;
            case "cherrybomb": addUpgrades(p, 0,0,0, 600,0,0, 0,0,25);
            return true;
            case "squash": addUpgrades(p, 0,0,0, 600,0,0, 0,0,0);
            return true;
            case "grapeshot": addUpgrades(p, 600,0,0, 0,0,0, 0,0,25);
            return true;
            case "jalapeno": addUpgrades(p, 0,0,0, 600,0,0, 0,0,25);
            return true;
            case "doomshroom": addUpgrades(p, 0,0,0, 800,0,0, 0,0,50);
            return true;
            default:
                return false;
        }
    }

    private static boolean addUpgradeGroupThree(Plant p, String type) {
        switch (type) {
            case "tanglekelp": addUpgrades(p, 0,0,0, 0,0,0, 0,0,25);
            return true;
            case "iceberglettuce": addUpgrades(p, 0,0,0, 0,0,0, 0,0,0);
            return true;
            case "bonkchoy": addUpgrades(p, 5,0,0, 0,0,0, 0,200,0);
            return true;
            case "phatbeet": addUpgrades(p, 10,0,0, 0,0,0, 0,200,0);
            return true;
            case "chomper": addUpgrades(p, 0,0,0, 0,200,0, 0,0,0);
            return true;
            case "wasabiwhip": addUpgrades(p, 10,0,0, 0,0,0, 0,200,0);
            return true;
            case "kiwibeast": addUpgrades(p, 0,200,0, 15,0,0, 0,0,0);
            return true;
            case "wallnut": addUpgrades(p, 0,1000,0, 0,0,0, 0,1500,0);
            return true;
            case "tallnut": addUpgrades(p, 0,2000,0, 0,0,0, 0,3000,0);
            return true;
            case "endurian": addUpgrades(p, 0,0,0, 0,1000,0, 0,0,25);
            return true;
            case "garlic": addUpgrades(p, 0,150,0, 0,0,0, 0,250,0);
            return true;
            case "sweetpotato": addUpgrades(p, 0,1000,0, 0,0,0, 0,1500,0);
            return true;
            case "explodeonut": addUpgrades(p, 0,1000,0, 0,0,0, 0,0,25);
            return true;
            case "pumpkin": addUpgrades(p, 0,1000,0, 0,0,0, 0,1500,0);
            return true;
            case "sunbean": addUpgrades(p, 0,0,0, 0,150,0, 0,0,25);
            return true;
            case "torchwood": addUpgrades(p, 0,300,0, 0,0,0, 0,0,25);
            return true;
            case "magnetshroom": addUpgrades(p, 0,0,0, 0,0,0, 0,200,0);
            return true;
            case "hypnoshroom": addUpgrades(p, 0,0,25, 0,0,0, 0,0,0);
            return true;
            default:
                return false;
        }
    }

    private static void addUpgradeGroupFour(Plant p, String type) {
        switch (type) {
            case "cattail": addUpgrades(p, 10,0,0, 0,200,0, 0,0,25);
            break;
            case "imitater": addUpgrades(p, 0,0,0, 0,0,25, 0,0,0);
            break;
            case "iceshroom": addUpgrades(p, 0,0,0, 0,0,0, 50,0,0);
            break;
            case "lilypad": addUpgrades(p, 0,0,25, 0,200,0, 0,0,0);
            break;
            case "hotpotato":
                case "gravebuster":
                addUpgrades(p, 0,0,0, 0,0,0, 0,0,0);
                break;
            case "enlightenmint":
                case "appeasemint":
                    case "armamint":
            case "bombardmint":
                case "enforcemint":
                    case "reinforcemint":
            case "enchantmint":
                case "piercemint":
                    case "cattailmint":
                addUpgrades(p, 0,0,0, 0,0,0, 0,0,0);
                break;
            default:
                break;
        }
    }
    private static void addUpgrades(Plant p, int d2, int h2, int c2,
                                    int d3, int h3, int c3,
                                    int d4, int h4, int c4) {
        p.levelUpgrades.add(new Upgrade(2, UNSPECIFIED_COST,
                UNSPECIFIED_COST, d2, h2, c2));
        p.levelUpgrades.add(new Upgrade(3, UNSPECIFIED_COST,
                UNSPECIFIED_COST, d3, h3, c3));
        p.levelUpgrades.add(new Upgrade(4, UNSPECIFIED_COST,
                UNSPECIFIED_COST, d4, h4, c4));
    }
}
