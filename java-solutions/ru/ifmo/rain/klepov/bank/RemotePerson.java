package ru.ifmo.rain.klepov.bank;

import java.rmi.Remote;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

public class RemotePerson extends UnicastRemoteObject implements Person {
    private LocalPerson localPerson;

    /**
     * Create a person instance by it's data
     */
    public RemotePerson(final String id, final String firstName, final String lastName) throws RemoteException {
        super();
        localPerson = new LocalPerson(id, firstName, lastName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean isAccountExists(String id) {
        return localPerson.isAccountExists(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void createAccount(final String accountId) {
        localPerson.createAccount(accountId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFirstName() {
        return localPerson.getFirstName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLastName() {
        return localPerson.getLastName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPassportNumber() {
        return localPerson.getPassportNumber();
    }

    @Override
    public synchronized int getAmount(String accountId) {
        return localPerson.getAmount(accountId);
    }

    @Override
    public synchronized void setAmount(String accountId, int amount) {
        localPerson.setAmount(accountId, amount);
    }

    /**
     * Returns a LocalPerson instance
     */
    public synchronized LocalPerson getLocalPerson() {
        return localPerson;
    }
}