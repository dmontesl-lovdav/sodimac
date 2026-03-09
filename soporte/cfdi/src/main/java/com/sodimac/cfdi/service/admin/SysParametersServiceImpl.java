package com.sodimac.cfdi.service.admin;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.cfdi.dto.DtoUser;
import com.sodimac.cfdi.entity.admin.UserParamEntity;
import com.sodimac.cfdi.entity.admin.SysParameterEntity;
import com.sodimac.cfdi.model.admin.PrivilegiosUsuario;
import com.sodimac.cfdi.model.admin.SysParameter;
import com.sodimac.cfdi.repository.admin.ParametrosROLRepository;
import com.sodimac.cfdi.repository.admin.PrivilegiosROLRepository;
import com.sodimac.cfdi.repository.admin.SysParametersRepository;
import com.sodimac.cfdi.service.admin.HistorialParametroService.TipoAccion;

/**
 * Servicio para el modulo Administración de Parametros
 * 
 */
@Service
@Transactional
public class SysParametersServiceImpl implements SysParametersService {

	int rowIndex = 1;

	@Autowired
	private SysParametersRepository sysParametersRepository;

	@Autowired
	private PrivilegiosROLRepository privilegiosROLRepository;

	@Autowired
	private ParametrosROLRepository parametrosROLRepository;

	@Autowired
	private HistorialParametroService historialService;

	public static final int SUPERVISOR_PAGOS = 9;
	
	/**
	 * Obtiene la lista de parametros
	 * 
	 * @param nombre: El nombre de el o los parametros a buscar. Puede ser una parte del nombre ya que la busqueda en la base de datos se hace con
	 *                un like. Si es nulo trae todos los parametros.
	 */
	@Override public List<SysParameter> findParameters(String nombre, Integer idusuario, Integer idrol) {

		List<SysParameter> parameterList = new ArrayList<SysParameter>();
		
		// Si el usuario es administrador se traen todos los parametros
		PrivilegiosUsuario privs = getPrivilegiosUsuario(idusuario);
		if(privs.getAdmin() == 1) {
			idusuario = null;
		}

		sysParametersRepository.findParameters(nombre, idusuario).forEach(parameter -> {
			SysParameter param = new SysParameter();
			param.setNombreCampo(parameter[0].toString());
			param.setValor(parameter[1].toString());
			param.setAplicacion(parameter[2].toString());
			param.setDescripcion(parameter[3].toString());
			param.setFechaCreacion(parameter[4].toString());
			param.setIdTipoDato(parameter[5].toString());
			param.setActivo((Boolean) parameter[6] ? 1 : 0);
			param.setValorInactivo(parameter[7].toString());
			param.setDescTipoDato(parameter[8].toString());
			param.setFechaModificacion(parameter[9].toString());
			param.setUsuarioUltimaModificacion(parameter[10] == null ? "" : parameter[10].toString());
			parameterList.add(param);

		});
		
		if (idrol == SUPERVISOR_PAGOS) {
			return parameterList.stream().filter(p -> p.getAplicacion().equals("Egresos")).collect(Collectors.toList());
		}
		return parameterList;
	}

