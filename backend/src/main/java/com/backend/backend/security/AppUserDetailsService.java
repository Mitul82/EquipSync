package com.backend.backend.security;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.backend.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        var user = userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() ->
                new UsernameNotFoundException("User not found"));

        // * id the users have multiple roles stored in the form of a set use the code below
        // var authorities = user.getRoles().stream()
        //     .map(role -> new SimpleGrantedAuthority(
        //         "ROLE_" + role.getRole().name()))
        //     .toList();

        // * code block for when users can only have any one given role at a time in the form of a string in db
        var authorities = Collections.singletonList(
            new SimpleGrantedAuthority(user.getRoles().getRole().name())
        );

        return User.withUsername(user.getEmail())
            .password(user.getPassword())
            .authorities(authorities)
            .build();
    }
}