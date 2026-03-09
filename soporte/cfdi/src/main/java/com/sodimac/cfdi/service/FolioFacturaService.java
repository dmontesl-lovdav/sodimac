package com.sodimac.cfdi.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.sodimac.cfdi.dto.vista.DtoFactura;
import com.sodimac.cfdi.entity.fiscal.FolioFacturaEntity;
import com.sodimac.cfdi.models.FacturaRelacionadaModel;
import com.sodimac.cfdi.models.FolioFacturaModel;

public interface FolioFacturaService {

	public List<FolioFacturaModel> getFoliosFacturaPPDByParams(String fechaInicial, String fechaFinal, Integer pFolioFactura, String rfcEncriptado, int start, int rowsPerPage);
	
	public List<Object[]> getPagosComplementoFoliosFactura(Integer pIdPagoComplemento, Integer pIdFolioFactura);
	
	public Double getSumaFoliosFactura(Integer pFolioFactura);
	
	public Double getSumaComplementoFoliosFactura(Integer pFolioFactura);
	
	public Integer getMaxFolioFactura();

	public void registrarFolioFactura(List<DtoFactura> listFacturas, Integer folioFactura);

	public void desvincularFolio(Integer idFactura);

	public byte[] createExcel(String fechaInicial, String fechaFinal, Integer pFolioFactura, String rfcEncriptado) throws IOException;		

	public Optional<FolioFacturaEntity> getIdFolioFactura(Integer idfolioFactura);

	public void guardar(FolioFacturaEntity entity);

	public List<FacturaRelacionadaModel> getMontoUuiRelacionado(String uuidRelacionado);

	public List<FacturaRelacionadaModel> getMontosUuiRelacionados(String uuidRelacionado);

	public FacturaRelacionadaModel getMontoRealFactura(Double montoFactura, String uuid);

	public FacturaRelacionadaModel getMontoRealFactura(Double montoFactura, List<FacturaRelacionadaModel> listFacturasRel);		
}
