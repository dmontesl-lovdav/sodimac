package com.sodimac.facturacion.clientews.wsft;

public class ClienteObtenerTicketRespTYPE {

	protected ClienteTicketTimbrarExpRespTYPE.Respuesta respuesta;

    public ClienteTicketTimbrarExpRespTYPE.Respuesta getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(ClienteTicketTimbrarExpRespTYPE.Respuesta value) {
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
