package com.sodimac.aclaraciones.api.service.cases.command.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import com.sodimac.aclaraciones.api.model.dto.CatalogDto;
import com.sodimac.aclaraciones.api.model.entity.Catalog;
import com.sodimac.aclaraciones.api.repository.CatalogRepository;
import com.sodimac.aclaraciones.api.service.cases.CatalogService;

/**
 * JPA-based implementation of {@link CatalogService}.
 */
@Component
public class CatalogServiceImpl implements CatalogService {

    private final CatalogRepository catalogs;

    public CatalogServiceImpl(CatalogRepository catalogs) {
        this.catalogs = catalogs;
    }

    /*
     * ----------------------------------------------------------------
     * CATALOG LIST
     * ----------------------------------------------------------------
     */
    @Override
    public List<CatalogDto> retrieveList(int type, int parentId) {

        List<Catalog> persistence = parentId != 0
                ? catalogs.findByTypeAndParentIdAndActive(type, parentId, true, Sort.by(Direction.ASC, "description"))
                : catalogs.findByTypeAndActive(type, true, Sort.by(Direction.ASC, "description"));

        List<CatalogDto> result = new ArrayList<>();
        if (persistence != null) {
            persistence.forEach(c -> result.add(transform(c)));
        }
        return result;
    }

    private CatalogDto transform(Catalog catalog) {
        if (catalog == null) {
            return null;
        }
        CatalogDto dto = new CatalogDto();
        dto.setId(catalog.getId());
        dto.setType(catalog.getType());
        dto.setDescription(catalog.getDescription());
        dto.setArea(catalog.getArea());
        return dto;
    }

    /*
     * ----------------------------------------------------------------
     * NEW METHOD: status validation
     * ----------------------------------------------------------------
     */
    @Override
    public void requireStatus(Integer statusId) {
        if (statusId == null) {
            throw new IllegalStateException("ERR_STATUS_NULL");
        }

        boolean exists = catalogs.existsById(statusId);
        if (!exists) {
            throw new IllegalStateException("ERR_STATUS_NOT_FOUND");
        }
    }
}
