select *
from CatMessage;

select *
from CatMessageType;

insert into CatMessageType values(1,'EXITO','Éxito',1,1,now(),1,null);
insert into CatMessageType values(2,'ADVERTENCIA','Advertencias',1,1,now(),1,null);
insert into CatMessageType values(3,'ERROR','Error',1,1,now(),1,null);



delete from CatMessage;
insert into CatMessage values (1,1,'C0001','Validación de CFDI realizada correctamente',1,1, now(),null,null);
insert into CatMessage values (2,3,'E1000','Error en la estructura del XML',1,1, now(),null,null);
insert into CatMessage values (3,2,'W2001','La factura debe ser ingreso (I) o egreso (E)',1,1, now(),null,null);
insert into CatMessage values (4,2,'W2002','La version del CFDI proporcionado es incorrecto o no se encuentra registrado',1,1, now(),null,null);
insert into CatMessage values (5,2,'W2003','El RFC emisor proporcionado es incorrecto',1,1, now(),null,null);
insert into CatMessage values (6,2,'W2004','El RFC receptor proporcionado es incorrecto',1,1, now(),null,null);

select * from CatMessage;

select * from CatModule;
insert into CatModule values (1,'Utilerías','Módulo genérico de referencia para ser utilizado por los otros modulos',1,1, now(),1,now());
insert into CatModule values (2,'Finanzas','Módulo genérico de referencia para procesos de finanzas como facturación',1,1, now(),1,now());
select * from CatParameterType;
insert into CatParameterType values (1,'Configuración','Estos parámtetros tienen un impacto en el desarrollo del aplicativo ya que dependiendo del valor, el flujo puede variar',1,1, now(),1,now());
select * from CatParameter;
insert into CatParameter values (1,2,1,'ValidaCartaPorte','Validar si es un documento con carta porte, mercancía o servicio','true',1, TO_DATE('01/01/2024', 'DD/MM/YYYY'),TO_DATE('31/12/2030', 'DD/MM/YYYY'),1,1,now(),1,now());
insert into CatParameter values (2,2,1,'ValidaVersionCartaPorte','Validar versión Activa de CartaPorte','2.0',1, TO_DATE('01/01/2024', 'DD/MM/YYYY'),TO_DATE('31/12/2030', 'DD/MM/YYYY'),1,1,now(),1,now());
select * from CatInvoiceVersion;
select * from CatInvoiceVersion;

CREATE TABLE CatInvoiceVersion (
  idInvoiceVersion SERIAL PRIMARY KEY,
  version varchar(10) NOT NULL,
  description VARCHAR(254) NOT NULL,
  status INT,
  idUserCreated INT,
  createdAt TIMESTAMP,
  idUserUpdated INT,
  updatedAt TIMESTAMP
);

COMMENT ON TABLE CatInvoiceVersion IS 'Catálogo de Version Factura';
COMMENT ON COLUMN CatInvoiceVersion.idInvoiceVersion IS 'Llave primaria';
COMMENT ON COLUMN CatInvoiceVersion.version IS 'Version de Factura';
COMMENT ON COLUMN CatInvoiceVersion.description IS 'Descripcion de Factura';
COMMENT ON COLUMN CatInvoiceVersion.status IS 'Estatus 0 inactivo, 1 activo';
COMMENT ON COLUMN CatItem.idUserCreated IS 'Campo de auditoría - id usuario registra';
COMMENT ON COLUMN CatItem.createdAt IS 'Campo de auditoría - fecha de creación';
COMMENT ON COLUMN CatItem.idUserUpdated IS 'Campo de auditoría - id usuario actualiza';
COMMENT ON COLUMN CatItem.updatedAt IS 'Campo de auditoría - fecha de actualización';


insert into CatInvoiceVersion values (1,3.3,'Version para Facturacion 3.3',1,1,'2022-03-31 12:50:52',1,now());
insert into CatInvoiceVersion values (2,4.0,'Version para Facturacion 4.0',1,2,'2022-03-31 12:50:52',2,now());