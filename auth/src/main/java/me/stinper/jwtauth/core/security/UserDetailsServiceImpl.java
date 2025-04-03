package me.stinper.jwtauth.core.security;

import lombok.RequiredArgsConstructor;
import me.stinper.jwtauth.repository.UserRepository;
import me.stinper.jwtauth.utils.MessageSourceHelper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    private final MessageSourceHelper messageSourceHelper;

    @Override
    public JwtAuthUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository
                .findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        messageSourceHelper.getLocalizedMessage("messages.user.not-found.email", username))
                );
    }
}
