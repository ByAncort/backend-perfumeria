package com.app.auth.Exception;

import org.hibernate.service.spi.ServiceException;

public class UserAlreadyExistsException extends ServiceException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}


