package com.hoaxify.hoaxify;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.hoaxify.hoaxify.hoax.HoaxService;
import com.hoaxify.hoaxify.user.User;
import com.hoaxify.hoaxify.user.UserService;


@SpringBootApplication
public class HoaxifyApplication {

	public static void main(String[] args) {
		SpringApplication.run(HoaxifyApplication.class, args);
	}
	@Bean
	
	CommandLineRunner createInitialUsers(UserService userService, HoaxService hoaxService) {
		return (args) -> {
			try {
				userService.getByUsername("user1");				
			} catch (Exception e) {				
				for(int i = 1; i<=25;i++) {				
					User user = new User();
					user.setUsername("user"+i);
					user.setDisplayName("display"+i);
					user.setPassword("P4ssword");
					userService.save(user);
					
				}
			}
		};
	}
	
}
