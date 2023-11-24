package ava.io.authentication_manager.security;

import ava.io.authentication_manager.AuthenticationManagerApplication;
import ava.io.authentication_manager.controllers.AuthController;
import ava.io.authentication_manager.dtos.UserDto;
import ava.io.authentication_manager.dtos.mappers.UserRepresentationMapper;
import ava.io.authentication_manager.services.AuthService;
import ava.io.authentication_manager.services.KeycloakService;
import com.c4_soft.springaddons.security.oauth2.test.annotations.Claims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenIdClaims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.StringClaim;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockJwtAuth;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.BDDMockito.given;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@TestPropertySource(
        locations = "classpath:application-test.yml")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = AuthenticationManagerApplication.class)
//@WebMvcTest(controllers = {AuthController.class,UserRepresentationMapper.class})
public class SecurityTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

//    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).apply(springSecurity()).build();
    }

    @MockBean
    private AuthService authService;
//    @MockBean
//    private KeycloakService keycloakService;
//    @MockBean
//    private UserRepresentationMapper userRepresentationMapper;

    @Value("${spring.base_url}")
    private String basePath;


    @SneakyThrows
    @Test
    @SuppressWarnings("unused")
    @WithAnonymousUser
    public void test_getInfo_unauthorized() {

        // Given
        String getInfoUrl = basePath + "/auth/user/info";

        // When
        this.mockMvc.perform(get(getInfoUrl).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())

                // then
                .andExpect(status().isUnauthorized());
    }

    @SneakyThrows
    @Test
    @SuppressWarnings("unused")
//    @WithMockJwtAuth(authorities = {"ROLE_wrong"}, claims = @OpenIdClaims(preferredUsername = "", sub = "", otherClaims = @Claims(stringClaims = @StringClaim(name = "tenant_id", value = "patient"))))
    public void test_getInfo_wrongRole_then_forbidden() {

        // Given
        String getInfoUrl = basePath + "/auth/user/info";




        // When
        MvcResult mvcResult = this.mockMvc.perform(get(getInfoUrl).contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("ROLE_wrong")))
                                .jwt(jwt -> jwt.subject("5ddcbafe-81ef-40bf-a701-da3eaffff615")
                                        .claims(c -> {
                                            c.put("tenant_id", "patient");
                                            c.put(StandardClaimNames.PREFERRED_USERNAME, "kais_alkotamy9819");
                                        })
                                )
                        ))
                .andDo(print())

                // then
                .andExpect(status().isForbidden())
                .andReturn();

        System.out.println();
    }

    @SneakyThrows
    @Test
    @SuppressWarnings("unused")
    @WithMockJwtAuth(authorities = {"ROLE_ppatient"}, claims = @OpenIdClaims(preferredUsername = "", sub = "5ddcbafe-81ef-40bf-a701-da3eaffff615", otherClaims = @Claims(stringClaims = @StringClaim(name = "tenant_id", value = "patient"))))
    public void test_getInfo_authorized() {

        // Given
        String getInfoUrl = basePath + "/auth/user/info";
        UserRepresentation user = new UserRepresentation();
        user.setFirstName("kais");
//        given(keycloakService.getUserInfo("patient", "5ddcbafe-81ef-40bf-a701-da3eaffff615")).willReturn(user);
//        given(userRepresentationMapper.toDto(user)).willReturn(UserDto.builder().firstName("kais").build());
        // When
        MvcResult mvcResult = this.mockMvc.perform(get(getInfoUrl).contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("ROLE_patient")))
                                .jwt(jwt -> jwt.subject("5ddcbafe-81ef-40bf-a701-da3eaffff615")//.tokenValue("eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJUeFVNZ3dKMDFsaWY5QTc0SVJRM3VhOFhmSWNUOVhPM3NOcFkwckhpWVhZIn0.eyJleHAiOjE2OTQ1Mjc1MjUsImlhdCI6MTY5NDUyNjMyNSwianRpIjoiMDIxMDk2MDItZDhiMS00MmMzLWEyYmMtOTM1YWIzNjg4MjU2IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MTgwL2F1dGgvcmVhbG1zL3BhdGllbnQiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiNWRkY2JhZmUtODFlZi00MGJmLWE3MDEtZGEzZWFmZmZmNjE1IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoicGF0aWVudC1hcHAiLCJzZXNzaW9uX3N0YXRlIjoiNTU0ODllM2UtMmY5Ny00MjFjLTllMDMtYTkzNDFhZWMxZjU5IiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsImRlZmF1bHQtcm9sZXMtaHlnaWFpX3BhdGllbnQiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7InBhdGllbnQtYXBwIjp7InJvbGVzIjpbInBhdGllbnQiXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoib3BlbmlkIGVtYWlsIHByb2ZpbGUiLCJzaWQiOiI1NTQ4OWUzZS0yZjk3LTQyMWMtOWUwMy1hOTM0MWFlYzFmNTkiLCJ0ZW5hbnRfaWQiOiJwYXRpZW50IiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJrYWlzIGFsa290YW15IiwicHJlZmVycmVkX3VzZXJuYW1lIjoia2Fpc19hbGtvdGFteTk4MTkiLCJnaXZlbl9uYW1lIjoia2FpcyIsImZhbWlseV9uYW1lIjoiYWxrb3RhbXkiLCJlbWFpbCI6ImFsa290YW15QGF2YWZpdmUuY29tIn0.KAy0phlpS5F5vMhc_YNfXKsvYPAt9_v2De9z7xflDTBR-LbZhNvOaAFAI003rDE_tlXv4rm6etYUZkCT7g5Cr7BUCk4rYW8oDUH82g2fQLcwJHpM5vk_SPyRID1-G6AmlX0at2LWoxnEINdFuDB2UiVWLvKhgZkyPApsXCziGaAslkFh3AgESvkBzijUr8QE6Z-0SlO0IpURLuMHTkaiBdShbk0jQAP3aCtqo-00aSI_ze0ZLvnWs3qE8OJR5r3LlIKkwT_STM8y52KlpFPtVnLrOYCsT__WcB050QQJCvAqA5nGveyo44SIjsvCYapsrYmAmFOsr2PMmwDfoFROpg")
                                        .claims(c -> {
                                            c.put("tenant_id", "patient");
                                            c.put(StandardClaimNames.PREFERRED_USERNAME, "kais_alkotamy9819");
                                        })
                                )
                        ))
                .andDo(print())

                // then
                .andExpect(status().isOk())
                .andReturn();

    }

    // Utility method to convert an object to a JSON string
    private String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


