package ru.ifmo.rain.klepov.bank;

import java.rmi.registry.Registry;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * Client application working with {@link Bank}
 */
public class Client {
    
    private static final int REG_PORT = 8888;
    
    private static boolean checkArgs(final String[] args) {
        if (args == null || args.length != 5) {
            System.out.println("Usage: Client <first name> <lastName> <passportId> <accountId> <change>");
            return false;
        }
        for (final String i : args) {
            if (i == null) {
                System.out.println("Non-null arguments expected");
                return false;
            }
        }
        return true;
    }

    private static String getGlobalID(String passport, String subID) {
        return passport+":"+subID;
    }
    
    /**
     * Client application with the following arguments:
     * {@code [name] [surname] [passport] [account id] [change]}
     */
    public static void main(final String[] args) throws RemoteException {
        final Bank bank;
        
        String firstName;
        String lastName;
        String passportNumber;
        String accountId;
        int change;
        
        try {
            Registry registry = LocateRegistry.getRegistry(REG_PORT);
            bank = (Bank) registry.lookup("bank");
        } catch (NotBoundException e) {
            System.out.println("Bank is not bound");
            return;
        }
        
        if (!checkArgs(args)) return;
        firstName = args[0];
        lastName = args[1];
        passportNumber = args[2];
        accountId = args[3];
        try {
            change = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.out.println("Integer params expected " + e.getMessage());
            return;
        }

        System.out.println("I'm " + firstName + " " + lastName + " and i want " + change);

        if (!bank.isPersonExists(passportNumber)) {
            System.out.println("Creating new person " + passportNumber);
            bank.createPerson(lastName, firstName, passportNumber);
        } else if (!bank.checkPerson(passportNumber, firstName, lastName)) {
            System.err.println("Incorrect person data");
            return;
        }
        Person person = bank.getRemotePerson(passportNumber);
        String fullID = getGlobalID(passportNumber, accountId);
        if (!person.isAccountExists(fullID)) {
            person.createAccount(fullID);
            System.out.println("Account has been created");
        }

        System.out.println("Money before: " + person.getAmount(fullID));
        person.setAmount(fullID, change);
        System.out.println("Money after: " + person.getAmount(fullID));
    }
}
