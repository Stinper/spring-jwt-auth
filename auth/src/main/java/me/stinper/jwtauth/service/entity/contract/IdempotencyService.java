package me.stinper.jwtauth.service.entity.contract;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.lang.NonNull;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Предоставляет методы для обработки идемпотентных операций
 */
public interface IdempotencyService {
    /**
     * Метод, который оборачивает заданную операцию, пытаясь выполнить ее идемпотентно. Операция выполнится только
     * в том случае, если заданный ключ идемпотентности не существует в БД. В таком случае, операция будет выполнена
     * и ее результат будет связан с этим ключом идемпотентности
     * @param idempotencyKey ключ идемпотентности
     * @param serviceOperation операция, которую необходимо выполнить идемпотентно
     * @param targetType тип возвращаемого значения операции сервиса
     * @return результат выполнения операции (если ключ идемпотентности не найден), либо связанные с ключом данные
     * @throws JsonProcessingException если в процессе преобразования JSON -> T или T -> JSON произошла ошибка
     */
    <T> T process(@NonNull UUID idempotencyKey, @NonNull Supplier<T> serviceOperation, @NonNull Class<T> targetType)
            throws JsonProcessingException;
}
