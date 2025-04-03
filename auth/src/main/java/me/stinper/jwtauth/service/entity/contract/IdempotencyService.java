package me.stinper.jwtauth.service.entity.contract;

import com.fasterxml.jackson.core.JsonProcessingException;
import me.stinper.jwtauth.exception.ObjectValueValidationException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Предоставляет методы для обработки идемпотентных операций
 */
public interface IdempotencyService {
    /**
     * Метод, который попытается получить из БД тело JSON, связанное с конкретным ключом идемпотентности.
     * <br>
     * <b>Контракт:</b> Если метод вернул null, значит ключа либо нет в БД, либо период для его возврата истек.
     * В этом случае нужно вызвать реальный сервис
     * @param idempotencyKey ключ идемпотентности
     * @param targetType тип, к которому сервис попытается привести полученный из базы данных JSON
     * @return тело ответа JSON, хранящееся в БД, приведенное к конкретному типу T
     * @throws JsonProcessingException если конвертация JSON -> T не удалась
     */
    @Nullable
    <T> T process(@NonNull UUID idempotencyKey, @NonNull Class<T> targetType) throws JsonProcessingException;

    /**
     * Метод, который записывает в БД значение JSON по заданному ключу идемпотентности
     * @param idempotencyKey ключ идемпотентности
     * @param data данные для записи
     * @throws JsonProcessingException если преобразование T -> JSON не удалось. Выброс этого исключения
     * означает, что запись в БД создана НЕ была
     * @throws ObjectValueValidationException если переданный ключ идемпотентности не уникален, т.е. уже существует в базе
     */
    <T> void write(@NonNull UUID idempotencyKey, @NonNull T data) throws JsonProcessingException, ObjectValueValidationException;

    /**
     * Оборачивает заданный метод, выполняя его идемпотентно
     * @param idempotencyKey ключ идемпотентности
     * @param serviceOperation метод, который необходимо выполнить идемпотентно
     * @param targetType тип возвращаемого значения метода
     * @return выполняет операцию идемпотентно и возвращает ее результат
     */
    default <T> T wrap(@NonNull UUID idempotencyKey, @NonNull Supplier<T> serviceOperation, @NonNull Class<T> targetType) {
        T responseData = null;

        try {
            responseData = this.process(idempotencyKey, targetType);

            if (responseData != null)
                return responseData;

            responseData = serviceOperation.get();
            this.write(idempotencyKey, responseData);

            return responseData;
        }
        catch (JsonProcessingException e) {
            if (responseData != null)
                return responseData;

            return serviceOperation.get();
        }
    }
}
