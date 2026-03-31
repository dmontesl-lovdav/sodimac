package com.sodimac.bctfacturacion.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
//import javax.persistence.TemporalType;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.sodimac.bctfacturacion.model.FacturacionGlobalVentaModel;
//import com.sodimac.bctfacturacion.repository.bct.FacHdrRepository;
import com.sodimac.bctfacturacion.service.FacHdrService;

@Service
public class FacHdrServiceImpl implements FacHdrService {
	private static final int IDX_UUID 		= 0;
	private static final int IDX_FECHA 		= 1;
	private static final int IDX_TIPO 		= 2;
	//private static final int IDX_TOTAL 		= 3;
	
	//@Autowired
	//private FacHdrRepository facHdrRepository;
	
	@Autowired
	@Qualifier("entityManagerFactoryBct")
	private EntityManager em;
	
	@Autowired
	private Environment env;

	
	@Override
	public List<FacturacionGlobalVentaModel> getTimbradoGlobalVenta(Date pFechaInicio,Date pFechaFinal) {
		String file =  this.env.getProperty("global.ventas.sql");
		return this.getTimbradoGlobalResult(file, pFechaInicio,pFechaFinal);
	}
	
	@Override
	public List<FacturacionGlobalVentaModel> getTimbradoGlobalDevolucionD(Date pFechaInicio,Date pFechaFinal) {
		String file =  this.env.getProperty("global.devolucion.sql");
		return this.getTimbradoGlobalResult(file, pFechaInicio,pFechaFinal);
	}

	@Override
	public List<FacturacionGlobalVentaModel> getTimbradoGlobalNotaCredito(Date pFechaInicio,Date pFechaFinal) {
		String file =  this.env.getProperty("global.nota.credito.sql");
		return this.getTimbradoGlobalResult(file, pFechaInicio,pFechaFinal);
	}

	@Override
	public List<FacturacionGlobalVentaModel> getTimbradoGlobalDevolucionFDC(Date pFechaInicio,Date pFechaFinal) {
		String file =  this.env.getProperty("global.devolucion.fdc.sql");
		return this.getTimbradoGlobalResult(file, pFechaInicio,pFechaFinal);
	}
	
	private List<FacturacionGlobalVentaModel> getTimbradoGlobalResult(String file, Date pFechaInicio,Date pFechaFinal) {
		
		List<FacturacionGlobalVentaModel> listFacturas = new ArrayList<FacturacionGlobalVentaModel>();
		String sql = "";
		try {
			
			sql = FileUtils.readFileToString(new File(file), "UTF-8");
			
			Query query = this.em.createNativeQuery(sql);
			query.setParameter("pFechaInicio",pFechaInicio);
			query.setParameter("pFechaFinal",pFechaFinal);
		
			@SuppressWarnings("unchecked")			
			List<Object[]> listObjFacturas = query.getResultList();
			if (listObjFacturas != null) {
				// Valida que existan registros para procesar
				if(listObjFacturas.size()>0)
				{
					for (Object[] arrFacturas : listObjFacturas) {
						
						String tienda 	= (arrFacturas[IDX_UUID] != null ? arrFacturas[IDX_UUID].toString() : null);
						String fecha 	= (arrFacturas[IDX_FECHA] != null ? arrFacturas[IDX_FECHA].toString() : null);
						String tipo 	= (arrFacturas[IDX_TIPO] != null ? arrFacturas[IDX_TIPO].toString() : null);
						
						FacturacionGlobalVentaModel global = new FacturacionGlobalVentaModel();
						global.setTienda(tienda);
						global.setFecha(fecha);
						global.setTipoTimbrado(tipo);
						//global.setTotal(total);
						
						listFacturas.add(global);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return listFacturas;
	}

}
