package ru.ifmo.rain.klepov.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class RecursiveWalk {

    private static void writeData(BufferedWriter writer, String ans) throws IOException {
            writer.write(ans);
            writer.newLine();
    }

    private static void process(String fileName, BufferedWriter writer, SimpleFileVisitor<? super Path> visitor) throws WalkException {
        try {
            try {
                Files.walkFileTree(Path.of(fileName), visitor);
            } catch (InvalidPathException e) {
                System.err.println("Invalid path '" + fileName + "'");
                writeData(writer, FNV.getNullAns() + " " + fileName);
            }
        } catch (IOException ex) {
            throw new WalkException("Can't write to output file", ex);
        }
    }

    public static void walk(BufferedReader reader, BufferedWriter writer) throws WalkException {
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                process(line, writer, new SimpleFileVisitor<>() {
                    private void writeAns(String ans, Path file) throws IOException {
                        writeData(writer, ans + " " + file.toString());
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        String ans;
                        ans = FNV.getHash(file);
                        writeAns(ans, file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        writeAns(FNV.getNullAns(), file);
                        System.err.println("Can't access to target file '" + file + "'");
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        } catch (IOException e) {
            throw new WalkException("Can't read from input file", e);
        }
    }

    public static void main(String[] args) {
        if(args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.out.println("Usage: input output");
            return;
        }

        String inFile = args[0];
        String outFile = args[1];

        try {
            Path outPath;
            try {
                outPath = Path.of(outFile);
                Path parentDirectory = outPath.getParent();
                if (parentDirectory != null) {
                    Files.createDirectories(parentDirectory);
                }
            } catch (IOException | InvalidPathException e) {
                throw new WalkException("Error while creating directories to file '" + outFile + "'", e);
            }

            try (BufferedReader reader = Files.newBufferedReader(Path.of(inFile))) {
                try (BufferedWriter writer = Files.newBufferedWriter(outPath)) {
                    walk(reader, writer);
                } catch (IOException e) {
                    throw new WalkException("Failed to access to output file " + outFile, e);
                }
            } catch (InvalidPathException | IOException e) {
                throw new WalkException("Can't open input file '" + inFile + "'", e);
            }
        } catch (WalkException e) {
            System.err.println("Walk error: " + e.getMessage());
        }
    }
}
