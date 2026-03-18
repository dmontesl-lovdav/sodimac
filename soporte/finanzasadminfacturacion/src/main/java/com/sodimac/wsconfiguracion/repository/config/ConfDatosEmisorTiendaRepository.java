package com.sodimac.wsconfiguracion.repository.config;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sodimac.wsconfiguracion.dto.ConfDatosEmisorTiendaDtoVM;
import com.sodimac.wsconfiguracion.dto.SerieSummaryDTO;
import com.sodimac.wsconfiguracion.entity.config.ConfDatosEmisorTiendaEntity;

@Repository("ConfDatosEmisorTiendaRepositoryConfig")
public interface ConfDatosEmisorTiendaRepository extends JpaRepository<ConfDatosEmisorTiendaEntity, Integer> {

	ConfDatosEmisorTiendaEntity findByIdTienda(Integer idTienda);
	
	@Query("SELECT new com.sodimac.wsconfiguracion.dto.SerieSummaryDTO(s.serie, s.id, cdet.id , cdet.idTienda, ccs.tipocomprobante) "
			+ " FROM CatSerieEntity s, CatTipoComprobanteSodimacEntity ccs, ConfDatosEmisorTiendaEntity cdet "
			+ " WHERE s.catTipoComprobanteSodimacEntity.id = ccs.id"
			+ " and ccs.tipocomprobante = :tipocomprobante "
			+ " and s.catTipoTiendaEntity.id = cdet.catTipoTiendaEntity.id"
			+ " and cdet.idTienda = :idTienda "
			)
    SerieSummaryDTO findConfigSerieAndTienda(String tipocomprobante, int idTienda);
	
	@Query("SELECT new com.sodimac.wsconfiguracion.dto.ConfDatosEmisorTiendaDtoVM(c.id, c.idConfDatosEmisor, e.rfc,  c.idTienda, c.descripcion, c.calle ,c.noExterior, c.noInterior, c.colonia, c.localidad  ,c.referencia, c.municipio, c.estado, cp.id ,t.id, t.tipotienda, c.activo ,c.fechaInicio)  "
			+ " FROM ConfDatosEmisorTiendaEntity c, ConfDatosEmisorEntity e, CatCodigoPostalEntity cp, CatTipoTiendaEntity t "
			+ " WHERE 1 = 1  "
			+ " and c.idConfDatosEmisor = e.id  "
			+ " and c.catCodigoPostalEntity.id = cp.id "
			+ " and c.catTipoTiendaEntity.id = t.id")
	List<ConfDatosEmisorTiendaDtoVM> findAllVM();
}


//Select s.serie, s.idcattipotienda, cdet.idtienda, ccs.tipocomprobante
//from catserie s, cattipocomprobantesodimac ccs, confdatosemisortienda cdet
//where s.idcattipocomprobantesodimac = ccs.id
//and ccs.tipocomprobante = 'FD'
//and  s.idcattipotienda = cdet.idcattipotienda
//and cdet.idtienda = 1010