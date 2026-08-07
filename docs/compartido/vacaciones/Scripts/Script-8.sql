select *
from catConfiguracion
where NombreCampo = 'Response.Comprobante.Concepto.Url';

update catConfiguracion
set Valor = 'https://imagedelivery.net/4fYuQyy-r8_rpBpcY7lH_A/sodimacMX/'
where NombreCampo = 'Response.Comprobante.Concepto.Url';

insert into catConfiguracion values (20,'Response.Comprobante.Concepto.Url.with','/w=110','Wsct','Ancho de la imagen',now());

update catConfiguracion
set Valor = '/w=auto'
where NombreCampo = 'Response.Comprobante.Concepto.Url.with';




