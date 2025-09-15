package com.flux.movieproject.utils;

import java.text.ParseException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Component
public class MemberJwtUtil {
    // 注入會員JWT簽名密鑰
    @Value("${jwt.member.secret}")
    private String secret;

    // 專屬於會員的 Token 有效時間
    @Value("${jwt.member.expiration}")
    private long expiration;

    // 生成 Token 的方法
    public String generateToken(String username) throws JOSEException {
        // 0. 確保密鑰長度足夠
        byte[] secretKeyBytes = secret.getBytes();
        if (secretKeyBytes.length < 32) {
            throw new JOSEException("JWT member secret key must be at least 32 bytes long.");
        }

        // 1. 建立 JWT 的聲明 (Claims)
        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder();
        claimsBuilder.subject(username); // 設定 Token 的主體 (通常是使用者帳號)
        claimsBuilder.issueTime(new Date()); // 設定發行時間
        claimsBuilder.expirationTime(new Date(System.currentTimeMillis() + expiration)); // 設定過期時間

        // 這裡加入 member 角色資訊
        claimsBuilder.claim("role", "member");

        JWTClaimsSet claimsSet = claimsBuilder.build();

        // 2. 建立 JWT 簽名物件
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        SignedJWT signedJWT = new SignedJWT(header, claimsSet);

        // 3. 執行簽名
        JWSSigner signer = new MACSigner(secret.getBytes());
        signedJWT.sign(signer);

        // 4. 將 JWT 物件序列化為字串
        return signedJWT.serialize();
    }

    // 解析 Token 的方法
    public JWTClaimsSet getClaimsFromToken(String token) throws JOSEException, ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);

        // 驗證簽名
        JWSVerifier verifier = new MACVerifier(secret.getBytes());
        if (!signedJWT.verify(verifier)) {
            throw new JOSEException("JWT Token 簽名驗證失敗。");
        }

        return signedJWT.getJWTClaimsSet();
    }

    public String generateToken(String email, Integer memberId) throws JOSEException {
        // 0. 確保密鑰長度足夠
        byte[] secretKeyBytes = secret.getBytes();
        if (secretKeyBytes.length < 32) {
            throw new JOSEException("JWT member secret key must be at least 32 bytes long.");
        }

        // 1. 建立 JWT 的聲明 (Claims)
        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder();
        claimsBuilder.subject(email);
        claimsBuilder.issueTime(new Date());
        claimsBuilder.expirationTime(new Date(System.currentTimeMillis() + expiration));
        claimsBuilder.claim("role", "member");

        // 加入 memberId
        if (memberId != null) {
            claimsBuilder.claim("memberId", memberId);
        }

        JWTClaimsSet claimsSet = claimsBuilder.build();

        // 2. 建立 JWT 簽名物件
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        SignedJWT signedJWT = new SignedJWT(header, claimsSet);

        // 3. 執行簽名
        JWSSigner signer = new MACSigner(secret.getBytes());
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }


}