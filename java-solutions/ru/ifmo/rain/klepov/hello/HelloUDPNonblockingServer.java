package ru.ifmo.rain.klepov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloUDPNonblockingServer implements HelloServer {
    private static final int WAIT_AFTER_CLOSE_MS = 2000;
    private static int DEFAULT_BUFFER_SIZE = 65536;
    private static final int MAX_TASKS = 100;

    private ExecutorService executor, workers;
    Selector selector;
    private final ByteBuffer ignoringBuffer = ByteBuffer.wrap(new byte[DEFAULT_BUFFER_SIZE]);
    DatagramChannel datagramChannel;
    final ConcurrentLinkedQueue<ByteBuffer> byteBuffers = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<AnsContainer> answers = new ConcurrentLinkedQueue<>();

    public static void main(final String[] args) {
        Utils.servMain(args, new HelloUDPNonblockingServer());
    }

    @Override
    public void start(final int port, final int threads) {
        try {
            selector = Selector.open();
        } catch (final IOException e) {
            System.out.println("Unable to open selector " + e.getMessage());
            e.printStackTrace();
            return;
        }
        try {
            datagramChannel = DatagramChannel.open();
            datagramChannel.configureBlocking(false);
            datagramChannel.bind(new InetSocketAddress(port));
        } catch (IOException e) {
            System.out.println("Unable to create a channel " + e.getMessage());
            e.printStackTrace();
            return;
        }
        try {
            DEFAULT_BUFFER_SIZE = datagramChannel.getOption(StandardSocketOptions.SO_RCVBUF);
        } catch (IOException ignored) { }
        for (int i = 0; i < MAX_TASKS; i++) {
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            byteBuffers.add(ByteBuffer.wrap(buffer));
        }
        try {
            datagramChannel.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            System.out.println("Unable to register ");
        }
        executor = Executors.newSingleThreadExecutor();
        workers = Executors.newFixedThreadPool(threads);
        executor.submit(this::run);
    }

    private void run() {
        while (selector.isOpen()) {
            try {
                int ready = selector.select();
                if (ready == 0) {
                    final SelectionKey key = selector.keys().iterator().next();
                    key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                    continue;
                }
                for (final Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext();) {
                    final SelectionKey key = i.next();
                    if (key.isReadable()) {
                        final DatagramChannel channel = (DatagramChannel) key.channel();
                        ByteBuffer buffer = byteBuffers.poll();
                        if (buffer == null) {
                            System.out.println("Too many requests, ignoring..");
                            ignoringBuffer.clear();
                            try {
                                channel.receive(ignoringBuffer);
                            } catch (IOException ignored) { }
                            continue;
                        }
                        try {
                            SocketAddress address = channel.receive(buffer);
                            buffer.flip();
                            workers.submit(() -> makeResponse(buffer, address));
                        } catch (IOException e) {
                            System.out.println("An error while receiving " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        while (!answers.isEmpty()) {
                            AnsContainer response = answers.poll();
                            ByteBuffer message = ByteBuffer.wrap(response.ans.getBytes());
                            try {
                                datagramChannel.send(message, response.address);
                            } catch (IOException e) {
                                System.out.println("Failure during send to socket: ");
                                e.printStackTrace();
                            }
                        }
                        key.interestOps(SelectionKey.OP_READ);
                    }
                    i.remove();
                }
            } catch (IOException e) {
                System.out.println("Unable to open channel " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void makeResponse(ByteBuffer buffer, SocketAddress address) {
        String request = new String(buffer.array(), 0, buffer.remaining(), StandardCharsets.UTF_8);
        buffer.clear();
        String response = Utils.getResponse(request);
        answers.add(new AnsContainer(response, address));
        byteBuffers.add(buffer);
        selector.wakeup();
    }

    private void closeExecutors(ExecutorService service) {
        service.shutdown();
        try {
            service.awaitTermination(WAIT_AFTER_CLOSE_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            if (selector != null && selector.isOpen())
                selector.close();
        } catch (IOException e) {
            System.out.println("Unable to close selector " + e.getMessage());
            e.printStackTrace();
        }
        closeExecutors(executor);
        closeExecutors(workers);
    }

    private static class AnsContainer {
        SocketAddress address;
        String ans;
        AnsContainer(String s, SocketAddress a) {
            address = a;
            ans = s;
        }
    }
}
