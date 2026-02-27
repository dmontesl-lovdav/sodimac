/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.sodimac.catman.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 *
 * @author ggalvan
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.sodimac.catman.api")
public class CatManApi {

    public static void main(String[] args) {
        SpringApplication.run(CatManApi.class, args);
    }
}
