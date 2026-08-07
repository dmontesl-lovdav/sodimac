-- Modelo Entidad-Relación Financiero - Sprint 4 - 2025
-- Normas de convención aplicadas para PostgreSQL
-- Cumplimiento estricto de buenas prácticas y requisitos funcionales

/*
Convenciones de nomenclatura:
  - Tablas: PascalCase singular (ej. OrdenCompra, Rebate)
  - Catálogos: prefijo "Cat" (ej. CatProveedor)
  - Campos PK: id<Entidad> (ej. idOrdenCompra)
  - Campos FK: id<EntidadRelacionada> (ej. idUsuario, idOrdenCompra)
  - Campos de auditoría estándar: idUsuarioRegistro, fechaRegistro, idUsuarioActualizacion, fechaActualizacion
  - Tipos: VARCHAR para texto, DECIMAL(18,2) para importes, TIMESTAMP para fechas
  - Índices: idx_<tabla>_<columna>
  - Evitar nombres plurales
  - Triggers: para setear automáticamente fechaRegistro y fechaActualizacion
*/
-- =========================================
-- MODULO: ORDENES DE INFORMACION FINANCIERA
-- =========================================


-- =========================================
-- FUNCIÓN DE AUDITORÍA (actualiza createdAt/updatedAt automáticamente)
-- =========================================
CREATE OR REPLACE FUNCTION set_audit_timestamps()
RETURNS TRIGGER AS $$
BEGIN
  IF TG_OP = 'INSERT' THEN
    NEW.createdAt := current_timestamp;
  END IF;
  NEW.updatedAt := current_timestamp;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE CatModule (
    idModule SERIAL PRIMARY KEY,
	name VARCHAR(50) NOT NULL,
    description VARCHAR(254) NOT NULL,
    status INT,
    idUserCreated INT,
    createdAt TIMESTAMP,
    idUserUpdated INT,
    updatedAt TIMESTAMP
); 
COMMENT ON TABLE CatModule IS 'Catálogo de Módulo';
COMMENT ON COLUMN CatModule.idModule IS 'Llave primaria';
COMMENT ON COLUMN CatModule.name IS 'Nombre del módulo';
COMMENT ON COLUMN CatModule.description IS 'Descripción del Módulo';
COMMENT ON COLUMN CatModule.status IS 'Estatus 0 inactivo, 1 activo';
COMMENT ON COLUMN CatModule.idUserCreated IS 'Campo de auditoría - id usuario registra';
COMMENT ON COLUMN CatModule.createdAt IS 'Campo de auditoría - fecha de creación';
COMMENT ON COLUMN CatModule.idUserUpdated IS 'Campo de auditoría - id usuario actualiza';
COMMENT ON COLUMN CatModule.updatedAt IS 'Campo de auditoría - fecha de actualización';

CREATE TABLE CatParameterType (
    idParameterType SERIAL PRIMARY KEY,
	name VARCHAR(50) NOT NULL,
    description VARCHAR(254) NOT NULL,
    status INT,
    idUserCreated INT,
    createdAt TIMESTAMP,
    idUserUpdated INT,
    updatedAt TIMESTAMP
); 
COMMENT ON TABLE CatParameterType IS 'Catálogo de Tipos de Parámetro';
COMMENT ON COLUMN CatParameterType.idParameterType IS 'Llave primaria';
COMMENT ON COLUMN CatParameterType.name IS 'Nombre del tipo de parámetro';
COMMENT ON COLUMN CatParameterType.description IS 'Descripción del tipo de parámetro';
COMMENT ON COLUMN CatParameterType.status IS 'Estatus 0 inactivo, 1 activo';
COMMENT ON COLUMN CatParameterType.idUserCreated IS 'Campo de auditoría - id usuario registra';
COMMENT ON COLUMN CatParameterType.createdAt IS 'Campo de auditoría - fecha de creación';
COMMENT ON COLUMN CatParameterType.idUserUpdated IS 'Campo de auditoría - id usuario actualiza';
COMMENT ON COLUMN CatParameterType.updatedAt IS 'Campo de auditoría - fecha de actualización';

