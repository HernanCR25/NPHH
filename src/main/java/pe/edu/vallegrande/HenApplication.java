package pe.edu.vallegrande;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;


@SpringBootApplication
public class HenApplication {

	public static void main(String[] args) {
		SpringApplication.run(HenApplication.class, args);
	}
	@Configuration
	public static class Myconfiguration{
		@Bean
		public WebFluxConfigurer corsConfigurer(){
			return new WebFluxConfigurer() {
				@Override
				public void addCorsMappings(CorsRegistry registry) {
					registry.addMapping("/**")
							.allowedMethods("HEAD", "GET", "PUT", "POST", "DELETE", "PATCH");
				}
			};
		}
	}

}
