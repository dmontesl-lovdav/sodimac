/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sodimac.aclaraciones.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sodimac.aclaraciones.api.exception.ExceptionWrapper;
import com.sodimac.aclaraciones.api.exception.GenericException;
import com.sodimac.aclaraciones.api.model.dto.AttachmentDto;
import com.sodimac.aclaraciones.api.security.RequireRole;
import com.sodimac.aclaraciones.api.security.Session;
import com.sodimac.aclaraciones.api.service.cases.AttachmentService;

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
@RequestMapping("/requests/{requestId}/attachments")
public class AttachmentController {

    private final AttachmentService service;

    @Autowired
    public AttachmentController(AttachmentService service) {
        this.service = service;
    }

    @Operation(operationId = "postAttachment", summary = "Creates a new Attachment onto related Request (Aclaración).", responses = {
            @ApiResponse(responseCode = "200", description = "Given Attachment has been successfully created."),
            @ApiResponse(responseCode = "401", description = "The request requires user authentication.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "404", description = "The server has not found anything matching the Request-URI.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Internal error.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
    })
    @PostMapping
    @ResponseBody
    @RequireRole
    public int post(@RequestBody AttachmentDto attachment,
            @Parameter(description = "Related Request (Aclaración) `id`.") @PathVariable int requestId,
            @RequestAttribute Session session) throws GenericException {
        return this.service.save(attachment, requestId, session);
    }

    @Operation(operationId = "getAttachments", summary = "Retrieves all Attachment objects of given Request (Aclaración) with no content.", responses = {
            @ApiResponse(responseCode = "200", description = "Attachment objects for given `requestId`.", content = @Content(schema = @Schema(implementation = AttachmentDto[].class))),
            @ApiResponse(responseCode = "401", description = "The request requires user authentication.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "404", description = "The server has not found anything matching the Request-URI.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Internal error.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
    })
    @GetMapping
    @ResponseBody
    @RequireRole
    public List<AttachmentDto> getAll(
            @Parameter(description = "Related Request (Aclaración) `id`.") @PathVariable int requestId,
            @RequestAttribute Session session) {
        return this.service.retrieveList(requestId);
    }

    @Operation(operationId = "getAttachment", summary = "Retrieves an Attachment with full content.", responses = {
            @ApiResponse(responseCode = "200", description = "Attachment object for given `attachmentId`. Includes Base64 `content`.", content = @Content(schema = @Schema(implementation = AttachmentDto.class))),
            @ApiResponse(responseCode = "401", description = "The request requires user authentication.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "404", description = "The server has not found anything matching the Request-URI.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Internal error.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
    })
    @GetMapping("/{attachmentId}")
    @ResponseBody
    @RequireRole
    public AttachmentDto get(@Parameter(description = "Attachment `id`.") @PathVariable int attachmentId,
            @PathVariable int requestId, @RequestAttribute Session session) throws GenericException {
        return this.service.retrieve(attachmentId);
    }

}
