update totem.usuarios
 --   set password = 'MDAwMDAwMDAwMDAwMDAwMPAPJ0FFs5Jhh5e9qyrjtPA=' -- REAL
   set password = 'MDAwMDAwMDAwMDAwMDAwMAu97NPl90MUZz+RmuUOeFo=' -- COMUN [BQu4JomESrt]
where idUsuario = 36; -- Joaquin


select *
from usuarios;

-- 9	iscortesz@sodimac.com.mx	MDAwMDAwMDAwMDAwMDAwMDC+qSILR/rE7kXqSIYduQU=
-- MDAwMDAwMDAwMDAwMDAwMFOmAXWcqcfCXktYmL6UEdI=

update totem.usuarios
set password = 'MDAwMDAwMDAwMDAwMDAwMFOmAXWcqcfCXktYmL6UEdI='
where idUsuario = 9;

select idusuario,usuario,password,primernombre,segundonombre,apellidopaterno,apellidomaterno,
activo,cambiarcontrasenia,idrelusuarioroles,idrol,rol 
from viewusuarios 
where usuario='iscortesz@sodimac.com.mx'