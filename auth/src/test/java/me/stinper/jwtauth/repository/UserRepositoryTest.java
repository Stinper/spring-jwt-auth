package me.stinper.jwtauth.repository;

import me.stinper.jwtauth.entity.Role;
import me.stinper.jwtauth.entity.User;
import me.stinper.jwtauth.testutils.TestContainersUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = TestContainersUtils.initPostgreSQLContainer();

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Test
    void findByEmailIgnoreCase_whenUserHasNoRoles_thenReturnsUser() {
        //GIVEN
        final String email = "user@gmail.com";

        final User userWithNoRoles = User.builder()
                .email(email)
                .password("123")
                .roles(Collections.emptySet())
                .build();

        userRepository.save(userWithNoRoles);

        //WHEN
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);

        //THEN
        assertThat(user).isNotNull();
        assertThat(user.getUuid()).isNotNull(); //Auto generated
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getRoles()).isEmpty();
    }


    @Test
    void findByEmailIgnoreCase_whenUserHasAtLeastOneRole_thenReturnsUser() {
        //GIVEN
        final String email = "user@gmail.com";

        final Role adminRole = Role.builder()
                .roleName("ROLE_ADMIN")
                .prefix("Admin")
                .permissions(Collections.emptySet())
                .build();

        final User userWithNoRoles = User.builder()
                .email(email)
                .password("123")
                .roles(Set.of(adminRole))
                .build();

        roleRepository.save(adminRole);
        userRepository.save(userWithNoRoles);

        //WHEN
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);

        //THEN
        assertThat(user).isNotNull();
        assertThat(user.getUuid()).isNotNull(); //Auto generated
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getRoles())
                .hasSize(1)
                .first()
                .satisfies(role -> {
                    assertThat(role.getId()).isNotNull(); // Role is saved, id is auto generated
                    assertThat(role.getRoleName()).isEqualTo("ROLE_ADMIN");
                    assertThat(role.getPrefix()).isEqualTo("Admin");
                    assertThat(role.getPermissions()).isEmpty();
                });
    }


    @Test
    void findByEmailIgnoreCase_whenEmailCaseDiffers_thenReturnsUser() {
        //GIVEN
        final String email = "user@gmail.com";
        final String emailUpperCase = email.toUpperCase();

        final User userWithNoRoles = User.builder()
                .email(email)
                .password("123")
                .build();

        userRepository.save(userWithNoRoles);

        //WHEN
        User user = userRepository.findByEmailIgnoreCase(emailUpperCase).orElse(null);

        //THEN
        assertThat(user).isNotNull();
        assertThat(user.getUuid()).isNotNull(); //Auto generated
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getRoles()).isEmpty();
    }


    @Test
    void isTableEmpty_whenTableIsEmpty_thenReturnsTrue() {
        //GIVEN -- None
        //WHEN
        boolean isTableEmpty = userRepository.isTableEmpty();

        //THEN
        assertThat(isTableEmpty).isTrue();
    }


    @Test
    void isTableEmpty_whenTableIsNotEmpty_thenReturnsFalse() {
        //GIVEN
        final User userWithNoRoles = User.builder()
                .email("user@gmail.com")
                .password("123")
                .build();

        userRepository.save(userWithNoRoles);

        //WHEN
        boolean isTableEmpty = userRepository.isTableEmpty();

        //THEN
        assertThat(isTableEmpty).isFalse();
    }
}