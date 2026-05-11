package com.cookiesstore.infra.auth.service;

import com.cookiesstore.infra.auth.repository.InfraUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class InfraUserDetailsService implements UserDetailsService {

    private final InfraUserRepository userRepository;

    public InfraUserDetailsService(InfraUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsernameIgnoreCase(username)
            .map(user -> User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .disabled(!user.isEnabled())
                .authorities("AUTHENTICATED")
                .build())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
