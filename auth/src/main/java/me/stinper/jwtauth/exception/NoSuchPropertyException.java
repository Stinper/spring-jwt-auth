package me.stinper.jwtauth.exception;

import lombok.Getter;
import org.springframework.beans.PropertyMatches;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.util.Lazy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Getter
public class NoSuchPropertyException extends RuntimeException {
    private final PropertyReferenceException propertyReferenceException;
    private final Lazy<Set<String>> propertyMatches;

    public NoSuchPropertyException(PropertyReferenceException propertyReferenceException) {
        this.propertyReferenceException = propertyReferenceException;
        this.propertyMatches = Lazy.of(
                () -> detectPotentialMatches(
                        propertyReferenceException.getPropertyName(),
                        propertyReferenceException.getType().getType()
                )
        );
    }

    public Set<String> getPropertyMatches() {
        return this.propertyMatches.get();
    }

    private static Set<String> detectPotentialMatches(String propertyName, Class<?> type) {

        Set<String> result = new HashSet<>();
        result.addAll(Arrays.asList(PropertyMatches.forField(propertyName, type).getPossibleMatches()));
        result.addAll(Arrays.asList(PropertyMatches.forProperty(propertyName, type).getPossibleMatches()));

        return result;
    }
}
