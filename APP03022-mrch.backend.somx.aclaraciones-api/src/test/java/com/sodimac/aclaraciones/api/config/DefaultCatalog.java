/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sodimac.aclaraciones.api.config;

import com.sodimac.aclaraciones.api.model.entity.Catalog;
import com.sodimac.aclaraciones.api.repository.CatalogRepository;
import jakarta.annotation.PostConstruct;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author ggalvan
 */
@Component
public class DefaultCatalog {

    public static final int DEFAULT_REASON = 1;

    @Autowired
    private CatalogRepository catalogs;

    @PostConstruct
    public void buildDefaultReason() {
        Catalog reason = new Catalog();

        // reason.setId(DEFAULT_REASON);
        reason.setActive(true);
        reason.setCreationTime(new Date());
        reason.setDescription("REASON TEST");
        reason.setStatus(Catalog.STATUS_DEFAULT);
        reason.setType(Catalog.TYPE_DEFAULT);

        this.catalogs.save(reason);
    }

}
