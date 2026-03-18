package com.sodimac.facturacion.cliente;

public class ClienteTimbrarTipoExpRespTYPE {

    protected ClienteTimbrarTipoExpRespTYPE.Respuesta respuesta;
    protected String facturaId;
    protected String uuid;

    public String getFacturaId() {
		return facturaId;
	}

	public void setFacturaId(String facturaId) {
		this.facturaId = facturaId;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public ClienteTimbrarTipoExpRespTYPE.Respuesta getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(ClienteTimbrarTipoExpRespTYPE.Respuesta value) {
        this.respuesta = value;
    }

    public static class Respuesta {

        protected String codigo;
        protected String descripcion;

        public String getCodigo() {
            return codigo;
        }

        public void setCodigo(String value) {
            this.codigo = value;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String value) {
            this.descripcion = value;
        }

    }

}
