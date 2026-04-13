package com.sodimac.rebates.model.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
*
* @author david.montes
*/
@Entity
@Table(name = "RebatesCicmxOC")
public class RebatesCicmxOcEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@Column(name = "COMPRADOR")
	private String comprador;
	
	@Column(name = "NUM_PROVEEDOR")
	private Integer numProveedor;
	
	@Column(name = "NOM_PROVEEDOR")
	private String nomProveedor;
	
	@Column(name = "NUM_OC")
	private Integer numOc;
	
	@Column(name = "TIPO_OC")
	private String tipoOc;
	
	@Column(name = "METODO_DISTRIBUCION")
	private String metodoDistribucion;
	
	@Column(name = "NUM_TIENDA")
	private Double numTienda;
	
	@Column(name = "NOM_TDA")
	private String nomTda;
	
	@Column(name = "NUM_BODEGA")
	private Double numBodega;
	
	@Column(name = "NOM_BODEGA")
	private String nomBodega;
	
	@Column(name = "FEC_CREA")
	@Temporal(TemporalType.TIMESTAMP)
	private Date fecCrea;
	
	@Column(name = "FEC_LIB")
	@Temporal(TemporalType.TIMESTAMP)
	private Date fecLib;
	
	@Column(name = "FEC_RECEPCION")
	@Temporal(TemporalType.TIMESTAMP)
	private Date fecRecepcion;
	
	@Column(name = "FEC_CANCELACION")
	@Temporal(TemporalType.TIMESTAMP)
	private Date fecCancelacion;
	
	@Column(name = "ESTADO_OC")
	private String estadoOc;
	
	@Column(name = "SKU")
	private String sku;
	
	@Column(name = "SKU_DESCRIPCION")
	private String skuDescripcion;
	
	@Column(name = "CLACOM")
	private String clacom;
	
	@Column(name = "CANT_OC")
	private Double cant_oc;
	
	@Column(name = "CANT_CASE_PACK")
	private Double cantCasePack;
	
	@Column(name = "CANT_RECIBIDA")
	private Double cantRecibida;
	
	@Column(name = "CANT_CANCELADA")
	private Double cantCancelada;
	
	@Column(name = "LT_ENVIO")
	private Double ltEnvio;
	
	@Column(name = "LT_PROCESO")
	private Double ltProceso;
	
	@Column(name = "COSTO_UNIT")
	private Double costoUnit;
	
	@Column(name = "COSTO_OC")
	private String costoOc;
	
	@Column(name = "FEC_LIB_INICIAL")
	@Temporal(TemporalType.TIMESTAMP)
	private Date fecLibInicial;
	
	@Column(name = "FEC_RECEPCION_INICIAL")
	@Temporal(TemporalType.TIMESTAMP)
	private Date fecRecepcionInicial;
	
	@Column(name = "FECHA_ULTIMA_RECEPCION")
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaUltimaRecepcion;
	
	@Column(name = "FECHA_CARGA")
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaCarga;
	
	
	
}
