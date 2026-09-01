package ir.ac.pvz.view.assets;

import ir.ac.pvz.model.enums.SeasonType;

import java.util.HashMap;
import java.util.Map;

public final class AnimationNames {
    private static final Map<String, String> PLANT_OVERRIDES = new HashMap<>();
    private static final Map<String, String> ZOMBIE_BY_TYPE = new HashMap<>();
    private static final Map<SeasonType, String> WORLD = new HashMap<>();

    static {
        PLANT_OVERRIDES.put("twinsunflower", "SUNFLOWER_TWIN");
        PLANT_OVERRIDES.put("kernelpult", "KERNALPULT");
        PLANT_OVERRIDES.put("iceberglettuce", "ICEBURG");
        PLANT_OVERRIDES.put("phatbeet", "PHATBEETS");
        PLANT_OVERRIDES.put("rotobaga", "ROTORUTABAGA");
        PLANT_OVERRIDES.put("megagatlingpea", "MEGAGATLING");
        PLANT_OVERRIDES.put("piercemint", "SPEARMINT");
        PLANT_OVERRIDES.put("cattail", "HOMINGTHISTLE");
        PLANT_OVERRIDES.put("cattailmint", "AILMINT");

        ZOMBIE_BY_TYPE.put("razombie", "ZOMBIE_EGYPT_RA");
        ZOMBIE_BY_TYPE.put("explorerzombie", "ZOMBIE_EXPLORER");
        ZOMBIE_BY_TYPE.put("tombraiserzombie", "ZOMBIE_EGYPT_TOMBRAISER");
        ZOMBIE_BY_TYPE.put("dodoriderzombie", "ZOMBIE_ICEAGE_DODORIDER");
        ZOMBIE_BY_TYPE.put("hunterzombie", "ZOMBIE_ICEAGE_HUNTER");
        ZOMBIE_BY_TYPE.put("troglobite", "ZOMBIE_ICEAGE_TROGLOBITE");
        ZOMBIE_BY_TYPE.put("fishermanzombie", "ZOMBIE_BEACH_FISHERMAN");
        ZOMBIE_BY_TYPE.put("octopuszombie", "ZOMBIE_BEACH_OCTOPUS");
        ZOMBIE_BY_TYPE.put("snorkelzombie", "ZOMBIE_BEACH_SNORKELER");
        ZOMBIE_BY_TYPE.put("jesterzombie", "ZOMBIE_DARK_JESTER");
        ZOMBIE_BY_TYPE.put("wizardzombie", "ZOMBIE_DARK_WIZARD");
        ZOMBIE_BY_TYPE.put("kingzombie", "ZOMBIE_DARK_KING");
        ZOMBIE_BY_TYPE.put("impdragon", "ZOMBIE_DARK_IMP_DRAGON");
        ZOMBIE_BY_TYPE.put("footballzombie", "ZOMBIE_MODERN_ALLSTAR");
        ZOMBIE_BY_TYPE.put("parasolzombie", "ZOMBIE_LOSTCITY_JANE");
        ZOMBIE_BY_TYPE.put("turquoisezombie", "ZOMBIE_LOSTCITY_CRYSTALSKULL");
        ZOMBIE_BY_TYPE.put("prospectorzombie", "ZOMBIE_PROSPECTOR");
        ZOMBIE_BY_TYPE.put("pianistzombie", "ZOMBIE_PIANO");
        ZOMBIE_BY_TYPE.put("newspaperzombie", "ZOMBIE_MODERN_NEWSPAPER");
        ZOMBIE_BY_TYPE.put("arcadezombie", "ZOMBIE_80S_ARCADE");
        ZOMBIE_BY_TYPE.put("barrelrollerzombie", "ZOMBIE_PIRATE_BARREL_PUSHER");

        WORLD.put(SeasonType.ANCIENT_EGYPT, "EGYPT");
        WORLD.put(SeasonType.FROSTBITE_CAVES, "ICEAGE");
        WORLD.put(SeasonType.BIG_WAVE_BEACH, "BEACH");
        WORLD.put(SeasonType.DARK_AGES, "DARK");
    }

    private AnimationNames() {
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (char character : value.toCharArray()) {
            if (Character.isLetterOrDigit(character)) {
                builder.append(Character.toLowerCase(character));
            }
        }
        return builder.toString();
    }

    public static String world(SeasonType season) {
        return WORLD.getOrDefault(season, "EGYPT");
    }

