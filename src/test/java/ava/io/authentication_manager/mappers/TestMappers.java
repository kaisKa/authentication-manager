package ava.io.authentication_manager.mappers;

import ava.io.authentication_manager.dtos.UserDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestMappers {

    @Test
    public void test_userDtoMapper() throws JsonProcessingException {

        UserDto current = UserDto.builder().firstName("kiki") .build();
        String  dto = "{\"firstname\":\"kiki\"}";

        ObjectMapper mapper = new ObjectMapper();

        var result = mapper.readValue(dto, UserDto.class);
        System.out.println(current.getFirstName());
        System.out.println(result.getFirstName());
        Assertions.assertEquals(current.getFirstName(),result.getFirstName());

    }
}
