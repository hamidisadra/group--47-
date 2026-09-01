package ir.ac.pvz.model.user;

public class PlayerWallet {
    private int coins;
    private int gems;

    public PlayerWallet() {
        coins = 0;
        gems = 0;
    }

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
        long result = (long) coins + amount;
        if (result > Integer.MAX_VALUE) {
            return TransactionStatus.INVALID_AMOUNT;
        }
        coins = (int) result;
        return TransactionStatus.SUCCESS;
    }

    public TransactionStatus spendCoins(int amount) {
        if (amount <= 0) {
            return TransactionStatus.INVALID_AMOUNT;
        }
        if (coins < amount) {
            return TransactionStatus.INSUFFICIENT_FUND;
        }
        coins -= amount;
        return TransactionStatus.SUCCESS;
    }

    public TransactionStatus addGems(int amount) {
        if (amount <= 0) {
            return TransactionStatus.INVALID_AMOUNT;
        }
        long result = (long) gems + amount;
        if (result > Integer.MAX_VALUE) {
            return TransactionStatus.INVALID_AMOUNT;
        }
        gems = (int) result;
        return TransactionStatus.SUCCESS;
    }

    public TransactionStatus spendGems(int amount) {
        if (amount <= 0) {
            return TransactionStatus.INVALID_AMOUNT;
        }
        if (gems < amount) {
            return TransactionStatus.INSUFFICIENT_FUND;
        }
        gems -= amount;
        return TransactionStatus.SUCCESS;
    }

    public TransactionStatus convertGemsToCoins(int gems) {
        if (gems <= 0 || gems > Integer.MAX_VALUE / 100) {
            return TransactionStatus.INVALID_AMOUNT;
        }
        TransactionStatus spendStatus = spendGems(gems);
        if (spendStatus != TransactionStatus.SUCCESS) {
            return spendStatus;
        }
        TransactionStatus addStatus = addCoins(gems * 100);
        if (addStatus != TransactionStatus.SUCCESS) {
            addGems(gems);
            return addStatus;
        }
        return TransactionStatus.SUCCESS;
    }
}
