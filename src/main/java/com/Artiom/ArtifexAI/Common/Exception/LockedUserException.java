package com.Artiom.ArtifexAI.Common.Exception;

import org.springframework.http.HttpStatus;

public class LockedUserException extends BusinessException{
    public LockedUserException(HttpStatus status, String message) {
        super(status, message);
    }

    public LockedUserException(String message) {
        super(HttpStatus.LOCKED, message);
    }
}
