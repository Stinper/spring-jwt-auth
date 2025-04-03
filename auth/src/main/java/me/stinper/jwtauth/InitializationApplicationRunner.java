package me.stinper.jwtauth;

import lombok.RequiredArgsConstructor;
import me.stinper.jwtauth.entity.User;
import me.stinper.jwtauth.service.initialization.contract.InitializationService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InitializationApplicationRunner implements ApplicationRunner {
    private final InitializationService<User> adminAccountInitializationService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        adminAccountInitializationService.initialize();
    }
}
