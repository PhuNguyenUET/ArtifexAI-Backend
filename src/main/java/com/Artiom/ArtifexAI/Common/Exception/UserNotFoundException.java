package com.Artiom.ArtifexAI.Common.Exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException(HttpStatus status, String message) {
        super(status, message);
    }

    public UserNotFoundException(String name) {
        super(HttpStatus.NOT_FOUND, "Cannot find user with username " + name);
    }
}
