update catConfiguracion
set Valor = '/w=auto'
where NombreCampo = 'Response.Comprobante.Concepto.Url.with';

insert into catConfiguracion values (20,'Response.Comprobante.Concepto.Url.with','/w=110','Wsct','Ancho de la imagen',now());
select * from catConfiguracion;