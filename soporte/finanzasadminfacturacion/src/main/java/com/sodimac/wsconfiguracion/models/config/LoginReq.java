package com.sodimac.wsconfiguracion.models.config;

import io.swagger.annotations.ApiModelProperty;

public class LoginReq {

	@ApiModelProperty(value = "Usuario", position = 1)
    private String username;
	@ApiModelProperty(value = "Contraseña", position = 2)
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
	
}
