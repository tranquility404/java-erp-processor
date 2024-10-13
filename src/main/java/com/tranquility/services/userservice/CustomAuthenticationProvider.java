package com.tranquility.services.userservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class CustomAuthenticationProvider implements AuthenticationProvider {

//    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationProvider.class);

    @Autowired
    private UserDetailsServiceImp userDetailsService;

//    @Bean
//    private NoOpPasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName().toLowerCase();
        String rawPassword = authentication.getCredentials().toString();

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Log both the raw password and the stored password
//        System.out.println("db user:  " + userDetails);
//        System.out.println("basic auth username: " + username);
//        System.out.println("basic auth password: "+ rawPassword);

        // Compare passwords
        if (rawPassword.equals(userDetails.getPassword())) {
            System.out.println("Password match successful for user: {} "+ username);
            return new UsernamePasswordAuthenticationToken(userDetails, rawPassword, userDetails.getAuthorities());
        } else {
            System.out.println("Password match failed for user: {} "+ username);
            throw new AuthenticationException("Invalid password") {};
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
