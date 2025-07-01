package br.com.danielsilva.ms_email;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MsEmailApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsEmailApplication.class, args);
	}

}
