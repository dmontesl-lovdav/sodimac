package com.sodimac.aclaraciones.api.service.command;

import org.springframework.web.multipart.MultipartFile;

import com.sodimac.aclaraciones.api.exception.GenericException;
import com.sodimac.aclaraciones.api.model.dto.FaqCategoryDto;
import com.sodimac.aclaraciones.api.model.dto.FaqCategoryResponse;

/** Command-side operations for FAQ Categories. */
public interface FaqCategoryCommandService {

    /** Publish / unpublish and return resulting state. */
    FaqCategoryResponse updatePublication(Long categoryId, Boolean published);

    /** Create / update with JSON body. */
    FaqCategoryResponse createPublication(FaqCategoryDto target) throws GenericException;

    FaqCategoryResponse updatePublication(Long categoryId, FaqCategoryDto target) throws GenericException;

    /** Create / update with multipart (JSON + icon file). */
    FaqCategoryResponse createPublication(FaqCategoryDto target, MultipartFile iconFile) throws GenericException;

    FaqCategoryResponse updatePublication(Long categoryId, FaqCategoryDto target, MultipartFile iconFile)
            throws GenericException;

    /** Hard delete. */
    void deleteCategory(Long categoryId);
}
