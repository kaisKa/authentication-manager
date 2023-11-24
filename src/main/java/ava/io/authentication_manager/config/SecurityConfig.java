package ava.io.authentication_manager.config;


import ava.io.authentication_manager.config.mullti_tenant.KeycloakJwtRolesConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver;

    public SecurityConfig(AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver) {
        this.authenticationManagerResolver = authenticationManagerResolver;
    }

    private static final String[] AUTH_WHITE_LIST = {
            "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**", "/v2/api-docs/**", "/webjars/**"
    };

    @Autowired
    @Qualifier("delegatedAuthenticationEntryPoint")
    AuthenticationEntryPoint authEntryPoint;


    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://swagger-ui-staging.hygiai-staging:9999", "*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("X-Requested-With", "Origin", "Content-Type", "Accept", "Authorization","Requestor-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        DelegatingJwtGrantedAuthoritiesConverter authoritiesConverter =
                // Using the delegating converter multiple converters can be combined
                new DelegatingJwtGrantedAuthoritiesConverter(
                        // First add the default converter
                        new JwtGrantedAuthoritiesConverter(),
                        // Second add our custom Keycloak specific converter
                        new KeycloakJwtRolesConverter());


        http.cors().and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                // manage routes secularisation here
                .and()
                .authorizeRequests()
//                .antMatchers("/api/v1/tenant/*/auth/**").permitAll()

                .antMatchers(HttpMethod.GET,"/api/v1/auth/user/info").hasRole("patient")
                .antMatchers(HttpMethod.PUT,"/api/v1/auth/user/info").authenticated()
                .antMatchers(HttpMethod.GET,"/api/v1/auth/logout").authenticated()


//                .antMatchers(HttpMethod.POST, "/api/v1/tenant/*/users/**", "/api/v1/users/**").hasAnyRole("super_admin", "admin", "doctor")
//                .antMatchers(HttpMethod.GET, "/api/v1/tenant/*/users/**", "/api/v1/users/**").hasAnyRole("patient", "doctor", "provider", "clinic", "pharmacy", "lab", "insurance")
//                .antMatchers(HttpMethod.DELETE , "/api/v1/tenant/*/users/tenant/{tenant}/{id}").permitAll()
//                .antMatchers("/api/v1/manage/tenant/**").permitAll()
//                .antMatchers("/api/v1/tenant/*/manage/**").hasAnyRole("hygiai_admin")
                .antMatchers(AUTH_WHITE_LIST).permitAll()
                .anyRequest().permitAll()
                .and().csrf().disable()
                .oauth2ResourceServer(oauth -> oauth
                        .authenticationManagerResolver(authenticationManagerResolver));
//                .exceptionHandling().authenticationEntryPoint(authEntryPoint); // needed to delegate the exception to the adviseController
//        jwtAuthenticationConverter(jwt -> new JwtAuthenticationToken(jwt, authoritiesConverter.convert(jwt)));

        return http.build();
    }
}
