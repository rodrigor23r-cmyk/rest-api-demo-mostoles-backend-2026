package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RestApiDemoMostolesBackend2026Application {

	public static void main(String[] args) {
		SpringApplication.run(RestApiDemoMostolesBackend2026Application.class, args);
	}

}
/**
set -a; source .env; set +a     # una sola vez al abrir la terminal
mvn test                         # ya funciona el resto de la sesión
./mvnw spring-boot:run           # y esto también 
 */