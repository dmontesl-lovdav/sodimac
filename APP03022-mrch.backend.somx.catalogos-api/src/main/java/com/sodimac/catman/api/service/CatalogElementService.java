package com.sodimac.catman.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.sodimac.catman.api.model.dto.CatalogElementCreateDto;
import com.sodimac.catman.api.model.dto.CatalogElementDto;
import com.sodimac.catman.api.model.dto.CatalogElementPageResponse;
import com.sodimac.catman.api.model.dto.CatalogElementUpdateDto;
import com.sodimac.catman.api.model.dto.CatalogSimpleDto;


public interface CatalogElementService {

    /**
     *
     * @param catalogId        
     * @param elementId       
     * @param element          
     * @param value            
     * @param parentCatalogId  
     * @param parentElementId  
     * @param status           
     * @param pageable         
     * @return 
     */
    CatalogElementPageResponse findElements(
            Integer catalogId,
            Integer elementId,
            String element,
            String value,
            Integer parentCatalogId,
            Integer parentElementId,
            Integer status,
            String key,
            Pageable pageable);

    /**
     *
     * @param catalogId  
     * @param elementId   
     * @return
     */
    Optional<CatalogElementDto> findElementById(Integer catalogId, Integer elementId);

    /**
     *
     * @param catalogId  
     * @param createDto  
     * @param userId     
     * @return 
     */
    CatalogElementDto createElement(Integer catalogId, CatalogElementCreateDto createDto, String userId);

    /**
     *
     * @param catalogId  
     * @param elementId 
     * @param updateDto  
     * @param userId      
     * @return 
     */
    CatalogElementDto updateElement(Integer catalogId, Integer elementId, CatalogElementUpdateDto updateDto, String userId);

    /**
     *
     * @param elementId 
     * @param newStatus   
     * @param userId     
     * @return 
     */
    CatalogElementDto changeStatus(Integer elementId, Integer newStatus, String userId);

    /**
     *
     * @return 
     */
    List<CatalogSimpleDto> findPrimaryCatalogs();

    /**
     *
     * @param catalogId   
     * @return 
     */
    List<CatalogElementDto> findActiveElements(Integer catalogId);

    /**
     *
     * @param catalogId   
     * @return
     */
    Optional<CatalogSimpleDto> findCatalogById(Integer catalogId);
}







