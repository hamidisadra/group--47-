package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.PlantCategory;
import ir.ac.pvz.model.enums.PlantTag;
import ir.ac.pvz.model.interfaces.IWall;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.ContinuousPosition;
import ir.ac.pvz.model.support.ProjectileResolver;
import ir.ac.pvz.model.zombies.Gargantuar;

import java.util.ArrayList;

public class WallPlant extends Plant implements IWall {
    public int reflectDamage;
    public WallPlant(int id, String name, int cost, int baseHp, float rechargeTime,
                     int reflectDamage, PlantTag... tags) {
        super(id, name, cost, baseHp, rechargeTime, 0f, reflectDamage,
                PlantCategory.WALL, tags);
        this.reflectDamage = reflectDamage;
    }
    @Override
    public void block(Zombie zombie) {
        if (zombie != null && reflectDamage > 0) {
            zombie.takeDamage(reflectDamage);
        }
    }
    @Override
    public int getHealth() {
        return health;
    }
    public static void resolvePassivePlant(Plant plant, GameSession session,
                                           ProjectileResolver resolver) {
        if (plant == null || !plant.isAlive) {
            return;
        }
        String type = plant.getNormalizedType();
        if (type.equals("sweetpotato")) {
            pullAdjacentZombies(plant, session.getBoard());
        } else if (plant.plantTags.contains(PlantTag.TRAP)) {
            ExplosivePlant.resolveNearbyTrap(plant, session, resolver);
        }
    }
    public static void resolveZombiePlantContact(Zombie zombie, Plant plant,
                                                 GameSession session) {
        if (zombie == null || plant == null || !plant.isAlive) {
            return;
        }
        String type = plant.getNormalizedType();
        if (type.equals("hypnoshroom")) {
            HypnoShroom hypno = null;
            if (plant instanceof HypnoShroom) {
                hypno = (HypnoShroom) plant;
            }
            if (hypno != null && hypno.consumeGargantuarPlantFood()) {
                transformEaterToGargantuar(zombie, hypno, session);
            }
            else {
                zombie.setHypnotized(true);
                applyHypnoLevelBuffs(zombie, plant.level);
            }
            plant.die();
            return;
        }
        zombie.onReachPlant(plant);
        if (type.equals("garlic") && plant.isAlive) {
            moveZombieToAdjacentLane(zombie, session.getBoard());
        }
        else if (type.equals("sunbean")) {
            int sunAmount = 5;
            if (plant.level >= 2) {
                sunAmount = 10;
            }
            session.getSunManager().addSuns(sunAmount);
        }
    }
    public static void resolvePlantDeath(Plant plant, GameSession session) {
        if (plant == null || session == null) {
            return;
        }
        String type = plant.getNormalizedType();
        if (type.equals("explodeonut")) {
            ExplosivePlant.explodeArea(plant, plant.location,
                    plant.attackPower, 1, session.getBoard());
        }
    }
    private static void transformEaterToGargantuar(Zombie eater,
                                                   Plant plant,
                                                   GameSession session) {
        ContinuousPosition position = new ContinuousPosition(
                eater.currentPosition.x, eater.lane);
        session.getBoard().removeZombieEverywhere(eater);
        eater.isAlive = false;
        eater.health = 0;
        eater.currentHealth = 0;
        Gargantuar ally = new Gargantuar();
        ally.setIdentity("Gargantuar", "Gargantuar");
        ally.setHypnotized(true);
        applyHypnoLevelBuffs(ally, plant.level);
        session.getBoard().placeZombie(ally, position);
    }
    private static void applyHypnoLevelBuffs(Zombie zombie, int level) {
        if (level >= 3) {
            int bonusHealth = Math.max(1, zombie.currentHealth / 2);
            zombie.health += bonusHealth;
            zombie.currentHealth += bonusHealth;
        }
        if (level >= 4) {
            zombie.attackDamage += Math.max(1, zombie.attackDamage / 2);
            zombie.damageToPlant += Math.max(1, zombie.damageToPlant / 2);
        }
    }
    private static void pullAdjacentZombies(Plant plant, Board board) {
        for (int row = plant.location.y - 1; row <= plant.location.y + 1; row++) {
            if (row == plant.location.y || row < 0 || row >= board.rows) {
                continue;
            }
            for (Zombie zombie : new ArrayList<>(board.getZombiesInLane(row))) {
                zombie.lane = plant.location.y;
                zombie.positionY = plant.location.y;
                zombie.currentPosition.y = plant.location.y;
            }
        }
    }
    private static void moveZombieToAdjacentLane(Zombie zombie, Board board) {
        int targetLane = zombie.lane + 1;
        if (zombie.lane > 0) {
            targetLane = zombie.lane - 1;
        }
        if (targetLane >= 0 && targetLane < board.rows) {
            zombie.lane = targetLane;
            zombie.positionY = targetLane;
            zombie.currentPosition.y = targetLane;
        }
    }

}
