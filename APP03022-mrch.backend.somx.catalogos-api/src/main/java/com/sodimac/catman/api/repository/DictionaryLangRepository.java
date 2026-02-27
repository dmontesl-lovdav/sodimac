package com.sodimac.catman.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.catman.api.model.entity.DictionaryLang;

@Repository
public interface DictionaryLangRepository extends JpaRepository<DictionaryLang, Integer> {

    Optional<DictionaryLang> findByDictIdAndLangId(Integer dictId, Integer langId);

    List<DictionaryLang> findByLangId(Integer langId);

    List<DictionaryLang> findByDictId(Integer dictId);
}
