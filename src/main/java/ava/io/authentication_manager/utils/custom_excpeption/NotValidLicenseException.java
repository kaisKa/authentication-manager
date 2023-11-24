package ava.io.authentication_manager.utils.custom_excpeption;

public class NotValidLicenseException extends RuntimeException {

    public NotValidLicenseException(String message) {
        super(message);
    }
}
