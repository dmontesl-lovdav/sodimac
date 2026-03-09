package com.sodimac.cfdi.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para verificar la configuración de perfiles activos
 *
 * @author Sistema CFDI
 */
@RestController
@RequestMapping("/api")
public class EnvironmentInfoController {

    @Autowired
    private Environment env;

    /**
     * Endpoint para verificar qué perfil de Spring está activo
     *
     * @return JSON con información del perfil activo
     */
    @GetMapping("/perfil-activo")
    public Map<String, Object> getActiveProfile() {
        Map<String, Object> response = new HashMap<>();

        String[] activeProfiles = env.getActiveProfiles();
        String[] defaultProfiles = env.getDefaultProfiles();

        response.put("perfiles_activos", activeProfiles.length > 0 ?
                Arrays.toString(activeProfiles) : "Ninguno");
        response.put("perfiles_por_defecto", Arrays.toString(defaultProfiles));
        response.put("perfil_usado", activeProfiles.length > 0 ?
                activeProfiles[0] : defaultProfiles[0]);

        // Información adicional útil
        response.put("ambiente", activeProfiles.length > 0 ? activeProfiles[0] : "default");
        response.put("timestamp", System.currentTimeMillis());

        return response;
    }
}
