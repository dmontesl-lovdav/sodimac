/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sodimac.aclaraciones.api.repository;

import com.sodimac.aclaraciones.api.model.entity.Attachment;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author ggalvan
 */
@Repository
public interface AttachmentRepository extends CrudRepository<Attachment, Integer> {

    public Attachment findByIdAndRequestActiveAndActive(int id, boolean requestActive, boolean active);

    public List<Attachment> findByRequestIdAndRequestActiveAndActive(int requestId, boolean requestActive, boolean active);

}
