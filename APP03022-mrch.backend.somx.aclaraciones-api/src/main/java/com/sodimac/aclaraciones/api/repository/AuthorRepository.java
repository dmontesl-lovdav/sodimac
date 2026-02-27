/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sodimac.aclaraciones.api.repository;

import com.sodimac.aclaraciones.api.model.entity.Author;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author ggalvan
 */
@Repository
public interface AuthorRepository extends CrudRepository<Author, Integer> {

    public Author findByEmail(String email);

}
