package ru.ifmo.rain.klepov.bank;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.MalformedURLException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;

/**
 * Server application working with {@link Bank}
 */
public class Server {
    private static final int PORT = 25565;
    private static final int REG_PORT = 8888;

    /** Start server application  */
    public static void main(final String[] args) {
        
        final Bank bank;
        try {
            LocateRegistry.createRegistry(REG_PORT);
            bank = new RemoteBank();
            Naming.rebind("//localhost:" + REG_PORT + "/bank", bank);
        } catch (RemoteException e) {
            System.out.println("remote error " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (final MalformedURLException e) {
            System.out.println("Malformed URL");
            System.exit(1);
        }
        System.out.println("Server started");
    }
}