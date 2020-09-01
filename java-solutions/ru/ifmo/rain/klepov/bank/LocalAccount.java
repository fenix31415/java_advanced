package ru.ifmo.rain.klepov.bank;

import java.io.Serializable;

/**
 * A local version of {@link Account}
 */
public class LocalAccount implements Account, Serializable {
    private int amount;

    /**
     * Create an instance of local account
     */
    public LocalAccount() {
        this.amount = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getAmount() {
        return amount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAmount(int amount) {
        this.amount = amount;
    }
}