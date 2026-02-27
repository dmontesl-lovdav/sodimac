/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sodimac.aclaraciones.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sodimac.aclaraciones.api.exception.ExceptionWrapper;
import com.sodimac.aclaraciones.api.model.dto.CatalogDto;
import com.sodimac.aclaraciones.api.security.Session;
import com.sodimac.aclaraciones.api.service.cases.CatalogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 *
 * @author ggalvan
 */
@RestController
@RequestMapping("/catalogs/{type}")
public class CatalogController {

    private final CatalogService service;

    @Autowired
    public CatalogController(CatalogService service) {
        this.service = service;
    }

    @Operation(
            operationId = "getCatalog",
            summary = "Retrieves all Catalog objects by type and by optional parent.",
            responses = {
                @ApiResponse(responseCode = "200", description = "Catalog objects that meet given criteria.", content = @Content(schema = @Schema(implementation = CatalogDto[].class))),
                @ApiResponse(responseCode = "401", description = "The request requires user authentication.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                @ApiResponse(responseCode = "404", description = "The server has not found anything matching the Request-URI.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                @ApiResponse(responseCode = "500", description = "Internal error.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
            }
    )
    @GetMapping
    @ResponseBody
    public List<CatalogDto> get(@Parameter(description = "Catalog type.") @PathVariable int type, @Parameter(description = "Catalog parent id value. Optional.") @RequestParam(required = false, defaultValue = "0") int parentId, @RequestAttribute Session session) {
        return this.service.retrieveList(type, parentId);
    }
}
