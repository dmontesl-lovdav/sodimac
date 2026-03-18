# AutoFacturador CFDI 4.0

_Proyecto Web de facturacion de clientes en tiendas Sodimac_

## Comenzando 🚀

_Estas instrucciones te permitirÃ¡n obtener una copia del proyecto en funcionamiento en tu mÃ¡quina local para propÃ³sitos de desarrollo y pruebas._

Mira **Deployment** para conocer como desplegar el proyecto.

### Pre-requisitos 📋

_Que cosas necesitas para instalar el software y como instalarlas_

```
1. JDK 1.8 jdk-8u231-windows-x64
2. Eclipse IDE Enterprise Developers 2019-12 Ã³ posterior
3. Base de Datos MariaDb mariadb-10.4.7-winx64
4. SoapUI y Postman
5. El jar de mariadb mariadb-java-client-2.4.3
6. Manejador de BaseDatos para mariaDB dbvis_windows-x64_10_0_25_jre
7. Servidor Apache Tomcat version 9.0.30
```

### Instalación 🔧

_Una serie de ejemplos paso a paso que te dice lo que debes ejecutar para tener un entorno de desarrollo ejecutandose_

#### Instalación MariaDb

```
1. Ejecuta e instala MariaDb mariadb-10.4.7-winx64 con root y el password que se le indique.
2. Ejecuta en windows MySql Client(MariaDB 10.4 (x64)) con root y password.
3. Ejecuta los comando para crear base de datos y el usuario:
	create database facturacion default character set utf8 default collate utf8_bin;
	GRANT ALL PRIVILEGES ON facturacion.* to facturaUser@'%' IDENTIFIED BY 'facturaUser';
	GRANT ALL PRIVILEGES ON facturacion.* to facturaUser@'localhost' IDENTIFIED BY 'facturaUser';
4. Inicia el dvvisualizer, Realiza la configuración de la conexión de la base de datos, para ello necesitas instalar el jar de mariadb mariadb-java-client-2.4.3 cuando se selecciona MariaDb como base de datos.
5. conectate a la base de datos de facturacion y ejecuta los scripts que vienen en este proyecto que están en la capeta src/main/java/webapp/resources/db en el orden indicado.
```

#### Instalación facturacion Web

```
1. Instala el JDK 1.8
2. Instala el Eclipse IDE
3. Abre el Eclipse con el workspace por default.
4. En una CMD clona el repositorio en el workspace.
5. Importa el proyecto tipo Maven en Eclipse y compila.
6. Instala un servidor Apache Tomcat 9 en Eclipse y añade el proyecto al servidor.
7. Inicia el servidor en modo Debug.
8. Navega a la url http://localhost:8080/facturacion.
```

_Finaliza con un ejemplo de cómo obtener datos del sistema o como usarlos para una pequeña demo_

## Despliegue 📦

_El despliegue se realiza como un WAR en el servidor productivo_

## Licencia 📄

Este proyecto está bajo la Licencia (Sodimac © Comercializadora SDMHC S.A. de C.V).

## Versionado 📌

Usamos [SemVer](http://semver.org/) para el versionado. Para todas las versiones disponibles, mira los [tags en este repositorio](https://gitlab.sodimac-it.com/desarrollo-sodimac-mexico/autofacturador/-/tags).

### v1.1.3
* Se integra validacion de monto y ticket en las pantallas de "Datos de su compra" y la generaciÃ³n de la factura en pantalla "Sus datos fiscales".
* El ticket se va 24 horas cuando el cliente mete un ticket invalido y la BCT no esta disponible cuando se factura en la pantalla "Sus datos fiscales". (el jar de refacturacion harÃ¡ el resto para volver a facturar)
* Si no coinciden ticket y monto al momento de generar factura se rechaza la facturacion.

