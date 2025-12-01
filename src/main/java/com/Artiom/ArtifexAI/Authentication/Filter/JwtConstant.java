package com.Artiom.ArtifexAI.Authentication.Filter;

public class JwtConstant {
    public static final String JWT_SECRET_USER = "f025eaa5854dc9950f14ffd8a2322db0457bf895f28b6b2a787996edeb41ba68513f93a858fb8463a5f7d467e569de57bb817014cb0507d4036e43e77dbf9f74";
    public static final String REFRESH_SECRET_USER = "8c66239e5ed02f4deadd30a6f3150025d2368dd964aaa63367c72165a2135da5a98f8abc909caaed821a9a1bacfb8d3661c2a070336ebd59515dd60de92cf3a4";
    public static final String JWT_TOKEN_PREFIX = "Bearer ";
    public static final String JWT_HEADER = "Authorization";
    public static final long JWT_EXPIRATION = 24 * 60 * 60 * 1000;
    public static final long REFRESH_EXPIRATION = 14 * 24 * 60 * 60 * 1000;
}