//.with(jwt().authorities(List.of(new SimpleGrantedAuthority("ROLE_patient")))
//                                .jwt(jwt -> jwt.subject("5ddcbafe-81ef-40bf-a701-da3eaffff615")//.tokenValue("eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJUeFVNZ3dKMDFsaWY5QTc0SVJRM3VhOFhmSWNUOVhPM3NOcFkwckhpWVhZIn0.eyJleHAiOjE2OTQ1Mjc1MjUsImlhdCI6MTY5NDUyNjMyNSwianRpIjoiMDIxMDk2MDItZDhiMS00MmMzLWEyYmMtOTM1YWIzNjg4MjU2IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MTgwL2F1dGgvcmVhbG1zL3BhdGllbnQiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiNWRkY2JhZmUtODFlZi00MGJmLWE3MDEtZGEzZWFmZmZmNjE1IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoicGF0aWVudC1hcHAiLCJzZXNzaW9uX3N0YXRlIjoiNTU0ODllM2UtMmY5Ny00MjFjLTllMDMtYTkzNDFhZWMxZjU5IiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsImRlZmF1bHQtcm9sZXMtaHlnaWFpX3BhdGllbnQiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7InBhdGllbnQtYXBwIjp7InJvbGVzIjpbInBhdGllbnQiXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoib3BlbmlkIGVtYWlsIHByb2ZpbGUiLCJzaWQiOiI1NTQ4OWUzZS0yZjk3LTQyMWMtOWUwMy1hOTM0MWFlYzFmNTkiLCJ0ZW5hbnRfaWQiOiJwYXRpZW50IiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJrYWlzIGFsa290YW15IiwicHJlZmVycmVkX3VzZXJuYW1lIjoia2Fpc19hbGtvdGFteTk4MTkiLCJnaXZlbl9uYW1lIjoia2FpcyIsImZhbWlseV9uYW1lIjoiYWxrb3RhbXkiLCJlbWFpbCI6ImFsa290YW15QGF2YWZpdmUuY29tIn0.KAy0phlpS5F5vMhc_YNfXKsvYPAt9_v2De9z7xflDTBR-LbZhNvOaAFAI003rDE_tlXv4rm6etYUZkCT7g5Cr7BUCk4rYW8oDUH82g2fQLcwJHpM5vk_SPyRID1-G6AmlX0at2LWoxnEINdFuDB2UiVWLvKhgZkyPApsXCziGaAslkFh3AgESvkBzijUr8QE6Z-0SlO0IpURLuMHTkaiBdShbk0jQAP3aCtqo-00aSI_ze0ZLvnWs3qE8OJR5r3LlIKkwT_STM8y52KlpFPtVnLrOYCsT__WcB050QQJCvAqA5nGveyo44SIjsvCYapsrYmAmFOsr2PMmwDfoFROpg")
//                                        .claims(c -> {
//                                            c.put("tenant_id", "patient");
//                                            c.put(StandardClaimNames.PREFERRED_USERNAME, "kais_alkotamy9819");
//                                        })
//                                )
//                        )