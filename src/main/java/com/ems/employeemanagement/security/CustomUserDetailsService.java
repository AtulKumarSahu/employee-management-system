package com.ems.employeemanagement.security;

import com.ems.employeemanagement.entity.User;
import com.ems.employeemanagement.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service that loads user-specific data for Spring Security authentication.
 *
 * <p>Implements {@link UserDetailsService} so that Spring Security's
 * DaoAuthenticationProvider can load credentials and verify passwords.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads the user by either username or email.
     *
     * @param usernameOrEmail the identifier entered into login forms / requests
     * @return UserDetails representation of the User
     * @throws UsernameNotFoundException if no user is found with this username or email
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByEmailOrUsername(usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username or email: " + usernameOrEmail));

        return CustomUserDetails.build(user);
    }
}

