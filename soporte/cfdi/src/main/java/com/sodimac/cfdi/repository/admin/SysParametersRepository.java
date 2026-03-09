package com.sodimac.cfdi.repository.admin;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entity.admin.SysParameterEntity;

@Repository("sysParametersRepository")
public interface SysParametersRepository extends JpaRepository<SysParameterEntity, String> {

	@Query(value = "select distinct catc.nombreCampo, catc.valor, catc.aplicacion, catc.descripcion, catc.fechaCreacion, catc.idTipoDato, catc.activo, catc.valorInactivo, "
			+ "case \r\n"
			+ "  when catc.idTipoDato = 1 then 'Aplicacion'\r\n"
			+ "  when catc.idTipoDato = 2 then 'Batch'\r\n"
			+ "  when catc.idTipoDato = 3 then 'Web Service'\r\n"
			+ "  when catc.idTipoDato = 4 then 'ETL'\r\n"
			+ "  when catc.idTipoDato = 5 then 'Procedimiento'   \r\n"
			+ "end as descTipoDato, "
			+ "ifnull((select max(fecha) from sodimacfiscal.hist_parametros where parametro = catc.NombreCampo),catc.fechaCreacion) ultimamodificacion, "
			+ "(select usuario from sodimacfiscal.hist_parametros where parametro = catc.NombreCampo and fecha = (select max(fecha) from sodimacfiscal.hist_parametros where parametro = catc.NombreCampo)) usuarioultimamodificacion"
			+ " from catconfiguracion catc, userparams up"
			+ " where ((up.parametro = catc.nombrecampo and up.idusuario = :idusuario) or :idusuario is null) and nombrecampo like concat('%', :nombre, '%')", nativeQuery = true)
	public List<Object[]> findParameters(@Param("nombre") String nombre, @Param("idusuario")Integer idusuario);

	@Query(value = "select id, nombre from catrol", nativeQuery = true)
	public List<Object[]> getAllRoles();

	@Query(value = "select id, nombre from cataplicaciones", nativeQuery = true)
	public List<Object[]> getAllAplicaciones();

}
