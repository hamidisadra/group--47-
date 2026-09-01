package ir.ac.pvz.model.others;

import ir.ac.pvz.controller.game_core.PlantFoodBoardSupport;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.ProjectileType;
import ir.ac.pvz.model.plants.ShooterPlant;
import ir.ac.pvz.model.zombies.Gargantuar;
import ir.ac.pvz.model.support.ContinuousPosition;
import ir.ac.pvz.model.support.BalanceDefaults;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.ProjectileResolver;
import java.util.List;

final class PlantFoodCategoryEffects {
    private PlantFoodCategoryEffects() {
    }

    static void applyShooterPlantFood(Plant plant, String type,
                                      GameSession session,
                                      ProjectileResolver resolver) {
        Board board = session.getBoard();

        if (applyDirectShooterFood(plant, type, session, resolver, board)) {
            return;
        }

        if (applyDelegatedShooterFood(plant, type, session, resolver, board)) {
            return;
        }

        PlantFoodBoardSupport.applyRepeatedAttacks(plant, session, resolver, 5);
    }

    private static boolean applyDirectShooterFood(Plant plant, String type,
                                                  GameSession session,
                                                  ProjectileResolver resolver,
                                                  Board board) {
        if (type.equals("citron")) {
            for (Zombie zombie : board.getZombiesInLane(plant.location.y)) {
                resolver.killZombie(plant, zombie);
            }

            return true;
        }

        if (type.equals("repeater")) {
            PlantFoodBoardSupport.applyRepeatedAttacks(
                plant, session, resolver, 5);

            Zombie target = resolver.nearestAhead(plant, board);

            if (target != null) {
                resolver.hitZombie(plant, target, plant.attackPower * 20,
                    ProjectileType.PEA, board);
            }

            return true;
        }

        if (type.equals("snowpea")) {
            PlantFoodBoardSupport.applySnowPeaFood(plant, session, resolver);
            return true;
        }

        if (type.equals("seashroom") || type.equals("puffshroom")) {
            resolver.resetSamePlantAges(plant, board);
            PlantFoodBoardSupport.applyRepeatedAttacks(
                plant, session, resolver, 5);

            return true;
        }

        return false;
    }

    private static boolean applyDelegatedShooterFood(Plant plant, String type,
                                                     GameSession session,
                                                     ProjectileResolver resolver,
                                                     Board board) {
        if (type.equals("threepeater")) {
            attackAllLanes(plant, board, resolver,
                BalanceDefaults.THREEPEATER_PLANT_FOOD_SHOTS);
            return true;
        }

        if (type.equals("bowlingbulb")) {
            attackExplosiveBulbs(plant, board, resolver);
            return true;
        }

        if (type.equals("megagatlingpea")) {
            attackMegaGatlingFood(plant, session, resolver);
            return true;
        }

        if (type.equals("peapod")) {
            attackPeaPodFood(plant, board, resolver);
            return true;
        }

        if (type.equals("firepeashooter") || type.equals("goopeashooter")) {
            attackWholeLane(plant, board, resolver);
            return true;
        }

        return false;
    }

    static void attackAllLanes(Plant plant, Board board,
                               ProjectileResolver resolver, int shots) {
        for (int row = 0; row < board.rows; row++) {
            Zombie target = board.getNearestZombieAhead(plant.location.x, row);
            if (target != null) {
                for (int shot = 0; shot < shots && !target.isDead(); shot++) {
                    resolver.hitZombie(plant, target, plant.attackPower,
                        ProjectileType.PEA, board);
                }
            }
        }
    }

    static void attackExplosiveBulbs(Plant plant, Board board,
                                     ProjectileResolver resolver) {
        for (Zombie target : resolver.randomTargets(board.getAllAliveZombies(), 3)) {
            int damage = BalanceDefaults.BOWLING_BULB_PLANT_FOOD_DAMAGE;
            resolver.hitZombie(plant, target, damage,
                ProjectileType.PIERCING, board);
            resolver.damageAreaAroundZombie(target, damage, 1, board, target);
            ir.ac.pvz.model.support.ProjectilePlantSupport.bounceBowlingBulb(
                resolver, plant, target, damage, board);
        }
    }

    static void attackMegaGatlingFood(Plant plant, GameSession session,
                                      ProjectileResolver resolver) {
        PlantFoodBoardSupport.applyRepeatedAttacks(
            plant, session, resolver, 5);
        Zombie target = resolver.nearestAhead(plant, session.getBoard());
        for (int i = 0; i < 4 && target != null && !target.isDead(); i++) {
            resolver.hitZombie(plant, target, plant.attackPower * 20,
                ProjectileType.PEA, session.getBoard());
        }
    }

