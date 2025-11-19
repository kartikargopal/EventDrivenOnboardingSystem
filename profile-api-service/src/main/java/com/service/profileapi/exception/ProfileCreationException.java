package com.service.profileapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

// Use this for any generic failure during profile creation or update
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ProfileCreationException extends RuntimeException {
    public ProfileCreationException(String message, DynamoDbException e) {
        super(message);
    }
}