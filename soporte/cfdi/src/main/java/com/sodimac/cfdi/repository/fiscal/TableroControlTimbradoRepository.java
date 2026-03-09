package com.sodimac.cfdi.repository.fiscal;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entity.fiscal.TableroControlTimbradoEntity;
import com.sodimac.cfdi.entity.fiscal.TableroControlTimbradoId;

@Repository
public interface TableroControlTimbradoRepository extends JpaRepository<TableroControlTimbradoEntity, TableroControlTimbradoId> {

	@Query("SELECT u FROM TableroControlTimbradoEntity u")
	public List<TableroControlTimbradoEntity> findTableroControlTimbradoCfdi();

	@Query(value = "{call uspObtenerTiendas()}", nativeQuery = true)
	List<Object[]> findTiendas();
	
	@Query(value = "{call uspObtenerCanales()}", nativeQuery = true)
	List<Object[]> findCanales();

	@Query(value = "{call uspObtenerTableroByParams (:fechaInicial, :fechaFinal, :start, :rowsPerPage, :ticket, :canal, :tienda )}", nativeQuery = true)
	List<Object[]> getTableroByParams(String fechaInicial, String fechaFinal, int start, int rowsPerPage, String ticket, String canal, String tienda);

	@Query(value = "{call uspObtenerTableroExcelByParams (:fechaInicial, :fechaFinal, :ticket, :canal, :tienda )}", nativeQuery = true)
	List<Object[]> getTableoExcelByParams(String fechaInicial, String fechaFinal, String ticket, String canal,String tienda);

	@Query(value = "{call uspObtenerDetalleExcelByParams (:fechaInicial, :fechaFinal, :ticket, :canal, :tienda )}", nativeQuery = true)
	public List<Object[]> getDetalleExcelByParams(String fechaInicial, String fechaFinal, String ticket, String canal,String tienda);
}
