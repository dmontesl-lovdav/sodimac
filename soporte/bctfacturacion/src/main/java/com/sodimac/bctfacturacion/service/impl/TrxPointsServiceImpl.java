package com.sodimac.bctfacturacion.service.impl;

import java.text.ParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.bctfacturacion.mapper.AdminPuntosCesMapper;
import com.sodimac.bctfacturacion.mapper.PuntosSkuMapper;
import com.sodimac.bctfacturacion.mapper.VentaCabMapper;
import com.sodimac.bctfacturacion.mapper.VentaDetImpuestoMapper;
import com.sodimac.bctfacturacion.model.AdminPuntosCesModel;
import com.sodimac.bctfacturacion.model.ListaPuntosSkuModel;
import com.sodimac.bctfacturacion.model.VentaCabModel;
import com.sodimac.bctfacturacion.model.VentaDetImpuestoModel;
import com.sodimac.bctfacturacion.repository.bct.TrxPointsRepository;
import com.sodimac.bctfacturacion.service.ITrxPointsService;

@Service
public class TrxPointsServiceImpl implements ITrxPointsService {

	@Autowired
	private TrxPointsRepository pointsRepository;
	
	@Override
	public List<AdminPuntosCesModel> obtenerPuntosCes(String fecha) {
		List<Object[]> listObject = this.pointsRepository.findPuntosCes(fecha);
		return AdminPuntosCesMapper.convertObjectToDtos(listObject);
	}

	@Override
	public List<AdminPuntosCesModel> obtenerPuntosCesPorTickets(List<String> tickets) {
		List<Object[]> listObject = this.pointsRepository.findPuntosCesByTickets(tickets);
		return AdminPuntosCesMapper.convertObjectToDtos(listObject);
	}

	@Override
	public ListaPuntosSkuModel obtenerSkuPuntos(Long idPuntosCes) {
		List<Object[]> listObject = this.pointsRepository.findSkuPuntosById(idPuntosCes);
		return PuntosSkuMapper.convertToDto(listObject);
	}
	
	@Override
	public List<VentaCabModel> obtenerTicket(String ticket) throws ParseException {
		List<Object[]> listObject = this.pointsRepository.findByTicket(ticket);
		return VentaCabMapper.convertObjectToDtos(listObject);
	}

	@Override
	public List<VentaDetImpuestoModel> obtenerImpuestos(String ticket) throws ParseException {
		List<Object[]> listObject = this.pointsRepository.findImpuestosByTicket(ticket);
		return VentaDetImpuestoMapper.convertObjectToDtos(listObject);
	}	
}
