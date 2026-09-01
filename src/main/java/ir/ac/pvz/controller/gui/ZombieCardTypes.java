package ir.ac.pvz.controller.gui;

import java.util.LinkedHashMap;
import java.util.Map;

final class ZombieCardTypes {
    private static final Map<String, String> MODEL_TYPES = createModelTypes();

    private ZombieCardTypes() {
    }

    static String modelTypeFor(String cardType) {
        return MODEL_TYPES.getOrDefault(cardType, "BasicZombie");
    }

    private static Map<String, String> createModelTypes() {
        Map<String, String> types = new LinkedHashMap<>();

        types.put("conehead", "ConeheadZombie");
        types.put("buckethead", "BucketheadZombie");
        types.put("blockhead", "BlockheadZombie");
        types.put("knight", "KnightZombie");
        types.put("football", "FootballZombie");
        types.put("newspaper", "NewspaperZombie");
        types.put("parasol", "ParasolZombie");
        types.put("arcade", "ArcadeZombie");
        types.put("imp", "ImpZombie");
        types.put("jester", "JesterZombie");
        types.put("wizard", "WizardZombie");
        types.put("gargantuar", "Gargantuar");

        return types;
    }
}
