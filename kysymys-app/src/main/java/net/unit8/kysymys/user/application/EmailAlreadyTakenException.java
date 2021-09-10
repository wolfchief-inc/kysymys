package net.unit8.kysymys.user.application;

public class EmailAlreadyTakenException extends RuntimeException {
    public EmailAlreadyTakenException(String s) {
        super(s);
    }
}
