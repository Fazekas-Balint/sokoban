package hu.sokoban.service;

import hu.sokoban.dto.RegistrationDto;
import hu.sokoban.model.User;
import java.util.Optional;

public interface UserService {

    User register(RegistrationDto dto);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
