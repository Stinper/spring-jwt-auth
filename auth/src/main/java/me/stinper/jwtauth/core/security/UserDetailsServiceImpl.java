package me.stinper.jwtauth.core.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.stinper.jwtauth.core.security.jwt.JwtAuthUserDetails;
import me.stinper.jwtauth.repository.UserRepository;
import me.stinper.jwtauth.utils.MessageSourceHelper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    private final MessageSourceHelper messageSourceHelper;

    @Override
    public JwtAuthUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository
                .findByEmailIgnoreCase(username)
                .orElseThrow(() -> {
                    log.atDebug().log("[#loadUserByUsername]: Пользователь с эл. почтой '{}' не найден", username);

                    return new UsernameNotFoundException(
                            messageSourceHelper.getLocalizedMessage("messages.user.not-found.email", username)
                    );
                });
    }
}