	/**
	 * Guarda un parametro en la base de datos.
	 * 
	 * @param parametro: El parametro a crear. Si es nuevo se crea y si ya existe se actualiza.
	 * @param user:      El usuario conectado a la sesión. Usado para registrar en bitacora el movimiento del parametro (creacion/actualizacion)
	 * @throws ParseException
	 */
	@Override public void guardarParametro(SysParameter parametro, DtoUser user) throws Exception {

		SysParameterEntity paramEntity = new SysParameterEntity();

		try {

			if (parametro.getAccion().equals("newParameter")) {

				Date fechaCreacion = new Date();
				paramEntity.setFechaCreacion(fechaCreacion);
				paramEntity.setIdGrupoUsuario("1");
				historialService.registrarAccion(user.getUsuario(), parametro.getNombreCampo(), fechaCreacion, TipoAccion.CREATE);
				UserParamEntity prEntity = new UserParamEntity();
				prEntity.setParametro(parametro.getNombreCampo());
				prEntity.setIdUsuario(user.getIdUsuario());
				parametrosROLRepository.save(prEntity);

			} else {

				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

				paramEntity.setFechaCreacion(dateFormat.parse(parametro.getFechaCreacion()));
				historialService.registrarAccion(user.getUsuario(), parametro.getNombreCampo(), new Date(), TipoAccion.UPDATE);
				
				paramEntity = sysParametersRepository.findById(parametro.getNombreCampo()).get();

			}

			paramEntity.setActivo(parametro.getActivo() == null ? 1 : parametro.getActivo());
			paramEntity.setAplicacion(parametro.getAplicacion());
			paramEntity.setDescripcion(parametro.getDescripcion());

			paramEntity.setIdTipoDato(parametro.getIdTipoDato());
			paramEntity.setNombreCampo(parametro.getNombreCampo());
			paramEntity.setValor(parametro.getValor());
			paramEntity.setValorInactivo(parametro.getValorInactivo());

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		sysParametersRepository.save(paramEntity);
	}

	/**
	 * Obtiene la lista de roles desde la tabla catroles
	 */
	@Override public List<Object[]> getAllRoles() {
		return sysParametersRepository.getAllRoles();
	}

	/**
	 * Obtiene la lista de aplicaciones desde la tabla cataplicaciones
	 */
	@Override public List<Object[]> getAllAplicaciones() {
		return sysParametersRepository.getAllAplicaciones();
	}

	/**
	 * Devuelve un Excel con la lista de parametros que coincidan con el parametro de búsqueda.
	 * 
	 * @param busqueda: El criterio de busqueda para los parametros que se van a incluir en el archivo xls.
	 * @param idrol:    El rol del usuario para filtrar los registros
	 *
	 */
	@Override public ByteArrayOutputStream getExcel(String busqueda, int idusuario, int idrol) {

		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Parametros");

		Row header = sheet.createRow(0);

		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		XSSFFont font = ((XSSFWorkbook) workbook).createFont();
		font.setFontName("Arial");
		font.setFontHeightInPoints((short) 16);
		font.setBold(true);
		font.setColor(IndexedColors.WHITE.getIndex());
		headerStyle.setFont(font);

		String[] columns = { "Nombre Parametro", "Valor", "Aplicacion", "Descripcion", "Fecha Creacion", "Fecha Modificacion", "Usuario Modificacion", "Tipo Parametro", "Estado", "Valor Inactivo" };

		for (int i = 0; i < columns.length; i++) {
			Cell headerCell = header.createCell(i);
			headerCell.setCellValue(columns[i]);
			headerCell.setCellStyle(headerStyle);
		}

		CellStyle style = workbook.createCellStyle();
		style.setWrapText(true);

		CellStyle styleCenter = workbook.createCellStyle();
		styleCenter.setWrapText(true);
		styleCenter.setAlignment(HorizontalAlignment.CENTER);

		rowIndex = 1;

		findParameters(busqueda, idusuario, idrol).forEach(parametro -> {

			Row row = sheet.createRow(rowIndex);

			Cell cell = row.createCell(0);
			cell.setCellValue(parametro.getNombreCampo()); // Nombre Parametro
			cell.setCellStyle(style);

			cell = row.createCell(1);
			cell.setCellValue(parametro.getValor()); // Valor
			cell.setCellStyle(style);

			cell = row.createCell(2);
			cell.setCellValue(parametro.getAplicacion()); // Aplicacion
			cell.setCellStyle(style);

			cell = row.createCell(3);
			cell.setCellValue(parametro.getDescripcion()); // Descripcion
			cell.setCellStyle(style);

			CreationHelper createHelper = workbook.getCreationHelper();
			CellStyle dateCellStyle = workbook.createCellStyle();
			dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy"));

			cell = row.createCell(4);
			cell.setCellValue(parametro.getFechaCreacion()); // Fecha creacion
			cell.setCellStyle(dateCellStyle);

			cell = row.createCell(5);
			cell.setCellValue(parametro.getFechaModificacion()); // Fecha modificacion
			cell.setCellStyle(dateCellStyle);

			cell = row.createCell(6);
			cell.setCellValue(parametro.getUsuarioUltimaModificacion()); // Usuario
			cell.setCellStyle(styleCenter);

			cell = row.createCell(7);
			cell.setCellValue(parametro.getDescTipoDato()); // Tipo parametro
			cell.setCellStyle(styleCenter);

			cell = row.createCell(8);
			cell.setCellValue(parametro.getActivo() == 1 ? "Activo" : "Inactivo"); // Estado
			cell.setCellStyle(styleCenter);

			cell = row.createCell(9);
			cell.setCellValue(parametro.getValorInactivo()); // ValorInactivo
			cell.setCellStyle(style);

			for (int i = 0; i < row.getLastCellNum(); i++) {
				sheet.autoSizeColumn(i);
			}

			rowIndex++;

		});

		ByteArrayOutputStream outputStream = null;

		try {
			outputStream = new ByteArrayOutputStream();
			workbook.write(outputStream);
			workbook.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return outputStream;
	}

	@Override public PrivilegiosUsuario getPrivilegiosUsuario(int idusuario) {
		PrivilegiosUsuario privilegios = new PrivilegiosUsuario();
		privilegiosROLRepository.getPrivilegiosUsuario(idusuario).forEach(priv -> {
			privilegios.setIdrol((int) priv[0]);
			privilegios.setC((int) priv[1]);
			privilegios.setR((int) priv[2]);
			privilegios.setU((int) priv[3]);
			privilegios.setD((int) priv[4]);
			privilegios.setAdmin((int) priv[5]);
		});
		return privilegios;
	}

}
