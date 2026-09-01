package ir.ac.pvz.model.support;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.DamageMode;
import ir.ac.pvz.model.enums.PlantTag;
import ir.ac.pvz.model.enums.ProjectileTrajectory;
import ir.ac.pvz.model.enums.ProjectileType;
import ir.ac.pvz.model.plants.ShooterPlant;

final class ProjectileKinds {
    private ProjectileKinds() {
    }

    static ProjectileType resolveType(Plant plant, ProjectileType type) {
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

    static DamageMode damageModeFor(ProjectileType type) {
        if (type == ProjectileType.POISON) {
            return DamageMode.IGNORE_ARMOR;
        }

        return DamageMode.ARMOR_FIRST;
    }

    static ProjectileTrajectory trajectoryForAttack(
            ProjectileType originalType, ProjectileType resolvedType) {
        if (originalType == ProjectileType.LOBBED) {
            return ProjectileTrajectory.ARC;
        }

        return trajectoryFor(resolvedType);
    }

    static ProjectileTrajectory trajectoryFor(ProjectileType type) {
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

    static ProjectileType projectileTypeFor(Plant plant) {
        if (plant instanceof ShooterPlant) {
            return ((ShooterPlant) plant).projectileType;
        }

        return ProjectileType.PEA;
    }

    static float chillDurationFor(Plant plant) {
        if (plant.level >= 3) {
            return 5f;
        }

        return 3f;
    }

    static void applyPostHitEffects(Plant plant, Zombie zombie,
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
            applyPoison(plant, zombie);
        }
    }

    private static void applyPoison(Plant plant, Zombie zombie) {
        int poisonDamage = BalanceDefaults.BASE_POISON_DPS;

        if (plant.level >= 2) {
            poisonDamage = BalanceDefaults.UPGRADED_POISON_DPS;
        }

        zombie.poison(poisonDamage, BalanceDefaults.POISON_DURATION_SECONDS);
    }
}
