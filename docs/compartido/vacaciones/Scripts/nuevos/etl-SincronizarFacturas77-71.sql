SELECT top 10 idFactura, idPac, idCliente, rfc, email, ticket, idVersionFacturaSodimac, idFacturaPac, uuid, fechaTimbrado, versionFacturacionSat, [xml], fechaCompra, idOrigen, idEstatusFactura, fechaCreacion, nombreArchivo, ticketBct, versionFactura, transaccion, nombreObra, idComprobante, uuidRelacionado, responsableObra, acuse, serie, folio, total, subTotal, metodoPago, estatusenviado
FROM SODIMAC_FISCAL_PROD.dbo.Facturas_Temp
;

select *
from SODIMAC_FISCAL_PROD.dbo.Facturas_Temp
where fechaCreacion >= convert(datetime,'2025-07-18 00:00:00',120)
and   fechaCreacion <= convert(datetime,'2025-07-19 00:00:00',120);

