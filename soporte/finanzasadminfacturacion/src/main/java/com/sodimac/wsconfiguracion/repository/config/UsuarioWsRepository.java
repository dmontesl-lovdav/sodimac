package com.sodimac.wsconfiguracion.repository.config;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.wsconfiguracion.entity.config.UsuarioWsEntity;

@Repository
public interface UsuarioWsRepository extends JpaRepository<UsuarioWsEntity, String> {

    List<UsuarioWsEntity> findByActivo(Integer activo);
}