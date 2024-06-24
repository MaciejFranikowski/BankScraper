package com.kontomatik.bankScraper;

import com.kontomatik.bankScraper.cli.UserInteraction;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.kontomatik.bankScraper.services.Authentication;

@SpringBootApplication
public class BankScraperApplication implements CommandLineRunner {

	private Authentication authentication;
	private UserInteraction userInteraction;
	public static void main(String[] args) {
		SpringApplication.run(BankScraperApplication.class, args);
	}
	public BankScraperApplication(Authentication authentication, UserInteraction userInteraction) {
		this.authentication = authentication;
		this.userInteraction = userInteraction;
	}
	@Override
	public void run(String... args) throws Exception {
		authentication.authenticate(userInteraction.getCredentials());
	}

}
