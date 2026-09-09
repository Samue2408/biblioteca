package com.ezertech.backend;

import com.ezertech.backend.config.JwtProperties;
import com.ezertech.backend.config.MailProperties;
import com.ezertech.backend.config.OpenLibraryProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, MailProperties.class, OpenLibraryProperties.class})
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
