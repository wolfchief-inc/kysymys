package net.unit8.kysymys.user.application;

public class EmailAlreadyTakenException extends Exception {
    public EmailAlreadyTakenException(String s) {
        super(s);
    }
}
