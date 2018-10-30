package com.pfm.auth;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {

  private UserRepository userRepository;
  private TokenService tokenService;

  public Optional<UserDetails> authenticateUser(User userToAuthenticate) {
    Optional<User> appUserFromDb = userRepository.findByUsernameIgnoreCase(userToAuthenticate.getUsername());
    if (!appUserFromDb.isPresent()) {
      return Optional.empty();
    }

    User userFromDb = appUserFromDb.get();

    if (!BCrypt.checkpw(userToAuthenticate.getPassword(), userFromDb.getPassword())) {
      return Optional.empty();
    }

    String token = tokenService.generateToken(userFromDb);

    UserDetails userDetails = new UserDetails(userFromDb.getId(), userFromDb.getUsername(), userFromDb.getFirstName(),
        userFromDb.getLastName(), token);
    return Optional.of(userDetails);
  }

  public User registerUser(User user) {
    String hashedPassword = hashPassword(user.getPassword());
    user.setPassword(hashedPassword);
    return userRepository.save(user);
  }

  private static String hashPassword(String passwordToHash) {
    return BCrypt.hashpw(passwordToHash, BCrypt.gensalt());
  }

  public boolean isUsernameAlreadyUsed(String username) {
    return userRepository.findByUsernameIgnoreCase(username).isPresent();
  }

}