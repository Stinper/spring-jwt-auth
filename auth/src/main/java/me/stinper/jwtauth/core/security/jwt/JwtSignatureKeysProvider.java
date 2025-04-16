package me.stinper.jwtauth.core.security.jwt;

import java.security.PrivateKey;
import java.security.PublicKey;

public interface JwtSignatureKeysProvider {
    PublicKey publicKey();

    PrivateKey privateKey();
}
