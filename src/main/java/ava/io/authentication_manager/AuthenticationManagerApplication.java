package ava.io.authentication_manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
//import springfox.documentation.swagger2.annotations.EnableSwagger2;
//import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

@SpringBootApplication
@EnableAutoConfiguration
//@EnableSwagger2
@EnableFeignClients
public class AuthenticationManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthenticationManagerApplication.class, args);
	}

}
