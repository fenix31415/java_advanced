package ru.ifmo.rain.klepov.walk;

public class WalkException extends Exception {
    /* protected WalkException(String msg) {
        super("Walk Exception: " + msg);
    } */

    protected WalkException(String message, Throwable cause) {
        super(message + ": " + cause.getMessage(),  cause);
    }
}
