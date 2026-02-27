/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sodimac.fiscal.api.exception;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 *
 * @author ggalvan
 */
public class ExceptionWrapper implements Serializable {

    @Schema(description = "Error description", example = "Unauthorized")
    private final String message;

    @Schema(description = "Numeric HTTP Status error", example = "401")
    private final int code;

    public ExceptionWrapper(String message, int code) {
        this.message = message;
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

}

