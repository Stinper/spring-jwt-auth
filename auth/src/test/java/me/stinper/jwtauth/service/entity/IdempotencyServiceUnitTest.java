package me.stinper.jwtauth.service.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import me.stinper.jwtauth.service.entity.contract.IdempotencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for IdempotencyService interface default methods")
class IdempotencyServiceUnitTest {
    private IdempotencyService idempotencyService;

    private TestData testData;


    @BeforeEach
    void setUp() {
        this.idempotencyService = mock(IdempotencyService.class, CALLS_REAL_METHODS);
        this.testData = new TestData();
    }


    @Test
    @DisplayName(
            """
            [#wrap]: Checks that the method does not invoke the service if the passed
             idempotence key is correct and the data is associated with it.
            """
    )
    void wrap_whenProcessMethodReturnsResponseData_thenNeverCallsServiceMethod() throws JsonProcessingException {
        //GIVEN
        when(idempotencyService.process(testData.IDEMPOTENCY_KEY_VALUE, TestData.Person.class)).thenReturn(testData.PERSON);

        //WHEN
        var response = idempotencyService.wrap(testData.IDEMPOTENCY_KEY_VALUE, testData.ILLEGAL_SERVICE_OPERATION, TestData.Person.class);

        //THEN
        assertThat(response).isEqualTo(testData.PERSON);

        verify(idempotencyService, never()).write(any(), any());
        verify(testData.ILLEGAL_SERVICE_OPERATION, never()).get();
    }


    @Test
    @DisplayName(
            """
            [#wrap]: Checks that the method invokes the service if the passed idempotence key is incorrect.
            """
    )
    void wrap_whenProcessMethodReturnsNull_thenCallsServiceMethod() throws JsonProcessingException {
        //GIVEN
        when(idempotencyService.process(any(), any())).thenReturn(null);

        //WHEN
        var response = idempotencyService.wrap(testData.IDEMPOTENCY_KEY_VALUE, testData.SERVICE_OPERATION, TestData.Person.class);

        //THEN
        assertThat(response).isEqualTo(testData.PERSON);

        verify(idempotencyService, times(1)).process(any(), any());
        verify(testData.SERVICE_OPERATION, times(1)).get();
        verify(idempotencyService, times(1)).write(any(), eq(testData.PERSON));
    }


    @Test
    @DisplayName(
            """
            [#wrap]: Checks that if a Json ProcessingException was thrown,
             but the data has already been received, the method will not call the service.
            """
    )
    void wrap_whenJsonProcessingExceptionIsThrownAndDataIsNotNull_thenNeverCallsServiceMethod() throws JsonProcessingException {
        //GIVEN
        when(idempotencyService.process(any(), eq(TestData.Person.class))).thenReturn(testData.PERSON);

        doThrow(JsonProcessingException.class)
                .when(idempotencyService)
                .write(any(), any());

        //WHEN
        var response = idempotencyService.wrap(testData.IDEMPOTENCY_KEY_VALUE, testData.SERVICE_OPERATION, TestData.Person.class);

        //THEN
        assertThat(response).isEqualTo(testData.PERSON);

        verify(testData.SERVICE_OPERATION, never()).get();
    }


    @Test
    @DisplayName(
            """
            [#wrap]: Checks that if a Json ProcessingException was thrown and no data was received, the method calls the service.
            """
    )
    void wrap_whenJsonProcessingExceptionIsThrownAndDataIsNull_thenCallsServiceMethod() throws JsonProcessingException {
        //GIVEN
        when(idempotencyService.process(any(), any())).thenThrow(JsonProcessingException.class);

        //WHEN
        var response = idempotencyService.wrap(testData.IDEMPOTENCY_KEY_VALUE, testData.SERVICE_OPERATION, TestData.Person.class);

        //THEN
        assertThat(response).isEqualTo(testData.PERSON);

        verify(testData.SERVICE_OPERATION, times(1)).get();
    }

    private static class TestData {
        final UUID IDEMPOTENCY_KEY_VALUE = UUID.randomUUID();
        final Person PERSON = new Person("Ivan", "Ivanov");

        /*
        Imitating service operation which should NOT be called

        !!! DO NOT REPLACE THIS WITH LAMBDA, IT WILL NOT WORK !!!
         */
        final Supplier<Person> ILLEGAL_SERVICE_OPERATION = spy(new Supplier<>() {
            @Override
            public TestData.Person get() {
                throw new IllegalStateException();
            }
        });

        /*
        Imitating service operation which should be called

        !!! DO NOT REPLACE THIS WITH LAMBDA, IT WILL NOT WORK !!!
        */
        final Supplier<Person> SERVICE_OPERATION = spy(new Supplier<>() {
            @Override
            public TestData.Person get() {
                return PERSON;
            }
        });

        public record Person (
                String firstName,
                String lastName
        ) {}
    }
}
