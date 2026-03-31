package hu.sokoban;

import hu.sokoban.model.User;
import hu.sokoban.model.enums.Role;
import hu.sokoban.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class SokobanPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(SokobanPortalApplication.class, args);
    }

    @Bean
    CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder encoder) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User("admin", "admin@sokoban.hu", encoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);
            }
        };
    }
}
