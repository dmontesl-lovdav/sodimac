/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sodimac.aclaraciones.api.service.sla;

import java.util.List;

import com.sodimac.aclaraciones.api.exception.GenericException;
import com.sodimac.aclaraciones.api.model.dto.SlaDto;
import com.sodimac.aclaraciones.api.security.Session;

/**
 *
 * @author ggalvan
 */
public interface SlaService {

    public int save(SlaDto dto, Session session) throws GenericException;

    public int update(int id, SlaDto dto, Session session) throws GenericException;

    public int publish(int id, Boolean publishStatus) throws GenericException;

    public int delete(int id) throws GenericException;

    public SlaDto retrieve(int id, Session session);

    public List<SlaDto> retrieve();

    String getResponseTimeByModule(int moduleId) throws GenericException;
}
