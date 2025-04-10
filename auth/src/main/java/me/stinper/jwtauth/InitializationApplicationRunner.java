package me.stinper.jwtauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.stinper.jwtauth.service.initialization.contract.InitializationService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitializationApplicationRunner implements ApplicationRunner {
    private final List<InitializationService<?>> initializationServices;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.atInfo().log("[#run]: Найдено {} сервисов инициализации", initializationServices.size());

        initializationServices.forEach(InitializationService::initialize);

        log.atInfo().log("[#run]: Все сервисы инициализации были вызваны и успешно завершились");
    }
}
