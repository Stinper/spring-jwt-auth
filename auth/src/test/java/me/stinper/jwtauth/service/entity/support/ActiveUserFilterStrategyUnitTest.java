package me.stinper.jwtauth.service.entity.support;

import me.stinper.jwtauth.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for ActiveUserFilterStrategy class")
class ActiveUserFilterStrategyUnitTest {
    private ActiveUserFilterStrategy activeUserFilterStrategy;

    @BeforeEach
    void setUp() {
        this.activeUserFilterStrategy = new ActiveUserFilterStrategy();
    }

    @Test
    void filterUsersPage_whenInputPageContainsDeactivatedUsers_thenReturnsPageWithoutThem() {
        //GIVEN
        final User firstUser = User.builder()
                .email("firstUser@gmail.com")
                .deactivatedAt(Instant.now())
                .build();

        final User secondUser = User.builder()
                .email("secondUser@gmail.com")
                .build();
        final Pageable pageable = mock(Pageable.class);

        Page<User> pageToFilter = new PageImpl<>(
                List.of(firstUser, secondUser),
                pageable,
                2
        );

        //WHEN
        Page<User> filteredPage = activeUserFilterStrategy.filterUsersPage(pageToFilter);

        //THEN
        assertThat(filteredPage.getContent())
                .containsExactlyInAnyOrder(secondUser)
                .doesNotContain(firstUser);

        assertThat(filteredPage.getPageable()).isEqualTo(pageable);
        assertThat(filteredPage.getTotalElements()).isEqualTo(1);
    }


    @Test
    void filterUsersPage_whenInputPageDoesNotContainsDeactivatedUsers_thenReturnsSamePage() {
        //GIVEN
        final User firstUser = User.builder().build();
        final User secondUser = User.builder().build();
        final Pageable pageable = mock(Pageable.class);

        Page<User> pageToFilter = new PageImpl<>(
                List.of(firstUser, secondUser),
                pageable,
                2
        );

        //WHEN
        Page<User> filteredPage = activeUserFilterStrategy.filterUsersPage(pageToFilter);

        //THEN
        assertThat(filteredPage.getContent()).containsExactlyInAnyOrder(firstUser, secondUser);
        assertThat(filteredPage.getPageable()).isEqualTo(pageable);
        assertThat(filteredPage.getTotalElements()).isEqualTo(2);
    }
}