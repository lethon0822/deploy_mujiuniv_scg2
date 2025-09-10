package com.green.scg2.configuration.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.green.scg2.configuration.constants.ConstJwt;
import com.green.scg2.configuration.model.JwtUser;
import com.green.scg2.configuration.model.UserPrincipal;
import com.green.scg2.configuration.utils.MyCookieUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Slf4j
@Component
public class JwtTokenProvider {
    private final ObjectMapper objectMapper;
    private final ConstJwt constJwt;
    private final SecretKey secretKey;
    private final MyCookieUtils myCookieUtils;

    public JwtTokenProvider(ObjectMapper objectMapper, ConstJwt constJwt, MyCookieUtils myCookieUtils) {
        this.objectMapper = objectMapper;
        this.constJwt = constJwt;
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(constJwt.getSecretKey())); //43자 이상
        this.myCookieUtils = myCookieUtils;
    }

    private String getAccessToken(ServerHttpRequest request) {
        return myCookieUtils.getValue(request, constJwt.getAccessTokenCookieName());
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public JwtUser getJwtUserFromToken(String token) {
        Claims claims = getClaims(token);
        String json = claims.get(constJwt.getClaimKey(), String.class);
        try {
            return objectMapper.readValue(json, JwtUser.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Authentication getAuthentication(ServerHttpRequest request) {
        String token = getAccessToken(request);
        log.info("token: {}", token);
        if(token == null) { return null; }

        JwtUser jwtUser = getJwtUserFromToken(token);
        UserPrincipal userPrincipal = new UserPrincipal(jwtUser.getSignedUserId(), jwtUser.getRole()); //시큐리티 - 인증/인가 처리를 위해
        return new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
    }
}
