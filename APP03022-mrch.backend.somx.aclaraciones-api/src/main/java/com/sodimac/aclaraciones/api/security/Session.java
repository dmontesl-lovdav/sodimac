package com.sodimac.aclaraciones.api.security;

import java.util.List;

public class Session {

    private final String name;
    private final String email;
    private final List<String> groups;
    private boolean operator; // true=operador, false=proveedor

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
