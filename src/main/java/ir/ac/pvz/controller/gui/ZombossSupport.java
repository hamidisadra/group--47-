package ir.ac.pvz.controller.gui;

import ir.ac.pvz.model.enums.SeasonType;

public final class ZombossSupport {
    private ZombossSupport() {
    }

    public static String[] summonPool(SeasonType season) {
        switch (season) {
            case DARK_AGES:
                return new String[] { "BasicZombie", "ConeheadZombie", "KnightZombie",
                    "WizardZombie", "JesterZombie" };
            case FROSTBITE_CAVES:
                return new String[] { "BasicZombie", "BucketheadZombie", "HunterZombie",
                    "Troglobite", "DodoRiderZombie" };
            case BIG_WAVE_BEACH:
                return new String[] { "BasicZombie", "ConeheadZombie", "FishermanZombie",
                    "SnorkelZombie", "OctopusZombie" };
            case ANCIENT_EGYPT:
            default:
                return new String[] { "BasicZombie", "ConeheadZombie", "BucketheadZombie",
                    "RaZombie", "ExplorerZombie" };
        }
    }

    public static String title(SeasonType season) {
        switch (season) {
            case DARK_AGES:
                return "Zomboss the Dragon";
            case FROSTBITE_CAVES:
                return "Zomboss the Mammoth";
            case BIG_WAVE_BEACH:
                return "Zomboss the Shark";
            case ANCIENT_EGYPT:
            default:
                return "Zomboss the Sphinx Walker";
        }
    }
}
