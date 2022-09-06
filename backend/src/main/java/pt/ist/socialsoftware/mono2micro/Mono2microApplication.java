package pt.ist.socialsoftware.mono2micro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@PropertySource({ "classpath:application.properties", "classpath:specific.properties"})
@EnableMongoRepositories
@SpringBootApplication
public class Mono2microApplication {

	public static void main(String[] args) {
		SpringApplication.run(Mono2microApplication.class, args);
	}

}

