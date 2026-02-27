/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sodimac.aclaraciones.api.model.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;

public class CatalogDto implements Serializable {

    private static final long serialVersionUID = -7176112228997419186L;

    @Schema(description = "Catalog `id`. This is the refered value.", example = "11")
    private int id;

    @Schema(description = """
                Type of family. Available known values are:
                - 1. Business Units.
                - 2. Countries.
                - 3. Systems (modules).
                - 4. Request reasons.
                - 5. Request details.
                - 11. Task kanban columns.
                - 12. Task progress.
                - 13. Task priority.
                - 14. Task repetitiveness.
            """, example = "1")
    private int type;

    @Schema(description = "Catalog human description.", example = "México")
    private String description;

    @Schema(description = "Area asociada al catálogo.", example = "Finanzas")
    private String area;

    // =======================
    // GETTERS / SETTERS
    // =======================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }
}
