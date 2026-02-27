/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sodimac.aclaraciones.api.service.notice;

import java.util.List;

import com.sodimac.aclaraciones.api.exception.GenericException;
import com.sodimac.aclaraciones.api.model.dto.NoticeDto;
import com.sodimac.aclaraciones.api.security.Session;

/**
 *
 * @author ggalvan
 */
public interface NoticeService {

    public int save(NoticeDto notice, Session session) throws GenericException;

    public int update(int noticeId, NoticeDto notice, Session session) throws GenericException;

    public int publish(int noticeId, Boolean publishStatus) throws GenericException;

    public int delete(int noticeId) throws GenericException;

    public NoticeDto retrieve(int noticeId, Session session);

    public List<NoticeDto> retrieve();

}