CREATE TABLE CatParameter (
    idParameter SERIAL PRIMARY KEY,
    idModule INT NOT NULL,
    idParameterType INT NOT NULL,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(254) NOT NULL,
    value VARCHAR(254) NOT NULL,
    version INT NOT NULL,
    startDate TIMESTAMP,
    endDate TIMESTAMP,
    status INT,
    idUserCreated INT,
    createdAt TIMESTAMP,
    idUserUpdated INT,
    updatedAt TIMESTAMP,
	FOREIGN KEY (idModule) REFERENCES CatModule(idModule),
	FOREIGN KEY (idParameterType) REFERENCES CatParameterType(idParameterType)
);

COMMENT ON TABLE CatParameter IS 'Catálogo parametros';
COMMENT ON COLUMN CatParameter.idParameter IS 'Llave primaria';
COMMENT ON COLUMN CatParameter.idModule IS 'Referencia a tabla de modulos';
COMMENT ON COLUMN CatParameter.idParameterType IS 'Referencia a tabla de tipos de parámetro';
COMMENT ON COLUMN CatParameter.name IS 'Nombre del parametro';
COMMENT ON COLUMN CatParameter.description IS 'Descripción del parámetro';
COMMENT ON COLUMN CatParameter.value IS 'Valor del parámetro';
COMMENT ON COLUMN CatParameter.version IS 'Versión';
COMMENT ON COLUMN CatParameter.startDate IS 'Fecha de inicio de vigencia del parámetro';
COMMENT ON COLUMN CatParameter.endDate IS 'Fecha fin de vigencia del parámetro';
COMMENT ON COLUMN CatParameter.status IS 'Estatus 0 inactivo, 1 activo';
COMMENT ON COLUMN CatParameter.idUserCreated IS 'Campo de auditoría - id usuario registra';
COMMENT ON COLUMN CatParameter.createdAt IS 'Campo de auditoría - fecha de creación';
COMMENT ON COLUMN CatParameter.idUserUpdated IS 'Campo de auditoría - id usuario actualiza';
COMMENT ON COLUMN CatParameter.updatedAt IS 'Campo de auditoría - fecha de actualización';

CREATE TABLE CatMessageType (
    idMessageType SERIAL PRIMARY KEY,
	name VARCHAR(50) NOT NULL,
    description VARCHAR(254) NOT NULL,
    status INT,
    idUserCreated INT,
    createdAt TIMESTAMP,
    idUserUpdated INT,
    updatedAt TIMESTAMP
); 

COMMENT ON TABLE CatMessageType IS 'Catálogo de Tipo de Mensajes';
COMMENT ON COLUMN CatMessageType.name IS 'Nombre del tipo de mensaje';
COMMENT ON COLUMN CatMessageType.description IS 'Descripción del mensaje';
COMMENT ON COLUMN CatMessageType.status IS 'Estatus 0 inactivo, 1 activo';
COMMENT ON COLUMN CatMessageType.idUserCreated IS 'Campo de auditoría - id usuario registra';
COMMENT ON COLUMN CatMessageType.createdAt IS 'Campo de auditoría - fecha de creación';
COMMENT ON COLUMN CatMessageType.idUserUpdated IS 'Campo de auditoría - id usuario actualiza';
COMMENT ON COLUMN CatMessageType.updatedAt IS 'Campo de auditoría - fecha de actualización';

CREATE TABLE CatMessage (
    idMessage SERIAL PRIMARY KEY,
    idMessageType INT NOT NULL,
    messageCode VARCHAR(50) NOT NULL,
    description VARCHAR(254) NOT NULL,
    status INT,
    idUserCreated INT,
    createdAt TIMESTAMP,
    idUserUpdated INT,
    updatedAt TIMESTAMP,
	FOREIGN KEY (idMessageType) REFERENCES CatMessageType(idMessageType)
);

COMMENT ON TABLE CatMessage IS 'Catálogo mensajes';
COMMENT ON COLUMN CatMessage.idMessage IS 'Llave primaria';
COMMENT ON COLUMN CatMessage.idMessageType IS 'Referencia a tipo de mensaje';
COMMENT ON COLUMN CatMessage.messageCode IS 'Código del mensaje';
COMMENT ON COLUMN CatMessage.description IS 'Descripción del mensaje';
COMMENT ON COLUMN CatMessage.status IS 'Estatus 0 inactivo, 1 activo';
COMMENT ON COLUMN CatMessage.idUserCreated IS 'Campo de auditoría - id usuario registra';
COMMENT ON COLUMN CatMessage.createdAt IS 'Campo de auditoría - fecha de creación';
COMMENT ON COLUMN CatMessage.idUserUpdated IS 'Campo de auditoría - id usuario actualiza';
COMMENT ON COLUMN CatMessage.updatedAt IS 'Campo de auditoría - fecha de actualización';

