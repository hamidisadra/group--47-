package com.pvz.game;

import com.pvz.model.core.Zombie;
import com.pvz.model.support.ArmorDataRepository;
import com.pvz.model.support.ArmorPiece;
import com.pvz.model.support.ZombieBaseStats;
import com.pvz.model.support.ZombieDataRepository;
import com.pvz.model.support.ZombieDefinition;
import com.pvz.model.support.ZombieDefinitionRepository;
import com.pvz.model.support.ArmorDefinitionRepository;
import com.pvz.model.zombies.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.List;
import java.util.stream.Collectors;

public final class ZombieTypeRegistry {
    private final Map<String, Supplier<Zombie>> factories;
    private final ZombieDefinitionRepository zombieRepository;
    private final ArmorDefinitionRepository armorRepository;
    public ZombieTypeRegistry(StageConfig stageConfig) {
        this(stageConfig, ZombieDataRepository.getInstance(),
                ArmorDataRepository.getInstance());
    }
    public ZombieTypeRegistry(StageConfig stageConfig,
                              ZombieDefinitionRepository zombieRepository,
                              ArmorDefinitionRepository armorRepository) {
        if (zombieRepository == null || armorRepository == null) {
            throw new IllegalArgumentException("Zombie repositories are required.");
        }
        this.zombieRepository = zombieRepository;
        this.armorRepository = armorRepository;
        factories = new LinkedHashMap<>();
        registerDefaults(stageConfig);
    }
    public boolean contains(String type) {
        return factories.containsKey(normalize(type))
                || zombieRepository.getByZombieType(type) != null;
    }
    public List<String> getZombieTypes() {
        return zombieRepository.getAll().stream()
                .map(definition -> definition.runtimeType)
                .distinct().collect(Collectors.toList());
    }
    public Zombie create(String type) {
        Supplier<Zombie> factory = factories.get(normalize(type));
        if (factory != null) {
            Zombie zombie = factory.get();
            zombieRepository.applyTo(zombie, type);
            zombie.setIdentity(type, type);
            return zombie;
        }
        ZombieDefinition definition = zombieRepository.getByZombieType(type);
        if (definition == null) {
            return null;
        }
        return new ComposableZombie(type, definition);
    }
    public void register(String type, Supplier<Zombie> factory) {
        if (type != null && factory != null) {
            factories.put(normalize(type), factory);
        }
    }
    private void registerDefaults(StageConfig stageConfig) {
        register("ConeheadZombie", () -> armored(
                "ConeheadZombie", "cone", "ConeDefault"));
        register("BucketheadZombie", () -> armored(
                "BucketheadZombie", "bucket", "BucketDefault"));
        register("BlockheadZombie", () -> armored(
                "BlockheadZombie", "block", "BrickDefault"));
        register("KnightZombie", this::knight);
        register("Gargantuar", Gargantuar::new);
        register("ImpZombie", ImpZombie::new);
        register("FootballZombie", FootballZombie::new);
        register("ArcadeZombie", ArcadeZombie::new);
        register("TurquoiseZombie", TurquoiseZombie::new);
        register("ProspectorZombie", ProspectorZombie::new);
        register("PianistZombie", PianistZombie::new);
        register("BarrelRollerZombie", () -> barrel(stageConfig));
        register("RaZombie", RaZombie::new);
        register("TombRaiserZombie", TombRaiserZombie::new);
        register("HunterZombie", HunterZombie::new);
        register("Troglobite", Troglobite::new);
        register("FishermanZombie", FishermanZombie::new);
        register("OctopusZombie", OctopusZombie::new);
        register("JesterZombie", JesterZombie::new);
        register("WizardZombie", WizardZombie::new);
        register("KingZombie", KingZombie::new);
    }
    private Zombie armored(
            String zombieType, String armorName, String armorAlias) {
        ZombieDefinition data = requireDefinition(zombieType);
        BasicGroup zombie = new BasicGroup(
                data.speed, data.health,
                data.eatDamagePerSecond, data.waveCost);
        zombie.addArmorPiece(new ArmorPiece(armorName,
                armorRepository.getHealth(armorAlias),
                armorRepository.isMetallic(armorAlias)));
        return zombie;
    }
    private Zombie knight() {
        ZombieDefinition data = requireDefinition("KnightZombie");
        BasicGroup zombie = new BasicGroup(
                data.speed, data.health,
                data.eatDamagePerSecond, data.waveCost);
        zombie.addArmorPiece(new ArmorPiece("helmet",
                armorRepository.getHealth("CrownDefault"),
                armorRepository.isMetallic("CrownDefault")));
        zombie.addArmorPiece(new ArmorPiece("shoulder armor",
                armorRepository.getHealth("ShoulderArmorDefault"),
                armorRepository.isMetallic("ShoulderArmorDefault")));
        return zombie;
    }
    private ZombieDefinition requireDefinition(String zombieType) {
        ZombieDefinition definition = zombieRepository.getByZombieType(
                zombieType);
        if (definition == null) {
            throw new IllegalStateException(
                    "Missing zombie definition: " + zombieType);
        }
        return definition;
    }
    private Zombie barrel(StageConfig stageConfig) {
        ZombieBaseStats stats = null;
        if (stageConfig != null) {
            stats = stageConfig.getZombieBaseStats("BarrelRollerZombie");
        }
        return new BarrelRollerZombie(stats);
    }
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("-", "").replace("_", "")
                .replace(" ", "").toLowerCase();
    }
}
