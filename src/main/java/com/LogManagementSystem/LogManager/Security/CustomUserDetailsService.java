package com.LogManagementSystem.LogManager.Security;

import com.LogManagementSystem.LogManager.Entity.User;
import com.LogManagementSystem.LogManager.Repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;

    public CustomUserDetailsService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepo.findByEmail(username);

        if(user.isPresent()){
            User usr = user.get();
            return User.builder()
                    .email(username)
                    .password(usr.getPassword())
                    .role(usr.getRole())
                    .build();
        }else{
            throw new UsernameNotFoundException("User not found with email : " + username);
        }
    }
}
