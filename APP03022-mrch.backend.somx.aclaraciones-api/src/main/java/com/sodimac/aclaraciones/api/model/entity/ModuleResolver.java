package com.sodimac.aclaraciones.api.model.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "module_resolver")
public class ModuleResolver implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "module_id", nullable = false)
    private Integer moduleId;

    @Column(name = "module_name", length = 128, nullable = false)
    private String moduleName;

    @Column(name = "person_name", length = 128, nullable = false)
    private String personName;

    @Column(name = "resolver_email", length = 160, nullable = false)
    private String resolverEmail;

    @Column(name = "area", length = 128)
    private String area; // ✅ Faltaba este campo

    // ---------------- getters / setters ----------------

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getModuleId() {
        return moduleId;
    }

    public void setModuleId(Integer moduleId) {
        this.moduleId = moduleId;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getResolverEmail() {
        return resolverEmail;
    }

    public void setResolverEmail(String resolverEmail) {
        this.resolverEmail = resolverEmail;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }
}
