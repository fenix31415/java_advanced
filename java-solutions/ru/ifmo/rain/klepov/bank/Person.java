package ru.ifmo.rain.klepov.bank;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Bank client interface.
 */
public interface Person extends Remote, Serializable {
    /** Check if person has {@code accountId} account */
    boolean isAccountExists(String accountId) throws RemoteException;

    /** Creates, if not present, account with {@code subID=accountId} */
    void createAccount(String accountId) throws RemoteException;

    /** Returns person's name. */
    String getFirstName() throws RemoteException;

    /** Returns person's last name. */
    String getLastName() throws RemoteException;

    /** Returns person's passport number. */
    String getPassportNumber() throws RemoteException;

    /** Returns amount of Person's account if it present */
    int getAmount(String accountId) throws RemoteException;

    /** Set amount of Person's account if it present */
    void setAmount(String accountId, int amount) throws RemoteException;
}