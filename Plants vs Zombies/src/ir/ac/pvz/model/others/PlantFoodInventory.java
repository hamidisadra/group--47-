package ir.ac.pvz.model.others;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.PlantCategory;
import ir.ac.pvz.model.enums.ProjectileType;
import ir.ac.pvz.model.plants.*;
import ir.ac.pvz.model.support.BalanceDefaults;
import ir.ac.pvz.model.support.Board;
import ir.ac.pvz.model.support.GridPosition;
import ir.ac.pvz.model.support.ProjectileResolver;
import ir.ac.pvz.model.support.Tile;

import java.util.ArrayList;
import java.util.List;

public class PlantFoodInventory {

    public int count;
    public int maxCapacity;

    public PlantFoodInventory(int maxCapacity) {
        this.count = 0;
        this.maxCapacity = maxCapacity;
    }

    public boolean addFromGlowingZombie(Zombie zombie) {
        if (zombie == null || !zombie.isGlowing || count >= maxCapacity) {
            return false;
        }
        count++;
        System.out.println("The glowing zombie dropeed a plant food; you have "
                + count + " plant foods now.");
        return true;
    }

    public boolean feedPlant(Plant plant) {
        if (plant == null || count <= 0) {
            return false;
        }
        count--;
        plant.applyPlantFoodEffect();
        return true;
    }

    public boolean feedPlant(Plant plant, GameSession session,
                             ProjectileResolver resolver) {
        if (plant == null || count <= 0 || session == null || resolver == null) {
            return false;
        }
        count--;
        applyPlantFood(plant, session, resolver);
        return true;
    }

    public boolean boostPlant(Plant plant, GameSession session,
                              ProjectileResolver resolver) {
        if (plant == null || session == null || resolver == null) {
            return false;
        }
        applyPlantFood(plant, session, resolver);
        return true;
    }

    public boolean cheatAddPlantFood() {
        if (count >= maxCapacity) {
            return false;
        }
        count++;
        return true;
    }

    public int getCount() {
        return count;
    }

    private void applyPlantFood(Plant plant, GameSession session,
                                ProjectileResolver resolver) {
        plant.applyPlantFoodEffect();
        String type = plant.getNormalizedType();
        if (plant instanceof SunProducerPlant) {
            int amount = ((SunProducerPlant) plant).consumeQueuedPlantFoodSun();
            session.getSunManager().produceBonusPlantSun(plant, amount);
        }
        else if (plant.category == PlantCategory.SHOOTER) {
            applyShooterPlantFood(plant, type, session, resolver);
        }
        else if (plant.category == PlantCategory.HOMING) {
            applyHomingPlantFood(plant, type, session, resolver);
        }
        else if (plant.category == PlantCategory.LOBBER) {
            applyLobberPlantFood(plant, type, session, resolver);
        }
        else if (plant.category == PlantCategory.STRIKE_THROUGH) {
            applyStrikePlantFood(plant, type, session, resolver);
        }
        else if (plant.category == PlantCategory.EXPLOSIVE) {
            applyExplosivePlantFood(plant, type, session, resolver);
        }
        else if (plant.category == PlantCategory.MELEE) {
            applyMeleePlantFood(plant, type, session, resolver);
        }
        else if (plant.category == PlantCategory.WALL) {
            applyWallPlantFood(plant, type, session, resolver);
        }
        else if (plant.category == PlantCategory.MODIFIER) {
            applyModifierPlantFood(plant, type, session, resolver);
        }
    }

