package ru.ifmo.rain.klepov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;
import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Util class helping #HelloUDPServer and #HelloUDPClient with common operations
 */
public class Utils {
    private static final String regex = "[\\D]*([0-9]+)[\\D]*([0-9]+)[\\D]*";
    private static final Pattern pattern = Pattern.compile(regex);

    private static boolean checkArgsServ(final String[] args) {
        if (args == null || args.length != 2) {
            System.out.println("Usage: HelloUDPServer port threads");
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

    public static boolean check(final String response, final int t, final int i) {
        Matcher matcher = pattern.matcher(response);
        return matcher.matches() && matcher.group(1).equals(Integer.toString(t)) && matcher.group(2).equals(Integer.toString(i));
    }

    public static void servMain(final String[] args, HelloServer helloServer) {
        if (!checkArgsServ(args)) return;

        try {
            helloServer.start(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
            final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            try {
                reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (final NumberFormatException e) {
            System.out.println("Integer params expected " + e.getMessage());
        }
    }

    private static boolean checkArgsClient(final String[] args) {
        if (args == null || args.length != 5) {
            System.out.println("Usage: HelloUDPClient host port prefix threads requests");
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

    public static void clientMain(final String[] args, HelloClient helloClient) {
        if (!checkArgsClient(args)) {
            return;
        }

        try {
            helloClient.run(args[0], Integer.parseInt(args[1]), args[2],
                    Integer.parseInt(args[3]), Integer.parseInt(args[4]));
        } catch (final NumberFormatException e) {
            System.out.println("Integer params expected " + e.getMessage());
        }
    }

    /**
     * @return A string containing in the packet
     */
    public static String fromPacket(final DatagramPacket packet) {
        return new String(packet.getData(), packet.getOffset(),
                packet.getLength(), StandardCharsets.UTF_8);
    }
    
    /**
     * Sets buffer and then receives on socket
     */
    public static void setAndRecieve(final DatagramSocket socket,
            final DatagramPacket packet, final byte[] buffer) throws IOException {
        packet.setData(buffer);
        socket.receive(packet);
    }
    
    /**
     * Sets msg and then sends on socket
     */
    public static void setAndSend(final DatagramSocket socket,
            final DatagramPacket packet, final String message) throws IOException {
        packet.setData(message.getBytes(StandardCharsets.UTF_8));
        socket.send(packet);
    }

    public static void putAndSend(ByteBuffer buffer, byte[] message, DatagramChannel datagramChannel, SocketAddress sendTo) throws IOException {
        buffer.put(message);
        buffer.flip();
        datagramChannel.send(buffer, sendTo);
    }

    public static String getRequestText(final String prefix, final int threadNum, final int i) {
        return prefix + threadNum + "_" + i;
    }

    public static String getResponse(String s) {
        return "Hello, " + s;
    }
}
