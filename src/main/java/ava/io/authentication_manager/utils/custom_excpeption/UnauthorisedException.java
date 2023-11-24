package ava.io.authentication_manager.utils.custom_excpeption;

public class UnauthorisedException extends RuntimeException{

    public UnauthorisedException(String message) {
        super(message);
    }

    public UnauthorisedException() {
        super();
    }
}
