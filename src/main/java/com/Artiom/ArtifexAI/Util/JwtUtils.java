package com.Artiom.ArtifexAI.Util;

import com.Artiom.ArtifexAI.Authentication.Filter.JwtConstant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@UtilityClass
public class JwtUtils {
    public static String extractJwtUsername(String token) {
        return extractJwtClaim(token, Claims::getSubject);
    }

    static Date extractJwtExpiration(String token) {
        return extractJwtClaim(token, Claims::getExpiration);
    }

    private static <T> T extractJwtClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllJwtClaims(token);
        return claimsResolver.apply(claims);
    }

    private static Claims extractAllJwtClaims(String token) {
        return Jwts.parser().setSigningKey(JwtConstant.JWT_SECRET_USER).parseClaimsJws(token).getBody();
    }

    private static Boolean isJwtTokenExpired(String token) {
        return extractJwtExpiration(token).before(new Date());
    }

    public static String generateJwtToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return JwtConstant.JWT_TOKEN_PREFIX + createJwtToken(claims, username);
    }

    private static String createJwtToken(Map<String, Object> claims, String subject) {
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JwtConstant.JWT_EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, JwtConstant.JWT_SECRET_USER).compact();
    }

    public static Boolean validateJwtToken(String token, UserDetails userDetails) {
        final String username = extractJwtUsername(token);
        return (username.equals(userDetails.getUsername()) && !isJwtTokenExpired(token));
    }

    public static String extractRefreshUsername(String token) {
        return extractRefreshClaim(token, Claims::getSubject);
    }

    static Date extractRefreshExpiration(String token) {
        return extractRefreshClaim(token, Claims::getExpiration);
    }

    static <T> T extractRefreshClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllRefreshClaims(token);
        return claimsResolver.apply(claims);
    }

    private static Claims extractAllRefreshClaims(String token) {
        return Jwts.parser().setSigningKey(JwtConstant.REFRESH_SECRET_USER).parseClaimsJws(token).getBody();
    }

    private static Boolean isRefreshTokenExpired(String token) {
        return extractRefreshExpiration(token).before(new Date());
    }

    public static String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return JwtConstant.JWT_TOKEN_PREFIX + createRefreshToken(claims, username);
    }

    private static String createRefreshToken(Map<String, Object> claims, String subject) {
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JwtConstant.REFRESH_EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, JwtConstant.REFRESH_SECRET_USER).compact();
    }

    public static Boolean validateRefreshToken(String token, UserDetails userDetails) {
        final String username = extractRefreshUsername(token);
        return (username.equals(userDetails.getUsername()) && !isRefreshTokenExpired(token));
    }
}
