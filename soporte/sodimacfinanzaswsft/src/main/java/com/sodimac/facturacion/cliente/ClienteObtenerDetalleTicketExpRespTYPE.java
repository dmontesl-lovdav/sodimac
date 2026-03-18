package com.sodimac.facturacion.cliente;

import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2022.ClienteTicketObtenerExpRespTYPE;

public class ClienteObtenerDetalleTicketExpRespTYPE {

    protected ClienteObtenerDetalleTicketExpRespTYPE.Respuesta respuesta;
    protected ClienteTicketObtenerExpRespTYPE responseWSObtenerTicket;

	public ClienteObtenerDetalleTicketExpRespTYPE.Respuesta getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(ClienteObtenerDetalleTicketExpRespTYPE.Respuesta value) {
        this.respuesta = value;
    }

    public ClienteTicketObtenerExpRespTYPE getResponseWSObtenerTicket() {
		return responseWSObtenerTicket;
	}

	public void setResponseWSObtenerTicket(ClienteTicketObtenerExpRespTYPE responseWSObtenerTicket) {
		this.responseWSObtenerTicket = responseWSObtenerTicket;
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
