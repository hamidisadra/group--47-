package ir.ac.pvz.controller.game_core;

import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.LootType;
import ir.ac.pvz.model.others.GameSession;

import java.util.Random;
import java.util.random.RandomGenerator;

public class LootDropService {
    public interface Listener {
        void onLootDropped(LootType type, int amount, int total);
    }

    private Listener listener;

    public float dropChance;
    public float coinChanceAfterDrop;
    public float diamondChanceAfterDrop;
    public float potChanceAfterDrop;
    public int coinDropAmount;
    public int diamondDropAmount;
    private final RandomGenerator random;

    public LootDropService() {
        this(new Random());
    }

    public LootDropService(RandomGenerator random) {
        if (random == null) {
            throw new IllegalArgumentException("Random generator cannot be null.");
        }

        dropChance = 0.10f;
        coinChanceAfterDrop = 0.80f;
        diamondChanceAfterDrop = 0.10f;
        potChanceAfterDrop = 0.10f;
        coinDropAmount = 50;
        diamondDropAmount = 1;
        this.random = random;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public LootType rollLoot(Zombie zombie) {
        validateConfiguration();
        if (zombie == null || random.nextFloat() >= dropChance) {
            return LootType.NONE;
        }

        float result = random.nextFloat();
        float coinUpperBound = coinChanceAfterDrop;
        float diamondUpperBound = coinChanceAfterDrop + diamondChanceAfterDrop;
        if (result < coinUpperBound) {
            return LootType.COIN;
        }

        if (result < diamondUpperBound) {
            return LootType.DIAMOND;
        }

        return LootType.POT;
    }

    public void applyLoot(LootType type, GameSession session) {
        if (type == null || type == LootType.NONE || session == null) {
            return;
        }

        if (type == LootType.COIN) {
            session.addCoins(coinDropAmount);
            printDrop("coin", session.getCoins(), "coins");
            notifyListener(type, coinDropAmount, session.getCoins());
        }

        else if (type == LootType.DIAMOND) {
            session.addDiamonds(diamondDropAmount);
            printDrop("diamond", session.getDiamonds(), "diamonds");
            notifyListener(type, diamondDropAmount, session.getDiamonds());
        }

        else if (type == LootType.POT) {
            session.addPots(1);
            printDrop("pot", session.getPots(), "pots");
            notifyListener(type, 1, session.getPots());
        }
    }

    private void notifyListener(LootType type, int amount, int total) {
        if (listener != null) {
            listener.onLootDropped(type, amount, total);
        }
    }

    private void validateConfiguration() {
        if (dropChance < 0f || dropChance > 1f) {
            throw new IllegalStateException("Drop chance must be between 0 and 1.");
        }

        if (coinChanceAfterDrop < 0f || diamondChanceAfterDrop < 0f
            || potChanceAfterDrop < 0f) {
            throw new IllegalStateException("Loot probabilities cannot be negative.");
        }

        float total = coinChanceAfterDrop + diamondChanceAfterDrop
            + potChanceAfterDrop;
        if (Math.abs(total - 1f) > 0.0001f) {
            throw new IllegalStateException("Loot probabilities must add up to 1.");
        }

        if (coinDropAmount < 0 || diamondDropAmount < 0) {
            throw new IllegalStateException("Loot amounts cannot be negative.");
        }
    }

    private void printDrop(String singular, int amount, String plural) {
        String unit = amount == 1 ? singular : plural;
        System.out.println("A zombie dropeed a " + singular + "; you have "
            + amount + " " + unit + " now.");
    }
}
