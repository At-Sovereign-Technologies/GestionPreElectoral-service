package com.selloLegitimo.GestionPreElectoral;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class GestionPreElectoralApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionPreElectoralApplication.class, args);
	}

}
