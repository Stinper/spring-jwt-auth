package me.stinper.commons.api.response.beanvalidation.provider.registry;

import jakarta.validation.ConstraintViolation;
import me.stinper.commons.api.response.beanvalidation.provider.ConstraintAnnotationErrorResponseProvider;
import me.stinper.commons.api.response.beanvalidation.provider.DefaultErrorResponseProvider;

import java.lang.annotation.Annotation;

/**
 * Представляет собой registry, который в зависимости от конкретного объекта {@link ConstraintViolation}
 * возвращает соответствующий <b>провайдер</b> {@link ConstraintAnnotationErrorResponseProvider},
 * который будет извлекать из него сообщение об ошибке
 * @see DefaultConstraintAnnotationErrorResponseProvidersRegistry
 */
public interface ConstraintAnnotationErrorResponseProvidersRegistry {

    /**
     * Метод, который позволяет добавлять кастомные ограничения и провайдеры для них. При использовании любых
     * кастомных аннотаций-ограничений, для них должен быть создан соответствущий <b>провайдер</b>
     * {@link ConstraintAnnotationErrorResponseProvider}, а затем они должны быть зарегистрированы с помощью этого метода
     * @param constraintAnnotationClass аннотация, которая представляет собой ограничение
     * @param provider класс-провайдер, который предоставляет сообщения об ошибке для этого ограничения
     */
    void registerProvider(Class<? extends Annotation> constraintAnnotationClass, ConstraintAnnotationErrorResponseProvider provider);

    /**
     * Возвращает соответствующий провайдер для конкретного объекта {@link ConstraintViolation}. Если провайдер
     * для этого объекта не был найден, метод должен предпринимать соответствующее действие, как описано в
     * {@link #onUnsupportedAnnotationType()}
     * @param constraintViolation объект, содержащий информацию о нарушении ограничения
     * @return провайдер для аннотации, содержащейся в объекте {@link ConstraintViolation}
     */
    ConstraintAnnotationErrorResponseProvider getResponseProviderFor(ConstraintViolation<?> constraintViolation);

    /**
     * Определяет действие, которое будет предпринято, если в метод {@link #getResponseProviderFor(ConstraintViolation)}
     * было передано ограничение {@link ConstraintViolation}, содержащее неподдерживаемую аннотацию, т.е. аннотацию,
     * для которой не был найден подходящий провайдер
     * @return действие, которое будет предпринято
     */
    default UnsupportedAnnotationPassedAction onUnsupportedAnnotationType() {
        return UnsupportedAnnotationPassedAction.CALL_DEFAULT_PROVIDER;
    }

    /**
     * Возвращает провайдер по-умолчанию, который используется, если в метод {@link #getResponseProviderFor(ConstraintViolation)}
     * было передано ограничение {@link ConstraintViolation}, содержащее неподдерживаемую аннотацию.
     * Возврат провайдера по-умолчанию - стандартное поведение,
     * но его можно изменить с помощью метода {@link #onUnsupportedAnnotationType()}
     * @return провайдер по-умолчанию
     * @see DefaultErrorResponseProvider
     * @see UnsupportedAnnotationPassedAction
     */
    default ConstraintAnnotationErrorResponseProvider getDefaultProvider() {
        return DefaultErrorResponseProvider.withUnknownConstraintViolationType();
    }

    /**
     * Действие, предпринимаемое, если была передана неподдерживаемая аннотация, для которой
     * отсутствует провайдер {@link ConstraintAnnotationErrorResponseProvider}
     */
    enum UnsupportedAnnotationPassedAction {
        /**
         * Выбросить исключение об ошибке, если провайдер не был найден
         * @see me.stinper.commons.api.response.beanvalidation.exception.NoSuchErrorResponseProvider
         */
        THROW_EXCEPTION,

        /**
         * Использовать провайдер по-умолчанию, который возвращается методом
         * {@link #getDefaultProvider()}, если провайдер не был найден
         */
        CALL_DEFAULT_PROVIDER
    }
}
