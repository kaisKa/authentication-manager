package ava.io.authentication_manager.controllers;


import ava.io.authentication_manager.AuthenticationManagerApplication;
import ava.io.authentication_manager.dtos.UserCredentials;
import ava.io.authentication_manager.dtos.UserDto;
import ava.io.authentication_manager.dtos.mappers.UserRepresentationMapper;
import ava.io.authentication_manager.model.GeneralResponse;
import ava.io.authentication_manager.model.LoginResponse;
import ava.io.authentication_manager.services.AuthService;
import ava.io.authentication_manager.services.KeycloakService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Run Unit test for AuthController  using Junit 5 and jupiter, also for effective assertion we are using Hamcrest and JSON path
 */
//@ExtendWith(SpringExtension.class)
////@ContextConfiguration(classes = {AppConfig.class, PathBasedConfigResolver1.class, TenantResolver.clas})
//@WebAppConfiguration
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
//        classes = AuthenticationManagerApplication.class)

@WebMvcTest(AuthController.class)
@TestPropertySource(
        locations = "classpath:application-test.yml")
//@WithMockJwtAuth(authorities = "ROLE_patient", claims = @OpenIdClaims(preferredUsername = "",sub = "",))
public class AuthControllerIntegTest {

//    @Autowired
//    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;


//    @BeforeEach
//    public void setup() throws Exception {
//        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
//    }

    //******** constants *********//
    @Value("${spring.base_url}")
    private String basePath;


    @MockBean
    private AuthService authService;

    @MockBean
    private KeycloakService keycloakService;

    @MockBean
    private UserRepresentationMapper userRepresentationMapper;


    @Test
    @SneakyThrows
    public void test_loginVerifiedUser_will_return_accessToken_okStatus_userInfo() {

        String loginUrl = basePath + "/auth/tenant/patient/get-token";

        // Given
        UserCredentials credentials = UserCredentials.builder()
                .username("kais_alkotamy9819")
                .password("123")
                .build();

        UserDto user = UserDto.builder()
                .firstName("kais")
                .lastName("alkotamy")
                .userId("5ddcbafe-81ef-40bf-a701-da3eaffff615")
                .emailId("alkotamy@avafive.com")
                .isVerified(true)
                .phone("+971544438824")
                .build();

        given(authService.login("patient", credentials)).willReturn(
                LoginResponse.builder()
                        .userInfo(UserDto.builder()
                                .firstName("kais")
                                .lastName("alkotamy")
                                .emailId("alkotamy@avafive.com")
                                .isVerified(false)
                                .phone("+971544438824")
                                .userName("kais_alkotamy9819")
                                .build()
                        ).token(null).idToken(null).sessionState(null).build()
        );

//        var t = authService.login("patient",credentials);

        // When
        MvcResult mvcResult = this.mockMvc.perform(post(loginUrl).content(asJsonString(credentials)).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
//        verify(authService.login("patient",credentials),times(1));
        System.out.println(new String (mvcResult.getResponse().getContentAsByteArray()));
        var response = objectMapper.readValue(new String(mvcResult.getResponse().getContentAsByteArray()), GeneralResponse.class);
        var body = objectMapper.convertValue(response.getData(), LoginResponse.class);

        // then
        //assert user info
        Assertions.assertNotNull(body.getUserInfo());
        Assertions.assertEquals(user.getFirstName(), body.getUserInfo().getFirstName());
        Assertions.assertEquals(user.getLastName(), body.getUserInfo().getLastName());
        Assertions.assertEquals(user.getPhone(), body.getUserInfo().getPhone());
        Assertions.assertEquals(user.getEmailId(), body.getUserInfo().getEmailId());
        Assertions.assertEquals(user.getIsVerified(), false);
        Assertions.assertEquals(user.getUserId(), body.getUserInfo().getUserId());
    }


    // Utility method to convert an object to a JSON string
    private String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


//    @MockBean
//    private AccessTokenMapper accessTokenMapper;

    /**
     * Assert the test env is working properly
     */
//    @Test
//    public void givenWac_whenServletContext_thenItProvidesGreetController() {
//        ServletContext servletContext = webApplicationContext.getServletContext();
//
//        Assertions.assertNotNull(servletContext);
//        Assertions.assertTrue(servletContext instanceof MockServletContext);
//        Assertions.assertNotNull(webApplicationContext.getBean("greetController"));
//    }
//    @Test
//    @SneakyThrows
//    public void test_register_existedUser_will_return_conflict() {
//        String registerUrl = basePath + "/auth/tenant/patient";
//
//        // Given
//        UserDto user = UserDto.builder().firstName("kais").lastName("alkotamy").emailId("alkotami@avafive.com")
//                .countryCode("+971").phone("544438827").build();
//
//        given(authService.register(user,Tenant.PATIENT,Channel.phone)).willReturn(Res)
////        given(keycloakService.createKeycloakUser("patient", user, List.of(Role.PATIENT), null)).willReturn(Response.created(new URI("")).build());
////        given(keycloakService.getUserByPhoneNum("patient","")).willReturn(new UserRepresentation());
////        given(keycloakService.getId(Response.created(new URI("")).build())).willReturn("5ddcbafe-81ef-40bf-a701-da3eaffff615");
////        when(authService.register(user, Tenant.PATIENT, Channel.phone)).thenReturn(Response.created(new URI("")).build());
////        when(keycloakService.createKeycloakUser("patient", user, List.of(Role.PATIENT), null)).thenReturn(Response.created(new URI("")).build());
//        // When
//        var result = this.mockMvc.perform(post(registerUrl).content(asJsonString(user)).contentType(MediaType.APPLICATION_JSON).param("channel", Channel.email.name()))
//                .andDo(System.out::print)
//
//
//                // then
//                .andExpect(MockMvcResultMatchers.status().isConflict())
//                .andReturn();
//
//    }

}
