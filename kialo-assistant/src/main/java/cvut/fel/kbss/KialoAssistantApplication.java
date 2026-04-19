package cvut.fel.kbss;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
public class KialoAssistantApplication {

	public static void main(String[] args) {
		SpringApplication.run(KialoAssistantApplication.class, args);
	}

}