CREATE TABLE CatApplication (
    idApplication SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(254) NOT NULL,
    status INT,
    idUserCreated INT,
    createdAt TIMESTAMP,
    idUserUpdated INT,
    updatedAt TIMESTAMP
);

COMMENT ON TABLE CatApplication IS 'Catálogo aplicaciones';
COMMENT ON COLUMN CatApplication.idApplication IS 'Llave primaria';
COMMENT ON COLUMN CatApplication.name IS 'Nombre del aplicativo';
COMMENT ON COLUMN CatApplication.description IS 'Descripción del aplicativo';
COMMENT ON COLUMN CatApplication.status IS 'Estatus 0 inactivo, 1 activo';
COMMENT ON COLUMN CatApplication.idUserCreated IS 'Campo de auditoría - id usuario registra';
COMMENT ON COLUMN CatApplication.createdAt IS 'Campo de auditoría - fecha de creación';
COMMENT ON COLUMN CatApplication.idUserUpdated IS 'Campo de auditoría - id usuario actualiza';
COMMENT ON COLUMN CatApplication.updatedAt IS 'Campo de auditoría - fecha de actualización';


CREATE TABLE ApplicationMsg (
    idApplicationMsg SERIAL PRIMARY KEY,
    idMessage INT NOT NULL,
    idApplication INT NOT NULL,
    status INT,
    idUserCreated INT,
    createdAt TIMESTAMP,
    idUserUpdated INT,
    updatedAt TIMESTAMP,
    FOREIGN KEY (idMessage) REFERENCES CatMessage(idMessage),
	FOREIGN KEY (idApplication) REFERENCES CatApplication(idApplication)
); 

COMMENT ON TABLE ApplicationMsg IS 'Tabla para relacionar aplicación con mensaje';
COMMENT ON COLUMN ApplicationMsg.idApplicationMsg IS 'Llave primaria';
COMMENT ON COLUMN ApplicationMsg.idMessage IS 'Referencia a catálogo de mensajes';
COMMENT ON COLUMN ApplicationMsg.idApplication IS 'Referencia a tabla de aplicación';
COMMENT ON COLUMN ApplicationMsg.status IS 'Estatus 0 inactivo, 1 activo';
COMMENT ON COLUMN ApplicationMsg.idUserCreated IS 'Campo de auditoría - id usuario registra';
COMMENT ON COLUMN ApplicationMsg.createdAt IS 'Campo de auditoría - fecha de creación';
COMMENT ON COLUMN ApplicationMsg.idUserUpdated IS 'Campo de auditoría - id usuario actualiza';
COMMENT ON COLUMN ApplicationMsg.updatedAt IS 'Campo de auditoría - fecha de actualización';

CREATE TABLE CatProcess (
    idProcess SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(254) NOT NULL,
    status INT,
    idUserCreated INT,
    createdAt TIMESTAMP,
    idUserUpdated INT,
    updatedAt TIMESTAMP
); 

COMMENT ON TABLE CatProcess IS 'Catálogo de procesos';
COMMENT ON COLUMN CatProcess.idProcess IS 'Llave primaria';
COMMENT ON COLUMN CatProcess.name IS 'Nombre del proceso';
COMMENT ON COLUMN CatProcess.description IS 'Descripción del proceso';
COMMENT ON COLUMN CatProcess.status IS 'Estatus 0 inactivo, 1 activo';
COMMENT ON COLUMN CatProcess.idUserCreated IS 'Campo de auditoría - id usuario registra';
COMMENT ON COLUMN CatProcess.createdAt IS 'Campo de auditoría - fecha de creación';
COMMENT ON COLUMN CatProcess.idUserUpdated IS 'Campo de auditoría - id usuario actualiza';
COMMENT ON COLUMN CatProcess.updatedAt IS 'Campo de auditoría - fecha de actualización';

CREATE TABLE CatItemType (
    idCatItemType SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(254) NOT NULL,
    status INT,
    idUserCreated INT,
    createdAt TIMESTAMP,
    idUserUpdated INT,
    updatedAt TIMESTAMP
);

