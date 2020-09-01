package ru.ifmo.rain.klepov;

public class ApplicationException extends Exception {
    ApplicationException(String message) {
        super(message);
    }

    ApplicationException(String message, Exception e) {
        super(message + " " + e.getMessage(), e);
    }
}
