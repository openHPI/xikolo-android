package de.xikolo.controller.exceptions;

public class WrongParameterException extends RuntimeException {

    public WrongParameterException() {
        super("Wrong parameter passed");
    }

    public WrongParameterException(String message) {
        super(message);
    }

}
