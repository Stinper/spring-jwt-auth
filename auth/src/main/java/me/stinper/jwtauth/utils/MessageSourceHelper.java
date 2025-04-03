package me.stinper.jwtauth.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Класс-обертка для объекта типа {@link MessageSource}, который позволяет писать меньше кода
 * при получении сообщений
 */
@RequiredArgsConstructor
@Slf4j
public class MessageSourceHelper {
    private final MessageSource messageSource;
    private final MessageNotFoundAction messageNotFoundAction;

    /**
     * Возвращает сообщение по заданному коду с заданными аргументами, исходя из текущей локали
     * (из LocaleContextHolder)
     * @see LocaleContextHolder
     * @param code код сообщения
     * @param args аргументы, которые передаются в сообщение
     * @return сообщение из конфигурационного файла
     * @throws NoSuchMessageException если сообщение по заданному коду не найдено
     * и {@link MessageNotFoundAction} установлен в значение {@link MessageNotFoundAction#THROW_EXCEPTION}
     */
    public String getLocalizedMessage(@NonNull String code, @Nullable Object... args) throws NoSuchMessageException {
        try {
            return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
        }
        catch (NoSuchMessageException e) {
            log.atError().log("Сообщение с кодом '{}' не найдено, заданная стратегия действий - {}", code, this.messageNotFoundAction.toString());

            if (this.messageNotFoundAction == MessageNotFoundAction.THROW_EXCEPTION)
                throw new NoSuchMessageException(code, LocaleContextHolder.getLocale());

            return code;
        }
    }

    /**
     * Эквивалентен методу {@link #getLocalizedMessage(String, Object...)}, только без аргументов для сообщения
     * @param code код сообщения
     * @return сообщение из конфигурационного файла
     * @throws NoSuchMessageException если сообщение по заданному коду не найдено
     * и {@link MessageNotFoundAction} установлен в значение {@link MessageNotFoundAction#THROW_EXCEPTION}
     */
    public String getLocalizedMessage(@NonNull String code) throws NoSuchMessageException {
        return this.getLocalizedMessage(code, new Object[]{});
    }

    /**
     * Определяет действие, предпринимаемое, если сообщение с указанным кодом не было найдено (то есть, если
     * {@link MessageSource} выбросил исключение {@link NoSuchMessageException})
     */
    public enum MessageNotFoundAction {
        /**
         * Выбросить исключение {@link NoSuchMessageException}
         */
        THROW_EXCEPTION,

        /**
         * Вернуть в качестве сообщения его код
         */
        RETURN_MESSAGE_CODE
    }
}
