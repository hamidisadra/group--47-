package com.pvz.model.plants;

import com.pvz.model.core.Plant;
import com.pvz.model.support.PlantDataRepository;
import com.pvz.model.support.Upgrade;

public final class PlantFactory {

    private static final String[] PLANT_TYPES = {
            "Sunflower",
            "Twin Sunflower",
            "Sun-shroom",
            "Primal Sunflower",
            "Gold Bloom",
            "Peashooter",
            "Repeater",
            "Threepeater",
            "Snow Pea",
            "Rotobaga",
            "Pea Pod",
            "Split Pea",
            "Citron",
            "Caulipower",
            "Electric Blueberry",
            "Bowling Bulb",
            "Cactus",
            "Fire Peashooter",
            "Starfruit",
            "Goo Peashooter",
            "Mega Gatling Pea",
            "Sea-shroom",
            "Puff-shroom",
            "Fume-shroom",
            "Cabbage-pult",
            "Kernel-pult",
            "Melon-pult",
            "Winter Melon",
            "Pepper-pult",
            "Potato Mine",
            "Primal Potato Mine",
            "Cherry Bomb",
            "Squash",
            "Grapeshot",
            "Jalapeno",
            "Doom-shroom",
            "Tangle Kelp",
            "Iceberg Lettuce",
            "Bonk Choy",
            "Phat Beet",
            "Chomper",
            "Wasabi Whip",
            "Kiwibeast",
            "Wall-nut",
            "Tall-nut",
            "Endurian",
            "Garlic",
            "Sweet Potato",
            "Explode-o-nut",
            "Pumpkin",
            "Sun Bean",
            "Torchwood",
            "Magnet-shroom",
            "Hypno-shroom",
            "Cat-tail",
            "Imitater",
            "Ice-shroom",
            "Lily Pad",
            "Hot Potato",
            "Grave Buster",
            "Enlighten-mint",
            "Appease-mint",
            "Arma-mint",
            "Bombard-mint",
            "Enforce-mint",
            "Reinforce-mint",
            "Enchant-mint",
            "Pierce-mint",
            "catTail-mint"
    };

    private PlantFactory() {
    }

    public static Plant create(int id, String type) {
        String normalized = normalize(type);
        Plant plant = createFirstGroup(id, normalized);
        if (plant == null) {
            plant = createSecondGroup(id, normalized);
        }
        if (plant == null) {
            plant = createThirdGroup(id, normalized);
        }
        if (plant != null) {
            PlantDataRepository.getInstance().applyTo(plant);
            Upgrade.configureFor(plant, normalized);
        }
        return plant;
    }

    private static Plant createFirstGroup(int id, String type) {
        switch (type) {
            case "sunflower": return new Sunflower(id);
            case "twinsunflower": return new TwinSunflower(id);
            case "sunshroom": return new SunShroom(id);
            case "primalsunflower": return new PrimalSunflower(id);
            case "goldbloom": return new GoldBloom(id);
            case "peashooter": return new Peashooter(id);
            case "repeater": return new Repeater(id);
            case "threepeater": return new Threepeater(id);
            case "snowpea": return new SnowPea(id);
            case "rotobaga": return new Rotobaga(id);
            case "peapod": return new PeaPod(id);
            case "splitpea": return new SplitPea(id);
            case "citron": return new Citron(id);
            case "caulipower": return new Caulipower(id);
            case "electricblueberry": return new ElectricBlueberry(id);
            case "bowlingbulb": return new BowlingBulb(id);
            case "cactus": return new Cactus(id);
            case "firepeashooter": return new FirePeashooter(id);
            case "starfruit": return new Starfruit(id);
            case "goopeashooter": return new GooPeashooter(id);
            case "megagatlingpea": return new MegaGatlingPea(id);
            case "seashroom": return new SeaShroom(id);
            case "puffshroom": return new PuffShroom(id);
            default: return null;
        }
    }

    private static Plant createSecondGroup(int id, String type) {
        switch (type) {
            case "fumeshroom": return new FumeShroom(id);
            case "cabbagepult": return new CabbagePult(id);
            case "kernelpult": return new KernelPult(id);
            case "melonpult": return new MelonPult(id);
            case "wintermelon": return new WinterMelon(id);
            case "pepperpult": return new PepperPult(id);
            case "potatomine": return new PotatoMine(id);
            case "primalpotatomine": return new PrimalPotatoMine(id);
            case "cherrybomb": return new CherryBomb(id);
            case "squash": return new Squash(id);
            case "grapeshot": return new Grapeshot(id);
            case "jalapeno": return new Jalapeno(id);
            case "doomshroom": return new DoomShroom(id);
            case "tanglekelp": return new TangleKelp(id);
            case "iceberglettuce": return new IcebergLettuce(id);
            case "bonkchoy": return new BonkChoy(id);
            case "phatbeet": return new PhatBeet(id);
            case "chomper": return new Chomper(id);
            case "wasabiwhip": return new WasabiWhip(id);
            case "kiwibeast": return new Kiwibeast(id);
            case "wallnut": return new WallNut(id);
            case "tallnut": return new TallNut(id);
            case "endurian": return new Endurian(id);
            default: return null;
        }
    }

    private static Plant createThirdGroup(int id, String type) {
        switch (type) {
            case "garlic": return new Garlic(id);
            case "sweetpotato": return new SweetPotato(id);
            case "explodeonut": return new ExplodeONut(id);
            case "pumpkin": return new Pumpkin(id);
            case "sunbean": return new SunBean(id);
            case "torchwood": return new Torchwood(id);
            case "magnetshroom": return new MagnetShroom(id);
            case "hypnoshroom": return new HypnoShroom(id);
            case "cattail": return new CatTail(id);
            case "imitater": return new Imitater(id);
            case "iceshroom": return new IceShroom(id);
            case "lilypad": return new LilyPad(id);
            case "hotpotato": return new HotPotato(id);
            case "gravebuster": return new GraveBuster(id);
            case "enlightenmint": return new EnlightenMint(id);
            case "appeasemint": return new AppeaseMint(id);
            case "armamint": return new ArmaMint(id);
            case "bombardmint": return new BombardMint(id);
            case "enforcemint": return new EnforceMint(id);
            case "reinforcemint": return new ReinforceMint(id);
            case "enchantmint": return new EnchantMint(id);
            case "piercemint": return new PierceMint(id);
            case "cattailmint": return new CatTailMint(id);
            default: return null;
        }
    }

    public static String[] getPlantTypes() {
        return PLANT_TYPES.clone();
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("-", "").replace("_", "")
                .replace(" ", "").toLowerCase();
    }
}
