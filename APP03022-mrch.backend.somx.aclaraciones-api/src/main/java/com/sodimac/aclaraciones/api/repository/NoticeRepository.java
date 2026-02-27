package com.sodimac.aclaraciones.api.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.sodimac.aclaraciones.api.model.entity.Notice;

public interface NoticeRepository extends CrudRepository<Notice, Integer> {

    public List<Notice> findByActive(boolean active);

}
