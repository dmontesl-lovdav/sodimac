package com.sodimac.bctfacturacion.job.impl;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.sodimac.bctfacturacion.job.SodimacJob;
import com.sodimac.bctfacturacion.model.FacturacionGlobalVentaModel;
import com.sodimac.bctfacturacion.service.FacHdrService;


@Component
public class TimbradoGlobalJob implements SodimacJob {
	
	private Logger logger = LoggerFactory.getLogger(TimbradoGlobalJob.class);
	private static final String SEPARADOR = ",";

	@Autowired
	private FacHdrService facHdrService;
	
	@Autowired
	private Environment env;
	
	@Override
	public void sincroniza() throws ParseException {
		
		try 
		{
		String fileVentasOut = this.env.getProperty("global.ventas.file");

		boolean ventasActivo = Boolean.valueOf( this.env.getProperty("global.ventas.activo")).booleanValue();
		boolean devolucionActivo = Boolean.valueOf( this.env.getProperty("global.devolucion.activo")).booleanValue();
		boolean notacreditoActivo = Boolean.valueOf( this.env.getProperty("global.notacredito.activo")).booleanValue();
		boolean devolucionFdcActivo = Boolean.valueOf( this.env.getProperty("global.devolucion.fdc.activo")).booleanValue();
		
		List<FacturacionGlobalVentaModel> listGlobalVentasTotal = new ArrayList<>(); 
		
		listGlobalVentasTotal.addAll(getListadoProceso(1,ventasActivo));
		listGlobalVentasTotal.addAll(getListadoProceso(2,devolucionActivo));
		listGlobalVentasTotal.addAll(getListadoProceso(3,notacreditoActivo));
		listGlobalVentasTotal.addAll(getListadoProceso(4,devolucionFdcActivo));
	
		this.timbradoGlobatoFile(listGlobalVentasTotal, fileVentasOut);
		} 
		catch(Exception ex)
		{
		  System.out.print(ex.toString());
		}
	}
	
	/**
	 * Trimestre actual o enviado por parametro
	 * @param pCalendario
	 * @return
	 */
	private int getTrimestre(Calendar pCalendario)
	{
	  int trimestre=0;
	  int mes = 0;
	  mes = pCalendario.get(Calendar.MONTH);
	  
	  if (mes >= 0 && mes<=2) {
		  trimestre=1;
	  }
	  if (mes >= 3 && mes<=5) {
		  trimestre=2;
	  }
	  if (mes >= 6 && mes<=8) {
		  trimestre=3;
	  }
	  if (mes >= 9 && mes<=12) {
		  trimestre=4;
	  }
	  return trimestre;
	}
	
	/**
	 * Obtiene la fecha inicio del trimestre
	 * @param numeroTrimestre indica el número de trimestre de busqueda
	 * @return
	 * @throws ParseException 
	 */
	private Date getFechaInicio(int anio,int numeroTrimestre) throws ParseException
	{
	   SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
	   String fecha="01/01/"+anio;  
	   Date fechaInicio=new Date();
	   switch(numeroTrimestre)
	   {
	      case 2:
	    	  fecha = "01/04/"+anio;
	         break;
	      case 3:
	    	  fecha = "01/07/"+anio;
		     break;
	      case 4:
	    	  fecha = "01/10/"+anio;
		     break;
		    default:
		    	fecha="01/01/"+anio;	
		     break;
	   }
	   fechaInicio = formatoFecha.parse(fecha);
	   
	   return fechaInicio;
	}
	
	/**
	 * Obtiene la fecha final del trimestre
	 * @param numeroTrimestre indica el número de trimestre de busqueda
	 * @return
	 * @throws ParseException 
	 */
	private Date getFechaFinal(int anio,int numeroTrimestre) throws ParseException
	{
		  SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
		  Integer diasProceso = Integer.valueOf( this.env.getProperty("global.proceso.dias") );
		  Calendar calendario = Calendar.getInstance();
		  Date fechaFinal=new Date();
		  Date fechaActual = new Date();
		  String fecha="31/03/"+anio;
		   
		   switch(numeroTrimestre)
		   {
		      case 2:
		    	  fecha = "30/06/"+anio;
		         break;
		      case 3:
		    	  fecha = "30/09/"+anio;
			     break;
		      case 4:
		    	  fecha = "31/12/"+anio;
			     break;
			    default:
			    	fecha="31/03/"+anio;	
			     break;
		   }
		   
		   fechaFinal = formatoFecha.parse(fecha);
		   
		   if(fechaFinal.after(fechaActual))
		   {
			   calendario.setTime(fechaActual);
			   calendario.add(Calendar.DAY_OF_YEAR, -diasProceso);
			   fechaFinal =  calendario.getTime();
		   }
		   
		   return fechaFinal;
	}
	

