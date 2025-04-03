package me.stinper.jwtauth.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

public record JwksDto(
        List<KeyBody> keys
) {

    @Data
    @Builder
    public static class KeyBody {
        private final String kty;
        private final String alg;
        private final String n;
        private final String e;

        public static KeyBodyBuilder RSA_KEY = KeyBody.builder()
                .alg("RS256")
                .kty("RSA");
    }

}
