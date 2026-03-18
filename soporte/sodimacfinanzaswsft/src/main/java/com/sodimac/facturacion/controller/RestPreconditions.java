package com.sodimac.facturacion.controller;

import com.sodimac.facturacion.exception.ResourceNotFoundException;

public class RestPreconditions {
    public static <T> T checkFound(T resource) {
        if (resource == null) {
            throw new ResourceNotFoundException();
        }
        return resource;
    }
}
