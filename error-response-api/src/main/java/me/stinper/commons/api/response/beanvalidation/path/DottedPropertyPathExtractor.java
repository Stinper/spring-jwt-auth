package me.stinper.commons.api.response.beanvalidation.path;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;

/**
 * <p> Реализация по-умолчанию для интерфейса {@link PropertyPathExtractor} </p>
 * Извлекает путь свойства в dotted-формате, например: my.nested.property. Если свойство одиночное, извлекает только
 * его имя, например: property. Если свойство извлечь не удалось, возвращает пустую строку
 */
public class DottedPropertyPathExtractor implements PropertyPathExtractor {
    @Override
    public String extractFrom(ConstraintViolation<?> constraintViolation) {
        Path pathObject = constraintViolation.getPropertyPath();

        StringBuilder propertyPath = new StringBuilder();

        for (Path.Node node : pathObject) {
            if (node.getKind().equals(ElementKind.PROPERTY)) {
                if (propertyPath.isEmpty())
                    propertyPath.append(node);
                else
                    propertyPath.append(".").append(node);
            }
        }

        return propertyPath.toString();
    }
}
