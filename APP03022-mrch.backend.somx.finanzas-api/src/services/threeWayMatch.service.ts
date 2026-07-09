import { getDataSource } from "@/config/typeorm-datasource.js";
import * as repo from "@/repositories/threeWayMatch.repo.js";
import * as ejec from "@/repositories/twmEjecucion.repo.js";
import * as logs from "@/repositories/twmLogs.repo.js";

import { Reception } from "@/entities/Reception.entity.js";
import { ThreeWayMatch } from "@/entities/ThreeWayMatch.entity.js";
import { TwmStatus } from "@/types/twmStatus.js";
import { FinanzasPayment } from "@/entities/FinanzasPayment.entities.js";

type SapDocumentRaw = {
    documentNumber?: string | null;
    referenceNumber?: string | null;
    vendorNumber?: string | number | null;
    docSap?: string | null;
    createdAt?: Date | string | null;
    amount?: string | number | null;
};

export async function runThreeWayMatch(
    fechaBase: Date,
    intento: number = 1
) {
    const ds = getDataSource();
    const runner = ds.createQueryRunner();

    await runner.connect();
    await runner.startTransaction();

    const ejecucion = await ejec.createRun(fechaBase, intento);

    let p1_total = 0;
    let p1_monto = 0;

    let p2_sinFactura = 0;
    let p2_conFactura = 0;
    let p2_montoFactura = 0;

    let p3_total = 0;
    let p3_monto = 0;

    let p4_total = 0;
    let p4_monto = 0;

    try {
        // Limpieza previa (no pagados)
        await repo.deleteNotPaid(fechaBase);

        // ===============================
        // PASO 1 – Publicación base OC + Recepción
        // ===============================

        const recepciones = await runner.manager
            .createQueryBuilder(Reception, "r")
            .leftJoinAndSelect("r.purchaseOrder", "po")
            .where("r.receptionDate <= :fechaBase", { fechaBase })
            .getMany();

        for (const r of recepciones) {
            if (!r.purchaseOrder) continue;

            const record = runner.manager.create(ThreeWayMatch, {
                numeroProveedor: r.purchaseOrder.supplierNumber.toString(),
                ordenCompra: r.purchaseOrder.orderNumber,
                fechaOrdenCompra:
                    r.purchaseOrder.purchaseOrderDate ??
                    r.purchaseOrder.createdAt,
                montoOrdenCompra:
                    r.purchaseOrder.amount ?? null,
                recepcion: r.receptionNumber,
                fechaRecepcion:
                    r.receptionDate ?? r.createdAt,
                montoRecepcion: r.amount ?? null,
                estatus: TwmStatus.BASE,
                fechaRegistro: new Date(),
            });

            await runner.manager.save(record);

            await logs.log(
                ejecucion.id,
                "INFO",
                "TWM_CREATED_BASE",
                {
                    proveedor: record.numeroProveedor,
                    oc: record.ordenCompra,
                    recepcion: record.recepcion,
                    estatus: record.estatus,
                }
            );

            p1_total++;
            p1_monto += Number(record.montoRecepcion ?? 0);
        }

        await runner.manager.save("tenant_finance.twm_cifras_control", {
            idEjecucion: ejecucion.id,
            paso: 1,
            totalRegistros: p1_total,
            totalMonto: p1_monto,
            detalleJson: null,
            fechaRegistro: new Date()
        });

        // ===============================
        // PASO 2 – Relacionar Factura
        // ===============================

        const bases = await runner.manager.find(ThreeWayMatch, {
            where: { estatus: TwmStatus.BASE },
        });

        for (const base of bases) {
            const addendum = await runner.manager
                .createQueryBuilder()
                .select("a.invoice_uuid", "invoiceUuid")
                .addSelect("a.reception_number", "receptionNumber")
                .addSelect("a.purchase_order_number", "purchaseOrderNumber")
                .from("tenant_fiscal.addendum", "a")
                .where("a.reception_number::text = :rec", {
                    rec: String(base.recepcion),
                })
                .andWhere("a.purchase_order_number::text = :oc", {
                    oc: String(base.ordenCompra),
                })
                .getRawOne();

            if (!addendum?.invoiceUuid) {
                await runner.manager.update(
                    ThreeWayMatch,
                    { id: base.id },
                    { estatus: TwmStatus.SIN_FACTURA }
                );

                await logs.log(
                    ejecucion.id,
                    "WARN",
                    "TWM_SIN_FACTURA",
                    {
                        recepcion: base.recepcion,
                        oc: base.ordenCompra,
                        proveedor: base.numeroProveedor,
                    }
                );

                p2_sinFactura++;

                continue;
            }

            const invoice = await runner.manager
                .createQueryBuilder()
                .select("i.invoice_uuid", "invoiceUuid")
                .addSelect("i.fiscal_uuid", "fiscalUuid")
                .addSelect("i.series", "series")
                .addSelect("i.folio", "folio")
                .addSelect("i.certification_date", "certificationDate")
                .addSelect("i.subtotal", "subtotal")
                .addSelect("i.total", "total")
                .from("tenant_fiscal.invoice", "i")
                .where(
                    `(i.invoice_uuid::text = :uuid OR i.fiscal_uuid::text = :uuid)`,
                    {
                        uuid: String(addendum.invoiceUuid),
                    }
                )
                .getRawOne();

            if (!invoice) {
                await runner.manager.update(
                    ThreeWayMatch,
                    { id: base.id },
                    { estatus: TwmStatus.SIN_FACTURA }
                );

                await logs.log(
                    ejecucion.id,
                    "WARN",
                    "TWM_FACTURA_NO_ENCONTRADA",
                    {
                        recepcion: base.recepcion,
                        oc: base.ordenCompra,
                        proveedor: base.numeroProveedor,
                        uuidAddendum: addendum.invoiceUuid,
                    }
                );

                p2_sinFactura++;

                continue;
            }

            const uuidReporte = invoice.fiscalUuid ?? invoice.invoiceUuid;
            const montoFacturaReporte = invoice.subtotal ?? invoice.total ?? null;

            await runner.manager.update(
                ThreeWayMatch,
                { id: base.id },
                {
                    serie: invoice.series ?? null,
                    folio: invoice.folio ?? null,
                    uuid: uuidReporte,
                    fechaTimbrado: invoice.certificationDate ?? null,
                    montoFactura: montoFacturaReporte,
                    estatus: TwmStatus.CON_FACTURA,
                }
            );

            p2_conFactura++;
            p2_montoFactura += Number(montoFacturaReporte ?? 0);

            await logs.log(
                ejecucion.id,
                "INFO",
                "TWM_CON_FACTURA",
                {
                    recepcion: base.recepcion,
                    oc: base.ordenCompra,
                    uuidAddendum: addendum.invoiceUuid,
                    uuidFacturaInterno: invoice.invoiceUuid,
                    uuidReporte,
                }
            );
        }

        await runner.manager.save("tenant_finance.twm_cifras_control", {
            idEjecucion: ejecucion.id,
            paso: 2,
            totalRegistros: p2_conFactura + p2_sinFactura,
            totalMonto: p2_montoFactura,
            detalleJson: null,
            fechaRegistro: new Date()
        });

        // ===============================
        // PASO 3 – Factura contabilizada
        // NOTA:
        // No usamos SapDocument entity aquí porque el entity no coincide con BD.
        // BD real:
        // - sap_document_uuid
        // - reference_number
        // - vendor_number
        // - doc_sap
        // ===============================

        const conFactura = await runner.manager.find(ThreeWayMatch, {
            where: { estatus: TwmStatus.CON_FACTURA },
        });

        for (const item of conFactura) {
            const sapDoc = await runner.manager
                .createQueryBuilder()
                .select("s.document_number", "documentNumber")
                .addSelect("s.reference_number", "referenceNumber")
                .addSelect("s.vendor_number", "vendorNumber")
                .addSelect("s.doc_sap", "docSap")
                .addSelect("s.created_at", "createdAt")
                .addSelect("s.amount", "amount")
                .from("tenant_finance.sap_document", "s")
                .where("s.vendor_number = :vendorNumber", {
                    vendorNumber: Number(item.numeroProveedor),
                })
                .andWhere("s.reference_number::text = :referenceNumber", {
                    referenceNumber: String(item.ordenCompra),
                })
                .getRawOne<SapDocumentRaw>();

            if (!sapDoc) {
                // Mantener estatus = 3
                continue;
            }

            item.documentoSap = sapDoc.docSap ?? sapDoc.documentNumber ?? null;
            item.fechaContable = sapDoc.createdAt
                ? new Date(sapDoc.createdAt)
                : null;
            item.montoContable =
                sapDoc.amount !== null && sapDoc.amount !== undefined
                    ? Number(sapDoc.amount)
                    : null;
            item.estatus = TwmStatus.CONTABILIZADA;

            await runner.manager.save(item);

            await logs.log(
                ejecucion.id,
                "INFO",
                "TWM_CONTABILIZADA",
                {
                    proveedor: item.numeroProveedor,
                    oc: item.ordenCompra,
                    documentoSap: item.documentoSap,
                }
            );

            p3_total++;
            p3_monto += Number(item.montoContable ?? 0);
        }

        await runner.manager.save("tenant_finance.twm_cifras_control", {
            idEjecucion: ejecucion.id,
            paso: 3,
            totalRegistros: p3_total,
            totalMonto: p3_monto,
            detalleJson: null,
            fechaRegistro: new Date()
        });

        // ===============================
        // PASO 4 – Pagada
        // ===============================

        const contabilizadas = await runner.manager.find(ThreeWayMatch, {
            where: { estatus: TwmStatus.CONTABILIZADA },
        });

        for (const item of contabilizadas) {
            if (!item.documentoSap) continue;

            const pago = await runner.manager.findOne(FinanzasPayment, {
                where: {
                    vendorNumber: Number(item.numeroProveedor),
                    documentNumber: item.documentoSap,
                },
            });

            if (!pago) {
                await logs.log(
                    ejecucion.id,
                    "INFO",
                    "TWM_SIN_PAGO",
                    {
                        proveedor: item.numeroProveedor,
                        documentoSap: item.documentoSap,
                    }
                );

                continue;
            }

            item.referenciaPago = pago.documentReference ?? null;
            item.fechaPago = pago.paymentDate ?? null;
            item.montoPago = Number(pago.amount) ?? null;
            item.estatus = TwmStatus.PAGADA;

            await runner.manager.save(item);

            await logs.log(
                ejecucion.id,
                "INFO",
                "TWM_PAGADA",
                {
                    proveedor: item.numeroProveedor,
                    documentoSap: item.documentoSap,
                    referenciaPago: item.referenciaPago,
                    montoPago: item.montoPago,
                }
            );

            p4_total++;
            p4_monto += Number(item.montoPago ?? 0);
        }

        const resumen = await runner.manager
            .createQueryBuilder(ThreeWayMatch, "t")
            .select("t.estatus", "estatus")
            .addSelect("COUNT(*)", "total")
            .groupBy("t.estatus")
            .getRawMany();

        await logs.log(
            ejecucion.id,
            "INFO",
            "TWM_RESUMEN_FINAL",
            resumen
        );

        await runner.manager.save("tenant_finance.twm_cifras_control", {
            idEjecucion: ejecucion.id,
            paso: 4,
            totalRegistros: p4_total,
            totalMonto: p4_monto,
            detalleJson: null,
            fechaRegistro: new Date()
        });

        await ejec.closeRun(ejecucion.id, "COMPLETA");
        await runner.commitTransaction();

    } catch (e: unknown) {
        await runner.rollbackTransaction();
        await ejec.closeRun(ejecucion.id, "ERROR");

        const errorMessage =
            e instanceof Error ? e.message : String(e);

        await logs.log(
            ejecucion.id,
            "CRITICAL",
            "TWM_EXEC_ERROR",
            { error: errorMessage }
        );

        throw e;
    } finally {
        await runner.release();
    }
}