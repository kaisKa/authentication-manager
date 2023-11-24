package ava.io.authentication_manager.service;

import ava.io.authentication_manager.AuthenticationManagerApplication;
import ava.io.authentication_manager.config.PathBasedConfigResolver1;
import ava.io.authentication_manager.config.mullti_tenant.TenantResolver;
import ava.io.authentication_manager.dtos.UserCredentials;
import ava.io.authentication_manager.dtos.UserDto;
import ava.io.authentication_manager.services.KeycloakService;
import ava.io.authentication_manager.services.TenantService;
import ava.io.authentication_manager.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = AuthenticationManagerApplication.class
)
@AutoConfigureMockMvc
public class TestAuthService {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserService service;

    @Autowired
    private PathBasedConfigResolver1 keyCloakConfig;

    @Autowired
    private KeycloakService keycloakService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private TenantResolver tenantResolver;

    @Autowired
    @Qualifier("notSecureRestTemplate")
    private RestTemplate restTemplate;

    /**
     * 1. define the current user
     * 2. get the token for a user
     * 3. define the expected user
     * 4. assertion
     *
     * @throws Exception
     */
    @Test
    void test_udpdateUserInfo() throws Exception {
        String END_POINT_PATH = "/api/v1/auth/user/info";
        String tenant = "";
        UserDto userDTO = new UserDto().builder().userName("kais_alkotamy8051").firstName("ooo").lastName("rrr")
                .password("123").emailId("alkotamy@avafive.com")
                .phone("544438824")
                .countryCode("+888").roles(List.of("patient")).build();


        UserCredentials cred = new UserCredentials();
        cred.setUsername("kais_alkotamy8051");
        cred.setPassword("123");
        // get the token
        String token =  keycloakService.getAccessToken("provider",cred).getToken();
        var requestBody = objectMapper.writeValueAsString(cred);

        System.out.println(keyCloakConfig.getRealmResource(tenant));
//        Mockito.when(service.createAndAssignRoles("",userDTO)).thenReturn(userDTO);
        mockMvc.perform(put(END_POINT_PATH).contentType(MediaType.APPLICATION_JSON).header("Authorization","bearer "+token).content(requestBody))
                .andExpect(status().isOk())
                .andDo(print());

    }
}
