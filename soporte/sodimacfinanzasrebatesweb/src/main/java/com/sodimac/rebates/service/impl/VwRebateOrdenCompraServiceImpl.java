package com.sodimac.rebates.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.model.entity.VwRebateOrdenCompraEntity;
import com.sodimac.rebates.repository.VwRebateOrdenCompraRepository;
import com.sodimac.rebates.service.IVwRebateOrdenCompraService;

@Service
public class VwRebateOrdenCompraServiceImpl implements IVwRebateOrdenCompraService {

	@Autowired
	private VwRebateOrdenCompraRepository repository;
	
	@Override
	public VwRebateOrdenCompraEntity leerOrdenCompra(int ordenCompra) {
		VwRebateOrdenCompraEntity result = null;
		  List<VwRebateOrdenCompraEntity> lst = this.repository.findByOrdenCompra(ordenCompra);
		  if (lst.size() > 0) {
			  result = lst.get(0);
		  }
		return result;
	}

}