    static void attackPeaPodFood(Plant plant, Board board,
                                 ProjectileResolver resolver) {
        int heads = 1;
        if (plant instanceof ShooterPlant) {
            ShooterPlant shooter = (ShooterPlant) plant;
            heads = Math.min(5, Math.max(1, shooter.multiShot));
        }

        Zombie target = resolver.nearestAhead(plant, board);
        for (int i = 0; i < heads && target != null && !target.isDead(); i++) {
            resolver.hitZombie(plant, target, plant.attackPower * 20,
                ProjectileType.PEA, board);
        }
    }

    static void attackWholeLane(Plant plant, Board board,
                                ProjectileResolver resolver) {
        ProjectileType type = ProjectileType.FIRE;
        if (plant.getNormalizedType().equals("goopeashooter")) {
            type = ProjectileType.POISON;
        }

        for (Zombie zombie : board.getZombiesInLane(plant.location.y)) {
            resolver.hitZombie(plant, zombie, plant.attackPower, type, board);
        }
    }

    static void applyHomingPlantFood(Plant plant, String type,
                                     GameSession session, ProjectileResolver resolver) {
        List<Zombie> zombies = session.getBoard().getAllAliveZombies();
        if (type.equals("electricblueberry")) {
            int targets = Math.max(1, (int) Math.round(plant.plantFoodValue));
            for (Zombie zombie : resolver.randomTargets(zombies, targets)) {
                resolver.killZombie(plant, zombie);
            }
        }

        else if (type.equals("caulipower")) {
            int targets = Math.max(1, (int) Math.round(plant.plantFoodValue));
            resolver.randomTargets(zombies, Math.min(targets, zombies.size()))
                .forEach(zombie -> zombie.setHypnotized(true));
        }

        else if (type.equals("magnetshroom")) {
            for (Zombie zombie : zombies) {
                zombie.armorPieces.removeIf(piece -> piece.magnetic);
                if (zombie.armorPieces.isEmpty()) {
                    zombie.armor = null;
                } else {
                    zombie.armor = zombie.armorPieces.get(0);
                }
            }
        }

        else {
            PlantFoodBoardSupport.applyRepeatedAttacks(
                plant, session, resolver, 5);
        }
    }

    static void applyLobberPlantFood(Plant plant, String type,
                                     GameSession session, ProjectileResolver resolver) {
        List<Zombie> zombies = session.getBoard().getAllAliveZombies();
        if (type.equals("kernelpult")) {
            zombies.forEach(zombie -> zombie.stun(
                BalanceDefaults.KERNEL_BUTTER_STUN_SECONDS));
        }

        else {
            int targetCount = Math.min(5, zombies.size());
            if (type.equals("pepperpult")) {
                targetCount = 3;
            }

            for (Zombie zombie : resolver.randomTargets(zombies, targetCount)) {
                resolver.hitZombie(plant, zombie, plant.attackPower,
                    ProjectileType.LOBBED, session.getBoard());
            }
        }
    }

    static void applyStrikePlantFood(Plant plant, String type,
                                     GameSession session, ProjectileResolver resolver) {
        List<Zombie> zombies = resolver.aliveAheadInLane(plant, session.getBoard());
        int damage = plant.attackPower;
        if (type.equals("cactus")) {
            damage *= BalanceDefaults.CACTUS_PLANT_FOOD_DAMAGE_MULTIPLIER;
        }

        for (Zombie zombie : zombies) {
            resolver.hitZombie(plant, zombie, damage,
                ProjectileType.PIERCING, session.getBoard());
            if (type.equals("fumeshroom")) {
                zombie.currentPosition.x = Math.min(session.getBoard().columns - 1,
                    zombie.currentPosition.x + 1f);
            }
        }
    }

    static void applyExplosivePlantFood(Plant plant, String type,
                                        GameSession session, ProjectileResolver resolver) {
        if (type.equals("potatomine") || type.equals("primalpotatomine")) {
            PlantFoodBoardSupport.cloneMineToEmptyTiles(
                plant, session.getBoard(),
                Math.max(0, (int) Math.round(plant.plantFoodValue)));
        }

        else if (type.equals("squash")) {
            for (Zombie zombie : resolver.randomTargets(
                session.getBoard().getAllAliveZombies(),
                Math.max(1, (int) Math.round(plant.plantFoodValue)))) {
                resolver.killZombie(plant, zombie);
            }
        }

        else if (type.equals("tanglekelp")) {
            int targetCount = Math.max(1,
                (int) Math.round(plant.plantFoodValue));
            if (plant.level >= 3) {
                targetCount++;
            }

            for (Zombie zombie : resolver.randomTargets(
                session.getBoard().getWaterZombies(), targetCount)) {
                resolver.killZombie(plant, zombie);
            }
        }

        else if (type.equals("iceberglettuce")) {
            int freezeTicks = 30;
            if (plant.level >= 3) {
                freezeTicks = 50;
            }

            resolver.freezeAllZombies(session.getBoard(), freezeTicks);
        }
    }

