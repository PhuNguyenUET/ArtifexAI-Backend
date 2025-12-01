package com.Artiom.ArtifexAI.Common.Api;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
public class ApiResponse {
    private int code;
    private String message;
    private Object results;

    public ApiResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ApiResponse success(String message) {
        return new ApiResponse(HttpServletResponse.SC_OK, message);
    }

    public static ApiResponse success(String message, Object results) {
        return new ApiResponse(HttpServletResponse.SC_OK, message, results);
    }

    public ApiResponse(HttpStatus httpStatus, String message, Object results) {
        this.code = httpStatus.value();
        this.message = message;
        this.results = results;
    }
}
