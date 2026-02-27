/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.sodimac.aclaraciones.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 *
 * @author ggalvan
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.sodimac.aclaraciones.api")
public class AclaracionesApi {

    public static void main(String[] args) {
        SpringApplication.run(AclaracionesApi.class, args);
    }
}