COMMENT ON TABLE CatItemType IS 'Catálogo de tipo de procesos';
COMMENT ON COLUMN CatItemType.idCatItemType IS 'Llave primaria';
COMMENT ON COLUMN CatItemType.name IS 'Nombre del tipo de proceso';
COMMENT ON COLUMN CatItemType.description IS 'Descripción del tipo de proceso'; 
COMMENT ON COLUMN CatItemType.status IS 'Estatus 0 inactivo, 1 activo';
COMMENT ON COLUMN CatItemType.idUserCreated IS 'Campo de auditoría - id usuario registra';
COMMENT ON COLUMN CatItemType.createdAt IS 'Campo de auditoría - fecha de creación';
COMMENT ON COLUMN CatItemType.idUserUpdated IS 'Campo de auditoría - id usuario actualiza';
COMMENT ON COLUMN CatItemType.updatedAt IS 'Campo de auditoría - fecha de actualización';


CREATE TABLE CatItem (
    idItem SERIAL PRIMARY KEY,
	idCatItemType INT NOT NULL,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(254) NOT NULL,
    status INT,
    idUserCreated INT,
    createdAt TIMESTAMP,
    idUserUpdated INT,
    updatedAt TIMESTAMP,
	FOREIGN KEY (idCatItemType) REFERENCES CatItemType(idCatItemType)
); 

COMMENT ON TABLE CatItem IS 'Catálogo de elementos';
COMMENT ON COLUMN CatItem.idItem IS 'Llave primaria';
COMMENT ON COLUMN CatItem.idCatItemType IS 'Referencia a tipo de elemento';
COMMENT ON COLUMN CatItem.name IS 'Nombre del proceso';
COMMENT ON COLUMN CatItem.description IS 'Descripción del proceso';
COMMENT ON COLUMN CatItem.status IS 'Estatus 0 inactivo, 1 activo';
COMMENT ON COLUMN CatItem.idUserCreated IS 'Campo de auditoría - id usuario registra';
COMMENT ON COLUMN CatItem.createdAt IS 'Campo de auditoría - fecha de creación';
COMMENT ON COLUMN CatItem.idUserUpdated IS 'Campo de auditoría - id usuario actualiza';
COMMENT ON COLUMN CatItem.updatedAt IS 'Campo de auditoría - fecha de actualización';


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


insert into CatMessage values (5,2,'E1003','Error el RFC proporcionado es incorrecto',1,1, now(),null,null);

1	3.3	Version para Facturacion 3.3	1	2022-03-31 12:50:52	




-- =========================================
-- TRIGGERS DE AUDITORÍA PARA CADA TABLA
-- =========================================
/*CREATE TRIGGER trg_catmodule_timestamp BEFORE INSERT OR UPDATE ON CatModule FOR EACH ROW EXECUTE FUNCTION set_audit_timestamps();
CREATE TRIGGER trg_catparametertype_timestamp BEFORE INSERT OR UPDATE ON CatParameterType FOR EACH ROW EXECUTE FUNCTION set_audit_timestamps();
CREATE TRIGGER trg_catparameter_timestamp BEFORE INSERT OR UPDATE ON CatParameter FOR EACH ROW EXECUTE FUNCTION set_audit_timestamps();
CREATE TRIGGER trg_catmessagetype_timestamp BEFORE INSERT OR UPDATE ON CatMessageType FOR EACH ROW EXECUTE FUNCTION set_audit_timestamps();
CREATE TRIGGER trg_catmessage_timestamp BEFORE INSERT OR UPDATE ON CatMessage FOR EACH ROW EXECUTE FUNCTION set_audit_timestamps();
CREATE TRIGGER trg_catapplication_timestamp BEFORE INSERT OR UPDATE ON CatApplication FOR EACH ROW EXECUTE FUNCTION set_audit_timestamps();
CREATE TRIGGER trg_applicationmsg_timestamp BEFORE INSERT OR UPDATE ON ApplicationMsg FOR EACH ROW EXECUTE FUNCTION set_audit_timestamps();
CREATE TRIGGER trg_catprocess_timestamp BEFORE INSERT OR UPDATE ON CatProcess FOR EACH ROW EXECUTE FUNCTION set_audit_timestamps();
CREATE TRIGGER trg_catitemtype_timestamp BEFORE INSERT OR UPDATE ON CatItemType FOR EACH ROW EXECUTE FUNCTION set_audit_timestamps();
CREATE TRIGGER trg_catitem_timestamp BEFORE INSERT OR UPDATE ON CatItem FOR EACH ROW EXECUTE FUNCTION set_audit_timestamps();*/

