package ru.ifmo.rain.klepov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;

public class HelloUDPClient implements HelloClient {
    private static final int WAITING_RECIEVE = 100;
    ExecutorService workers;
    
    /**
     * Executes {@link #run with given parameters}
     */
    public static void main(final String[] args) {
        Utils.clientMain(args, new HelloUDPClient());
    }

    private void sendAllRequests(final SocketAddress addr, final String prefix, final int threadNum, final int requests) {
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(WAITING_RECIEVE);
            final DatagramPacket packet = new DatagramPacket(new byte[0], 0, addr);
            packet.setSocketAddress(addr);
            final byte[] buffer = new byte[socket.getReceiveBufferSize()];

            for (int i = 0; i < requests; i++) {
                final String requestText = Utils.getRequestText(prefix, threadNum, i);
                while (!socket.isClosed()) {
                    try {
                        Utils.setAndSend(socket, packet, requestText);
                        Utils.setAndRecieve(socket, packet, buffer);
                        
                        final String responseText = Utils.fromPacket(packet);
                        if (Utils.check(responseText, threadNum, i)) {
                            break;
                        }
                    } catch (final IOException e) {
                        System.out.println("Timeout, resending ...");;
                    }
                }
            }
        } catch (final SocketException e) {
            e.printStackTrace();
        }
    }

    /**
     * Runs Hello client.
     * @param host server host
     * @param port server port
     * @param prefix request prefix
     * @param threads number of request threads
     * @param requests number of requests per thread.
     */
    @Override
    public void run(final String host, final int port, final String prefix, final int threads, final int requests) {
        final InetAddress addr;
        try {
            addr = InetAddress.getByName(host);
        } catch (final UnknownHostException e) {
            System.err.println("Unable to find host: " + host);
            return;
        }
        
        final InetSocketAddress toSocket = new InetSocketAddress(addr, port);
        
        workers = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            final int ind = i;
            workers.submit(() -> sendAllRequests(toSocket, prefix, ind, requests));
        }
        workers.shutdown();

        try {
            workers.awaitTermination(10  * requests * WAITING_RECIEVE, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException ignored) { }
    }
}
