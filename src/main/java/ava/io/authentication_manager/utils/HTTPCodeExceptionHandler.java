package ava.io.authentication_manager.utils;

import ava.io.authentication_manager.model.GeneralResponse;
import ava.io.authentication_manager.utils.custom_excpeption.*;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


//import javax.ws.rs.NotFoundException;
import javax.security.auth.login.CredentialException;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class HTTPCodeExceptionHandler {


    @ExceptionHandler(HttpCustomException.class)
    public ResponseEntity<GeneralResponse<Object>> handleTestEX(HttpCustomException e) {
//        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.valueOf(e.getCode())).body(new GeneralResponse<>(true, e.getMessage(), Map.of()));
    }




    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<GeneralResponse<Object>> userNotFoundHandler(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new GeneralResponse<>(true, ErrorCode.USER_NOT_FOUND.getMessage(), Map.of()));
    }

    @ExceptionHandler(WebApplicationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<GeneralResponse<Object>> userAlreadyExistHandler(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new GeneralResponse<>(true, e.getMessage(), Map.of()));
    }

    @ExceptionHandler(UnauthorisedException.class)
    public ResponseEntity<GeneralResponse<Object>> handleUnAuthorizedAccess(Exception e) {
        e.printStackTrace();
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(status).body(new GeneralResponse<>(true, e.getMessage(), Map.of()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<GeneralResponse<Object>> handleForbidden(Exception e) {
        e.printStackTrace();
        HttpStatus status = HttpStatus.FORBIDDEN;
        return ResponseEntity.status(status).body(new GeneralResponse<>(true, e.getMessage(), Map.of()));
    }

    @ExceptionHandler(UserAlreadyExistedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<GeneralResponse<Object>> handleNoUserExist(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new GeneralResponse<>(true, e.getMessage(), Map.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<GeneralResponse<Map<Object, Object>>> argumentExceptionHandler(MethodArgumentNotValidException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(new GeneralResponse<>(true,
                        e.getBindingResult().getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining(" & ")),
                        Map.of()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GeneralResponse<Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(true, e.getMessage(), Map.of()));
    }




//    @ExceptionHandler(BadRequestException.class)
////    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public ResponseEntity<GeneralResponse<Map<Object, Object>>> BadRequestExceptionHandler(BadRequestException e) {
//        e.printStackTrace();
//        HttpStatus status = HttpStatus.BAD_REQUEST;
//        return ResponseEntity.status(status).body(new GeneralResponse<>(true, e.getMessage(), Map.of()));
//    }

    @ExceptionHandler(javax.ws.rs.BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<GeneralResponse<Map<Object, Object>>> BadRequestException(BadRequestException e) {
        e.printStackTrace();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(new GeneralResponse<>(true, e.getMessage(), Map.of()));
    }

    @ExceptionHandler(NotValidLicenseException.class)
    public ResponseEntity<GeneralResponse<Object>> notValidLicenseExceptionHandler(Exception e) {
        return ResponseEntity.ok(new GeneralResponse<>(true, e.getMessage(), Map.of()));
    }


    @ExceptionHandler(CredentialException.class)
    public ResponseEntity<GeneralResponse<Object>> notValidPasswordExceptionHandler(Exception e) {
        e.printStackTrace();
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(status).body(new GeneralResponse<>(true, e.getMessage(), Map.of()));
    }

    @ExceptionHandler(NotFoundException.class) //NotFoundException
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<GeneralResponse<Object>> NotFoundExceptionsHandler(
            Exception e
    ) {
        HttpStatus status = HttpStatus.NOT_FOUND; // 404
        // converting the stack trace to String
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        String stackTrace = stringWriter.toString();

        var response = new GeneralResponse<>(true, e.getLocalizedMessage(), (Object) Map.of());
//        response.setStackTrace(stackTrace);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    //fallback handler method
    @ExceptionHandler(Exception.class) // exception handled
    public ResponseEntity<GeneralResponse<Object>> handleExceptions(
            Exception e
    ) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR; // 500
        e.printStackTrace();
        // converting the stack trace to String
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        String stackTrace = stringWriter.toString();

        GeneralResponse<Object> response = new GeneralResponse<>(true, e.getLocalizedMessage(), (Object) Map.of());
//        response.setStackTrace(stackTrace);
        return ResponseEntity.status(status).body(response);
    }
}
