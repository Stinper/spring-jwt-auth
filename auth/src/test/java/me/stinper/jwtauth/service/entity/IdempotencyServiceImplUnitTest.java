package me.stinper.jwtauth.service.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.stinper.jwtauth.entity.IdempotencyKey;
import me.stinper.jwtauth.exception.ObjectValueValidationException;
import me.stinper.jwtauth.repository.IdempotencyKeyRepository;
import me.stinper.jwtauth.service.entity.IdempotencyServiceImpl;
import me.stinper.jwtauth.validation.IdempotencyKeyValidator;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for IdempotencyServiceImpl class")
class IdempotencyServiceImplUnitTest {
    @Mock private IdempotencyKeyRepository idempotencyKeyRepository;
    @Mock private ObjectMapper objectMapper;
    @Mock private IdempotencyKeyValidator idempotencyKeyValidator;

    private IdempotencyServiceImpl idempotencyService;
    private TestData testData;

    @BeforeEach
    void setUp() {
        this.idempotencyService = new IdempotencyServiceImpl(
                idempotencyKeyRepository, objectMapper, idempotencyKeyValidator, Duration.ofMinutes(5)
        );

        this.testData = new TestData();
    }

    @Test
    @DisplayName("[#process]: Checks that the method returns a result if the idempotence key is valid")
    void process_whenIdempotencyKeyIsValid_thenReturnsResult() throws JsonProcessingException {
        //GIVEN
        when(idempotencyKeyRepository.findByKey(testData.IDEMPOTENCY_KEY_VALUE)).thenReturn(Optional.of(testData.PERSON_RESPONSE_IDEMPOTENCY_KEY));
        when(objectMapper.readValue(testData.PERSON_JSON_REPRESENTATION, TestData.Person.class)).thenReturn(testData.PERSON);

        //WHEN
        var person = idempotencyService.process(testData.IDEMPOTENCY_KEY_VALUE, TestData.Person.class);

        //THEN
        assertThat(person).isEqualTo(testData.PERSON);

        verify(idempotencyKeyRepository, times(1)).findByKey(testData.IDEMPOTENCY_KEY_VALUE);
    }


    @Test
    @DisplayName("[#process]: Checks that the method returns NULL if the idempotence key is NOT valid")
    void process_whenIdempotencyKeyIsInvalid_thenReturnsNull() throws JsonProcessingException {
        //GIVEN
        when(idempotencyKeyRepository.findByKey(testData.IDEMPOTENCY_KEY_VALUE)).thenReturn(Optional.empty());

        //WHEN
        var person = idempotencyService.process(testData.IDEMPOTENCY_KEY_VALUE, TestData.Person.class);

        //THEN
        assertThat(person).isNull();

        verify(idempotencyKeyRepository, times(1)).findByKey(testData.IDEMPOTENCY_KEY_VALUE);
    }


    @Test
    @DisplayName("[#write]: Checks that the method saves data to the database if the idempotence key is valid")
    void write_whenIdempotencyKeyIsValid_thenSave() throws JsonProcessingException {
        //GIVEN
        IdempotencyKey unsavedKey = IdempotencyKey.builder()
                .key(testData.IDEMPOTENCY_KEY_VALUE)
                .responseData(testData.PERSON_JSON_REPRESENTATION)
                .build();

        //Empty Errors Object (request is valid)
        when(idempotencyKeyValidator.validateObject(testData.IDEMPOTENCY_KEY_VALUE))
                .thenReturn(new SimpleErrors(testData.IDEMPOTENCY_KEY_VALUE));

        when(objectMapper.writeValueAsString(testData.PERSON)).thenReturn(testData.PERSON_JSON_REPRESENTATION);

        //WHEN
        idempotencyService.write(testData.IDEMPOTENCY_KEY_VALUE, testData.PERSON);

        //THEN
        verify(idempotencyKeyRepository, times(1)).save(unsavedKey);
    }


    @Test
    @DisplayName("[#write]: Checks that the method throws an exception if the idempotence key is NOT valid")
    void write_whenIdempotencyKeyIsInvalid_thenThrowsException() throws JsonProcessingException {
        //GIVEN
        Errors idempotencyKeyValidationErrors = new SimpleErrors(testData.IDEMPOTENCY_KEY_VALUE);
        idempotencyKeyValidationErrors.reject("ERROR_CODE");

        when(idempotencyKeyValidator.validateObject(testData.IDEMPOTENCY_KEY_VALUE)).thenReturn(idempotencyKeyValidationErrors);

        //WHEN & THEN
        assertThatExceptionOfType(ObjectValueValidationException.class)
                .isThrownBy(() -> idempotencyService.write(testData.IDEMPOTENCY_KEY_VALUE, null))
                .satisfies(ex ->
                        assertThat(ex.getObjectErrors())
                                .hasSize(1)
                );
    }

    private static class TestData {
        final UUID IDEMPOTENCY_KEY_VALUE = UUID.randomUUID();

        final Person PERSON = new Person("Ivan", "Ivanov");

        final String PERSON_JSON_REPRESENTATION = "{\"firstName\":\"Ivan\",\"lastName\":\"Ivanov\"}";

        final IdempotencyKey PERSON_RESPONSE_IDEMPOTENCY_KEY = IdempotencyKey.builder()
                .id(1L)
                .key(IDEMPOTENCY_KEY_VALUE)
                .issuedAt(Instant.now())
                .responseData(PERSON_JSON_REPRESENTATION)
                .build();

        public record Person (
                String firstName,
                String lastName
        ) {}


    }

}
