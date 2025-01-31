package es.santander.ascender.individual;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	// @Bean
    // public OpenAPI customOpenAPI() {
    //     return new OpenAPI()
    //             .info(new Info().title("Mi API")
    //             .description("Descripción de mi API")
    //             .version("1.0"));
    // }
}