-- =========================================
-- ÍNDICES 
-- =========================================



-- =========================================
-- RECOMENDACIONES ADICIONALES
-- =========================================
-- Agregar campo uuid para trazabilidad global si se integran varios sistemas.
-- Agregar campo idTransaccion en tablas críticas para seguimiento de procesos.
-- Usar dominios ENUM o Catálogo para el campo estatus (ordenCompra, recepcion).
-- Separar tablas históricas si se requiere auditar cada modificación (Hist<Entidad>).
-- Crear VISTAS para unir OrdenCompra + Detalle + Recepcion para reporting rápido.
-- Asegurar constraints NOT NULL en campos obligatorios.
-- Documentar con comentarios en columnas (`COMMENT ON COLUMN`).



/*

drop trigger trg_catmodule_timestamp on CatModule;
drop trigger trg_catparametertype_timestamp on CatParameterType;
drop trigger trg_catparameter_timestamp on CatParameter;
drop trigger trg_catmessagetype_timestamp on CatMessageType;
drop trigger trg_catmessage_timestamp on CatMessage;
drop trigger trg_catapplication_timestamp on CatApplication;
drop trigger trg_applicationmsg_timestamp on ApplicationMsg;
drop trigger trg_catprocess_timestamp on CatProcess;
drop trigger trg_catitemtype_timestamp on CatItemType;
drop trigger trg_catitem_timestamp on CatItem;


drop table applicationmsg;
drop table catparameter;
drop table catmodule;
drop table catmessage;
drop table catapplication;
drop table catprocess;
drop table catitem;

drop table catitemtype;
drop table catmessagetype;
drop table catparametertype;
drop function set_audit_timestamps;
*/

select * from CatMessageType;

insert into CatMessageType values(1,'EXITO','Éxito',1,1,now(),1,null);
insert into CatMessageType values(2,'ADVERTENCIA','Advertencias',1,1,now(),1,null);
insert into CatMessageType values(3,'ERROR','Error',1,1,now(),1,null);


insert into CatMessage values (1,1,'C0001','Validación de CFDI realizada correctamente',1,1, now(),null,null);
insert into CatMessage values (2,3,'E1000','Error en la estructura del XML',1,1, now(),null,null);

-- ===========================================
-- Table: stamped_rebate (Timbrado Rebate)
-- ===========================================
CREATE TABLE stamped_rebate (
    uuid              CHAR(36)     NOT NULL,    -- UUID
    document_number   VARCHAR(100) NOT NULL,    -- Número de documento
    reference_number  VARCHAR(100) NOT NULL,    -- Referencia de documento
    created_by        INTEGER      NOT NULL,    -- Usuario responsable
    created_at        TIMESTAMP    NOT NULL,    -- Fecha de registro
    updated_by        INTEGER,                   -- Usuario de última actualización
    updated_at        TIMESTAMP,                 -- Fecha de última actualización
    status            INTEGER      NOT NULL,    -- Estatus
    PRIMARY KEY(uuid)
);
CREATE INDEX idx_stamped_rebate_docnum ON stamped_rebate(document_number);

CREATE TABLE rebate (
	rebate_id SERIAL PRIMARY KEY,
    document_number   VARCHAR(100) NOT NULL,
    reference_number  VARCHAR(100) NOT NULL,
    sap_document      VARCHAR(50)  NOT NULL,
    vendor_number     INTEGER      NOT NULL,
    amount            DECIMAL(15,2) NOT NULL,
    source            INTEGER      NOT NULL,
    period_id         INTEGER      NOT NULL,
    due_date          TIMESTAMP    NOT NULL,
    posting_date      TIMESTAMP    NOT NULL,
    created_at        TIMESTAMP    NOT NULL,
    updated_at        TIMESTAMP,
    status            INTEGER      NOT null --,
--     PRIMARY KEY (document_number, reference_number),
--    FOREIGN KEY (document_number)  REFERENCES stamped_rebate(document_number)
);
CREATE INDEX idx_rebate_vendor ON rebate(vendor_number);

select * from receipt_fiscal 
select * from issuer_fiscal;
select * from receiver_fiscal;
select * from addenda_fiscal;

/*
 * 	Comprobante
	Emisor
	Receptor
	Addenda
 * */


select gen_random_uuid();
'55c98d72-8d37-4d1c-bc0d-5b305df85ec2'

