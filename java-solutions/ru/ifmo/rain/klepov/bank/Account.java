package ru.ifmo.rain.klepov.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Bank account interface.
 */
public interface Account extends Remote {
    /** Returns amount of money at the account. */
    int getAmount() throws RemoteException;

    /** Sets amount of money at the account. */
    void setAmount(int amount) throws RemoteException;
}