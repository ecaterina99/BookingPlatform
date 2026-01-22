package com.server.shared.infrastructure.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.server.organization.domain.enums.GlobalRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;

@Service
public class JwtService {

    private final byte[] secretKey;
    private final long expirationMs;
    private final String issuer;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs,
            @Value("${jwt.issuer}") String issuer
    ) {
        this.secretKey = secret.getBytes();
        this.expirationMs = expirationMs;
        this.issuer = issuer;
    }

    public String generateToken(String email, int userId, GlobalRole globalRole) {
        try {
            JWSSigner signer = new MACSigner(secretKey);

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(email)
                    .claim("userId", userId)
                    .claim("globalRole", globalRole.name())
                    .issuer(issuer)
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + expirationMs))
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256),
                    claimsSet
            );

            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Error generating JWT token", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(secretKey);

            if (!signedJWT.verify(verifier)) {
                return false;
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            // Check expiration
            Date expiration = claims.getExpirationTime();
            if (expiration == null || expiration.before(new Date())) {
                return false;
            }

            // Check issuer
            String tokenIssuer = claims.getIssuer();
            if (!issuer.equals(tokenIssuer)) {
                return false;
            }

            return true;
        } catch (ParseException | JOSEException e) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            throw new RuntimeException("Error parsing JWT token", e);
        }
    }

    public int getUserIdFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getIntegerClaim("userId");
        } catch (ParseException e) {
            throw new RuntimeException("Error parsing JWT token", e);
        }
    }

    public GlobalRole getGlobalRoleFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            String role = signedJWT.getJWTClaimsSet().getStringClaim("globalRole");
            return GlobalRole.valueOf(role);
        } catch (ParseException e) {
            throw new RuntimeException("Error parsing JWT token", e);
        }
    }
}
