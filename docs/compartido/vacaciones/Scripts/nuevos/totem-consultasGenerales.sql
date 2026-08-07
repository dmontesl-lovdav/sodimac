CREATE TABLE cattiposeguridad (
  idTipoSeguridad int NOT NULL,
  clave  varchar(50) NOT null,
  descripcion varchar(50) NOT NULL,
  tipoSeguridadActivo bit(1) NOT NULL,
  PRIMARY KEY (idTipoSeguridad)
);

insert into cattiposeguridad values (1,'MAC_ADDRESS','Mac Address',1);
insert into cattiposeguridad values (2,'ANDROID_ID','Android Id',1);

select * from cattiposeguridad;

alter table catdispositivos add idTipoSeguridad int;


ALTER TABLE catdispositivos ADD FOREIGN KEY (idTipoSeguridad) REFERENCES cattiposeguridad(idTipoSeguridad); 
 
 update catdispositivos set idTipoSeguridad = 1;
 -- update catmensajes set descripcionMensaje = 'Ya existe un registro  activo con este Número de acceso' where codMensaje = '26';
 
 insert into catmensajes values (57,'57','Ya existe un registro activo con este Número de acceso',3,1);
 
select * from catdispositivos c where c.idTipoSeguridad = 2;


select * -- 36
from totem.usuarios
where primerNombre = 'Joaquin'; -- PERMISOS [jtmartinezn@sodimac.com.mx]

select * -- 36
from totem.usuarios
where usuario = 'jtmartinezn@sodimac.com.mx';

select * -- 106
from totem.usuarios
where usuario = 'atorresga@sodimac.com.mx'; -- GENERICO

select * -- 106
from totem.usuarios
where usuario = 'cevazquezf@sodimac.com.mx';

update totem.usuarios
set activo = 0
  , usuario = 'cevazquezf_error@sodimac.com.mx'
where idUsuario = 95;

delete from usuarios 
where idUsuario = 95;


update totem.usuarios
   set password = 'MDAwMDAwMDAwMDAwMDAwMPAPJ0FFs5Jhh5e9qyrjtPA=' -- REAL
 -- set password = 'MDAwMDAwMDAwMDAwMDAwMAu97NPl90MUZz+RmuUOeFo=' -- COMUN
where idUsuario = 36; -- Joaquin

update totem.usuarios
--   set password = 'MDAwMDAwMDAwMDAwMDAwMHh7+XeMhJ8ZUW1pH+R1aio=' -- REAL
 set password = 'MDAwMDAwMDAwMDAwMDAwMAu97NPl90MUZz+RmuUOeFo=' -- COMUN
where idUsuario = 41; -- César Vazquez | cevazquezf@sodimac.com.mx

select * from catdispositivos c where macAddress not like '%-%'
order by c.macAddress 

update catdispositivos 
set idTipoSeguridad = 2
where macAddress not like '%-%'

SELECT totem.uspExistingDispositivo('123456', '12345', '12345', '12345', 1);




select * from catdispositivos c where macAddress = '50-C2-E8-5F-8D-5F'

UPDATE catdispositivos 
SET macAddress = '50-C2-E8-5F-8D-5F'
where macAddress = '50-C2-E8-5F-8D-5X';


select * -- 36
from totem.usuarios
where usuario = 'molguinm@sod.com.mx';

select *
from totem.usuarios
where primerNombre  like 'Maria%';

delete from totem.usuarios
where idUsuario = 143;