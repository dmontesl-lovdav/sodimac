
CREATE FUNCTION `uspExistingDispositivo`(
	`vidClave` varchar(50),
	`vMac` varchar(50),
	`vidEquipo` VARCHAR(50),
	`vDescripcion` varchar(50),
	`vidTipoSeguridad` int
) RETURNS varchar(3) CHARSET utf8mb4
    READS SQL DATA
BEGIN
/*Valida por idClaveEquipo*/
IF(EXISTS(SELECT * FROM catdispositivos 
             WHERE idclaveEquipo = vidClave)) then 
    RETURN "24"; 
   END IF; 
/*Valida por MAC*/
IF(EXISTS(SELECT * FROM catdispositivos 
             WHERE macAddress = vMac)) then
             
             IF vidTipoSeguridad = 2 THEN return "57"; END IF;
             
    RETURN "26"; 
   END IF; 
/*Valida por Id Equipo*/
IF(EXISTS(SELECT * FROM catdispositivos 
             WHERE idEquipo = vidEquipo)) then 
    RETURN "36"; 
   END IF; 
/*Valida por descripcion*/
IF(EXISTS(SELECT * FROM catdispositivos 
             WHERE descripcionDispositivo = vDescripcion)) then 
    RETURN "25"; 
   END IF; 
RETURN "1";
END;