    private void applyShooterPlantFood(Plant plant, String type,
                                       GameSession session, ProjectileResolver resolver) {
        Board board = session.getBoard();
        if (type.equals("citron")) {
            for (Zombie zombie : board.getZombiesInLane(plant.location.y)) {
                resolver.killZombie(plant, zombie);
            }
        }
        else if (type.equals("repeater")) {
            applyRepeatedAttacks(plant, session, resolver, 5);
            Zombie target = resolver.nearestAhead(plant, board);
            if (target != null) {
                resolver.hitZombie(plant, target, plant.attackPower * 20,
                        ProjectileType.PEA, board);
            }
        }
        else if (type.equals("threepeater")) {
            attackAllLanes(plant, board, resolver);
        }
        else if (type.equals("snowpea")) {
            int freezeTicks = plant.level >= 3 ? 50 : 30;
            board.getZombiesInLane(plant.location.y)
                    .forEach(zombie -> zombie.freeze(freezeTicks));
            applyRepeatedAttacks(plant, session, resolver, 5);
        }
        else if (type.equals("bowlingbulb")) {
            attackExplosiveBulbs(plant, board, resolver);
        }
        else if (type.equals("megagatlingpea")) {
            attackMegaGatlingFood(plant, session, resolver);
        }
        else if (type.equals("peapod")) {
            attackPeaPodFood(plant, board, resolver);
        }
        else if (type.equals("firepeashooter") || type.equals("goopeashooter")) {
            attackWholeLane(plant, board, resolver);
        }
        else if (type.equals("seashroom") || type.equals("puffshroom")) {
            resolver.resetSamePlantAges(plant, board);
            applyRepeatedAttacks(plant, session, resolver, 5);
        }
        else {
            applyRepeatedAttacks(plant, session, resolver, 5);
        }
    }

    private void attackAllLanes(Plant plant, Board board, ProjectileResolver resolver) {
        for (int row = 0; row < board.rows; row++) {
            Zombie target = board.getNearestZombieAhead(plant.location.x, row);
            if (target != null) {
                resolver.hitZombie(plant, target, plant.attackPower,
                        ProjectileType.PEA, board);
            }
        }
    }

    private void attackExplosiveBulbs(Plant plant, Board board,
                                      ProjectileResolver resolver) {
        for (Zombie target : resolver.randomTargets(board.getAllAliveZombies(), 3)) {
            resolver.hitZombie(plant, target, 180, ProjectileType.PIERCING, board);
            resolver.damageAreaAroundZombie(target, 180, 1, board, target);
        }
    }

    private void attackMegaGatlingFood(Plant plant, GameSession session,
                                       ProjectileResolver resolver) {
        applyRepeatedAttacks(plant, session, resolver, 5);
        Zombie target = resolver.nearestAhead(plant, session.getBoard());
        for (int i = 0; i < 4 && target != null && !target.isDead(); i++) {
            resolver.hitZombie(plant, target, plant.attackPower * 20,
                    ProjectileType.PEA, session.getBoard());
        }
    }

    private void attackPeaPodFood(Plant plant, Board board,
                                  ProjectileResolver resolver) {
        int heads = plant instanceof ShooterPlant
                ? Math.min(5, Math.max(1, ((ShooterPlant) plant).multiShot)) : 1;
        Zombie target = resolver.nearestAhead(plant, board);
        for (int i = 0; i < heads && target != null && !target.isDead(); i++) {
            resolver.hitZombie(plant, target, plant.attackPower * 20,
                    ProjectileType.PEA, board);
        }
    }

    private void attackWholeLane(Plant plant, Board board,
                                 ProjectileResolver resolver) {
        ProjectileType type = plant.getNormalizedType().equals("goopeashooter")
                ? ProjectileType.POISON : ProjectileType.FIRE;
        for (Zombie zombie : board.getZombiesInLane(plant.location.y)) {
            resolver.hitZombie(plant, zombie, plant.attackPower, type, board);
        }
    }

    private void applyHomingPlantFood(Plant plant, String type,
                                      GameSession session, ProjectileResolver resolver) {
        List<Zombie> zombies = session.getBoard().getAllAliveZombies();
        if (type.equals("electricblueberry")) {
            for (Zombie zombie : resolver.randomTargets(zombies, 3)) {
                resolver.killZombie(plant, zombie);
            }
        }
        else if (type.equals("caulipower")) {
            resolver.randomTargets(zombies, Math.min(3, zombies.size()))
                    .forEach(zombie -> zombie.setHypnotized(true));
        }
        else if (type.equals("magnetshroom")) {
            for (Zombie zombie : zombies) {
                zombie.armorPieces.removeIf(piece -> piece.magnetic);
                zombie.armor = zombie.armorPieces.isEmpty()
                        ? null : zombie.armorPieces.get(0);
            }
        }
        else {
            applyRepeatedAttacks(plant, session, resolver, 5);
        }
    }

