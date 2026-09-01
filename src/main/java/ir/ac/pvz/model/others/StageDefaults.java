package ir.ac.pvz.model.others;

import ir.ac.pvz.model.support.BalanceDefaults;
import ir.ac.pvz.model.support.ZombieBaseStats;
import ir.ac.pvz.model.support.ZombieDataRepository;
import ir.ac.pvz.model.support.ZombieDefinition;

import java.util.LinkedHashMap;
import java.util.Map;

final class StageDefaults {
    private StageDefaults() {
    }

    static Map<String, Float> abilityCooldowns() {
        ZombieDataRepository data = ZombieDataRepository.getInstance();
        Map<String, Float> cooldowns = new LinkedHashMap<>();

        cooldowns.put("tombraiserzombie", (float) data.getNumber(
                "TombRaiserZombie", "TimeBetweenRaisings", 6d));
        cooldowns.put("fishermanzombie", (float) data.getNumber(
                "FishermanZombie", "DelayBetweenCasting", 2.5d));
        cooldowns.put("kingzombie", (float) data.getNumber(
                "KingZombie", "DelayBetweenKnightings", 2.5d));
        cooldowns.put("pianistzombie",
                BalanceDefaults.PIANIST_MUSIC_LOOP_SECONDS);
        cooldowns.put("hunterzombie",
                BalanceDefaults.HUNTER_THROW_COOLDOWN_SECONDS);

        return cooldowns;
    }

    static Map<String, Integer> abilityValues() {
        Map<String, Integer> values = new LinkedHashMap<>();

        values.put("huntericehealth", BalanceDefaults.HUNTER_ICE_HEALTH);
        values.put("octopusblockhealth", BalanceDefaults.OCTOPUS_BLOCK_HEALTH);

        return values;
    }

    static Map<String, ZombieBaseStats> zombieBaseStats() {
        Map<String, ZombieBaseStats> stats = new LinkedHashMap<>();

        ZombieDefinition barrel = ZombieDataRepository
                .getInstance().getByZombieType("BarrelRollerZombie");

        if (barrel == null) {
            throw new IllegalStateException(
                    "Missing zombie definition: BarrelRollerZombie");
        }

        stats.put("barrelrollerzombie", new ZombieBaseStats(
                barrel.speed, barrel.health, barrel.eatDamagePerSecond,
                barrel.waveCost, (int) Math.round(barrel.getNumber(
                "BarrelHealth", BalanceDefaults.BARREL_HEALTH))));

        return stats;
    }
}
