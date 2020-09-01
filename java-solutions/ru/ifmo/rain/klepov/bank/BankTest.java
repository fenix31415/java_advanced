package ru.ifmo.rain.klepov.bank;

import org.junit.Test;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.Rule;
import org.junit.runner.Description;
import org.junit.rules.TestWatcher;
import org.junit.rules.TestRule;
import org.junit.runner.notification.Failure;
import static org.junit.Assert.*;

import java.rmi.AccessException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.net.MalformedURLException;
import java.util.function.Consumer;
import java.util.Random;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.rmi.registry.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BankTest {
    private static final long start = System.currentTimeMillis();
    private static final int TEST_AMOUNT = 10;
    private static final int PORT = 9999;
    private static Bank bank;
    private static final Random random = new Random(314);
    private static final String firstName = "Dmitriy";
    private static final String lastName = "Klepov";
    private static final String id = "314";
    private static final String accountName = "qivi";

    private void startServ() throws RemoteException {
        try {
            bank = new RemoteBank();
            Naming.rebind("//localhost:" + PORT + "/bank", bank);
        } catch (final MalformedURLException e) {
            System.out.println("Malformed URL");
        }
    }

    private void startClient() throws RemoteException {
        try {
            Registry registry = LocateRegistry.getRegistry(PORT);
            bank = (Bank) registry.lookup("bank");
        } catch (NotBoundException e) {
            System.out.println("Bank is not bound");
        }
    }

    @Before
    public void before() throws RemoteException {
        startServ();
        startClient();
    }

    @BeforeClass
    public static void beforeClass() throws RemoteException {
        LocateRegistry.createRegistry(9999);
    }

    @Rule
    public TestRule watcher = watcher(description -> System.err.println("=== Running " + description.getMethodName()));

    protected static TestWatcher watcher(final Consumer<Description> watcher) {
        return new TestWatcher() {
            @Override
            protected void starting(final Description description) {
                watcher.accept(description);
            }
        };
    }

    private Person createPerson(final String id, String lastName, String firstName) throws RemoteException {
        assertFalse(bank.isPersonExists(id));
        assertFalse(bank.checkPerson(id, lastName, firstName));
        assertNull(bank.getRemotePerson(id));
        bank.createPerson(lastName, firstName, id);
        assertTrue(bank.isPersonExists(id));
        assertTrue(bank.checkPerson(id, firstName, lastName));
        Person ans = bank.getRemotePerson(id);
        assertNotNull(ans);
        assertEquals(id, ans.getPassportNumber());
        assertEquals(lastName, ans.getLastName());
        assertEquals(firstName, ans.getFirstName());
        return ans;
    }

    private void createAcc(final Person person, final String accID) throws RemoteException {
        assertFalse(person.isAccountExists(accID));
        person.createAccount(accID);
        assertTrue(person.isAccountExists(accID));
        assertEquals(0, person.getAmount(accID));
    }

    @Test
    public void test01_createOneRemotePerson() throws RemoteException {
        createPerson(id, lastName, firstName);
    }
    
    @Test
    public void test02_createOneLocalPerson() throws RemoteException {
        bank.createPerson(lastName, firstName, id);
        bank.getLocalPerson(id);
    }

    @Test
    public void test03_createManyPersons() throws RemoteException {
        final int tests = 1000;
        for (int i = 0; i < tests; i++) {
            final String passportNumber = Integer.toString(i);
            final String newLastName = lastName + "_" + passportNumber;
            final String newFirstName = firstName + "_" + passportNumber;
            createPerson(passportNumber, newLastName, newFirstName);
            bank.getLocalPerson(passportNumber);
        }
    }

    @Test
    public void test04_createOneAccount() throws RemoteException {
        final Person remotePerson = createPerson(id, lastName, firstName);
        createAcc(remotePerson, accountName);
    }

    @Test
    public void test05_createManyAccounts() throws RemoteException {
        final int peoples = 100;
        final int rMax = 100;
        for (int i = 0; i < peoples; i++) {
            final String passportNumber = Integer.toString(i);
            final Person remotePerson = createPerson(passportNumber, lastName, firstName);
            final int accounts = random.nextInt(rMax);
            for (int j = 0; j < accounts; j++) {
                createAcc(remotePerson, accountName + "_" + j);
            }
        }
    }

    @Test
    public void test06_someUserOperations() throws RemoteException {
        final int peoples = 100;
        final int accounts = 100;
        final int operations = 1000;
        final Map<String, Integer> answer = new HashMap<>();
        for (int i = 0; i < peoples; i++) {
            final String passportNumber = Integer.toString(i);
            final String newLastName = lastName + "_" + passportNumber;
            final String newFirstName = firstName + "_" + passportNumber;
            final Person person = createPerson(passportNumber, newLastName, newFirstName);
            for (int j = 0; j < accounts; j++) {
                final String accountId = accountName + "_" + newFirstName + "_" + j;
                createAcc(person, accountId);
                answer.put(accountId, 0);
            }
        }

        for (int k = 0; k < operations; k++) {
            final int value = random.nextInt();
            final String passportNumber = Integer.toString(random.nextInt(peoples));
            final String newFirstName = firstName + "_" + passportNumber;
            final String accountId = accountName + "_" + newFirstName + "_" + random.nextInt(accounts);
            Person person = bank.getRemotePerson(passportNumber);
            person.setAmount(accountId, value);
            answer.put(accountId, value);
        }

        for (int i = 0; i < accounts; i++) {
            final String passportNumber = Integer.toString(i);
            final String newFirstName = firstName + "_" + passportNumber;
            for (int j = 0; j < peoples; j++) {
                final String accountId = accountName + "_" + newFirstName + "_" + j;
                assertEquals(answer.get(accountId).intValue(), bank.getRemotePerson(passportNumber).getAmount(accountId));
            }
        }
    }

    @Test
    public void test7_visibleRemote_LocalAccount() throws RemoteException {

        final Person remote = createPerson(id, lastName, firstName);
        final String accName = accountName;
        final String accNameRemote = accountName + "_remote";
        final String accNameLocal = accountName + "_local";
        final int amount = 27;
        final int newAmountRemote = 18;
        final int newAmountLocal = 28;
        createAcc(remote, accName);
        remote.setAmount(accName, amount);
        LocalPerson local = bank.getLocalPerson(id);

        assertEquals(amount, local.getAmount(accName));
        remote.setAmount(accName, newAmountRemote);
        assertEquals(amount, local.getAmount(accName));
        assertEquals(newAmountRemote, remote.getAmount(accName));

        createAcc(remote, accNameRemote);
        assertFalse(local.isAccountExists(accNameRemote));
        remote.setAmount(accNameRemote, newAmountRemote);
        createAcc(local, accNameLocal);
        assertEquals(newAmountRemote, remote.getAmount(accNameRemote));
        local.setAmount(accNameLocal, newAmountLocal);
        assertEquals(newAmountRemote, remote.getAmount(accNameRemote));

        createAcc(local, accNameRemote);
        assertEquals(newAmountRemote, remote.getAmount(accNameRemote));
        local.setAmount(accNameRemote, newAmountLocal);
        assertEquals(newAmountRemote, remote.getAmount(accNameRemote));
        createAcc(remote, accNameLocal);
        assertEquals(newAmountLocal, local.getAmount(accNameLocal));
        remote.setAmount(accNameLocal, newAmountRemote);
        assertEquals(newAmountLocal, local.getAmount(accNameLocal));
    }

    public static void main(final String[] args) {
        final Result result = new JUnitCore().run(BankTest.class);
        if (!result.wasSuccessful()) {
            for (final Failure failure : result.getFailures()) {
                System.err.printf("Test %s failed: %s%n", failure.getDescription().getMethodName(), failure.getMessage());
                if (failure.getException() != null) {
                    failure.getException().printStackTrace();
                }
            }
            System.exit(1);
        } else {
            final long time = System.currentTimeMillis() - start;
            System.out.printf("SUCCESS in %dms %n", time);
            System.exit(0);
        }
    }
}