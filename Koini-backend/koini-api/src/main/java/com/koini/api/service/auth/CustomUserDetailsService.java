package com.koini.api.service.auth;

import com.koini.core.domain.entity.User;
import com.koini.persistence.repository.UserRepository;
import com.koini.core.exception.UserNotFoundException;
import java.util.UUID;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findById(UUID.fromString(username))
        .orElseThrow(() -> new UserNotFoundException("User not found"));
    return new KoiniUserDetails(user);
  }
}
