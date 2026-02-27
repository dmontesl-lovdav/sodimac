/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sodimac.aclaraciones.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sodimac.aclaraciones.api.exception.ExceptionWrapper;
import com.sodimac.aclaraciones.api.exception.GenericException;
import com.sodimac.aclaraciones.api.model.dto.SlaDto;
import com.sodimac.aclaraciones.api.security.RequireRole;
import com.sodimac.aclaraciones.api.security.Session;
import com.sodimac.aclaraciones.api.service.sla.SlaService;

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
@RequestMapping("/slas")
public class SlaController {

        private final SlaService service;

        @Autowired
        public SlaController(SlaService service) {
                this.service = service;
        }

        @Operation(operationId = "postSla", summary = "Creates a new Sla.", responses = {
                        @ApiResponse(responseCode = "200", description = "Given sla has been successfully created."),
                        @ApiResponse(responseCode = "401", description = "The request requires user authentication.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "404", description = "The server has not found anything matching the requested URI.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "500", description = "Internal error.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
        @ResponseBody
        @PostMapping
        @RequireRole
        public int post(@RequestBody SlaDto sla, @RequestAttribute(name = "session") Session session)
                        throws GenericException {
                return this.service.save(sla, session);
        }

        @Operation(operationId = "publishSla", summary = "De/publish a Sla.", responses = {
                        @ApiResponse(responseCode = "401", description = "The request requires user authentication.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "404", description = "The server has not found anything matching the requested URI.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "500", description = "Internal error.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
        @PostMapping("/{id}/publish")
        @RequireRole
        public void configure(
                        @Parameter(description = "Sla `id`.", example = "112233") @PathVariable int id,
                        @Parameter(description = "Publishing status", example = "true") @RequestParam(defaultValue = "true") Boolean publish,
                        @RequestAttribute(name = "session") Session session) throws GenericException {
                this.service.publish(id, publish);
        }

        @Operation(operationId = "getSla", summary = "Retrieves a Sla based on its id.", responses = {
                        @ApiResponse(responseCode = "200", description = "Sla objects with given `id`.", content = @Content(schema = @Schema(implementation = SlaDto.class))),
                        @ApiResponse(responseCode = "401", description = "The request requires user authentication.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "404", description = "The server has not found anything matching the requested URI.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "500", description = "Internal error.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
        @ResponseBody
        @GetMapping("/{id}")
        @RequireRole
        public SlaDto get(
                        @Parameter(description = "Sla `id`.", example = "112233") @PathVariable int id,
                        @RequestAttribute(name = "session") Session session) {
                return this.service.retrieve(id, session);
        }

        @Operation(operationId = "getSlas", summary = "Retrieves a list of Sla based for the current user session.", responses = {
                        @ApiResponse(responseCode = "200", description = "Sla objects that meets given criteria.", content = @Content(schema = @Schema(implementation = SlaDto[].class))),
                        @ApiResponse(responseCode = "401", description = "The request requires user authentication.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "404", description = "The server has not found anything matching the requested URI.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "500", description = "Internal error.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
        @ResponseBody
        @GetMapping
        @RequireRole
        public List<SlaDto> get(@RequestAttribute(name = "session") Session session) {
                return this.service.retrieve();
        }

        @Operation(operationId = "putSla", summary = "Updates a Sla", responses = {
                        @ApiResponse(responseCode = "200", description = "Given sla has been successfully updated."),
                        @ApiResponse(responseCode = "401", description = "The request requires user authentication.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "404", description = "The server has not found anything matching the requested URI.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "500", description = "Internal error.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
        @ResponseBody
        @PutMapping("/{id}")
        @RequireRole
        public int put(@Parameter(description = "Sla `id`.", example = "112233") @PathVariable Integer id,
                        @RequestBody SlaDto sla,
                        @RequestAttribute(name = "session") Session session)
                        throws GenericException {
                return this.service.update(id, sla, session);
        }

        @Operation(operationId = "deleteSla", summary = "Deletes a Sla", responses = {
                        @ApiResponse(responseCode = "200", description = "Given sla has been successfully deleted."),
                        @ApiResponse(responseCode = "401", description = "The request requires user authentication.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "404", description = "The server has not found anything matching the requested URI.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "500", description = "Internal error.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
        @ResponseBody
        @DeleteMapping("/{id}")
        @RequireRole
        public int delete(
                        @Parameter(description = "Sla `id`.", example = "112233") @PathVariable Integer id,
                        @RequestAttribute(name = "session") Session session)
                        throws GenericException {
                return this.service.delete(id);
        }

        @Operation(operationId = "getSlaResponseTimeByModule", summary = "Retrieves SLA first response time description by module.", responses = {
                        @ApiResponse(responseCode = "200", description = "SLA response time description."),
                        @ApiResponse(responseCode = "401", description = "The request requires user authentication.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "404", description = "SLA not found for given module.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "500", description = "Internal error.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
        @ResponseBody
        @GetMapping("/module/{moduleId}/response-time")
        @RequireRole
        public String getResponseTimeByModule(
                        @Parameter(description = "Module id", example = "61") @PathVariable int moduleId,
                        @RequestAttribute(name = "session") Session session) throws GenericException {
                return this.service.getResponseTimeByModule(moduleId);
        }

}
