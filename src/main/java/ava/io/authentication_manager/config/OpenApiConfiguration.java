package ava.io.authentication_manager.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;


@Configuration
@SecurityScheme(name = "Bearer Authentication" , type = SecuritySchemeType.HTTP,bearerFormat = "JWT",scheme = "bearer")
public class OpenApiConfiguration {

    // TODO handle the authorization through feign
//    @Bean
//    public RequestInterceptor requestInterceptor() {
//        return requestTemplate -> {
//            requestTemplate.header()
//            requestTemplate.header("user", username);
//            requestTemplate.header("password", password);
//            requestTemplate.header("Accept", ContentType.APPLICATION_JSON.getMimeType());
//        };
//    }

    @Value("${springdoc.version}")
    private String version;

    @Bean
    public OpenAPI customOpenAPI() {
        Tag t = new Tag();
        t.setName("Identification&Authentication");
        return new OpenAPI()
                .info(new Info()
                        .title("Identification and authorization service")
                        .version(version)
                        .description("Set of APIs helps integrating with keycloak providing the ability creating and authenticating users"))
                .addTagsItem(t);

    }


//    @Bean
//    public GroupedOpenApi publicApi() {
//        return GroupedOpenApi.builder()
//                .group("keycloak-users")
//                .displayName("keycloak users apis")
//                .pathsToMatch("**", "/users/**")
//                .group("auth")
//                .displayName("Authentication and Identification")
//                .pathsToMatch("**", "/auth/**")
//                .build();
//    }
}
