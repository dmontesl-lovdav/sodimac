package com.sodimac.wsconfiguracion.controller;

import com.sodimac.wsconfiguracion.exception.ResourceNotFoundException;

public class RestPreconditions {
    public static <T> T checkFound(T resource) {
        if (resource == null) {
            throw new ResourceNotFoundException();
        }
        return resource;
    }
}
