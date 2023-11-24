package ava.io.authentication_manager.service;


import ava.io.authentication_manager.services.AuthService;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;

@WebMvcTest(AuthService.class)
@TestPropertySource(
        locations = "classpath:application-test.yml")
public class AuthServiceTest {

}
