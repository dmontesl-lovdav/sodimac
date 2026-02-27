/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sodimac.aclaraciones.api.repository;

import com.sodimac.aclaraciones.api.model.entity.Comment;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author ggalvan
 */
@Repository
public interface CommentRepository extends CrudRepository<Comment, Integer> {

    public List<Comment> findByRequestIdAndRequestActiveAndActive(int requestId, boolean requestActive, boolean active);

}
