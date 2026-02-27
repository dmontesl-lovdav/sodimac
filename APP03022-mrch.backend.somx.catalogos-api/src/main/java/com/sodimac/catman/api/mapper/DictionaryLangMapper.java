package com.sodimac.catman.api.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.sodimac.catman.api.model.entity.DictionaryLang;

@Component
public class DictionaryLangMapper {

    public DictionaryLang toEntity(Integer dictId, Integer langId, String description) {
        if (description == null) {
            return null;
        }

        DictionaryLang entity = new DictionaryLang();
        entity.setDictId(dictId);
        entity.setLangId(langId);
        entity.setDescription(description);

        return entity;
    }

    public List<DictionaryLang> toEntityList(List<DictionaryLang> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .collect(Collectors.toList());
    }
}
