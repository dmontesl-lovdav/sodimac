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
import com.sodimac.aclaraciones.api.model.dto.NoticeDto;
import com.sodimac.aclaraciones.api.security.RequireRole;
import com.sodimac.aclaraciones.api.security.Session;
import com.sodimac.aclaraciones.api.service.notice.NoticeService;

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
@RequestMapping("/notices")
public class NoticeController {

        private final NoticeService service;

        @Autowired
        public NoticeController(NoticeService service) {
                this.service = service;
        }

        @Operation(operationId = "postNotice", summary = "Creates a new Notice (Información Relacionada)", responses = {
                        @ApiResponse(responseCode = "200", description = "Given notice (Información Relacionada) has been successfully created."),
                        @ApiResponse(responseCode = "401", description = "The request requires user authentication.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "404", description = "The server has not found anything matching the requested URI.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "500", description = "Internal error.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
        @ResponseBody
        @PostMapping
        @RequireRole
        public int post(@RequestBody NoticeDto notice, @RequestAttribute(name = "session") Session session)
                        throws GenericException {
                return this.service.save(notice, session);
        }

        @Operation(operationId = "publishNotice", summary = "De/publish a Notice (Información Relacionada).", responses = {
                        @ApiResponse(responseCode = "401", description = "The request requires user authentication.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "404", description = "The server has not found anything matching the requested URI.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "500", description = "Internal error.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
        @PostMapping("/{noticeId}/publish")
        @RequireRole
        public void configure(
                        @Parameter(description = "Notice (Información Relacionada) `id`.", example = "112233") @PathVariable int noticeId,
                        @Parameter(description = "Publishing status", example = "true") @RequestParam(defaultValue = "true") Boolean publish,
                        @RequestAttribute(name = "session") Session session) throws GenericException {
                this.service.publish(noticeId, publish);
        }

        @Operation(operationId = "getNotice", summary = "Retrieves a Notice (Información Relacionada) based on its id.", responses = {
                        @ApiResponse(responseCode = "200", description = "Notice objects with given `id`.", content = @Content(schema = @Schema(implementation = NoticeDto.class))),
                        @ApiResponse(responseCode = "401", description = "The request requires user authentication.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "404", description = "The server has not found anything matching the requested URI.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "500", description = "Internal error.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
        @ResponseBody
        @GetMapping("/{noticeId}")
        @RequireRole
        public NoticeDto get(
                        @Parameter(description = "Notice (Información Relacionada) `id`.", example = "112233") @PathVariable int noticeId,
                        @RequestAttribute(name = "session") Session session) {
                return this.service.retrieve(noticeId, session);
        }

        @Operation(operationId = "getNotices", summary = "Retrieves a list of Notice (Información Relacionada) based for the current user session.", responses = {
                        @ApiResponse(responseCode = "200", description = "Notice objects that meets given criteria.", content = @Content(schema = @Schema(implementation = NoticeDto[].class))),
                        @ApiResponse(responseCode = "401", description = "The request requires user authentication.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "404", description = "The server has not found anything matching the requested URI.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "500", description = "Internal error.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
        @ResponseBody
        @GetMapping
        @RequireRole
        public List<NoticeDto> get(@RequestAttribute(name = "session") Session session) {
                return this.service.retrieve();
        }

        @Operation(operationId = "putNotice", summary = "Updates a Notice (Información Relacionada)", responses = {
                        @ApiResponse(responseCode = "200", description = "Given notice (Información Relacionada) has been successfully updated."),
                        @ApiResponse(responseCode = "401", description = "The request requires user authentication.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "404", description = "The server has not found anything matching the requested URI.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "500", description = "Internal error.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
        @ResponseBody
        @PutMapping("/{noticeId}")
        @RequireRole
        public int put(@Parameter(description = "Notice (Información Relacionada) `id`.", example = "112233") @PathVariable Integer noticeId,
                        @RequestBody NoticeDto notice,
                        @RequestAttribute(name = "session") Session session)
                        throws GenericException {
                return this.service.update(noticeId, notice, session);
        }

        @Operation(operationId = "deleteNotice", summary = "Deletes a Notice (Información Relacionada)", responses = {
                        @ApiResponse(responseCode = "200", description = "Given notice (Información Relacionada) has been successfully deleted."),
                        @ApiResponse(responseCode = "401", description = "The request requires user authentication.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "404", description = "The server has not found anything matching the requested URI.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "500", description = "Internal error.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
        @ResponseBody
        @DeleteMapping("/{noticeId}")
        @RequireRole
        public int delete(
                        @Parameter(description = "Notice (Información Relacionada) `id`.", example = "112233") @PathVariable Integer noticeId,
                        @RequestAttribute(name = "session") Session session)
                        throws GenericException {
                return this.service.delete(noticeId);
        }

}
