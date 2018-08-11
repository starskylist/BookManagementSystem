package com.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class PageNotFound extends RuntimeException {
    public PageNotFound(String exception) {
        super(exception);
    }

}


