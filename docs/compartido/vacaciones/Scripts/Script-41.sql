-- DEV
call cli_2fac_3cab_4det_5lfa_6ler ('2026030310000055525');
call uspEliminarTicket('2026021810100091355');

-- EstatusId: , ErrorMessage: Xml invalido [String was not recognized as a valid DateTime.], ErrorDesc: , Ticket: 2026021810100091355, Folio: , FacturaId: 


SELECT nombreCampo, valor 
FROM facturacion.catConfiguracion 
WHERE nombreCampo = 'WebService.Configuracion.Url.Emisor';


SELECT id, nombre, endPoint, endPoint40, licencia, activo 
FROM pacs;

SELECT nombreCampo, valor FROM catConfiguracion 
WHERE nombreCampo IN (
    'WebService.Configuracion.Url.Login',
    'WebService.Configuracion.Url.Emisor',
    'WebService.Facturacion.Usuario',
    'WebService.Facturacion.Password'
);


SELECT * FROM confDatosEmisor;

update confDatosEmisor
set razonSocial = 'XENON INDUSTRIAL ARTICLES'
where idConfDatosEmisor = 1;
