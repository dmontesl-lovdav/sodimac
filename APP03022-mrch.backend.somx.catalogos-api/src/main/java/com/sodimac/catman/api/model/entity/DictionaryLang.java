package com.sodimac.catman.api.model.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "dictionary_lang",
    uniqueConstraints = @UniqueConstraint(columnNames = {"dict_id", "lang_id"}),
    indexes = {
        @Index(columnList = "dict_id"),
        @Index(columnList = "lang_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DictionaryLang implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "dict_id", nullable = false)
    private Integer dictId;

    @Column(name = "lang_id", nullable = false)
    private Integer langId;

    @Column(nullable = false, length = 512)
    private String description;
}
