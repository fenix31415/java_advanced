package ru.ifmo.rain.klepov.bank;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LocalPerson implements Person, Serializable {
    private final String firstName;
    private final String lastName;
    private final String id;
    private Map<String, LocalAccount> accounts = new HashMap<>();

    /**
     * Create a person instance by it's data
     */
    public LocalPerson(final String id, final String firstName, final String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAccountExists(String accountId) {
        return accounts.containsKey(accountId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createAccount(final String accountId) {
        accounts.computeIfAbsent(accountId, l -> new LocalAccount());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFirstName() {
        return firstName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLastName() {
        return lastName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPassportNumber() {
        return id;
    }

    @Override
    public int getAmount(String accountId) {
        if (!isAccountExists(accountId))
            throw new NullPointerException();
        return accounts.get(accountId).getAmount();
    }

    @Override
    public void setAmount(String accountId, int amount) {
        if (!isAccountExists(accountId))
            throw new NullPointerException();
        accounts.get(accountId).setAmount(amount);
    }
}