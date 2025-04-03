package me.stinper.jwtauth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RelatedEntityExistsException extends RuntimeException {
    /**
     * Код (идентификатор) ошибки, который будет отображаться в JSON ответе
     */
    private final String errorCode;

    /**
     * <b>Код сообщения</b> об ошибке, содержащегося в файле
     */
    private final String messageCode;

    public RelatedEntityExistsException(String errorCode, String messageCode, Throwable cause) {
        super(cause);
        this.messageCode = messageCode;
        this.errorCode = errorCode;
    }
}
