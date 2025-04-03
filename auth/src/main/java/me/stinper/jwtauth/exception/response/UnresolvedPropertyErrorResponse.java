package me.stinper.jwtauth.exception.response;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import me.stinper.commons.api.response.Problem;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class UnresolvedPropertyErrorResponse extends Problem {
    private final String property;
    private final String hint;

    public UnresolvedPropertyErrorResponse(String type, String localizedMessage, String property, String hint) {
        super(type, localizedMessage);
        this.property = property;
        this.hint = hint;
    }
}
