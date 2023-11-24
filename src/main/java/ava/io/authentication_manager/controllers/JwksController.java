package ava.io.authentication_manager.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/jwks")
public class JwksController {


    @Autowired
    @Qualifier("notSecureRestTemplate")
    private RestTemplate restTemplate;

    @Value("${kcloak.auth-server-url}")
    private String keycloakAuthServerUrl;

    @GetMapping
    public Map<String, Object> getJwks() {

        String jwksUrl = keycloakAuthServerUrl + "/realms/patient/protocol/openid-connect/certs";

        ResponseEntity<Map> response = restTemplate.exchange(jwksUrl, HttpMethod.GET, null, Map.class);
        return response.getBody();
    }
}
