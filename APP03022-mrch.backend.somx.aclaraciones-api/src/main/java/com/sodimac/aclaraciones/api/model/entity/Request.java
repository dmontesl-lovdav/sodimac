/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sodimac.aclaraciones.api.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.List;

/**
 *
 * @author ggalvan
 */
@Entity
@Table
public class Request extends AbstractModel {

    private static final long serialVersionUID = 8959586847782725401L;

    public static final int STATUS_UNATTENDED = 10;
    public static final int STATUS_ONGOING = 20;
    public static final int STATUS_RESOLVED = 30;
    public static final int STATUS_CANCELLED = 40;
    //
    public static final int CLAZZ_CATALOG_TYPE = 11;
    public static final int PROGRESS_CATALOG_TYPE = 12;
    public static final int PRIORITY_CATALOG_TYPE = 13;
    public static final int REPEATITIVENESS_CATALOG_TYPE = 14;
    //

    @Column(nullable = false, length = 256)
    private String description;

    @Column(nullable = false, length = 32)
    private String orderId;

    @ManyToOne(cascade = { CascadeType.PERSIST })
    @JoinColumn(name = "requester", nullable = false)
    private Author requester;

    @ManyToOne(cascade = { CascadeType.PERSIST })
    @JoinColumn(name = "operator")
    private Author operator;

    @ManyToOne(cascade = { CascadeType.PERSIST })
    @JoinColumn
    private Company company;

    @ManyToOne
    @JoinColumn(name = "module", nullable = false)
    private Catalog module;

    @ManyToOne
    @JoinColumn(name = "reason", nullable = false)
    private Catalog reason;

    @ManyToOne
    @JoinColumn(name = "detail", nullable = false)
    private Catalog detail;

    @ManyToOne
    @JoinColumn(name = "progress")
    private Catalog progress;

    @ManyToOne
    @JoinColumn(name = "clazz")
    private Catalog clazz;

    @ManyToOne
    @JoinColumn(name = "priority")
    private Catalog priority;

    @Column(name = "responsible", length = 255)
    private String responsible;

    @Column(name = "nombre_proveedor", length = 255)
    private String nombreProveedor;

    @ManyToOne
    @JoinColumn(name = "repeatitiveness")
    private Catalog repeatitiveness;

    @OneToMany(mappedBy = "request", cascade = CascadeType.PERSIST)
    private List<Attachment> attachments;

    @OneToMany(mappedBy = "request", cascade = CascadeType.PERSIST)
    private List<Comment> comments;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrder(String orderId) {
        this.orderId = orderId;
    }

    public Author getRequester() {
        return requester;
    }

    public void setRequester(Author requester) {
        this.requester = requester;
    }

    public Author getOperator() {
        return operator;
    }

    public void setOperator(Author operator) {
        this.operator = operator;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Catalog getReason() {
        return reason;
    }

    public Catalog getModule() {
        return module;
    }

    public void setModule(Catalog module) {
        this.module = module;
    }

    public void setReason(Catalog reason) {
        this.reason = reason;
    }

    public Catalog getDetail() {
        return detail;
    }

    public void setDetail(Catalog detail) {
        this.detail = detail;
    }

    public Catalog getClazz() {
        return clazz;
    }

    public void setClazz(Catalog clazz) {
        this.clazz = clazz;
    }

    public Catalog getProgress() {
        return progress;
    }

    public void setProgress(Catalog progress) {
        this.progress = progress;
    }

    public Catalog getPriority() {
        return priority;
    }

    public void setPriority(Catalog priority) {
        this.priority = priority;
    }

    public Catalog getRepeatitiveness() {
        return repeatitiveness;
    }

    public void setRepeatitiveness(Catalog repeativeness) {
        this.repeatitiveness = repeativeness;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
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
}
