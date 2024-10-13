package com.tranquility.services.userservice;

import com.tranquility.data.entities.User;
import com.tranquility.data.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImp implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

//    public UserDetailsServiceImp(UserRepo userRepo) {
//        this.userRepo = userRepo;
//    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username.toLowerCase());
//        System.out.println("auth running: " + user + "\n" + (user!=null));

        if (user != null) {
            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .password(user.getPassword())
                    .username(user.getUsername())
                    .roles(user.getRoles())
                    .build();
            return userDetails;
        }
        throw  new UsernameNotFoundException("User not found with username: " + username);
    }


}
