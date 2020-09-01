package ru.ifmo.rain.klepov.concurrent;

import java.util.List;

public class Utils {
    public static void addAndStart(final List<Thread> workers, final Thread thread) {
        workers.add(thread);
        thread.start();
    }

    public static void checkThreads(final int threads) throws IllegalArgumentException {
        if (threads <= 0) {
            throw new IllegalArgumentException("Number of threads must be positive");
        }
    }
}