    static void applyMeleePlantFood(Plant plant, String type,
                                    GameSession session, ProjectileResolver resolver) {
        List<Zombie> targets = session.getBoard().getZombiesAround(plant.location, 1);
        if (type.equals("chomper")) {
            for (Zombie zombie : resolver.randomTargets(
                session.getBoard().getAllAliveZombies(),
                Math.max(1, (int) Math.round(plant.plantFoodValue)))) {
                resolver.killZombie(plant, zombie);
            }

            return;
        }

        int damage = plant.attackPower * 5;
        if (type.equals("kiwibeast")) {
            damage = 45;
        }

        ProjectileType projectileType = ProjectileType.PEA;
        if (type.equals("wasabiwhip")) {
            projectileType = ProjectileType.FIRE;
        }

        for (Zombie zombie : targets) {
            resolver.hitZombie(plant, zombie, damage, projectileType,
                session.getBoard());
        }
    }

    static void applyWallPlantFood(Plant plant, String type,
                                   GameSession session, ProjectileResolver resolver) {
        if (applyWallArmorPlantFood(plant, type)) {
            return;
        }

        applyWallControlPlantFood(plant, type, session);
    }

    static boolean applyWallArmorPlantFood(Plant plant, String type) {
        if (type.equals("wallnut")) {
            PlantFoodBoardSupport.addHealth(plant,
                PlantFoodBoardSupport.dataAmount(plant,
                    BalanceDefaults.WALL_NUT_PLANT_FOOD_ARMOR));
            return true;
        }

        if (type.equals("tallnut")) {
            PlantFoodBoardSupport.addHealth(plant,
                PlantFoodBoardSupport.dataAmount(plant,
                    BalanceDefaults.TALL_NUT_PLANT_FOOD_ARMOR));
            return true;
        }

        if (type.equals("pumpkin")) {
            PlantFoodBoardSupport.addHealth(plant,
                PlantFoodBoardSupport.dataAmount(plant,
                    BalanceDefaults.PUMPKIN_PLANT_FOOD_ARMOR));
            return true;
        }

        if (type.equals("sunbean")) {
            PlantFoodBoardSupport.addHealth(plant,
                PlantFoodBoardSupport.dataAmount(plant,
                    BalanceDefaults.SUN_BEAN_PLANT_FOOD_ARMOR));
            return true;
        }

        return false;
    }

    static void applyWallControlPlantFood(Plant plant, String type,
                                          GameSession session) {
        if (type.equals("sweetpotato")) {
            PlantFoodBoardSupport.pullAdjacentZombies(
                plant, session.getBoard());
            plant.health = plant.baseHp;
            plant.currentHp = plant.baseHp;
        }

        else if (type.equals("garlic")) {
            for (Zombie zombie : session.getBoard().getZombiesInLane(plant.location.y)) {
                PlantFoodBoardSupport.moveZombieToAdjacentLane(
                    zombie, session.getBoard());
            }
        }

        else if (type.equals("endurian")) {
            PlantFoodBoardSupport.addHealth(plant,
                PlantFoodBoardSupport.dataAmount(plant,
                    BalanceDefaults.ENDURIAN_PLANT_FOOD_ARMOR));
            if (plant instanceof ir.ac.pvz.model.plants.WallPlant) {
                ((ir.ac.pvz.model.plants.WallPlant) plant).reflectDamage += 20;
            }
        }

        else if (type.equals("explodeonut")
            && plant instanceof ir.ac.pvz.model.plants.ExplodeONut) {
            ((ir.ac.pvz.model.plants.ExplodeONut) plant).equipMetalArmor(
                PlantFoodBoardSupport.dataAmount(plant,
                    BalanceDefaults.EXPLODE_O_NUT_PLANT_FOOD_ARMOR));
        }
    }

    static void applyModifierPlantFood(Plant plant, String type,
                                       GameSession session, ProjectileResolver resolver) {
        if (type.equals("torchwood")) {
            plant.isBoostedByPlantFood = true;
        }

        else if (type.equals("lilypad")) {
            PlantFoodBoardSupport.cloneLilyPads(
                session.getBoard(), Math.max(0,
                    (int) Math.round(plant.plantFoodValue)));
        }
    }
}
