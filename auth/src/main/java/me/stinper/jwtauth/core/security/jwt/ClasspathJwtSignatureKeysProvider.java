package me.stinper.jwtauth.core.security.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Component
public class ClasspathJwtSignatureKeysProvider implements JwtSignatureKeysProvider {
    @Value("${app.auth.security.jwt.public-key}")
    private RSAPublicKey publicKey;

    @Value("${app.auth.security.jwt.private-key}")
    private RSAPrivateKey privateKey;

    @Override
    public PublicKey publicKey() {
        return this.publicKey;
    }

    @Override
    public PrivateKey privateKey() {
        return this.privateKey;
    }
}