    private void applyLobberPlantFood(Plant plant, String type,
                                      GameSession session, ProjectileResolver resolver) {
        List<Zombie> zombies = session.getBoard().getAllAliveZombies();
        if (type.equals("kernelpult")) {
            zombies.forEach(zombie -> zombie.stun(
                    BalanceDefaults.KERNEL_BUTTER_STUN_SECONDS));
        }
        else {
            int targetCount = type.equals("pepperpult") ? 3 : Math.min(5, zombies.size());
            for (Zombie zombie : resolver.randomTargets(zombies, targetCount)) {
                resolver.hitZombie(plant, zombie, plant.attackPower,
                        ProjectileType.LOBBED, session.getBoard());
            }
        }
    }

    private void applyStrikePlantFood(Plant plant, String type,
                                      GameSession session, ProjectileResolver resolver) {
        List<Zombie> zombies = resolver.aliveAheadInLane(plant, session.getBoard());
        for (Zombie zombie : zombies) {
            resolver.hitZombie(plant, zombie, plant.attackPower,
                    ProjectileType.PIERCING, session.getBoard());
            if (type.equals("fumeshroom")) {
                zombie.currentPosition.x = Math.min(session.getBoard().columns - 1,
                        zombie.currentPosition.x + 1f);
            }
        }
    }

    private void applyExplosivePlantFood(Plant plant, String type,
                                         GameSession session, ProjectileResolver resolver) {
        if (type.equals("potatomine") || type.equals("primalpotatomine")) {
            cloneMineToEmptyTiles(plant, session.getBoard(), 2);
        }
        else if (type.equals("squash")) {
            for (Zombie zombie : resolver.randomTargets(
                    session.getBoard().getAllAliveZombies(), 2)) {
                resolver.killZombie(plant, zombie);
            }
        }
        else if (type.equals("tanglekelp")) {
            int targetCount = plant.level >= 3 ? 3 : 2;
            for (Zombie zombie : resolver.randomTargets(
                    session.getBoard().getWaterZombies(), targetCount)) {
                resolver.killZombie(plant, zombie);
            }
        }
        else if (type.equals("iceberglettuce")) {
            resolver.freezeAllZombies(session.getBoard(), plant.level >= 3 ? 50 : 30);
        }
    }

    private void applyMeleePlantFood(Plant plant, String type,
                                     GameSession session, ProjectileResolver resolver) {
        List<Zombie> targets = session.getBoard().getZombiesAround(plant.location, 1);
        if (type.equals("chomper")) {
            for (Zombie zombie : resolver.randomTargets(
                    session.getBoard().getAllAliveZombies(), 3)) {
                resolver.killZombie(plant, zombie);
            }
            return;
        }
        int damage = type.equals("kiwibeast") ? 45 : plant.attackPower * 5;
        for (Zombie zombie : targets) {
            resolver.hitZombie(plant, zombie, damage, type.equals("wasabiwhip")
                    ? ProjectileType.FIRE : ProjectileType.PEA, session.getBoard());
        }
    }

    private void applyWallPlantFood(Plant plant, String type,
                                    GameSession session, ProjectileResolver resolver) {
        if (type.equals("wallnut") || type.equals("tallnut")) {
            return;
        }
        if (type.equals("sweetpotato")) {
            pullAdjacentZombies(plant, session.getBoard());
            plant.health = plant.baseHp;
            plant.currentHp = plant.baseHp;
        }
        else if (type.equals("garlic")) {
            for (Zombie zombie : session.getBoard().getZombiesInLane(plant.location.y)) {
                moveZombieToAdjacentLane(zombie, session.getBoard());
            }
        }
        else if (type.equals("endurian")) {
            addHealth(plant, BalanceDefaults.ENDURIAN_PLANT_FOOD_ARMOR);
            if (plant instanceof WallPlant) {
                ((WallPlant) plant).reflectDamage += 20;
            }
        }
        else if (type.equals("explodeonut")
                && plant instanceof ExplodeONut) {
            ((ExplodeONut) plant).equipMetalArmor(
                    BalanceDefaults.EXPLODE_O_NUT_PLANT_FOOD_ARMOR);
        }
        else if (type.equals("pumpkin")) {
            addHealth(plant, BalanceDefaults.PUMPKIN_PLANT_FOOD_ARMOR);
        }
        else if (type.equals("sunbean")) {
            addHealth(plant, BalanceDefaults.SUN_BEAN_PLANT_FOOD_ARMOR);
        }
    }

