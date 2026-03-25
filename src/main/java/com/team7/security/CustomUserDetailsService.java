package com.team7.security;

import com.team7.repository.client.UserSecurityRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
  private final UserSecurityRepository userSecurityRepository;

  public CustomUserDetailsService(UserSecurityRepository userSecurityRepository) {
    this.userSecurityRepository = userSecurityRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    UserSecurityRepository.SecurityUserRecord user = userSecurityRepository.findByEmail(username);
    if (user == null) {
      throw new UsernameNotFoundException("User not found");
    }

    List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
    return new User(user.email(), user.passwordHash(), authorities);
  }
}

