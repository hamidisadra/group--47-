package ir.ac.pvz.model.support;
import ir.ac.pvz.model.others.GameSession;
import ir.ac.pvz.model.core.GameObject;
import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.DamageMode;
import ir.ac.pvz.model.enums.PlantCategory;
import ir.ac.pvz.model.enums.PlantTag;
import ir.ac.pvz.model.enums.ProjectileTrajectory;
import ir.ac.pvz.model.enums.ProjectileType;
import ir.ac.pvz.model.plants.LobberPlant;
import ir.ac.pvz.model.plants.MeleePlant;
import ir.ac.pvz.model.plants.ShooterPlant;
import ir.ac.pvz.model.plants.StrikeThroughPlant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.random.RandomGenerator;

public class ProjectileResolver {
    private final Map<Plant, Integer> attackCycles = new IdentityHashMap<>();
    private final RandomGenerator random;
    private final ProjectilePathResolver pathResolver = new ProjectilePathResolver();
    private final ProjectileCollisionResolver collisionResolver =
            new ProjectileCollisionResolver();
    public ProjectileResolver() {
        this(new Random());
    }
    public ProjectileResolver(RandomGenerator random) {
        if (random == null) {
            throw new IllegalArgumentException("Random generator cannot be null.");
        }
        this.random = random;
    }
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
        int shots = 1;
        if (plant instanceof ShooterPlant) {
            shots = ((ShooterPlant) plant).multiShot;
        }
        float plantFoodChance = BalanceDefaults.MEGA_GATLING_PLANT_FOOD_CHANCE;
        if (plant.level >= 3) {
            plantFoodChance =
                    BalanceDefaults.MEGA_GATLING_LEVEL_THREE_PLANT_FOOD_CHANCE;
        }
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
        int limit = zombies.size();
        if (plant instanceof StrikeThroughPlant) {
            limit = ((StrikeThroughPlant) plant).pierceCount;
        }
        if (type.equals("fumeshroom")) {
            limit = zombies.size();
        }
        int targetCount = Math.min(zombies.size(), Math.max(0, limit));
        for (int i = 0; i < targetCount; i++) {
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
        float butterChance = BalanceDefaults.KERNEL_BUTTER_CHANCE;
        if (plant.level >= 2) {
            butterChance = BalanceDefaults.KERNEL_LEVEL_TWO_BUTTER_CHANCE;
        }
        if (type.equals("kernelpult") && random.nextFloat() < butterChance) {
            damage = 40;
            target.stun(BalanceDefaults.KERNEL_BUTTER_STUN_SECONDS);
        }
        hitZombie(plant, target, damage, ProjectileType.LOBBED, session.getBoard());
        if (plant.plantTags.contains(PlantTag.AOE)) {
            int splashBonus = 0;
            if (plant.level >= 3) {
                splashBonus = 15;
            }
            int splashDamage = damage + splashBonus;
            damageAreaAroundZombie(target, splashDamage, 1, session.getBoard(), target);
        }
        if (type.equals("wintermelon")) {
            for (Zombie zombie : session.getBoard().getZombiesAround(
                    new GridPosition((int) target.currentPosition.x, target.lane), 1)) {
                zombie.chill(0.5f, chillDurationFor(plant));
            }
        }
        return true;
    }
    private boolean attackMeleePlant(Plant plant, String type, GameSession session) {
        Board board = session.getBoard();
        List<Zombie> targets;
        if (type.equals("phatbeet") || type.equals("kiwibeast")) {
            targets = board.getZombiesAround(plant.location, 1);
        }
        else {
            int range = 1;
            if (plant.level >= 3 && type.equals("wasabiwhip")) {
                range = 2;
            }
            targets = zombiesFrontAndBack(plant, range, board);
        }
        if (targets.isEmpty()) {
            return false;
        }
        int damage = plant.attackPower;
        if (type.equals("kiwibeast")) {
            damage = kiwibeastDamage(plant);
        }
        if (type.equals("chomper")) {
            killZombie(plant, targets.get(0));
        }
        else {
            ProjectileType projectileType = ProjectileType.PEA;
            if (type.equals("wasabiwhip")) {
                projectileType = ProjectileType.FIRE;
            }
            for (Zombie zombie : targets) {
                hitZombie(plant, zombie, damage, projectileType, board);
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
        ProjectileType resolvedType = resolveProjectileType(plant, type);
        int resolvedDamage = damage;
        if (isPeaProjectile(type) && hasTorchwoodBetween(plant, zombie, board)) {
            Plant torchwood = torchwoodBetween(plant, zombie, board);
            resolvedType = ProjectileType.FIRE;
            int multiplier = 2;
            if (torchwood != null && torchwood.isBoostedByPlantFood) {
                multiplier = 3;
            }
            resolvedDamage *= multiplier;
        }
        ProjectileTrajectory trajectory = trajectoryForAttack(type, resolvedType);
        DamageMode mode = damageModeFor(type);
        Projectile projectile = new Projectile(resolvedType, trajectory,
                new ContinuousPosition(plant.location.x, plant.location.y), 0f,
                resolvedDamage, plant, zombie, 0, 0f,
                trajectory != ProjectileTrajectory.STRAIGHT, mode);
        zombie.lastDamageSource = plant;
        pathResolver.deliver(projectile, zombie, board);
        applyPostHitEffects(plant, zombie, type, resolvedType,
                projectile, board);
    }
    private ProjectileType resolveProjectileType(
            Plant plant, ProjectileType type) {
        if (type != ProjectileType.LOBBED) {
            return type;
        }
        if (plant.plantTags.contains(PlantTag.FIRE)) {
            return ProjectileType.FIRE;
        }
        if (plant.plantTags.contains(PlantTag.ICE)) {
            return ProjectileType.ICE;
        }
        return type;
    }
    private DamageMode damageModeFor(ProjectileType type) {
        if (type == ProjectileType.POISON) {
            return DamageMode.IGNORE_ARMOR;
        }
        return DamageMode.ARMOR_FIRST;
    }
    private ProjectileTrajectory trajectoryForAttack(
            ProjectileType originalType, ProjectileType resolvedType) {
        if (originalType == ProjectileType.LOBBED) {
            return ProjectileTrajectory.ARC;
        }
        return trajectoryFor(resolvedType);
    }
    private void applyPostHitEffects(Plant plant, Zombie zombie,
                                     ProjectileType originalType,
                                     ProjectileType resolvedType,
                                     Projectile projectile, Board board) {
        if (resolvedType == ProjectileType.ICE && !projectile.isReflected) {
            zombie.chill(0.5f, chillDurationFor(plant));
        }
        if (resolvedType == ProjectileType.FIRE) {
            ProjectilePlantSupport.meltFrozenBlockAt(zombie, board);
        }
        if (originalType == ProjectileType.POISON) {
            int poisonDamage = BalanceDefaults.BASE_POISON_DPS;
            if (plant.level >= 2) {
                poisonDamage = BalanceDefaults.UPGRADED_POISON_DPS;
            }
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
    private float chillDurationFor(Plant plant) {
        if (plant.level >= 3) {
            return 5f;
        }
        return 3f;
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
        float regenerationReduction = 0f;
        if (plant.level >= 2) {
            regenerationReduction = 1f;
        }
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
        return ProjectileTargetSupport.chooseHomingTarget(
                plant, zombies, random);
    }
    private boolean removeMagneticArmor(Plant plant, List<Zombie> zombies) {
        return MagneticArmorSupport.removeNearest(plant, zombies);
    }
    public void freezeAllZombies(Board board, int ticks) {
        ProjectileTargetSupport.freezeAllZombies(board, ticks);
    }
    public void damageAreaAroundZombie(Zombie center, int damage, int radius,
                                       Board board, Zombie excluded) {
        ProjectileTargetSupport.damageAreaAroundZombie(
                center, damage, radius, board, excluded);
    }
    public void resetSamePlantAges(Plant source, Board board) {
        ProjectileTargetSupport.resetSamePlantAges(source, board);
    }
    private int kiwibeastDamage(Plant plant) {
        return ProjectileTargetSupport.kiwibeastDamage(plant);
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
        return ProjectileTargetSupport.nearestAhead(plant, board);
    }

    public List<Zombie> aliveAheadInLane(Plant plant, Board board) {
        return ProjectileTargetSupport.aliveAheadInLane(plant, board);
    }
    private List<Zombie> zombiesFrontAndBack(Plant plant, int range, Board board) {
        return ProjectileTargetSupport.zombiesFrontAndBack(
                plant, range, board);
    }
    public List<Zombie> randomTargets(List<Zombie> source, int count) {
        return ProjectileTargetSupport.randomTargets(source, count, random);
    }
    private double distance(GridPosition plant, ContinuousPosition zombie) {
        return ProjectileTargetSupport.distance(plant, zombie);
    }
}