	/**
	 * Proceso para obtener el listado de los dias que se requieren reprocesar o falta
	 * por timbrar
	 * @param tipoFactura
	 * @param pProceso
	 * @return
	 * @throws ParseException
	 */
    private List<FacturacionGlobalVentaModel> getListadoProceso(int tipoFactura,boolean pProceso) throws ParseException
    {
    	List<FacturacionGlobalVentaModel> listGlobalTotal = new ArrayList<>(); 
    	Boolean anioFijo = Boolean.valueOf( this.env.getProperty("global.anio.fijo") );
    	SimpleDateFormat  formatoFecha=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		int anio = 0;
		int contador = 0;
		int trimestre = 0;
		int numTrimestre = 0;
		
		Calendar cal = Calendar.getInstance();
		
		if (anioFijo) {
			anio = Integer.valueOf( this.env.getProperty("global.anio") ).intValue();
		} else {
			anio = cal.get(Calendar.YEAR);
		}
		
		logger.info(getDescTipoFactura(tipoFactura)+" estatus de proceso ["+pProceso+"]");
		
		if (pProceso) 
		{
			
			trimestre = getTrimestre(cal);
			numTrimestre = getNumTrimetreProceso(tipoFactura);
			
			logger.info(getDescTipoFactura(tipoFactura)+" Numero de trimestres para procesar ["+numTrimestre+"] - Trimestre actual ["+trimestre+"]");	
			
			if (trimestre < numTrimestre)
			{
				numTrimestre=trimestre;
			}
			
			logger.info(getDescTipoFactura(tipoFactura)+" Trimestres reales de proceso ["+numTrimestre+"] ");	
			
			
			for(contador=0;contador<trimestre;contador++)
			{
			    Date fechaInicio=new Date();
			    Date fechaFinal=new Date();
			    
			    fechaInicio = getFechaInicio(anio, (trimestre - contador));
			    fechaFinal  = getFechaFinal(anio,  (trimestre - contador));
			  
					List<FacturacionGlobalVentaModel> listGlobalLocal = null;
					
					if (contador<numTrimestre)
					{

						logger.info(getDescTipoFactura(tipoFactura)+" Fecha Inicio [" + formatoFecha.format(fechaInicio) + "] a Fecha Final ["+formatoFecha.format(fechaFinal)+"]");		
						if(tipoFactura==1)
						{ 
							listGlobalLocal=this.facHdrService.getTimbradoGlobalVenta(fechaInicio,fechaFinal);
						}
						
						if(tipoFactura==2)
						{ 		
						    listGlobalLocal=this.facHdrService.getTimbradoGlobalDevolucionD(fechaInicio,fechaFinal);
						}
			
						if(tipoFactura==3)
						{ 
							listGlobalLocal=this.facHdrService.getTimbradoGlobalNotaCredito(fechaInicio,fechaFinal);
						}
						
						if(tipoFactura==4)
						{ 
							listGlobalLocal=this.facHdrService.getTimbradoGlobalDevolucionFDC(fechaInicio,fechaFinal);
						}
						
						logger.info(" Total de dias de reproceso "+listGlobalLocal.size());

						
						if (listGlobalLocal != null && listGlobalLocal.size() > 0) {
						    listGlobalTotal.addAll(listGlobalLocal);
					    }
					
				}
			
			}
		
		}
		return listGlobalTotal;
    }

    /**
     * Obtiene la descripcion del tipo de nomina
     * @param pTipoFactura
     * @return
     */
    private String getDescTipoFactura(int pTipoFactura)
    {
       String descripcion="";

		if(pTipoFactura==1)
		{ 
			descripcion="Facturación global Ventas";		
		}
		
		if(pTipoFactura==2)
		{ 		
			descripcion="Devolución global";		
		}

		if(pTipoFactura==3)
		{ 
			descripcion="Facturación global Nota Crédito";		
		}
		
		if(pTipoFactura==4)
		{ 
			descripcion="Facturación global Nota Crédito";		
		}

       return descripcion;
    }
    
    /**
     * Retorna el numero de trimestres a procesar pot tipo de nomina
     * @param pNumTrimestre Numero de trimestre actual
     * @return
     */
    private int getNumTrimetreProceso(int pTipoFactura)
    {
        Integer numTrimentreProceso = 0;
        
        if(pTipoFactura==1)
        {
          numTrimentreProceso = Integer.valueOf( this.env.getProperty("global.ventas.numtrimestre") );
        }
        
        if(pTipoFactura==2)
        {
          numTrimentreProceso = Integer.valueOf( this.env.getProperty("global.devolucion.numtrimestre") );
        }

        if(pTipoFactura==3)
        {
          numTrimentreProceso = Integer.valueOf( this.env.getProperty("global.notacredito.numtrimestre") );
        }
        
        if(pTipoFactura==4)
        {
          numTrimentreProceso = Integer.valueOf( this.env.getProperty("global.devolucion.fdc.numtrimestre") );
        }
        
    	return numTrimentreProceso;
    }
	
	private void timbradoGlobatoFile(List<FacturacionGlobalVentaModel> listGlobal, String fileNameOut) {
		
		if (listGlobal != null && listGlobal.size() > 0) {
			StringBuilder sbGlobal = new StringBuilder();
			for (FacturacionGlobalVentaModel global : listGlobal) {
				logger.info( global.toString() );
				
				sbGlobal.append( global.getTienda() ).append( SEPARADOR )
						.append( global.getFecha() ).append( SEPARADOR )
						.append( global.getTipoTimbrado() )
						.append(System.getProperty("line.separator"));
			}
			
			File file = new File(fileNameOut); 
			try {
				
				if (file.exists()) {
					file.delete();
					logger.info("El archivo ya existe, se elimina");
				}
				logger.info("Creación de archivo ["+file.getAbsolutePath()+"]");
				
				this.write(sbGlobal.toString(), file);
			} catch (IOException e) {
				logger.error("Error al crear el archivo: " + file.getAbsolutePath());
				e.printStackTrace();
				
			}
		}
	}
	
	private void write(String str, File fileName) throws IOException {
		FileUtils.writeStringToFile(fileName, str, "UTF-8");
	}

}
