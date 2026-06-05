package com.sodimac.fiscal.api.model.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "cat_parameter", schema = "core_utils")
public class CatParameterEntity {

    @Id
    @Column(name = "id_parameter")
    private Integer idParameter;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "value", length = 1000)
    private String value;

    @Column(name = "status")
    private Integer status;
}
