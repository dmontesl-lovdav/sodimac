package com.sodimac.cfdi.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.cfdi.entity.fiscal.ProcesoDescargaEntity;
import com.sodimac.cfdi.models.ProcesoDescargaModel;
import com.sodimac.cfdi.repository.fiscal.ProcesoDescargaRepository;

@Service
public class DescargaServiceImpl implements DescargaService {

	@Autowired
	ProcesoDescargaRepository procesoDescargaRepository;

	private final static SimpleDateFormat MI_FORMATO = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

	@Override
	public List<ProcesoDescargaModel> obtenerDescargaByParams(String fechaInicial, String fechaFinal, int start, int rowsPerPage, String idEjecucion, String estatus) {
		List<ProcesoDescargaModel> list = new ArrayList<ProcesoDescargaModel>();
		procesoDescargaRepository.getDescargaByParams(fechaInicial, fechaFinal, start, rowsPerPage, idEjecucion, estatus)
				.forEach(item -> {
					ProcesoDescargaModel model = new ProcesoDescargaModel();
					model.setIdEjecucion((String) item[0]);
					Date fechaSolicitudTemp = (Date) item[1];
					model.setFechaSolicitud(MI_FORMATO.format(fechaSolicitudTemp));
					Date fechaGeneracionTemp = item[2] == null ? null : (Date) item[2];
					model.setFechaGeneracion(fechaGeneracionTemp == null ? "" : MI_FORMATO.format(fechaGeneracionTemp));
					model.setParametros((String) item[3]);
					model.setModulo((String) item[4]);
					model.setUsuario((String) item[5]);
					model.setEstatus((Integer) item[6]);
					model.setMensaje((String) item[7]);

					list.add(model);
				});

		return list;
	}

	@Override
	public String registrarProceso(String parametros, String modulo, String usuario) {
		long ahora = Calendar.getInstance().getTimeInMillis();
		Date dateAhora = new Date(ahora);

		ProcesoDescargaEntity entity = new ProcesoDescargaEntity();
		entity.setIdEjecucion(Long.toString(ahora));
		entity.setFechaSolicitud(dateAhora);
		entity.setParametros(parametros);
		entity.setModulo(modulo);
		entity.setUsuario(usuario);
		entity.setEstatus(0); // Procesando
		entity.setMensaje("Procesando Reporte");

		procesoDescargaRepository.save(entity);

		return Long.toString(ahora);
	}

	@Override
	public void updateStatusProceso(String idEjecucion, int estatus, String mensaje, String archivos) throws Exception {
		Optional<ProcesoDescargaEntity> optionalEntity = procesoDescargaRepository.findById(idEjecucion);
		if (optionalEntity.isPresent()) {
			ProcesoDescargaEntity entity = optionalEntity.get();
			entity.setFechaGeneracion(new Date());
			entity.setEstatus(estatus);
			entity.setMensaje(mensaje);
			if (archivos != null) {
				entity.setListaArchivos(archivos);
			}
			procesoDescargaRepository.save(entity);
		} else {
			throw new Exception("No se encontro el Id de Ejecucion");
		}
	}

	@Override
	public String obtenerNombreArchivosByIdEjecucion(String idEjecucion) throws Exception {
		Optional<ProcesoDescargaEntity> optionalEntity = procesoDescargaRepository.findById(idEjecucion);
		if (optionalEntity.isPresent()) {
			ProcesoDescargaEntity entity = optionalEntity.get();

			return entity.getListaArchivos();
		} else {
			throw new Exception("No se encontro el Id de Ejecucion");
		}
	}

	@Override
	public List<String> obtenerNombreArchivoForSchedule() {
		List<String> list = new ArrayList<String>();
		
		procesoDescargaRepository.getProcesosDescargaSchedule().forEach(item -> {
			String archivos = item[8] == null ? "" : (String) item[8];
			list.add(archivos);
		});
		
		return list;
	}

	@Override
	public void updateStatusProcesoSchedule() {
		procesoDescargaRepository.updateProcesosDescargaSchedule();
	}

}
