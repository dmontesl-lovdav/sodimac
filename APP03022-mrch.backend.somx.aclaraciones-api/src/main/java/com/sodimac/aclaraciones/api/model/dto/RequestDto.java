/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sodimac.aclaraciones.api.model.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;

/**
 *
 * @author ggalvan
 */
public class RequestDto implements Serializable {

    private static final long serialVersionUID = 6725690881187869033L;

    @Schema(description = "Numeric Unique Identifier", example = "99888")
    private int id;
    @Schema(description = "Creation time in ISO 8601 format", example = "2025-05-14T15:51:31Z")
    private Date creationTime;
    @Schema(description = "Elapsed time since `creationTime` in days", example = "5")
    private Integer elapsedTime;
    @Schema(description = """
            Status of this Request (Aclaración). Available values are:
            - STATUS_UNATTENDED = 10;
            - STATUS_ONGOING = 20;
            - STATUS_RESOLVED = 30;
            - STATUS_CANCELLED = 40;
            """, example = "40")
    private Integer status;
    @Schema(description = "Related Order Id", example = "999-888-777")
    private String orderId;
    @Schema(description = "Reason for this Request. See `catalogs` methods for further info.", example = "5")
    private Integer reason;
    @Schema(description = "Details of an specific reason for this Request. See `catalogs` methods for further info.", example = "6")
    private Integer detail;
    @Schema(description = "String description.", example = "Random string")
    private String description;
    @Schema(description = "Name or email of requester.", example = "Gabriel Galván")
    private String requester;
    @Schema(description = "Name or email of current supporter.", example = "Gabriel Galván")
    private String operator;
    //
    @Schema(description = "Requester company name.", example = "Empresa S.A.")
    private String company;
    @Schema(description = "Requester tax identifier.", example = "XAXA010101SV5")
    private String rut;
    //
    @Schema(description = "Business Unit for this Request. See `catalogs` methods for further info.", example = "6")
    private Integer businessUnit;
    @Schema(description = "Country of origin for this Request. See `catalogs` methods for further info.", example = "6")
    private Integer country;
    @Schema(description = "System's module for this Request. See `catalogs` methods for further info.", example = "6")
    private Integer module;
    //
    @Schema(description = "Task Kanban board column. See `catalogs` methods for further info.", example = "6")
    private Integer clazz;
    @Schema(description = "Task progress. See `catalogs` methods for further info.", example = "6")
    private Integer progress;
    @Schema(description = "Task priority. See `catalogs` methods for further info.", example = "6")
    private Integer priority;
    @Schema(description = "Task repeatitiveness. See `catalogs` methods for further info.", example = "6")
    private Integer repeatitiveness;
    //
    @Schema(description = "Responsible identifier (free text / external id)")
    private String responsible;
    @Schema(description = "Supplier name", example = "Proveedor XYZ")
    private String nombreProveedor;
    @Schema(description = "Attached files.")
    private List<AttachmentDto> attachments;
    @Schema(description = "Comments.")
    private List<CommentDto> comments;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Integer getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(Integer elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String order) {
        this.orderId = order;
    }

    public Integer getReason() {
        return reason;
    }

    public void setReason(Integer reason) {
        this.reason = reason;
    }

    public Integer getDetail() {
        return detail;
    }

    public void setDetail(Integer detail) {
        this.detail = detail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequester() {
        return requester;
    }

    public void setRequester(String requester) {
        this.requester = requester;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public Integer getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(Integer businessUnit) {
        this.businessUnit = businessUnit;
    }

    public Integer getCountry() {
        return country;
    }

    public void setCountry(Integer country) {
        this.country = country;
    }

    public Integer getModule() {
        return module;
    }

    public void setModule(Integer module) {
        this.module = module;
    }

    public Integer getClazz() {
        return clazz;
    }

    public void setClazz(Integer clazz) {
        this.clazz = clazz;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getRepeatitiveness() {
        return repeatitiveness;
    }

    public void setRepeatitiveness(Integer repeatitiveness) {
        this.repeatitiveness = repeatitiveness;
    }

    public List<AttachmentDto> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentDto> attachments) {
        this.attachments = attachments;
    }

    public List<CommentDto> getComments() {
        return comments;
    }

    public void setComments(List<CommentDto> comments) {
        this.comments = comments;
    }

    public String getResponsible() {
        return responsible;
    }

    public void setResponsible(String responsible) {
        this.responsible = responsible;
    }

    public String getNombreProveedor() {
        return nombreProveedor;
    }

    public void setNombreProveedor(String nombreProveedor) {
        this.nombreProveedor = nombreProveedor;
    }

    @Schema(description = "Generic pagination response wrapper.")
    public static class PageResponse<T> {

        @Schema(description = "Current page data list.")
        private List<T> data;

        @Schema(description = "Total number of items available.")
        private long total;

        @Schema(description = "Current page number (1-based).")
        private int page;

        @Schema(description = "Total number of pages available.")
        private int totalPages;

        public PageResponse() {
        }

        public PageResponse(List<T> data, long total, int page, int totalPages) {
            this.data = data;
            this.total = total;
            this.page = page;
            this.totalPages = totalPages;
        }

        public List<T> getData() {
            return data;
        }

        public void setData(List<T> data) {
            this.data = data;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }
    }

}
