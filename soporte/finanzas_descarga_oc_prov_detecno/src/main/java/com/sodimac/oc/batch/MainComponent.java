package com.sodimac.oc.batch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sodimac.oc.batch.client.DetecnoClient;
import com.sodimac.oc.batch.dto.detecno.DetecnoResponse;
import com.sodimac.oc.batch.service.OrdenCompraService;

@Component
public class MainComponent {

	@Autowired
	DetecnoClient detecnoClient;

	@Autowired
	OrdenCompraService ordenCompraService;

	// Modo backfill manual: rango fijo configurado. Tiene prioridad sobre los demas.
	@Value("${descarga.periodo.enabled:false}")
	private boolean periodoEnabled;

	@Value("${descarga.periodo.fechaInicio:}")
	private String periodoFechaInicio;

	@Value("${descarga.periodo.fechaFin:}")
	private String periodoFechaFin;

	// Modo diario: ventana movil de N dias hacia atras, recalculada en cada corrida.
	@Value("${descarga.ultimos-dias.enabled:false}")
	private boolean ultimosDiasEnabled;

	@Value("${descarga.ultimos-dias.cantidad:30}")
	private int ultimosDiasCantidad;

	@Value("${descarga.periodo.dias-por-bloque:7}")
	private int diasPorBloque;

	private final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");

	private Logger logger = LoggerFactory.getLogger(MainComponent.class);

	public void mainMethod() {
		// Precedencia: periodo (backfill manual) > ultimos-dias (diario) > original.
		if (periodoEnabled) {
			mainMethodPorPeriodos();
			return;
		}
		if (ultimosDiasEnabled) {
			mainMethodUltimosDias();
			return;
		}

		logger.info("Inicia obtencion de ordenes de compra desde Detecno");

		DetecnoResponse ordenesCompra = detecnoClient.getOrdenesCompra();

		logger.info("Se obtuvieron {} ordenes", ordenesCompra.getTotalCount());

		logger.info("Inicia guardado de ordenes de compra en SODIMAC_SAP_PROD");

		ordenCompraService.saveOrdenesBatch(ordenesCompra.getData());

		logger.info("Inicia ejecución de SP [uspRegistroOrdenCompraProveedor]");

		ordenCompraService.ejecutaSP();

		logger.info("Termina proceso correctamente");
	}

	// Backfill manual: descarga el rango fijo configurado en application.properties.
	private void mainMethodPorPeriodos() {
		try {
			Date inicio = format.parse(periodoFechaInicio);
			Date fin = format.parse(periodoFechaFin);

			if (inicio.after(fin)) {
				throw new RuntimeException("descarga.periodo.fechaInicio es posterior a fechaFin");
			}

			procesarRango(inicio, fin, "Descarga por periodos");
		} catch (ParseException e) {
			throw new RuntimeException("Formato de fecha invalido en descarga.periodo (esperado yyyy/MM/dd)", e);
		}
	}

	// Diario: ventana movil de los ultimos N dias (hoy - (N-1) .. hoy), recalculada cada corrida.
	// La re-descarga del solapamiento es inofensiva: el indice unico rechaza lo ya cargado.
	// Se auto-cura: si un dia fallo, la corrida siguiente lo vuelve a cubrir.
	private void mainMethodUltimosDias() {
		Calendar cal = Calendar.getInstance();
		Date fin = cal.getTime();
		cal.add(Calendar.DAY_OF_YEAR, -(ultimosDiasCantidad - 1));
		Date inicio = cal.getTime();

		procesarRango(inicio, fin, "Descarga ultimos " + ultimosDiasCantidad + " dias");
	}

	// Loop comun: parte [inicio, fin] en bloques de N dias. Cada bloque descarga e inserta en temp
	// (resiliente: si uno falla, se registra y sigue). El SP corre UNA sola vez al final.
	private void procesarRango(Date inicio, Date fin, String etiqueta) {
		logger.info("{}: {} al {} en bloques de {} dias", etiqueta, format.format(inicio), format.format(fin), diasPorBloque);

		Calendar bloqueIni = Calendar.getInstance();
		bloqueIni.setTime(inicio);

		int bloque = 0;
		int bloquesOk = 0;
		long totalOrdenes = 0;
		List<String> bloquesFallidos = new ArrayList<>();

		while (!bloqueIni.getTime().after(fin)) {
			Calendar bloqueFin = (Calendar) bloqueIni.clone();
			bloqueFin.add(Calendar.DAY_OF_YEAR, diasPorBloque - 1);
			if (bloqueFin.getTime().after(fin)) {
				bloqueFin.setTime(fin);
			}

			String fi = format.format(bloqueIni.getTime());
			String ff = format.format(bloqueFin.getTime());

			bloque++;

			try {
				logger.info("Bloque {}: descargando {} al {}", bloque, fi, ff);

				DetecnoResponse ordenes = detecnoClient.getOrdenesCompra(fi, ff);
				logger.info("Bloque {}: se obtuvieron {} ordenes", bloque, ordenes.getTotalCount());

				ordenCompraService.saveOrdenesBatch(ordenes.getData());

				totalOrdenes += ordenes.getData().size();
				bloquesOk++;
			} catch (Exception e) {
				bloquesFallidos.add(fi + " al " + ff);
				logger.error("Bloque {} FALLO ({} al {}): {}. Se continua con el siguiente.", bloque, fi, ff, e.getMessage(), e);
			}

			bloqueIni.setTime(bloqueFin.getTime());
			bloqueIni.add(Calendar.DAY_OF_YEAR, 1);
		}

		logger.info("Descarga terminada. {} bloques OK de {}, {} ordenes insertadas en temp.", bloquesOk, bloque, totalOrdenes);
		if (!bloquesFallidos.isEmpty()) {
			logger.warn("Bloques FALLIDOS ({}): {}. Re-ejecutar solo esos rangos.", bloquesFallidos.size(), bloquesFallidos);
		}

		// SP una sola vez sobre toda la temp ya cargada.
		if (totalOrdenes > 0) {
			logger.info("Inicia ejecucion del SP [uspRegistroOrdenCompraProveedor] sobre {} ordenes", totalOrdenes);
			ordenCompraService.ejecutaSP();
			logger.info("SP finalizado.");
		} else {
			logger.warn("No se inserto ninguna orden, se omite la ejecucion del SP.");
		}
	}
}
