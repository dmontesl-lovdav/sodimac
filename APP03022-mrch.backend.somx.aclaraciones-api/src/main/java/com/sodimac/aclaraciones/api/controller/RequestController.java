/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sodimac.aclaraciones.api.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sodimac.aclaraciones.api.exception.ExceptionWrapper;
import com.sodimac.aclaraciones.api.exception.GenericException;
import com.sodimac.aclaraciones.api.model.dto.RequestDto;
import com.sodimac.aclaraciones.api.model.dto.RequestDto.PageResponse;
import com.sodimac.aclaraciones.api.security.RequireRole;
import com.sodimac.aclaraciones.api.security.Session;
import com.sodimac.aclaraciones.api.service.cases.RequestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/requests")
public class RequestController {

        private final RequestService service;

        @Autowired
        public RequestController(RequestService service) {
                this.service = service;
        }

        @Operation(operationId = "postRequest", summary = "Creates a new Request (Aclaración)", security = {
                        @SecurityRequirement(name = "bearerAuth") }, responses = {
                                        @ApiResponse(responseCode = "200", description = "Given request (aclaración) has been successfully created.", content = @Content(schema = @Schema(implementation = Integer.class))),
                                        @ApiResponse(responseCode = "401", description = "The request requires user authentication.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                                        @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                                        @ApiResponse(responseCode = "404", description = "The server has not found anything matching the Request-URI.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                                        @ApiResponse(responseCode = "500", description = "Internal error.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
                        })
        @ResponseBody
        @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE) // sin "consumes" para no romper DEV
        @RequireRole
        public int post(
                        @RequestBody(required = true, description = "Request payload (JSON).", content = @Content(schema = @Schema(implementation = RequestDto.class), mediaType = MediaType.APPLICATION_JSON_VALUE)) @Valid @org.springframework.web.bind.annotation.RequestBody RequestDto request,
                        @RequestAttribute(name = "session") Session session) throws GenericException {
                return this.service.save(request, session);
        }

        @Operation(operationId = "configureRequest", summary = "Configures a Request (Aclaración).", responses = {
                        @ApiResponse(responseCode = "401", description = "The request requires user authentication.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "404", description = "The server has not found anything matching the Request-URI.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "500", description = "Internal error.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
        @PostMapping(path = "/{requestId}/configure", produces = MediaType.APPLICATION_JSON_VALUE)
        @RequireRole
        public void configure(
                        @Parameter(description = "Request (Aclaración) `id`.", example = "112233") @PathVariable int requestId,
                        @org.springframework.web.bind.annotation.RequestBody RequestDto request,
                        @RequestAttribute(name = "session") Session session) throws GenericException {
                this.service.configure(requestId, request, session);
        }

        @Operation(operationId = "getRequest", summary = "Retrieves a Request (Aclaración) based on its id.", responses = {
                        @ApiResponse(responseCode = "200", description = "Request objects with given `id`.", content = @Content(schema = @Schema(implementation = RequestDto.class))),
                        @ApiResponse(responseCode = "401", description = "The request requires user authentication.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "404", description = "The server has not found anything matching the Request-URI.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "500", description = "Internal error.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
        @ResponseBody
        @GetMapping("/{requestId}")
        @RequireRole
        public RequestDto get(
                        @Parameter(description = "Request (Aclaración) `id`.", example = "112233") @PathVariable int requestId,
                        @RequestAttribute(name = "session") Session session) {
                return this.service.retrieve(requestId, session);
        }

        @Operation(operationId = "getRequests", summary = "Retrieves paginated requests for the current user.", responses = {
                        @ApiResponse(responseCode = "200", description = "Paginated requests.", content = @Content(schema = @Schema(implementation = PageResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Authentication required."),
                        @ApiResponse(responseCode = "403", description = "Access forbidden."),
                        @ApiResponse(responseCode = "500", description = "Internal error.")
        })
        @ResponseBody
        @GetMapping
        @RequireRole
        public PageResponse<RequestDto> get(
                        @RequestParam(required = false) String criteria,
                        @RequestParam(required = false) String dateFrom,
                        @RequestParam(required = false) String dateTo,
                        @RequestParam(required = false) Integer reason,
                        @RequestParam(required = false, name = "clazz") Integer clazz,
                        @RequestParam(required = false, defaultValue = "1") int page,
                        @RequestParam(required = false, defaultValue = "10") int size,
                        @RequestAttribute(name = "session") Session session) {

                Date _dateFrom, _dateTo;

                try {
                        _dateFrom = (dateFrom != null)
                                        ? new SimpleDateFormat("yyyy-MM-dd").parse(dateFrom)
                                        : null;
                } catch (Exception e) {
                        _dateFrom = null;
                }

                try {
                        _dateTo = (dateTo != null)
                                        ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                                                        .parse(dateTo + " 23:59:59.999")
                                        : null;
                } catch (Exception e) {
                        _dateTo = null;
                }

                // Nueva llamada al servicio paginado
                List<RequestDto> result = this.service.retrievePaged(
                                criteria, _dateFrom, _dateTo, reason, clazz, page, size, session);

                long total = this.service.count(
                                criteria, _dateFrom, _dateTo, reason, clazz, session);

                int totalPages = (int) Math.ceil((double) total / size);

                return new PageResponse<>(result, total, page, totalPages);
        }

        @Operation(operationId = "deleteRequest", summary = "Deletes a Request (Aclaración) based on its id.", responses = {
                        @ApiResponse(responseCode = "200", description = "Request objects with given `id`.", content = @Content(schema = @Schema(implementation = RequestDto.class))),
                        @ApiResponse(responseCode = "401", description = "The request requires user authentication.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "404", description = "The server has not found anything matching the Request-URI.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                        @ApiResponse(responseCode = "500", description = "Internal error.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
        })
        @ResponseBody
        @DeleteMapping("/{requestId}")
        @RequireRole
        public RequestDto delete(
                        @Parameter(description = "Request (Aclaración) `id`.", example = "112233") @PathVariable int requestId,
                        @RequestAttribute(name = "session") Session session) throws GenericException {
                return this.service.delete(requestId, session);
        }

        @Operation(operationId = "getRequestsByModule", summary = "Retrieves all requests filtered by moduleId with optional filters and pagination.", security = {
                        @SecurityRequirement(name = "bearerAuth") }, responses = {
                                        @ApiResponse(responseCode = "200", description = "Paginated list of Request objects filtered by module and optional criteria.", content = @Content(schema = @Schema(implementation = PageResponse.class))),
                                        @ApiResponse(responseCode = "401", description = "Authentication required.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                                        @ApiResponse(responseCode = "403", description = "Access forbidden.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                                        @ApiResponse(responseCode = "500", description = "Internal error.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
                        })
        @ResponseBody
        @GetMapping("/by-module")
        @RequireRole
        public PageResponse<RequestDto> getByModule(
                        @RequestParam(name = "moduleId") int moduleId,
                        @RequestParam(required = false) String criteria,
                        @RequestParam(required = false) String dateFrom,
                        @RequestParam(required = false) String dateTo,
                        @RequestParam(required = false) Integer reason,
                        @RequestParam(required = false, name = "clazz") Integer clazz,
                        @RequestParam(required = false, defaultValue = "1") int page,
                        @RequestParam(required = false, defaultValue = "10") int size,
                        @RequestAttribute(name = "session") Session session) {

                Date _dateFrom, _dateTo;
                try {
                        _dateFrom = (dateFrom != null)
                                        ? new SimpleDateFormat("yyyy-MM-dd").parse(dateFrom)
                                        : null;
                } catch (Exception e) {
                        _dateFrom = null;
                }

                try {
                        _dateTo = (dateTo != null)
                                        ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                                                        .parse(dateTo + " 23:59:59.999")
                                        : null;
                } catch (Exception e) {
                        _dateTo = null;
                }

                List<RequestDto> result = service.retrieveByModule(moduleId, criteria, _dateFrom, _dateTo, reason,
                                clazz, page,
                                size);
                long total = service.countByModule(moduleId, criteria, _dateFrom, _dateTo, reason, clazz);
                int totalPages = (int) Math.ceil((double) total / size);

                return new PageResponse<>(result, total, page, totalPages);
        }

        // ===============================================================
        // 🔹 NUEVO ENDPOINT: obtener requests por múltiples módulos
        // ===============================================================
        @Operation(operationId = "getRequestsByModules", summary = "Retrieves all requests filtered by multiple moduleIds with optional filters and pagination.", security = {
                        @SecurityRequirement(name = "bearerAuth") }, responses = {
                                        @ApiResponse(responseCode = "200", description = "Paginated list of Request objects filtered by modules.", content = @Content(schema = @Schema(implementation = PageResponse.class))),
                                        @ApiResponse(responseCode = "401", description = "Authentication required.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                                        @ApiResponse(responseCode = "403", description = "Access forbidden.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class))),
                                        @ApiResponse(responseCode = "500", description = "Internal error.", content = @Content(schema = @Schema(implementation = ExceptionWrapper.class)))
                        })
        @ResponseBody
        @GetMapping("/by-modules")
        @RequireRole
        public PageResponse<RequestDto> getByModules(
                        @RequestParam List<Integer> moduleIds,
                        @RequestParam(required = false) String criteria,
                        @RequestParam(required = false) String dateFrom,
                        @RequestParam(required = false) String dateTo,
                        @RequestParam(required = false) Integer reason,
                        @RequestParam(required = false, name = "clazz") Integer clazz,
                        @RequestParam(required = false, defaultValue = "1") int page,
                        @RequestParam(required = false, defaultValue = "10") int size,
                        @RequestAttribute(name = "session") Session session) {

                // Reutilizando EXACTO el mismo parseo
                Date _dateFrom, _dateTo;

                try {
                        _dateFrom = (dateFrom != null)
                                        ? new SimpleDateFormat("yyyy-MM-dd").parse(dateFrom)
                                        : null;
                } catch (Exception e) {
                        _dateFrom = null;
                }

                try {
                        _dateTo = (dateTo != null)
                                        ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                                                        .parse(dateTo + " 23:59:59.999")
                                        : null;
                } catch (Exception e) {
                        _dateTo = null;
                }

                List<RequestDto> result = service.retrieveByModules(
                                moduleIds, criteria, _dateFrom, _dateTo, reason, clazz, session.getEmail(), page, size);

                long total = service.countByModules(
                                moduleIds, criteria, _dateFrom, _dateTo, reason, clazz, session.getEmail());

                int totalPages = (int) Math.ceil((double) total / size);

                return new PageResponse<>(result, total, page, totalPages);
        }

}
