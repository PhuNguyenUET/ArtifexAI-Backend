package com.Artiom.ArtifexAI.Common.Exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class BusinessException extends RuntimeException {
    private String message;
    private HttpStatus status;
    private String detailedMessage;
    private Object results;

    public BusinessException(HttpStatus status, String message) {
        this.message = message;
        this.status = status;
    }
}
