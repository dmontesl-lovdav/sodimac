/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sodimac.fiscal.api.security;

import java.util.List;

/**
 *
 * @author ggalvan
 */
public class Session {

    private final String name;
    private final String email;
    private final List<String> groups;
    //
    private boolean operator;

    public Session(String name, String email, List<String> groups) {
        this.name = name;
        this.email = email;
        this.groups = groups;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getGroups() {
        return groups;
    }

    public boolean isOperator() {
        return operator;
    }

    public void setOperator(boolean operator) {
        this.operator = operator;
    }

}

