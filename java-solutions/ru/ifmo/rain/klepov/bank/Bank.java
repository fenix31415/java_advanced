package ru.ifmo.rain.klepov.bank;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * An interface to a remote data, works with persons and their accounts
 */
public interface Bank extends Remote {

    /**
     * Creates a new person with specified data if it is not already exists.
     */
    void createPerson(String lastName, String firstName, String passportNumber) throws RemoteException;

    /**
     * Checks person storing data (first and last names) by passport
     * @return true if person present and have correct name
     */
    boolean checkPerson(String passportNumber, String firstName, String lastName) throws RemoteException;

    /**
     * Check if person's record present
     */
    boolean isPersonExists(String passportNumber) throws RemoteException;

    /**
     * Returns serializable person by passport.
     * @return person with specified passport number or {@code null} if person do not exist.
     */
    LocalPerson getLocalPerson(String passportNumber) throws RemoteException;

    /**
     * Returns remote person by passport.
     * @return person with specified passport number or {@code null} if person do not exist.
     */
    Person getRemotePerson(String passportNumber) throws RemoteException;
}