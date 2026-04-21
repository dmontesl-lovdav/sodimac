import { getDataSource } from "@/config/typeorm-datasource.js";
import * as repo from "@/repositories/threeWayMatch.repo.js";
import * as ejec from "@/repositories/twmEjecucion.repo.js";
import * as logs from "@/repositories/twmLogs.repo.js";

import { Reception } from "@/entities/Reception.entity.js";
import { ThreeWayMatch } from "@/entities/ThreeWayMatch.entity.js";
import { TwmStatus } from "@/types/twmStatus.js";
import { SapDocument } from "@/entities/SapDocument.entity.js";
import { FinanzasPayment } from "@/entities/FinanzasPayment.entities.js";

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
                .select("a")
                .from("tenant_fiscal.addendum", "a")
                .where("a.reception_number = :rec", { rec: base.recepcion })
                .getRawOne();

            if (!addendum) {
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
                        proveedor: base.numeroProveedor,
                    }
                );

                p2_sinFactura++;

                continue;
            }

            const invoice = await runner.manager
                .createQueryBuilder()
                .select("i")
                .from("tenant_fiscal.invoice", "i")
                .where("i.invoice_uuid = :uuid", { uuid: addendum.invoice_uuid })
                .getRawOne();

            if (!invoice) {
                await runner.manager.update(
                    ThreeWayMatch,
                    { id: base.id },
                    { estatus: TwmStatus.SIN_FACTURA }
                );

                p2_sinFactura++;

                continue;
            }

            await runner.manager.update(
                ThreeWayMatch,
                { id: base.id },
                {
                    serie: invoice.series,
                    folio: invoice.folio,
                    uuid: invoice.invoice_uuid,
                    fechaTimbrado: invoice.certification_date ?? null,
                    montoFactura: invoice.total ?? null,
                    estatus: TwmStatus.CON_FACTURA,
                }
            );

            p2_conFactura++;
            p2_montoFactura += Number(invoice.total ?? 0);

            await logs.log(
                ejecucion.id,
                "INFO",
                "TWM_CON_FACTURA",
                {
                    recepcion: base.recepcion,
                    uuid: invoice.invoice_uuid,
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
        // ===============================

        const conFactura = await runner.manager.find(ThreeWayMatch, {
            where: { estatus: TwmStatus.CON_FACTURA },
        });

        for (const item of conFactura) {
            const sapDoc = await runner.manager.findOne(SapDocument, {
                where: {
                    supplierNumber: Number(item.numeroProveedor),
                    documentReference: item.ordenCompra,
                },
            });

            if (!sapDoc) {
                // Mantener estatus = 3
                continue;
            }

            item.documentoSap = sapDoc.documentNumber ?? null;
            item.fechaContable = sapDoc.createdAt ?? null;
            item.montoContable = sapDoc.amount ?? null;
            item.estatus = TwmStatus.CONTABILIZADA;

            await runner.manager.save(item);
            await logs.log(
                ejecucion.id,
                "INFO",
                "TWM_CONTABILIZADA",
                {
                    proveedor: item.numeroProveedor,
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