    private void applyModifierPlantFood(Plant plant, String type,
                                        GameSession session, ProjectileResolver resolver) {
        if (type.equals("torchwood")) {
            plant.isBoostedByPlantFood = true;
        }
        else if (type.equals("lilypad")) {
            cloneLilyPads(session.getBoard());
        }
    }

    public void applyMint(Plant mint, GameSession session,
                          ProjectileResolver resolver) {
        if (!(mint instanceof MintPlant)) {
            return;
        }
        PlantCategory family = ((MintPlant) mint).familyCategory;
        for (int row = 0; row < session.getBoard().rows; row++) {
            for (Plant plant : session.getBoard().getPlantsInLane(row)) {
                if (plant != mint && plant.category == family) {
                    applyPlantFood(plant, session, resolver);
                }
            }
        }
        if (mint.level >= 4) {
            session.resetFamilyCooldown(family);
        }
        MintPlant mintPlant = (MintPlant) mint;
        mint.setLifeSpanSeconds(mintPlant.durationSeconds);
    }

    private void applyRepeatedAttacks(Plant plant, GameSession session,
                                      ProjectileResolver resolver, int attacks) {
        for (int i = 0; i < attacks; i++) {
            if (!resolver.resolvePlantAttack(plant, session)) {
                return;
            }
        }
    }

    private void cloneMineToEmptyTiles(Plant source, Board board, int count) {
        List<Tile> emptyTiles = new ArrayList<>();
        for (int row = 0; row < board.rows; row++) {
            for (int x = 0; x < board.columns; x++) {
                Tile tile = board.getTile(new GridPosition(x, row));
                if (tile != null && tile.canPlant && tile.getPlants().isEmpty()) {
                    emptyTiles.add(tile);
                }
            }
        }
        java.util.Collections.shuffle(emptyTiles);
        for (int i = 0; i < count && i < emptyTiles.size(); i++) {
            Plant clone = Plant.createSpreadsheetPlant(0, source.type);
            if (clone != null) {
                clone.isBoostedByPlantFood = true;
                emptyTiles.get(i).addPlant(clone);
            }
        }
    }

    private void cloneLilyPads(Board board) {
        List<Tile> waterTiles = new ArrayList<>();
        for (int row = 0; row < board.rows; row++) {
            for (int x = 0; x < board.columns; x++) {
                Tile tile = board.getTile(new GridPosition(x, row));
                if (tile != null && tile.isWater && tile.getPlants().isEmpty()) {
                    waterTiles.add(tile);
                }
            }
        }
        java.util.Collections.shuffle(waterTiles);
        for (int i = 0; i < 3 && i < waterTiles.size(); i++) {
            waterTiles.get(i).addPlant(Plant.createSpreadsheetPlant(0, "Lily Pad"));
        }
    }

    private void pullAdjacentZombies(Plant plant, Board board) {
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

    private void moveZombieToAdjacentLane(Zombie zombie, Board board) {
        int targetLane = zombie.lane > 0 ? zombie.lane - 1 : zombie.lane + 1;
        if (targetLane >= 0 && targetLane < board.rows) {
            zombie.lane = targetLane;
            zombie.positionY = targetLane;
            zombie.currentPosition.y = targetLane;
        }
    }

    private void addHealth(Plant plant, int amount) {
        plant.baseHp += amount;
        plant.health += amount;
        plant.currentHp += amount;
    }
}
