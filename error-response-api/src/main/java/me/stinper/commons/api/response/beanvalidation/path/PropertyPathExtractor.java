package me.stinper.commons.api.response.beanvalidation.path;

import jakarta.validation.ConstraintViolation;

/**
 * Используется для извлечения пути свойства из объекта {@link ConstraintViolation}
 * @see DottedPropertyPathExtractor
 */
public interface PropertyPathExtractor {

    /**
     * Извлекает путь свойства, содержащегося в объекте нарушения ограничения {@link ConstraintViolation}
     * @param constraintViolation - объект, содержащий полную информацию о нарушении ограничения
     * @return путь свойства в произвольном формате
     */
    String extractFrom(ConstraintViolation<?> constraintViolation);

}
