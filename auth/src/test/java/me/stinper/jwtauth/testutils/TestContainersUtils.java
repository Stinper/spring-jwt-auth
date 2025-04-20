package me.stinper.jwtauth.testutils;

import org.testcontainers.containers.PostgreSQLContainer;

public final class TestContainersUtils {

    public static PostgreSQLContainer<?> initPostgreSQLContainer() {
        return new PostgreSQLContainer<>("postgres:17-alpine");
    }

}
