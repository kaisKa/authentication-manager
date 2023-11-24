package ava.io.authentication_manager.service;

import ava.io.authentication_manager.AuthenticationManagerApplication;
import ava.io.authentication_manager.config.PathBasedConfigResolver1;
import ava.io.authentication_manager.dtos.UserCredentials;
import ava.io.authentication_manager.services.KeycloakService;
import ava.io.authentication_manager.services.TenantService;
import ava.io.authentication_manager.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration TEST, test all the layers
 */
//@SpringBootTest(
//        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
//        classes = AuthenticationManagerApplication.class
//)
//@AutoConfigureMockMvc
public class TestKeycloakUserservice {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserService service;

    @MockBean
    private PathBasedConfigResolver1 keyCloakConfig;

    @MockBean
    private KeycloakService keycloakService;

    @MockBean
    private TenantService tenantService;

    @Test
    void test_createUserAndAssignRole() throws Exception {
        String END_POINT_PATH = "http://localhost:7777/api/v1/auth/tenant/provider/get-token";
        String tenant = "";
//        UserDTO userDTO = new UserDTO().builder().userName("kais_alkotamy8051").firstName("ksais").lastName("lolo")
//                .password("123").emailId("kaski@av2.com")
//                .phone("888888811")
//                .countryCode("+888").roles(List.of("patient")).build();

        UserCredentials cred = new UserCredentials();
        cred.setUsername("kais_alkotamy8051");
        cred.setPassword("123");
        var requestBody = objectMapper.writeValueAsString(cred);

        System.out.println(keyCloakConfig.getRealmResource(tenant));
//        Mockito.when(service.createAndAssignRoles("",userDTO)).thenReturn(userDTO);
        mockMvc.perform(post(END_POINT_PATH).contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isOk())
                .andDo(print());

    }}

//    @Test
//    void test_createUserAndAssignRole() throws Exception {
//        String END_POINT_PATH = "/api/v1/users";
//        UserDTO userDTO = new UserDTO().builder().userName("kiwreki32").firstName("ksais").lastName("lolo")
//                .password("123").emailId("kaski@av2.com")
//                .phone("888888811")
//                .countryCode("+888").roles(List.of("patient")).build();
//
//        var requestBody = objectMapper.writeValueAsString(userDTO);
//
//        System.out.println(keyCloakConfig.getResource());
//        Mockito.when(service.createAndAssignRoles(userDTO)).thenReturn(userDTO);
//        mockMvc.perform(post(END_POINT_PATH).contentType(MediaType.APPLICATION_JSON).content(requestBody))
//                .andExpect(status().isCreated())
//                .andDo(print());
//
//    }
//
////    @Test
//    void test_serachByCriteria() throws Exception {
//
//        UserLoginData user = new UserLoginData();
//        user.setFirstname("kais");
//
//        List<UserDTO> users = service.searchByCriteria("kais");
//
//        if (users.size() > 0)
//            assertThat(user.getFirstname()).isEqualTo(users.get(0).getFirstName());
//        else
//            MatcherAssert.assertThat("Fail here", false);
//
//    }
//
//
//}
