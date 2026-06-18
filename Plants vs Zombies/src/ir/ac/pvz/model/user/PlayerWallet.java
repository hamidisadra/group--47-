package ir.ac.pvz.model.user;

public class PlayerWallet {
    private int coins;
    private int gems;

    //Constructor

    public PlayerWallet() {
        this.coins = 0;
        this.gems = 0;
    }

    //Getters

    public int getCoins() {
        return coins;
    }

    public int getGems() {
        return gems;
    }

    public TransactionStatus addCoins(int amount) {
        if (amount <= 0) {
            return TransactionStatus.INVALID_AMOUNT;
        }
        this.coins += amount;
        return TransactionStatus.SUCCESS;
    }

    public TransactionStatus spendCoins(int amount) {
        if (amount <= 0) {
            return TransactionStatus.INVALID_AMOUNT;
        }

        if (this.coins < amount) {
            return TransactionStatus.INSUFFICIENT_FUND;
        }

        this.coins -= amount;
        return TransactionStatus.SUCCESS;
    }

    public TransactionStatus addGems(int amount) {
        if (amount <= 0) {
            return TransactionStatus.INVALID_AMOUNT;
        }
        this.gems += amount;
        return TransactionStatus.SUCCESS;
    }

    public TransactionStatus spendGems(int amount) {
        if (amount <= 0) {
            return TransactionStatus.INVALID_AMOUNT;
        }

        if (this.gems < amount) {
            return TransactionStatus.INSUFFICIENT_FUND;
        }

        this.gems -= amount;
        return TransactionStatus.SUCCESS;
    }

    public TransactionStatus convertGemsToCoins(int gems) {
        if (gems <= 0) {
            return TransactionStatus.INVALID_AMOUNT;
        }

        TransactionStatus spendStatus = spendGems(gems);

        if (spendStatus == TransactionStatus.SUCCESS) {
            addCoins(gems * 100);
            return TransactionStatus.SUCCESS;
        }

        return spendStatus;
    }
}
