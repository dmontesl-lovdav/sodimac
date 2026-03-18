package com.sodimac.wsconfiguracion.util.enums;

public enum ECodigo {
   Error(0) {
      public String message() {
         return "La solicitud es inválida u ocurrió un error";
      }
   },
   Ok(1) {
      public String message() {
         return "OK";
      }
   },
   AccesoDenegado(5) {
      public String message() {
         return "Acceso denegado, usuario/token no autorizado";
      }
   },
   DocumentoInvalido(105) {
      public String message() {
         return "Estructura de documento inválido";
      }
   },
   FechaInvalida(112) {
      public String message() {
         return "Fecha invalida";
      }
   },
   EmisorOTiendaNoEcontrado(113) {
      public String message() {
         return "No se encontro al Emisor o la tienda. Favor de contactar al administrador del Web Service. ";
      }
   },
   ConfiguracionVersionNoEncontrado(114) {
      public String message() {
         return "No existe configuración de Version para la aplicación solicitada. Favor de contactar al administrador del Web Service.";
      }
   },
   ConfiguracionPersonasNoEncontrada(115) {
      public String message() {
         return "No existe configuración de tipo de personas para el valor solicitado. Favor de contactar al administrador del Web Service.";
      }
   },
   CodigoPostalNoExiste(116) {
      public String message() {
         return "No existe el codigo postal solicitado. Favor de contactar al administrador del Web Service.";
      }
   },
   RazonSocialInvalida(117) {
      public String message() {
         return "La razón social contiene concatenado el regimen de capital";
      }
   },
   TipoDeComprobanteNoEncontrado(118) {
      public String message() {
         return "No se encontro el tipo de comprobante solicitado";
      }
   },
   ConfigMesNoEncontrado(119) {
      public String message() {
         return "No se encontro configuracion del mes con la clave proporcionada";
      }
   },
   PeriodicidadNoEncontrado(120) {
      public String message() {
         return "No se encontro periodicidad con la clave proporcionada";
      }
   },
   ConfDatsEmisorTiendaNoEncontrado(121) {
      public String message() {
         return "No existen datos del catalogo de ConfDatosEmisorTienda";
      }
   },
   ConfDatsEmisorTiendaNoGuardado(121) {
      public String message() {
         return "No fue posible guardar en el catalogo ConfDatosEmisorTienda";
      }
   },
   ConfDatsEmisorTiendaCreacion(122) {
      public String message() {
         return "No fue posible crear en el catalogo ConfDatosEmisorTienda";
      }
   };

   int valor;

   private ECodigo(int v) {
      this.valor = v;
   }

   public int getValor() {
      return this.valor;
   }

   public abstract String message();

   ECodigo(int var3, ECodigo var4) {
      this(var3);
   }
}
