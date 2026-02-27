/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sodimac.aclaraciones.api.model.dto;

import java.io.Serializable;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 *
 * @author ggalvan
 */
public class AttachmentDto implements Serializable {

    private static final long serialVersionUID = 8318767158459000058L;

    private int id;
    @Schema(description = "Attachment filename", example = "constancia_fiscal.pdf")
    private String name;
    @Schema(description = "Base64 file content", example = "R2FicmllbCBHYWx2w6Fu")
    private String content;
    private Date creationTime;
    @Schema(description = "Name or email of uploader.", example = "Gabriel Galván")
    private String author;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

}
