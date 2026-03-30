package com.sodimac.oc.batch.service.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sodimac.oc.batch.dto.detecno.DetecnoData;
import com.sodimac.oc.batch.entity.OrdenCompraProveedorEntity;
import com.sodimac.oc.batch.service.OrdenCompraService;
import com.sodimac.oc.batch.util.Conexion;

@Service
public class OrdenCompraServiceImpl implements OrdenCompraService {

	@Autowired
	private Conexion conexion;

	@Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
	private int batchSize;

	private Logger logger = LoggerFactory.getLogger(OrdenCompraServiceImpl.class);

	@Override
	public void saveOrdenesBatch(List<DetecnoData> data) {
		ejecutarBatch(ordenCompraDtoToEntity(data));
	}

	private void ejecutarBatch(List<OrdenCompraProveedorEntity> listData) {
		logger.info("Inserts using jdbc batch: {} ", Integer.valueOf(this.batchSize));
		Connection connection = null;
		try {
			String sql = String.format(
					"INSERT INTO %s (IdOrdenCompra,NumeroProveedor,OrdenCompra,Recepcion,Sucursal,NoGuia,ImporteSinImpuesto,FechaRecepcion,Estatus,Origen,FechaMovimiento,MotivoCancelacion,FechaRegistro) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
					new Object[] {
							((Table) OrdenCompraProveedorEntity.class.<Table>getAnnotation(Table.class)).name() });
			connection = this.conexion.getConexion();
			PreparedStatement statement = connection.prepareStatement(sql);
			int counter = 0;
			for (OrdenCompraProveedorEntity item : listData) {
				statement.clearParameters();
				statement.setInt(1, item.getId());
				statement.setInt(2, item.getNumeroProveedor());
				statement.setInt(3, item.getOrdenCompra());
				statement.setInt(4, item.getRecepcion());
				statement.setString(5, item.getSucursal());
				statement.setString(6, item.getNoGuia());
				statement.setDouble(7, item.getImporteSinImpuesto());
				statement.setString(8, item.getFechaRecepcion());
				statement.setString(9, item.getEstatus());
				statement.setString(10, item.getOrigen());
				statement.setString(11, item.getFechaMovimiento());
				statement.setString(12, item.getMotivoCancelacion());
				statement.setTimestamp(13, new java.sql.Timestamp(item.getFechaRegistro().getTime()));
				statement.addBatch();
				if ((counter + 1) % this.batchSize == 0 || counter + 1 == listData.size()) {
					statement.executeBatch();
					statement.clearBatch();
				}
				counter++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null)
				this.conexion.close(connection);
		}
	}
	
	private List<OrdenCompraProveedorEntity> ordenCompraDtoToEntity(List<DetecnoData> data) {
		return (List<OrdenCompraProveedorEntity>) data.parallelStream().map(item -> {
			OrdenCompraProveedorEntity entity = new OrdenCompraProveedorEntity();
			entity.setId(item.getId());
			entity.setNumeroProveedor(Integer.parseInt(item.getCodProvSAP()));
			entity.setOrdenCompra(Integer.parseInt(item.getNoOC()));
			entity.setRecepcion(Integer.parseInt(item.getNoRecepcion()));
			entity.setSucursal(item.getSucursal());
			entity.setNoGuia(item.getNoGuia());
			entity.setImporteSinImpuesto(item.getImporteSinImpuestos());
			entity.setFechaRecepcion(item.getFechaRecepcion());
			entity.setEstatus(item.getEstatus());
			entity.setOrigen(item.getOrigen());
			entity.setFechaMovimiento(item.getFechaMovimiento());
			entity.setMotivoCancelacion(item.getMotivoCancelacion());
			entity.setFechaRegistro(new Date());
			return entity;
		}).collect(Collectors.toList());
	}

	@Override
	public void ejecutaSP() {
		Connection connection = null;
		try {
			String sql = "{call uspRegistroOrdenCompraProveedor()}";
			connection = this.conexion.getConexion();
			CallableStatement statement = connection.prepareCall(sql);
			statement.execute();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null)
				this.conexion.close(connection);
		}
	}
}
