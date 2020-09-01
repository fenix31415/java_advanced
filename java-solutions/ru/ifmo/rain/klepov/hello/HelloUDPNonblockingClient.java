package ru.ifmo.rain.klepov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;

public class HelloUDPNonblockingClient implements HelloClient {
    String prefix;
    List<ByteBuffer> byteBuffers;
    List<DatagramChannel> datagramChannels;
    SocketAddress sendTo;
    int requests;
    final static int WAITING_MS = 100;
    Selector selector;

    private boolean init(final int i) {
        try {
            final DatagramChannel channel = DatagramChannel.open();
            channel.configureBlocking(false);
            final int receiveBufferSize = channel.getOption(StandardSocketOptions.SO_RCVBUF);
            byteBuffers.add(ByteBuffer.wrap(new byte[receiveBufferSize]));
            final Context context = new Context(i, 0);
            channel.register(selector, SelectionKey.OP_WRITE, context);
            datagramChannels.add(channel);
        } catch (IOException e) {
            System.out.println("Unable to initialize channel");
            datagramChannels.forEach((DatagramChannel channel) -> {
                try {
                    channel.close();
                } catch (IOException ignored) { }
            });
            return false;
        }
        return true;
    }

    public static void main(final String[] args) {
        Utils.clientMain(args, new HelloUDPNonblockingClient());
    }

    @Override
    public void run(final String host, final int port, final String pref, final int threads, final int req) {
        prefix = pref;
        requests = req;
        byteBuffers = new ArrayList<>();
        datagramChannels = new ArrayList<>();
        try {
            sendTo = new InetSocketAddress(InetAddress.getByName(host), port);
            selector = Selector.open();
        } catch (final UnknownHostException e) {
            System.out.println("Unable to connect" + e.getMessage());
            e.printStackTrace();
            return;
        } catch (final IOException e) {
            System.out.println("Failed to open selector " + e.getMessage());
            e.printStackTrace();
            return;
        }

        if(!IntStream.range(0, threads).mapToObj(this::init).reduce(true, Boolean::logicalAnd))
            return;

        while (!Thread.currentThread().isInterrupted()) {
            try {
                int ready = selector.select(WAITING_MS);
                if (ready == 0) {
                    Set<SelectionKey> allKeys = selector.keys();
                    if (allKeys.isEmpty()) {
                        break;
                    }
                    allKeys.forEach(this::writeIf);
                }
                for (final Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext();) {
                    final SelectionKey key = i.next();
                    if (key.isReadable()) {
                        read(key);
                    } else {
                        write(key);
                    }
                    i.remove();
                }
            } catch (final IOException e) {
                System.out.println("An error while select " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void writeIf(SelectionKey key) {
        if (key.interestOps() == SelectionKey.OP_READ) {
            write(key);
        }
    }

    private void write(SelectionKey key) {
        final Context context = (Context)key.attachment();
        try {
            int t = context.t;
            final ByteBuffer currentBuffer = byteBuffers.get(t);
            currentBuffer.clear();
            Utils.putAndSend(currentBuffer, Utils.getRequestText(prefix, t, context.id).getBytes(), datagramChannels.get(t), sendTo);
            key.interestOps(SelectionKey.OP_READ);
        } catch (final IOException e) {
            System.out.println("Write of failed" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void read(SelectionKey key) {
        final DatagramChannel channel = (DatagramChannel) key.channel();
        final Context context = (Context) key.attachment();

        final int t = context.t;
        final ByteBuffer currentBuffer = byteBuffers.get(t);
        currentBuffer.clear();

        try {
            channel.receive(currentBuffer);
            currentBuffer.flip();
            final int length = currentBuffer.remaining();
            final String response = new String(currentBuffer.array(), 0, length, StandardCharsets.UTF_8);
            if (Utils.check(response, t, context.id)) {
                context.id++;
            }
            if (context.id != requests) {
                key.interestOps(SelectionKey.OP_WRITE);
            } else {
                channel.close();
            }
        } catch (IOException e) {
            System.out.println("Read failed" + e.getMessage());
            e.printStackTrace();
        }
    }

    private static class Context {
        Context(final int t,
                final int i) {
            this.t = t;
            this.id = i;
        }
        final int t;
        int id;
    }
}
