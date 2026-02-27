/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sodimac.aclaraciones.api.repository;

import com.sodimac.aclaraciones.api.model.entity.Catalog;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author ggalvan
 */
@Repository
public interface CatalogRepository extends CrudRepository<Catalog, Integer> {

    public Catalog findByIdAndActive(int id, boolean active);

    public List<Catalog> findByTypeAndActive(int type, boolean active, Sort sort);
    
    public List<Catalog> findByTypeAndParentIdAndActive(int type, int parentId, boolean active, Sort sort);

}