    public static String plant(String plantName) {
        String key = normalize(plantName);
        String override = PLANT_OVERRIDES.get(key);
        return override != null ? override : key.toUpperCase();
    }

    public static String[] zombieCandidates(String gameType, SeasonType season) {
        String key = normalize(gameType);
        String mapped = ZOMBIE_BY_TYPE.get(key);
        if (mapped != null) {
            return new String[] { mapped };
        }
        String world = world(season);
        switch (key) {
            case "basiczombie":
            case "coneheadzombie":
            case "bucketheadzombie":
                return new String[] { "ZOMBIE_" + world + "_BASIC", "ZOMBIE_EGYPT_BASIC" };
            case "blockheadzombie":
                return new String[] { "ZOMBIE_" + world + "_BASIC_BRICK",
                        "ZOMBIE_" + world + "_BASIC", "ZOMBIE_EGYPT_BASIC" };
            case "knightzombie":
                return new String[] { "ZOMBIE_DARK_BASIC", "ZOMBIE_EGYPT_BASIC" };
            case "gargantuar":
                return new String[] { world + "_GARGANTUAR", "ZOMBIE_" + world + "_GARGANTUAR",
                        "GARGANTUAR" };
            case "impzombie":
                return new String[] { "ZOMBIE_" + world + "_IMP", "ZOMBIE_BEACH_IMP_MERMAID",
                        "ZOMBIE_DARK_IMP_MONK", "ZOMBIE_IMP_BARE" };
            case "zomboss":
                return new String[] { "ZOMBIE_" + world + "_ZOMBOSS" };
            case "peashooterzombie":
                return zombotany(world, "PEASHOOTER");
            case "wallnutzombie":
                return zombotany(world, "WALLNUT");
            case "jalapenozombie":
                return zombotany(world, "JALAPENO");
            case "squashzombie":
                return zombotany(world, "SQUASH");
            default:
                return new String[] { "ZOMBIE_" + world + "_BASIC", "ZOMBIE_EGYPT_BASIC" };
        }
    }

    private static String[] zombotany(String world, String plant) {
        return new String[] {
            "ZOMBIE_ZOMBOTANY_" + plant,
            "ZOMBIE_" + world + "_ZOMBOTANY_" + plant,
            "ZOMBIE_MODERN_ZOMBOTANY_" + plant,
            "ZOMBIE_" + world + "_BASIC",
            "ZOMBIE_EGYPT_BASIC"
        };
    }

    private static final String[] EFFECT_PARTS = {
        "_particles", "particle_arm", "particle_head", "ink", "butter",
        "ground_swatch"
    };

    public static boolean isEffectPart(String part) {
        if (part == null) {
            return false;
        }

        for (String effect : EFFECT_PARTS) {
            if (effect.equals(part)) {
                return true;
            }
        }

        return false;
    }

    public static String armorSlot(String armorName) {
        String key = normalize(armorName);
        if (key.contains("cone")) {
            return "cone";
        }

        if (key.contains("bucket")) {
            return "bucket";
        }

        if (key.contains("block") || key.contains("brick")) {
            return "brick";
        }

        return null;
    }

    public static String armorSlotOfPart(String part) {
        if (part == null) {
            return null;
        }

        String key = part.toLowerCase(java.util.Locale.ROOT);
        boolean armorKey = key.contains("armor_") || key.startsWith("brick_");
        if (!armorKey) {
            return null;
        }

        return armorSlot(key);
    }

    public static boolean isDamagedPart(String part) {
        if (part == null) {
            return false;
        }

        String key = part.toLowerCase(java.util.Locale.ROOT);
        return key.contains("damage");
    }

    public static String armorPart(String armorName, int damageStage) {
        String key = normalize(armorName);
        String slot;
        if (key.contains("cone")) {
            slot = "cone";
        }

        else if (key.contains("bucket")) {
            slot = "bucket";
        }

        else if (key.contains("block") || key.contains("brick")) {
            slot = "brick";
        }

        else {
            return null;
        }
        if (damageStage <= 0) {
            return "zombie_armor_" + slot + "_norm";
        }
        if (slot.equals("brick")) {
            return damageStage == 1 ? "brick_damaged1" : "brick_damaged2";
        }
        return "zombie_armor_" + slot + "_damage_0" + Math.min(2, damageStage);
    }

    public static String mower(SeasonType season) {
        return "MOWER_" + world(season);
    }

    public static String lawnImage(SeasonType season) {
        return "IMAGE_BACKGROUNDS_" + world(season) + "_TEXTURE";
    }
}
