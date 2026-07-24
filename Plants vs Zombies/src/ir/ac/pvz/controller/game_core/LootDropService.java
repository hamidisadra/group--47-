package ir.ac.pvz.controller.game_core;

import ir.ac.pvz.model.others.*;

import ir.ac.pvz.model.core.Zombie;
import ir.ac.pvz.model.enums.LootType;
import java.util.Random;
import java.util.random.RandomGenerator;

public class LootDropService {
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
        this.dropChance = 0.10f;
        this.coinChanceAfterDrop = 0.80f;
        this.diamondChanceAfterDrop = 0.10f;
        this.potChanceAfterDrop = 0.10f;
        this.coinDropAmount = 50;
        this.diamondDropAmount = 1;
        if (random == null) {
            throw new IllegalArgumentException("Random generator cannot be null.");
        }
        this.random = random;
    }
    public LootType rollLoot(Zombie zombie) {
        if (zombie == null || random.nextFloat() >= dropChance) {
            return LootType.NONE;
        }
        float result = random.nextFloat();
        if (result < coinChanceAfterDrop) {
            return LootType.COIN;
        }
        float diamondUpperBound = 1f - potChanceAfterDrop;
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
        }
        else if (type == LootType.DIAMOND) {
            session.addDiamonds(diamondDropAmount);
            printDrop("diamond", session.getDiamonds(), "diamonds");
        }
        else if (type == LootType.POT) {
            session.addPots(1);
            printDrop("pot", session.getPots(), "pots");
        }
    }
    private void printDrop(String item, int amount, String plural) {
        System.out.println("A zombie dropeed a " + item + "; you have "
                + amount + " " + plural + " now.");
    }
}
