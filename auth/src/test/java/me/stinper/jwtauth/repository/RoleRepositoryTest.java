package me.stinper.jwtauth.repository;

import me.stinper.jwtauth.entity.Role;
import me.stinper.jwtauth.testutils.TestContainersUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RoleRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = TestContainersUtils.initPostgreSQLContainer();

    @Autowired
    RoleRepository roleRepository;


    @Test
    void isTableEmpty_whenTableIsEmpty_thenReturnsTrue() {
        //GIVEN -- None
        //WHEN
        boolean isTableEmpty = roleRepository.isTableEmpty();

        //THEN
        assertThat(isTableEmpty).isTrue();
    }


    @Test
    void isTableEmpty_whenTableIsNotEmpty_thenReturnsFalse() {
        //GIVEN
        final Role role = Role.builder()
                .roleName("ROLE_ADMIN")
                .prefix("admin")
                .build();

        roleRepository.save(role);

        //WHEN
        boolean isTableEmpty = roleRepository.isTableEmpty();

        //THEN
        assertThat(isTableEmpty).isFalse();
    }
}