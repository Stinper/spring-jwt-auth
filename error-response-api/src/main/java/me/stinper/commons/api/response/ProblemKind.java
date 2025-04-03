package me.stinper.commons.api.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * Представляет собой вид ошибки, т.е. самую верхнюю логическую группу ошибок.
 * Может быть использован в заголовке, как дополнительная машиночитаемая метаинформация для клиента.
 * </p>
 *
 * <br>
 *
 *
 * <p> Пример ответа от API с заголовком, используя ответ на основе {@link Problem} </p>
 * X-MyApiName-Error-Kind: EMPLOYEE_ERROR
 * <pre>{@code
 * {
 *   "type": "employee.email-not-unique",
 *   "localized_message": "Сотрудник с такой почтой уже зарегистрирован"
 * }
 * }</pre>
 *
 */
@RequiredArgsConstructor
@Getter
public enum ProblemKind {
    /**
     * Служит для группировки всех ошибок <b>аутентификации</b>, к примеру, неверный логин/пароль,
     * несуществующий аккаунт, невалидный токен, деактивированная учетная запись и т.д.
     */
    AUTHENTICATION_ERROR("AUTHENTICATION_ERROR"),

    /**
     * Служит для группировки всех ошибок <b>валидации</b>
     */
    VALIDATION_ERROR("VALIDATION_ERROR"),

    /**
     * Служит для группировки всех ошибок, связанных с <b>компонентами запроса</b>, такими как заголовки, параметры
     * и т.д. Пример: не был передан обязательный заголовок, заголовку был передан некорректный тип данных, не был
     * передан обязательный параметр и т.д.
     */
    REQUEST_COMPONENTS_ERROR("REQUEST_COMPONENTS_ERROR");


    private final String kind;
}
