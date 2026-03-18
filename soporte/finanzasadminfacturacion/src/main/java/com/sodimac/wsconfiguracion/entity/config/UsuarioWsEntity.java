package com.sodimac.wsconfiguracion.entity.config;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "usuariosws")
public class UsuarioWsEntity {

    @Id
    @Column(name = "usuario", length = 100)
    private String usuario;

    @Column(name = "contrasena", length = 255)
    private String contrasena;

    @Column(name = "rol", length = 50)
    private String rol;

    @Column(name = "activo")
    private Integer activo;

    public UsuarioWsEntity() {
    }

    public UsuarioWsEntity(String usuario, String contrasena, String rol) {
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.rol = rol;
        this.activo = 1;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Integer getActivo() {
        return activo;
    }

    public void setActivo(Integer activo) {
        this.activo = activo;
    }
}