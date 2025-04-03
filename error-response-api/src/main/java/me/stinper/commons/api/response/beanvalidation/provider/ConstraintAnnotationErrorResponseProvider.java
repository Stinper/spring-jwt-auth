package me.stinper.commons.api.response.beanvalidation.provider;

import jakarta.validation.ConstraintViolation;
import me.stinper.commons.api.response.beanvalidation.ConstraintViolationProblemDetails;
import me.stinper.commons.api.response.beanvalidation.exception.UnsupportedConstraintAnnotationType;
import me.stinper.commons.api.response.beanvalidation.path.PropertyPathExtractor;
import me.stinper.commons.api.response.beanvalidation.provider.registry.ConstraintAnnotationErrorResponseProvidersRegistry;

import java.lang.annotation.Annotation;

/**
 * Служит для построения сообщения об ошибке, основывающееся на конкретном объекте {@link ConstraintViolation}.
 * Имеется реализация по-умолчанию для всех стандартных аннотаций из пакета Jakarta Validation API. <br>
 * Кастомные аннотации-ограничения могут быть зарегистрированы в <b>фабрике</b>, как описано в
 * {@link ConstraintAnnotationErrorResponseProvidersRegistry}
 */
public interface ConstraintAnnotationErrorResponseProvider {

    /**
     * Метод, сигнализирующий о том, поддерживает ли провайдер заданный тип аннотации. <br>
     * Контракт: <br>
     * Если метод вернул true, это значит, что он может обработать переданный тип аннотации без каких-либо ошибок <br>
     * Если метод вернул false, при вызове {@link #buildErrorResponseDetails(ConstraintViolation, PropertyPathExtractor)}
     * должно быть выброшено исключение {@link UnsupportedConstraintAnnotationType} <br>
     * @param annotationClass тип аннотации
     * @return true - провайдер поддерживает аннотацию, false - провайдер НЕ поддерживает аннотацию
     */
    boolean supports(Class<? extends Annotation> annotationClass);

    /**
     * Метод для построения сообщения об ошибке, основанного на объекте нарушения ограничения {@link ConstraintViolation}
     * @param constraintViolation объект, содержащий сведения о нарушении ограничения
     * @param propertyPathExtractor объект для извлечения пути свойства, для которого было нарушено ограничения
     * @return полностью сконструированное сообщение об ошибке
     */
    ConstraintViolationProblemDetails buildErrorResponseDetails(
            ConstraintViolation<?> constraintViolation,
            PropertyPathExtractor propertyPathExtractor
    );

    /**
     * Извлекает из объекта {@link ConstraintViolation} аннотацию-ограничение, для которой сразу же вызывается метод
     * {@link #supports(Class)}
     * @param constraintViolation объект, содержащий сведения о нарушении ограничения
     * @return аннотация, содержащаяся в объекте
     * @throws UnsupportedConstraintAnnotationType - если тип аннотации, содержащейся в ограничении, не поддерживается
     */
    default Annotation extractIfSupportsOrThrow(ConstraintViolation<?> constraintViolation) throws UnsupportedConstraintAnnotationType {
        Annotation constraintAnnotation = constraintViolation.getConstraintDescriptor().getAnnotation();

        if (!this.supports(constraintAnnotation.annotationType()))
            throw new UnsupportedConstraintAnnotationType(
                    "Annotation with type"
                            + constraintAnnotation.annotationType().getName() +
                            " is not supported, because supports() returned false"
            );

        return constraintAnnotation;
    }

    /**
     * Эквивалентен методу {@link #extractIfSupportsOrThrow(ConstraintViolation)}, но тип аннотации автоматически приводится
     * к указанному типу
     * @param constraintViolation объект, содержащий сведения о нарушении ограничения
     * @param annotationClass тип, к которму будет приведена аннотация, извлеченная из constraintViolation
     * @return Аннотация, приведенная к указанному типу
     */
    @SuppressWarnings("unchecked")
    default <T extends Annotation> T extractAsIfSupportsOrThrow(ConstraintViolation<?> constraintViolation, Class<T> annotationClass) {
        return (T) this.extractIfSupportsOrThrow(constraintViolation);
    }
}
