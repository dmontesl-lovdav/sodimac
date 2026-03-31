package com.sodimac.bctfacturacion.service.impl;

import java.text.ParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.bctfacturacion.entity.ces.AdminPuntosCesEntity;
import com.sodimac.bctfacturacion.enums.EEstatus;
import com.sodimac.bctfacturacion.mapper.AdminPuntosCesMapper;
import com.sodimac.bctfacturacion.model.AdminPuntosCesModel;
import com.sodimac.bctfacturacion.repository.ces.AdminPuntosCesRepository;
import com.sodimac.bctfacturacion.service.IAdminPuntosCesService;

@Service
public class AdminPuntosCesServiceImpl implements IAdminPuntosCesService {

	@Autowired 
	private AdminPuntosCesRepository adminPuntosCesRepository;
	
	@Override
	public boolean existeTicket(String ticket, String tipoTransaccionCes) {
		long count = this.adminPuntosCesRepository.countByTicketAndTipoTransaccionCesAndEstatus(ticket, tipoTransaccionCes, EEstatus.ACTIVO.getId());
		if (count > 0) {
			return true;
		}
		return false;
	}

	@Override
	public AdminPuntosCesModel getTicket(String ticket, String tipoTransaccionCes) {
		AdminPuntosCesEntity entity = this.adminPuntosCesRepository.findByTicketAndTipoTransaccionCesAndEstatus(ticket, tipoTransaccionCes, EEstatus.ACTIVO.getId());
		return AdminPuntosCesMapper.convertToDto(entity);
	}

	@Override
	public List<AdminPuntosCesModel> getTickets(String tipoTransaccionCes) {
		List<AdminPuntosCesEntity> entities = this.adminPuntosCesRepository.findByTipoTransaccionCesAndEstatus(tipoTransaccionCes, EEstatus.ACTIVO.getId());
		return AdminPuntosCesMapper.convertToDtos(entities);
	}

	@Override
	public void guardar(AdminPuntosCesModel dto) throws ParseException {
		AdminPuntosCesEntity entity = AdminPuntosCesMapper.convertToEntity(dto);
		this.adminPuntosCesRepository.save(entity);
	}

}
