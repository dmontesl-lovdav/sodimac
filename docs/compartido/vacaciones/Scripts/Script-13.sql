SELECT id, version, descripcion, activo, creationDate, updateDate
FROM configuracion.catversiontimbrado;

show create procedure configuracion.uspObtenerUsoCfdi;

select *
from configuracion.catusoscfdi;


select usuario, contrasena, rol from usuariosws where activo = 1;

select *
from pacs;

SELECT id, rfc, razonSocial, activo FROM confdatosemisor;

-- CSD161207R2A
-- COMERCIALIZADORA SDMHC



select version(), database(), current_user();

SHOW CREATE PROCEDURE uspGetTokenMultiple;

SELECT ROUTINE_SCHEMA, ROUTINE_NAME, ROUTINE_TYPE FROM information_schema.ROUTINES WHERE ROUTINE_NAME LIKE '%TokenMultiple%';

SHOW CREATE PROCEDURE configuracion.uspGetTokenMultiple;

SELECT DEFINER, SECURITY_TYPE FROM information_schema.ROUTINES WHERE ROUTINE_SCHEMA='configuracion' AND ROUTINE_NAME='uspGetTokenMultiple';


select * from catconfiguracion  -- SET valor='/opt/tomcat/conf/crypto.properties'
WHERE nombreCampo='ENCRYPT_PATH';


SHOW CREATE PROCEDURE uspObtenerUsoCfdi;

CALL uspObtenerUsoCfdi(2, 1, '612');