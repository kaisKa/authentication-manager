package ava.io.authentication_manager.utils;

import ava.io.authentication_manager.utils.custom_excpeption.HttpCustomException;
import ava.io.authentication_manager.utils.custom_excpeption.UnauthorisedException;
import ava.io.authentication_manager.utils.custom_excpeption.UserAlreadyExistedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;


import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.util.Map;

public class RestTemplateResponseErrorHandler
        implements ResponseErrorHandler {
    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return (response.getStatusCode() != HttpStatus.valueOf(201) && response.getStatusCode() != HttpStatus.valueOf(200));
    }

    @Autowired
    private ObjectMapper mapper;


    @Override
    public void handleError(ClientHttpResponse response) throws IOException {

        switch (response.getStatusCode()) {
            case FORBIDDEN:
                throw new ForbiddenException(ErrorCode.FORBIDDEN_ACCESS.getMessage());
            case UNAUTHORIZED:
                throw new UnauthorisedException(ErrorCode.UNAUTHORIZED.getMessage());
            case CONFLICT:
                throw new UserAlreadyExistedException(ErrorCode.EXITED_USER.getMessage());
            case NOT_FOUND:
                throw new NotFoundException();
            case BAD_REQUEST:
                var body = new String(StreamUtils.copyToByteArray(response.getBody()));
                var tree = new ObjectMapper().readValue(body, Map.class);
                throw new BadRequestException(tree.isEmpty() ? ErrorCode.NOT_VALID_PHONE.name()
                        : tree.containsKey("error_description") ? tree.get("error_description").toString()
                        : tree.containsKey("message") ? tree.get("message").toString()
                        : tree.toString());
            default:
                throw new HttpCustomException(response.getStatusCode().value(), response.getStatusText());
        }


    }
}
