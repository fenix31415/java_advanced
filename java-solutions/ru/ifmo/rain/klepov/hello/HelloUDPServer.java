package ru.ifmo.rain.klepov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.*;

public class HelloUDPServer implements HelloServer {
    private int buffSize;
    private DatagramSocket socket;
    private ExecutorService workers;

    /**
     * Constructs a default instance of hello server
     */
    public HelloUDPServer() {
    }

    /**
     * Executes {@link #start with given parameters}
     */
    public static void main(final String[] args) {
        Utils.servMain(args, new HelloUDPServer());
    }

    /**
     * Starts a new Hello server.
     * @param port server port.
     * @param threads number of working threads.
     */
    @Override
    public void start(final int port, final int threads) {
        try {
            socket = new DatagramSocket(port);
            buffSize = socket.getReceiveBufferSize();
        } catch (final SocketException e) {
            System.out.println("Unable to create a socket on port " + port + " " + e.getMessage());
            return;
        }

        workers = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            workers.submit(this::recieveAndRespond);
        }
    }

    private void recieveAndRespond() {
        final byte[] buffer = new byte[buffSize];
        final DatagramPacket packet = new DatagramPacket(buffer, buffSize);
        try {
            while (!socket.isClosed()) {
                Utils.setAndRecieve(socket, packet, buffer);
                final String request = Utils.fromPacket(packet);
                final String response = Utils.getResponse(request);
                Utils.setAndSend(socket, packet, response);
            }
        } catch (final IOException e) {
            if (!socket.isClosed()) {
                System.out.println("Failed to receive packet " + e.getMessage());
            }
        }
    }

    /**
     * Stops server and deallocates all resources.
     */
    @Override
    public void close() {
        socket.close();
        workers.shutdown();
        try {
            workers.awaitTermination(1, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            System.err.println("Could not terminate executor pools: " + e.getMessage());
        }
    }
}
