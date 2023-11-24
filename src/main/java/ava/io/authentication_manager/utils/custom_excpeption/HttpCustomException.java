package ava.io.authentication_manager.utils.custom_excpeption;

import lombok.Getter;

@Getter
public class HttpCustomException extends RuntimeException{
    int code;

    /**
     *
     * @param message
     */
    public HttpCustomException(String message) {
        super(message);
    }

    /**
     *
     * @param code
     * @param message
     */
    public HttpCustomException(int code, String message ) {
        super(message);
        this.code = code;
    }
}
