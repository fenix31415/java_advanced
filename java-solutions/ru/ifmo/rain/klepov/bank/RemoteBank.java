package ru.ifmo.rain.klepov.bank;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Implementation of Bank
 */
public class RemoteBank extends UnicastRemoteObject implements Bank {
    private final ConcurrentMap<String, RemotePerson> persons = new ConcurrentHashMap<>();

    /**
     * Creates an instance of RemoteBank
     */
    public RemoteBank() throws RemoteException { }

    /**
     * {@inheritDoc}
     */
    // :NOTE: Многопоточность
    @Override
    public void createPerson(String lastName, String firstName, String passportNumber) throws RemoteException {
        if (lastName == null || firstName == null || passportNumber == null)
            return;
        try {
            persons.computeIfAbsent(passportNumber, i -> {
                try {
                    return new RemotePerson(i, firstName, lastName);
                } catch (RemoteException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (UncheckedIOException e) {
            throw new RemoteException("Failed to create RemotePerson", e.getCause());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkPerson(final String passportNumber, final String firstName, final String lastName) throws RemoteException {
        if (passportNumber == null || firstName == null || lastName == null)
            return false;

        final Person person = persons.get(passportNumber);
        return person != null && person.getLastName().equals(lastName) && person.getFirstName().equals(firstName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalPerson getLocalPerson(final String passportName) {
        try {
            /*FileOutputStream fos = new FileOutputStream("temp.out");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            LocalPerson ts = persons.get(passportName).getLocalPerson();
            oos.writeObject(ts);
            oos.flush();
            oos.close();

            FileInputStream fis = new FileInputStream("temp.out");
            ObjectInputStream oin = new ObjectInputStream(fis);
            return (LocalPerson) oin.readObject();*/
        } catch (Exception ignored) {

        }
        return persons.get(passportName).getLocalPerson();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPersonExists(final String passportNumber) {
        return persons.containsKey(passportNumber);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Person getRemotePerson(final String passportNumber) {
        return persons.get(passportNumber);
    }
}