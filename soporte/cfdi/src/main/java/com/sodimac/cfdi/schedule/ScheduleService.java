package com.sodimac.cfdi.schedule;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sodimac.cfdi.service.DescargaService;
import com.sodimac.cfdi.util.UtilsFile;

@Component
public class ScheduleService {
	
	@Autowired
	private DescargaService descargaService;
	
	@Scheduled(cron = "0 0 */8 * * *")
	public void updateEstatusDescarga() {
		
		System.out.println("Inicia UpdateEstatusProcesoDescarga");
		System.out.println(new Date());
		
	    // Obtener archivos
		List<String> nombresArchivos = descargaService.obtenerNombreArchivoForSchedule();
		if (!nombresArchivos.isEmpty()) {
			for (String nombreArchivos : nombresArchivos) {
				String[] listArchivos = null;
				listArchivos = nombreArchivos.split(",");
				
				if (listArchivos != null) {
					for(String archivo : listArchivos) {
						UtilsFile.EliminarArchivo(archivo);
					}	
				} else {
					System.out.println("No contiene archivos a eliminar");
				}
			}
			
			descargaService.updateStatusProcesoSchedule();
		} else {
			System.out.println("No hay procesos por actualizar");	
		}
		
	    System.out.println("Termina UpdateEstatusProcesoDescarga");
	}
}
