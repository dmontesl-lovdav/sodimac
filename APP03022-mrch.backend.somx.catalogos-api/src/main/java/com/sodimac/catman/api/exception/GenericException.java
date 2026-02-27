/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sodimac.catman.api.exception;

/**
 *
 * @author ggalvan
 */
public class GenericException extends RuntimeException {

    private static final long serialVersionUID = -7051709488615576604L;
    private final int code;

    public GenericException(String description, int code) {
        super(description);
        this.code = code;
    }

    public GenericException(int code, String description) {
        super(description);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
