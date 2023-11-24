package ava.io.authentication_manager.utils.custom_excpeption;

public class UserAlreadyExistedException extends RuntimeException{
    public String message;

    public UserAlreadyExistedException() {
        super();
    }

    public UserAlreadyExistedException(String message) {
        super(message);
    }
}
