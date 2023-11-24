package ava.io.authentication_manager.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.http.HttpStatus;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Map;


@RequiredArgsConstructor
@Getter
@Setter

public class GeneralResponse<T> {


//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
//    private Date timestamp = new Date(System.currentTimeMillis());

    @NotNull
    Boolean error;

    String message;
    String errorMessage;

    @NotNull
    T data;

    public GeneralResponse(Boolean error, String message, T data) {
        this.error = error;
        this.data = data;
        if (error)
            this.errorMessage = message;
        else this.message = message;

    }


    //    String stackTrace;

//    public GeneralResponse(
//            HttpStatus httpStatus,
//            String message
//    ) {
//        this();
//
//        this.code = httpStatus.value();
//        this.message = message;
//    }
}
