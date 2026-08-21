package com.sodimac.fiscal.api.model.entity;

import jakarta.persistence.*;
import lombok.Getter;

/**
 * Mapea shared_catalogs.status_train (tren de estatus). Solo lectura desde fiscal-api:
 * se consulta DIRECTO por JPA para validar transiciones, sin pasar por util-api.
 * option_id: 1=Factura, 2=NC, 4=CartaPorte, 5=Recepcion.
 */
@Getter
@Entity
@Table(name = "status_train", schema = "shared_catalogs")
public class StatusTrainEntity {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "option_id")
    private Integer optionId;

    @Column(name = "source_status")
    private Integer sourceStatus;

    @Column(name = "target_status")
    private Integer targetStatus;
}
