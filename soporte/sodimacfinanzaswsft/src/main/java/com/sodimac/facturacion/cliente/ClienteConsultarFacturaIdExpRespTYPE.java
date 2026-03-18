package com.sodimac.facturacion.cliente;

public class ClienteConsultarFacturaIdExpRespTYPE {

    protected ClienteConsultarFacturaIdExpRespTYPE.Respuesta respuesta;
    protected String facturaId;
    protected String uuid;
    protected String xml;
    protected String mensaje;
    protected String estatus;

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

	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public String getEstatus() {
		return estatus;
	}

	public void setEstatus(String estatus) {
		this.estatus = estatus;
	}

	public ClienteConsultarFacturaIdExpRespTYPE.Respuesta getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(ClienteConsultarFacturaIdExpRespTYPE.Respuesta value) {
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
