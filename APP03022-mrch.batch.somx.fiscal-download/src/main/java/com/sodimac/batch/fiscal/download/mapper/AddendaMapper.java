package com.sodimac.batch.fiscal.download.mapper;

import com.sodimac.batch.fiscal.download.model.entity.sap.AddendaEntity;

public class AddendaMapper {

    /**
     * Mapea posicionalmente las columnas Extra1..6 de la tabla real SODIMAC_SAP_DEV.dbo.Addenda.
     * El significado de cada Extra depende del Tipo (nodo de addenda):
     * - Tipo 1 (Addenda_Sodimac_Detecno): Proveedor, NoOC, NoRecepcion, Folio, UUID, RFC.
     * - Tipo 2 (Addenda_Transportistas_Sodimac_Detecno): IdProveedor, -, -, Version, IdGuiaEntrega, IdViaje.
     * - Tipo 3 (NC): IdProveedor, -, -, Version, -, TipoNC.
     */
    public static AddendaEntity toEntity(String fiscalUuid, String e1, String e2, String e3,
                                          String e4, String e5, String e6, Integer tipo) {
        AddendaEntity entity = new AddendaEntity();
        entity.setUuid(fiscalUuid);
        entity.setExtra1(e1);
        entity.setExtra2(e2);
        entity.setExtra3(e3);
        entity.setExtra4(e4);
        entity.setExtra5(e5);
        entity.setExtra6(e6);
        entity.setTipo(tipo);
        return entity;
    }
}
