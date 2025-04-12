package me.stinper.jwtauth.service.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.stinper.jwtauth.entity.IdempotencyKey;
import me.stinper.jwtauth.exception.IdempotencyKeyExpiredException;
import me.stinper.jwtauth.exception.ObjectValueValidationException;
import me.stinper.jwtauth.repository.IdempotencyKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;
import org.springframework.validation.SimpleErrors;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for IdempotencyServiceImpl class")
class IdempotencyServiceImplUnitTest {
    @Mock
    private IdempotencyKeyRepository idempotencyKeyRepository;
    @Mock
    private ObjectMapper objectMapper;

    private final Duration idempotencyPeriod = Duration.ofDays(1);

    private IdempotencyServiceImpl idempotencyService;

    @BeforeEach
    void setUp() {
        this.idempotencyService = new IdempotencyServiceImpl(idempotencyKeyRepository, objectMapper);
        idempotencyService.setIdempotencyPeriod(idempotencyPeriod);
    }


    @Test
    void process_whenIdempotencyKeyExistsAndNotExpired_thenReturnsSavedResultAndNeverInvokesService() throws JsonProcessingException {
        //GIVEN
        final UUID idempotencyKey = UUID.fromString("bd4620ef-cb3c-4da8-b10e-47b100489d5b");
        final String contentData = "CONTENT_DATA";

        final Person person = new Person("Ivan", "Ivanov");

        final IdempotencyKey key = IdempotencyKey.builder()
                .id(1L)
                .key(idempotencyKey)
                .issuedAt(Instant.now())
                .responseData(contentData)
                .build();

        /*
            Imitating service operation which should NOT be called
            !!! DO NOT REPLACE THIS WITH LAMBDA, LAMBDAS ARE FINAL, AND SPY() NEEDS TO CREATE A PROXY, IT WILL NOT WORK !!!
         */
        final Supplier<Person> serviceOperation = spy(new Supplier<>() {
            @Override
            public Person get() {
                throw new IllegalStateException();
            }
        });

        when(idempotencyKeyRepository.findByKey(idempotencyKey)).thenReturn(Optional.of(key));
        when(objectMapper.readValue(contentData, Person.class)).thenReturn(person);

        //WHEN
        Person response = idempotencyService.process(idempotencyKey, serviceOperation, Person.class);

        //THEN
        assertThat(response).isEqualTo(person);

        verify(idempotencyKeyRepository).findByKey(idempotencyKey);
        verify(objectMapper).readValue(contentData, Person.class);

        verifyNoMoreInteractions(idempotencyKeyRepository, objectMapper);
        verifyNoInteractions(serviceOperation);
    }


    @Test
    void process_whenIdempotencyKeyIsExpired_thenThrowsException() {
        //GIVEN
        final UUID idempotencyKey = UUID.fromString("bd4620ef-cb3c-4da8-b10e-47b100489d5b");

        final IdempotencyKey key = IdempotencyKey.builder()
                .id(1L)
                .key(idempotencyKey)
                .issuedAt(Instant.now().minus(idempotencyPeriod.multipliedBy(2))) //Expired
                .build();

        /*
            Imitating service operation which should NOT be called
            !!! DO NOT REPLACE THIS WITH LAMBDA, LAMBDAS ARE FINAL, AND SPY() NEEDS TO CREATE A PROXY, IT WILL NOT WORK !!!
         */
        final Supplier<Person> serviceOperation = spy(new Supplier<>() {
            @Override
            public Person get() {
                throw new IllegalStateException();
            }
        });

        when(idempotencyKeyRepository.findByKey(idempotencyKey)).thenReturn(Optional.of(key));

        //WHEN & THEN
        assertThatExceptionOfType(IdempotencyKeyExpiredException.class)
                .isThrownBy(() -> idempotencyService.process(idempotencyKey, serviceOperation, Person.class))
                .satisfies(ex -> {
                    assertThat(ex.getErrorMessageCode()).isEqualTo("messages.idempotency-key.expired");
                    assertThat(ex.getArgs()).containsExactly(idempotencyKey);
                });

        verify(idempotencyKeyRepository).findByKey(idempotencyKey);
        verifyNoMoreInteractions(idempotencyKeyRepository);

        verifyNoInteractions(objectMapper, serviceOperation);
    }


    @Test
    void process_whenIdempotencyKeyDoesNotExist_thenInvokesServiceAndSavesKey() throws JsonProcessingException {
        //GIVEN
        final UUID idempotencyKey = UUID.fromString("bd4620ef-cb3c-4da8-b10e-47b100489d5b");
        final Person person = new Person("Ivan", "Ivanov");
        final String jsonValue = "JSON_VALUE";

        /*
            Imitating service operation which should be called
            !!! DO NOT REPLACE THIS WITH LAMBDA, LAMBDAS ARE FINAL, AND SPY() NEEDS TO CREATE A PROXY, IT WILL NOT WORK !!!
         */
        final Supplier<Person> serviceOperation = spy(new Supplier<>() {
            @Override
            public Person get() {
                return person;
            }
        });

        when(idempotencyKeyRepository.findByKey(idempotencyKey)).thenReturn(Optional.empty());
        when(objectMapper.writeValueAsString(person)).thenReturn(jsonValue);

        //WHEN
        Person response = idempotencyService.process(idempotencyKey, serviceOperation, Person.class);

        //THEN
        assertThat(response).isEqualTo(person);

        verify(idempotencyKeyRepository).findByKey(idempotencyKey);
        verify(idempotencyKeyRepository).save(
                argThat(arg -> arg.getKey().equals(idempotencyKey) && arg.getResponseData().equals(jsonValue))
        );

        verify(objectMapper).writeValueAsString(person);

        verify(serviceOperation).get();
    }


    private record Person(String firstName, String lastName) {}
}
