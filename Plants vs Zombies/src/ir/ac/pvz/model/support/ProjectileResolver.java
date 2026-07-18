package ir.ac.pvz.model.support;


import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.core.GameObject;
import ir.ac.pvz.model.enums.DamageMode;
import ir.ac.pvz.model.enums.PlantCategory;
import ir.ac.pvz.model.enums.PlantTag;
import ir.ac.pvz.model.enums.ProjectileTrajectory;
import ir.ac.pvz.model.enums.ProjectileType;
import ir.ac.pvz.model.enums.ZombieEffectType;
import ir.ac.pvz.model.plants.ShooterPlant;
import ir.ac.pvz.model.plants.StrikeThroughPlant;
import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ProjectileResolver {
    private final Map<Plant, Integer> attackCycles = new IdentityHashMap<>();
    private final transient Random random = new Random();
    private final ProjectilePathResolver pathResolver = new ProjectilePathResolver();
    private final ProjectileCollisionResolver collisionResolver =
            new ProjectileCollisionResolver();
    public void resolveMovement(Projectile projectile, Board board) {
        collisionResolver.resolveMovement(projectile, board);
    }
    public void resolveCollision(Projectile projectile, Board board) {
        collisionResolver.resolveCollision(projectile, board);
    }
    public void applyDamage(Projectile projectile, GameObject target) {
        collisionResolver.applyDamage(projectile, target);
    }
    public void applySpecialEffect(Projectile projectile, Zombie target) {
        collisionResolver.applySpecialEffect(projectile, target);
    }
    public boolean isBlockedByTombstone(Projectile projectile,
                                        Tombstone tombstone) {
        return collisionResolver.isBlockedByTombstone(projectile, tombstone);
    }
    public boolean canLobberIgnoreObstacle(Projectile projectile) {
        return collisionResolver.canLobberIgnoreObstacle(projectile);
    }
    public boolean canPoisonIgnoreArmor(Projectile projectile) {
        return collisionResolver.canPoisonIgnoreArmor(projectile);
    }
    public boolean resolvePlantAttack(Plant plant, GameSession session) {
        if (plant == null || !plant.canAct() || session == null) {
            return false;
        }
        String type = plant.getNormalizedType();
        if (attackOctopusBlock(plant, session.getBoard())) {
            return true;
        }
        if (plant.category == PlantCategory.HOMING) {
            return attackHomingPlant(plant, type, session);
        }
        if (plant.category == PlantCategory.STRIKE_THROUGH) {
            return attackStrikeThroughPlant(plant, type, session);
        }
        if (plant.category == PlantCategory.LOBBER) {
            return attackLobberPlant(plant, type, session);
        }
        if (plant.category == PlantCategory.MELEE) {
            return attackMeleePlant(plant, type, session);
        }
        if (plant.category == PlantCategory.SHOOTER) {
            return attackShooterPlant(plant, type, session);
        }
        return false;
    }

    private boolean attackOctopusBlock(Plant attacker, Board board) {
        Plant target = null;
        double bestDistance = Double.MAX_VALUE;
        for (Plant candidate : board.getAllPlants()) {
            if (!candidate.isOctopusBlocked || candidate.blockingOctopus == null
                    || candidate.blockingOctopus.health <= 0 || candidate == attacker) {
                continue;
            }
            if (attacker.category != PlantCategory.HOMING
                    && candidate.location.y != attacker.location.y) {
                continue;
            }
            if (attacker.category != PlantCategory.HOMING
                    && candidate.location.x < attacker.location.x) {
                continue;
            }
            double candidateDistance = distance(attacker.location,
                    new ContinuousPosition(candidate.location.x,
                            candidate.location.y));
            if (candidateDistance < bestDistance) {
                target = candidate;
                bestDistance = candidateDistance;
            }
        }
        if (target == null) {
            return false;
        }
        target.blockingOctopus.takeDamage(Math.max(1, attacker.attackPower));
        return true;
    }

    private boolean attackShooterPlant(Plant plant, String type, GameSession session) {
        Board board = session.getBoard();
        if (type.equals("threepeater")) {
            return attackThreeLanes(plant, board);
        }
        if (type.equals("rotobaga")) {
            return attackDiagonalLanes(plant, board);
        }
        if (type.equals("splitpea")) {
            return attackSplitPea(plant, board);
        }
        if (type.equals("starfruit")) {
            return attackStarfruit(plant, board);
        }
        Zombie target = nearestAhead(plant, board);
        if (target == null) {
            return false;
        }
        int damage = getShooterDamage(plant, target, board);
        if (type.equals("bowlingbulb")) {
            damage = nextBowlingDamage(plant);
            hitZombie(plant, target, damage, projectileTypeFor(plant), board);
            ProjectilePlantSupport.bounceBowlingBulb(
                    this, plant, target, damage, board);
            return true;
        }
        int shots = plant instanceof ShooterPlant ? ((ShooterPlant) plant).multiShot : 1;
        float plantFoodChance = plant.level >= 3
                ? BalanceDefaults.MEGA_GATLING_LEVEL_THREE_PLANT_FOOD_CHANCE
                : BalanceDefaults.MEGA_GATLING_PLANT_FOOD_CHANCE;
        if (type.equals("megagatlingpea")
                && random.nextFloat() < plantFoodChance) {
            shots += 4;
        }
        for (int shot = 0; shot < Math.max(1, shots) && !target.isDead(); shot++) {
            hitZombie(plant, target, damage, projectileTypeFor(plant), board);
        }
        return true;
    }
    private boolean attackHomingPlant(Plant plant, String type, GameSession session) {
        List<Zombie> zombies = session.getBoard().getAllAliveZombies();
        if (zombies.isEmpty()) {
            return false;
        }
        if (type.equals("magnetshroom")) {
            return removeMagneticArmor(plant, zombies);
        }
        Zombie target = chooseHomingTarget(plant, zombies);
        if (type.equals("caulipower")) {
            target.setHypnotized(true);
        } else if (type.equals("electricblueberry")) {
            killZombie(plant, target);
        } else {
            hitZombie(plant, target, plant.attackPower, ProjectileType.HOMING,
                    session.getBoard());
        }
        return true;
    }
    private boolean attackStrikeThroughPlant(Plant plant, String type, GameSession session) {
        List<Zombie> zombies = aliveAheadInLane(plant, session.getBoard());
        if (zombies.isEmpty()) {
            return false;
        }
        int limit = plant instanceof StrikeThroughPlant
                ? ((StrikeThroughPlant) plant).pierceCount : zombies.size();
        if (type.equals("fumeshroom")) {
            limit = zombies.size();
        }
        for (int i = 0; i < zombies.size() && i < limit; i++) {
            hitZombie(plant, zombies.get(i), plant.attackPower, ProjectileType.PIERCING,
                    session.getBoard());
        }
        return true;
    }
    private boolean attackLobberPlant(Plant plant, String type, GameSession session) {
        Zombie target = nearestAhead(plant, session.getBoard());
        if (target == null) {
            return false;
        }
        int damage = plant.attackPower;
        float butterChance = plant.level >= 2
                ? BalanceDefaults.KERNEL_LEVEL_TWO_BUTTER_CHANCE
                : BalanceDefaults.KERNEL_BUTTER_CHANCE;
        if (type.equals("kernelpult") && random.nextFloat() < butterChance) {
            damage = 40;
            target.stun(BalanceDefaults.KERNEL_BUTTER_STUN_SECONDS);
        }
        hitZombie(plant, target, damage, ProjectileType.LOBBED, session.getBoard());
        if (plant.plantTags.contains(PlantTag.AOE)) {
            int splashDamage = damage + (plant.level >= 3 ? 15 : 0);
            damageAreaAroundZombie(target, splashDamage, 1, session.getBoard(), target);
        }
        if (type.equals("wintermelon")) {
            for (Zombie zombie : session.getBoard().getZombiesAround(
                    new GridPosition((int) target.currentPosition.x, target.lane), 1)) {
                zombie.chill(0.5f, plant.level >= 3 ? 5f : 3f);
            }
        }
        return true;
    }
    private boolean attackMeleePlant(Plant plant, String type, GameSession session) {
        Board board = session.getBoard();
        List<Zombie> targets;
        if (type.equals("phatbeet") || type.equals("kiwibeast")) {
            targets = board.getZombiesAround(plant.location, 1);
        } else {
            targets = zombiesFrontAndBack(plant, plant.level >= 3
                    && type.equals("wasabiwhip") ? 2 : 1, board);
        }
        if (targets.isEmpty()) {
            return false;
        }
        int damage = type.equals("kiwibeast") ? kiwibeastDamage(plant) : plant.attackPower;
        if (type.equals("chomper")) {
            killZombie(plant, targets.get(0));
        } else {
            for (Zombie zombie : targets) {
                hitZombie(plant, zombie, damage, type.equals("wasabiwhip")
                        ? ProjectileType.FIRE : ProjectileType.PEA, board);
            }
        }
        return true;
    }

    public void killZombie(Plant source, Zombie target) {
        if (target == null || target.isDead()) {
            return;
        }
        target.lastDamageSource = source;
        target.receiveInstantKill(ProjectileTrajectory.STRAIGHT);
    }

    public void hitZombie(Plant plant, Zombie zombie, int damage, ProjectileType type,
                          Board board) {
        if (zombie == null || zombie.isDead()) {
            return;
        }
        DamageMode mode = type == ProjectileType.POISON
                ? DamageMode.IGNORE_ARMOR : DamageMode.ARMOR_FIRST;
        ProjectileType resolvedType = type;
        int resolvedDamage = damage;
        if (type == ProjectileType.LOBBED && plant.plantTags.contains(PlantTag.FIRE)) {
            resolvedType = ProjectileType.FIRE;
        } else if (type == ProjectileType.LOBBED
                && plant.plantTags.contains(PlantTag.ICE)) {
            resolvedType = ProjectileType.ICE;
        }
        if (isPeaProjectile(type) && hasTorchwoodBetween(plant, zombie, board)) {
            Plant torchwood = torchwoodBetween(plant, zombie, board);
            resolvedType = ProjectileType.FIRE;
            resolvedDamage *= torchwood != null && torchwood.isBoostedByPlantFood ? 3 : 2;
        }
        ProjectileTrajectory trajectory = type == ProjectileType.LOBBED
                ? ProjectileTrajectory.ARC : trajectoryFor(resolvedType);
        Projectile projectile = new Projectile(resolvedType, trajectory,
                new ContinuousPosition(plant.location.x, plant.location.y), 0f,
                resolvedDamage, plant, zombie, 0, 0f,
                trajectory != ProjectileTrajectory.STRAIGHT, mode);
        zombie.lastDamageSource = plant;
        pathResolver.deliver(projectile, zombie, board);
        if (resolvedType == ProjectileType.ICE && !projectile.isReflected) {
            zombie.chill(0.5f, plant.level >= 3 ? 5f : 3f);
        }
        if (resolvedType == ProjectileType.FIRE) {
            ProjectilePlantSupport.meltFrozenBlockAt(zombie, board);
        }
        if (type == ProjectileType.POISON) {
            int poisonDamage = plant.level >= 2
                    ? BalanceDefaults.UPGRADED_POISON_DPS
                    : BalanceDefaults.BASE_POISON_DPS;
            zombie.poison(poisonDamage, BalanceDefaults.POISON_DURATION_SECONDS);
        }
    }
    private ProjectileTrajectory trajectoryFor(ProjectileType type) {
        if (type == ProjectileType.LOBBED) {
            return ProjectileTrajectory.ARC;
        }
        if (type == ProjectileType.HOMING) {
            return ProjectileTrajectory.HOMING;
        }
        if (type == ProjectileType.LASER) {
            return ProjectileTrajectory.INSTANT_LINE;
        }
        return ProjectileTrajectory.STRAIGHT;
    }
    private ProjectileType projectileTypeFor(Plant plant) {
        if (plant instanceof ShooterPlant) {
            return ((ShooterPlant) plant).projectileType;
        }
        return ProjectileType.PEA;
    }
    private int getShooterDamage(Plant plant, Zombie target, Board board) {
        return plant.attackPower;
    }
    private int nextBowlingDamage(Plant plant) {
        int cycle = nextCycle(plant, 3);
        float regenerationReduction = plant.level >= 2 ? 1f : 0f;
        if (cycle == 0) {
            plant.actionInterval = Math.max(0.1f, 5f - regenerationReduction);
            return 40;
        }
        if (cycle == 1) {
            plant.actionInterval = Math.max(0.1f, 10f - regenerationReduction);
            return 120;
        }
        plant.actionInterval = Math.max(0.1f, 2f - regenerationReduction);
        return 180;
    }
    private int nextCycle(Plant plant, int length) {
        int cycle = attackCycles.getOrDefault(plant, 0);
        attackCycles.put(plant, (cycle + 1) % length);
        return cycle;
    }
    private boolean attackThreeLanes(Plant plant, Board board) {
        boolean attacked = false;
        for (int row = plant.location.y - 1; row <= plant.location.y + 1; row++) {
            Zombie target = board.getNearestZombieAhead(plant.location.x, row);
            if (target != null) {
                hitZombie(plant, target, plant.attackPower, ProjectileType.PEA, board);
                attacked = true;
            }
        }
        return attacked;
    }
    private boolean attackDiagonalLanes(Plant plant, Board board) {
        boolean attacked = false;
        int[] rows = {plant.location.y - 1, plant.location.y + 1};
        for (int row : rows) {
            Zombie ahead = board.getNearestZombieAhead(plant.location.x, row);
            Zombie behind = board.getNearestZombieBehind(plant.location.x, row);
            attacked |= hitIfPresent(plant, ahead, board, 3);
            attacked |= hitIfPresent(plant, behind, board, 3);
        }
        return attacked;
    }
    private boolean attackSplitPea(Plant plant, Board board) {
        Zombie ahead = nearestAhead(plant, board);
        Zombie behind = board.getNearestZombieBehind(plant.location.x, plant.location.y);
        boolean attacked = hitIfPresent(plant, ahead, board, 1);
        return hitIfPresent(plant, behind, board, 2) || attacked;
    }
    private boolean attackStarfruit(Plant plant, Board board) {
        boolean attacked = false;
        Zombie forward = board.getNearestZombieAhead(plant.location.x, plant.location.y);
        Zombie upperForward = board.getNearestZombieAhead(plant.location.x, plant.location.y - 1);
        Zombie lowerForward = board.getNearestZombieAhead(plant.location.x, plant.location.y + 1);
        Zombie upperBack = board.getNearestZombieBehind(plant.location.x, plant.location.y - 1);
        Zombie lowerBack = board.getNearestZombieBehind(plant.location.x, plant.location.y + 1);
        attacked |= hitIfPresent(plant, forward, board, 1);
        attacked |= hitIfPresent(plant, upperForward, board, 1);
        attacked |= hitIfPresent(plant, lowerForward, board, 1);
        attacked |= hitIfPresent(plant, upperBack, board, 1);
        attacked |= hitIfPresent(plant, lowerBack, board, 1);
        return attacked;
    }
    private boolean hitIfPresent(Plant plant, Zombie target, Board board, int shots) {
        if (target == null) {
            return false;
        }
        for (int i = 0; i < shots && !target.isDead(); i++) {
            hitZombie(plant, target, plant.attackPower, projectileTypeFor(plant), board);
        }
        return true;
    }
    private Zombie chooseHomingTarget(Plant plant, List<Zombie> zombies) {
        if (plant.getNormalizedType().equals("electricblueberry") && plant.level >= 3) {
            return zombies.stream().max(Comparator.comparingInt(zombie -> zombie.currentHealth))
                    .orElse(zombies.get(0));
        }
        if (plant.getNormalizedType().equals("caulipower")) {
            return zombies.get(random.nextInt(zombies.size()));
        }
        return zombies.stream().min(Comparator.comparingDouble(zombie ->
                distance(plant.location, zombie.currentPosition))).orElse(zombies.get(0));
    }
    private boolean removeMagneticArmor(Plant plant, List<Zombie> zombies) {
        Zombie target = zombies.stream().filter(this::hasMagneticArmor)
                .filter(zombie -> ProjectilePlantSupport.isWithinAttackRange(
                        plant, zombie))
                .min(Comparator.comparingDouble(zombie ->
                        distance(plant.location, zombie.currentPosition))).orElse(null);
        if (target == null) {
            return false;
        }
        removeOneMagneticArmor(target);
        return true;
    }
    private boolean hasMagneticArmor(Zombie zombie) {
        return zombie.armorPieces.stream().anyMatch(piece -> piece.magnetic);
    }
    private void removeOneMagneticArmor(Zombie zombie) {
        for (int i = 0; i < zombie.armorPieces.size(); i++) {
            if (zombie.armorPieces.get(i).magnetic) {
                zombie.armorPieces.remove(i);
                zombie.armor = zombie.armorPieces.isEmpty() ? null : zombie.armorPieces.get(0);
                return;
            }
        }
    }
    private void removeAllMagneticArmor(Zombie zombie) {
        zombie.armorPieces.removeIf(piece -> piece.magnetic);
        zombie.armor = zombie.armorPieces.isEmpty() ? null : zombie.armorPieces.get(0);
    }
    public void freezeAllZombies(Board board, int ticks) {
        board.getAllAliveZombies().forEach(zombie -> zombie.freeze(ticks));
    }
    public void damageAreaAroundZombie(Zombie center, int damage, int radius,
                                       Board board, Zombie excluded) {
        GridPosition position = new GridPosition((int) Math.floor(center.currentPosition.x), center.lane);
        for (Zombie zombie : board.getZombiesAround(position, radius)) {
            if (zombie != excluded) {
                zombie.takeDamage(damage);
            }
        }
    }
    public void resetSamePlantAges(Plant source, Board board) {
        for (int row = 0; row < board.rows; row++) {
            for (Plant plant : board.getPlantsInLane(row)) {
                if (plant.getNormalizedType().equals(source.getNormalizedType())) {
                    plant.resetAge();
                }
            }
        }
    }
    private int kiwibeastDamage(Plant plant) {
        if (plant.level >= 4 && plant.getAgeSeconds() >= 120f) return 60;
        if (plant.getAgeSeconds() >= 72f) return 45;
        if (plant.getAgeSeconds() >= 24f) return 30;
        return 15;
    }
    private void addOrRefreshEffect(Zombie zombie, ZombieEffectType type, float seconds) {
        for (ZombieEffect effect : zombie.effects) {
            if (effect.type == type) {
                effect.remainingSeconds = Math.max(effect.remainingSeconds, seconds);
                return;
            }
        }
        zombie.effects.add(new ZombieEffect(type, seconds));
    }
    private boolean isPeaProjectile(ProjectileType type) {
        return type == ProjectileType.PEA || type == ProjectileType.ICE
                || type == ProjectileType.POISON;
    }
    private boolean hasTorchwoodBetween(Plant plant, Zombie zombie, Board board) {
        return torchwoodBetween(plant, zombie, board) != null;
    }
    private Plant torchwoodBetween(Plant plant, Zombie zombie, Board board) {
        for (Plant candidate : board.getPlantsInLane(plant.location.y)) {
            if (candidate.getNormalizedType().equals("torchwood")
                    && candidate.location.x > plant.location.x
                    && candidate.location.x < zombie.currentPosition.x) {
                return candidate;
            }
        }
        return null;
    }
    public Zombie nearestAhead(Plant plant, Board board) {
        Zombie target = board.getNearestZombieAhead(plant.location.x, plant.location.y);
        return target != null && ProjectilePlantSupport
                .isWithinAttackRange(plant, target) ? target : null;
    }
    public List<Zombie> aliveAheadInLane(Plant plant, Board board) {
        List<Zombie> result = new ArrayList<>();
        for (Zombie zombie : board.getZombiesInLane(plant.location.y)) {
            if (!zombie.isDead() && zombie.currentPosition.x >= plant.location.x
                    && ProjectilePlantSupport.isWithinAttackRange(
                    plant, zombie)) {
                result.add(zombie);
            }
        }
        result.sort(Comparator.comparingDouble(zombie -> zombie.currentPosition.x));
        return result;
    }
    private List<Zombie> zombiesFrontAndBack(Plant plant, int range, Board board) {
        List<Zombie> result = new ArrayList<>();
        for (Zombie zombie : board.getZombiesInLane(plant.location.y)) {
            if (!zombie.isDead() && Math.abs(zombie.currentPosition.x - plant.location.x) <= range) {
                result.add(zombie);
            }
        }
        return result;
    }
    public List<Zombie> randomTargets(List<Zombie> source, int count) {
        List<Zombie> copy = new ArrayList<>(source);
        java.util.Collections.shuffle(copy, random);
        return new ArrayList<>(copy.subList(0, Math.min(count, copy.size())));
    }
    private double distance(GridPosition plant, ContinuousPosition zombie) {
        double dx = plant.x - zombie.x;
        double dy = plant.y - zombie.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
