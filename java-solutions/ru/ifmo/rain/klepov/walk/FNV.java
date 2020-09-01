package ru.ifmo.rain.klepov.walk;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FNV {
    private final static String NULL_ANS = "00000000";
    private final static int INITIAL = (int) 2166136261L;
    private final static int PART_SIZE = 1<<10;
    private final static int MULTIPLIER = 16777619;

    static String getHash(Path file) {
        try (InputStream istream = Files.newInputStream(file)) {
            byte[] part = new byte[PART_SIZE];
            int hash = INITIAL;
            int curSize;
            while ((curSize = istream.read(part)) != -1) {
                hash = partHash(hash, part, curSize);
            }
            return String.format("%08x", hash);
        } catch (IOException e) {
            System.err.println("Can't read from target file '" + file + "'");
            return NULL_ANS;
        }
    }

    private static int partHash(int hash, byte[] part, int size) {
        for (int i = 0; i < size; ++i) {
            hash *= MULTIPLIER;
            hash ^= Byte.toUnsignedInt(part[i]);
        }
        return hash;
    }

    static String getNullAns() {
        return NULL_ANS;
    }
}
