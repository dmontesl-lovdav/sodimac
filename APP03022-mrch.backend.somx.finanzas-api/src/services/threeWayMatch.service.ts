import { getDataSource } from "@/config/typeorm-datasource.js";
import * as repo from "@/repositories/threeWayMatch.repo.js";
import * as ejec from "@/repositories/twmEjecucion.repo.js";
import * as logs from "@/repositories/twmLogs.repo.js";

import { Reception } from "@/entities/Reception.entity.js";
import { ThreeWayMatch } from "@/entities/ThreeWayMatch.entity.js";
import { TwmStatus } from "@/types/twmStatus.js";
import { FinanzasPayment } from "@/entities/FinanzasPayment.entities.js";
import type {
    EntityManager,
    QueryRunner,
} from "typeorm";

type SapDocumentRaw = {
    documentNumber?: string | null;
    referenceNumber?: string | null;
    vendorNumber?: string | number | null;
    docSap?: string | null;
    createdAt?: Date | string | null;
    amount?: string | number | null;
};

type AddendumRaw = {
    invoiceUuid: string | null;
    receptionNumber: string | number | null;
    purchaseOrderNumber: string | number | null;
};

type InvoiceRaw = {
    invoiceUuid: string | null;
    fiscalUuid: string | null;
    series: string | null;
    folio: string | null;
    certificationDate: Date | string | null;
    subtotal: string | number | null;
    total: string | number | null;
};

type StepStats = {
    total: number;
    amount: number;
};

type InvoiceStepStats = {
    withoutInvoice: number;
    withInvoice: number;
    amount: number;
};

function createStepStats(): StepStats {
    return {
        total: 0,
        amount: 0,
    };
}

function createInvoiceStepStats(): InvoiceStepStats {
    return {
        withoutInvoice: 0,
        withInvoice: 0,
        amount: 0,
    };
}

async function saveControlFigures(
    manager: EntityManager,
    executionId: string,
    step: number,
    totalRecords: number,
    totalAmount: number,
): Promise<void> {
    await manager.save(
        "tenant_finance.twm_cifras_control",
        {
            idEjecucion: executionId,
            paso: step,
            totalRegistros: totalRecords,
            totalMonto: totalAmount,
            detalleJson: null,
            fechaRegistro: new Date(),
        },
    );
}

async function publishBaseReceptions(
    runner: QueryRunner,
    executionId: string,
    fechaBase: Date,
): Promise<StepStats> {
    const stats = createStepStats();

    const receptions = await runner.manager
        .createQueryBuilder(Reception, "r")
        .leftJoinAndSelect("r.purchaseOrder", "po")
        .where("r.receptionDate <= :fechaBase", {
            fechaBase,
        })
        .getMany();

    for (const reception of receptions) {
        if (!reception.purchaseOrder) {
            continue;
        }

        const record = runner.manager.create(
            ThreeWayMatch,
            {
                numeroProveedor:
                    reception.purchaseOrder.supplierNumber.toString(),
                ordenCompra:
                    reception.purchaseOrder.orderNumber,
                fechaOrdenCompra:
                    reception.purchaseOrder.purchaseOrderDate ??
                    reception.purchaseOrder.createdAt,
                montoOrdenCompra:
                    reception.purchaseOrder.amount ?? null,
                recepcion:
                    reception.receptionNumber,
                fechaRecepcion:
                    reception.receptionDate ??
                    reception.createdAt,
                montoRecepcion:
                    reception.amount ?? null,
                estatus:
                    TwmStatus.BASE,
                fechaRegistro:
                    new Date(),
            },
        );

        await runner.manager.save(record);

        await logs.log(
            executionId,
            "INFO",
            "TWM_CREATED_BASE",
            {
                proveedor: record.numeroProveedor,
                oc: record.ordenCompra,
                recepcion: record.recepcion,
                estatus: record.estatus,
            },
        );

        stats.total++;
        stats.amount += Number(
            record.montoRecepcion ?? 0,
        );
    }

    return stats;
}

async function findAddendum(
    manager: EntityManager,
    base: ThreeWayMatch,
): Promise<AddendumRaw | undefined> {
    return manager
        .createQueryBuilder()
        .select(
            "a.invoice_uuid",
            "invoiceUuid",
        )
        .addSelect(
            "a.reception_number",
            "receptionNumber",
        )
        .addSelect(
            "a.purchase_order_number",
            "purchaseOrderNumber",
        )
        .from(
            "tenant_fiscal.addendum",
            "a",
        )
        .where(
            "a.reception_number::text = :rec",
            {
                rec: String(base.recepcion),
            },
        )
        .andWhere(
            "a.purchase_order_number::text = :oc",
            {
                oc: String(base.ordenCompra),
            },
        )
        .getRawOne<AddendumRaw>();
}

async function findInvoice(
    manager: EntityManager,
    invoiceUuid: string,
): Promise<InvoiceRaw | undefined> {
    return manager
        .createQueryBuilder()
        .select(
            "i.invoice_uuid",
            "invoiceUuid",
        )
        .addSelect(
            "i.fiscal_uuid",
            "fiscalUuid",
        )
        .addSelect(
            "i.series",
            "series",
        )
        .addSelect(
            "i.folio",
            "folio",
        )
        .addSelect(
            "i.certification_date",
            "certificationDate",
        )
        .addSelect(
            "i.subtotal",
            "subtotal",
        )
        .addSelect(
            "i.total",
            "total",
        )
        .from(
            "tenant_fiscal.invoice",
            "i",
        )
        .where(
            "(i.invoice_uuid::text = :uuid OR i.fiscal_uuid::text = :uuid)",
            {
                uuid: invoiceUuid,
            },
        )
        .getRawOne<InvoiceRaw>();
}

async function markWithoutInvoice(
    runner: QueryRunner,
    executionId: string,
    base: ThreeWayMatch,
    eventName: string,
    extraData: Record<string, unknown> = {},
): Promise<void> {
    await runner.manager.update(
        ThreeWayMatch,
        {
            id: base.id,
        },
        {
            estatus: TwmStatus.SIN_FACTURA,
        },
    );

    await logs.log(
        executionId,
        "WARN",
        eventName,
        {
            recepcion: base.recepcion,
            oc: base.ordenCompra,
            proveedor: base.numeroProveedor,
            ...extraData,
        },
    );
}

function getInvoiceReportData(
    invoice: InvoiceRaw,
): {
    uuid: string | null;
    amount: number | null;
} {
    const uuid =
        invoice.fiscalUuid ??
        invoice.invoiceUuid ??
        null;

    const amountRaw =
        invoice.subtotal ??
        invoice.total ??
        null;

    return {
        uuid,
        amount:
            amountRaw !== null
                ? Number(amountRaw)
                : null,
    };
}

async function attachInvoice(
    runner: QueryRunner,
    executionId: string,
    base: ThreeWayMatch,
    addendum: AddendumRaw,
    invoice: InvoiceRaw,
): Promise<number> {
    const reportData =
        getInvoiceReportData(invoice);

    await runner.manager.update(
        ThreeWayMatch,
        {
            id: base.id,
        },
        {
            serie:
                invoice.series ?? null,
            folio:
                invoice.folio ?? null,
            uuid:
                reportData.uuid,
            fechaTimbrado:
                invoice.certificationDate ?? null,
            montoFactura:
                reportData.amount,
            estatus:
                TwmStatus.CON_FACTURA,
        },
    );

    await logs.log(
        executionId,
        "INFO",
        "TWM_CON_FACTURA",
        {
            recepcion:
                base.recepcion,
            oc:
                base.ordenCompra,
            uuidAddendum:
                addendum.invoiceUuid,
            uuidFacturaInterno:
                invoice.invoiceUuid,
            uuidReporte:
                reportData.uuid,
        },
    );

    return reportData.amount ?? 0;
}

async function relateInvoices(
    runner: QueryRunner,
    executionId: string,
): Promise<InvoiceStepStats> {
    const stats = createInvoiceStepStats();

    const bases = await runner.manager.find(
        ThreeWayMatch,
        {
            where: {
                estatus: TwmStatus.BASE,
            },
        },
    );

    for (const base of bases) {
        const addendum = await findAddendum(
            runner.manager,
            base,
        );

        if (!addendum?.invoiceUuid) {
            await markWithoutInvoice(
                runner,
                executionId,
                base,
                "TWM_SIN_FACTURA",
            );

            stats.withoutInvoice++;
            continue;
        }

        const invoice = await findInvoice(
            runner.manager,
            addendum.invoiceUuid,
        );

        if (!invoice) {
            await markWithoutInvoice(
                runner,
                executionId,
                base,
                "TWM_FACTURA_NO_ENCONTRADA",
                {
                    uuidAddendum:
                        addendum.invoiceUuid,
                },
            );

            stats.withoutInvoice++;
            continue;
        }

        stats.amount += await attachInvoice(
            runner,
            executionId,
            base,
            addendum,
            invoice,
        );

        stats.withInvoice++;
    }

    return stats;
}

async function findSapDocument(
    manager: EntityManager,
    item: ThreeWayMatch,
): Promise<SapDocumentRaw | undefined> {
    return manager
        .createQueryBuilder()
        .select(
            "s.document_number",
            "documentNumber",
        )
        .addSelect(
            "s.reference_number",
            "referenceNumber",
        )
        .addSelect(
            "s.vendor_number",
            "vendorNumber",
        )
        .addSelect(
            "s.doc_sap",
            "docSap",
        )
        .addSelect(
            "s.created_at",
            "createdAt",
        )
        .addSelect(
            "s.amount",
            "amount",
        )
        .from(
            "tenant_finance.sap_document",
            "s",
        )
        .where(
            "s.vendor_number = :vendorNumber",
            {
                vendorNumber:
                    Number(item.numeroProveedor),
            },
        )
        .andWhere(
            "s.reference_number::text = :referenceNumber",
            {
                referenceNumber:
                    String(item.ordenCompra),
            },
        )
        .getRawOne<SapDocumentRaw>();
}

function applySapDocument(
    item: ThreeWayMatch,
    sapDocument: SapDocumentRaw,
): void {
    item.documentoSap =
        sapDocument.docSap ??
        sapDocument.documentNumber ??
        null;

    item.fechaContable =
        sapDocument.createdAt
            ? new Date(sapDocument.createdAt)
            : null;

    item.montoContable =
        sapDocument.amount !== null &&
            sapDocument.amount !== undefined
            ? Number(sapDocument.amount)
            : null;

    item.estatus =
        TwmStatus.CONTABILIZADA;
}

async function accountInvoices(
    runner: QueryRunner,
    executionId: string,
): Promise<StepStats> {
    const stats = createStepStats();

    const invoices = await runner.manager.find(
        ThreeWayMatch,
        {
            where: {
                estatus:
                    TwmStatus.CON_FACTURA,
            },
        },
    );

    for (const item of invoices) {
        const sapDocument =
            await findSapDocument(
                runner.manager,
                item,
            );

        if (!sapDocument) {
            continue;
        }

        applySapDocument(
            item,
            sapDocument,
        );

        await runner.manager.save(item);

        await logs.log(
            executionId,
            "INFO",
            "TWM_CONTABILIZADA",
            {
                proveedor:
                    item.numeroProveedor,
                oc:
                    item.ordenCompra,
                documentoSap:
                    item.documentoSap,
            },
        );

        stats.total++;
        stats.amount += Number(
            item.montoContable ?? 0,
        );
    }

    return stats;
}

function applyPayment(
    item: ThreeWayMatch,
    payment: FinanzasPayment,
): void {
    item.referenciaPago =
        payment.documentReference ?? null;
    item.fechaPago =
        payment.paymentDate ?? null;
    item.montoPago =
        Number(payment.amount);
    item.estatus =
        TwmStatus.PAGADA;
}

async function registerPaymentStatus(
    runner: QueryRunner,
    executionId: string,
    item: ThreeWayMatch,
): Promise<number | null> {
    if (!item.documentoSap) {
        return null;
    }

    const payment = await runner.manager.findOne(
        FinanzasPayment,
        {
            where: {
                vendorNumber:
                    Number(item.numeroProveedor),
                documentNumber:
                    item.documentoSap,
            },
        },
    );

    if (!payment) {
        await logs.log(
            executionId,
            "INFO",
            "TWM_SIN_PAGO",
            {
                proveedor:
                    item.numeroProveedor,
                documentoSap:
                    item.documentoSap,
            },
        );

        return null;
    }

    applyPayment(
        item,
        payment,
    );

    await runner.manager.save(item);

    await logs.log(
        executionId,
        "INFO",
        "TWM_PAGADA",
        {
            proveedor:
                item.numeroProveedor,
            documentoSap:
                item.documentoSap,
            referenciaPago:
                item.referenciaPago,
            montoPago:
                item.montoPago,
        },
    );

    return item.montoPago ?? 0;
}

async function registerPayments(
    runner: QueryRunner,
    executionId: string,
): Promise<StepStats> {
    const stats = createStepStats();

    const accountedInvoices =
        await runner.manager.find(
            ThreeWayMatch,
            {
                where: {
                    estatus:
                        TwmStatus.CONTABILIZADA,
                },
            },
        );

    for (const item of accountedInvoices) {
        const paymentAmount =
            await registerPaymentStatus(
                runner,
                executionId,
                item,
            );

        if (paymentAmount === null) {
            continue;
        }

        stats.total++;
        stats.amount += Number(paymentAmount);
    }

    return stats;
}

async function logFinalSummary(
    runner: QueryRunner,
    executionId: string,
): Promise<void> {
    const summary = await runner.manager
        .createQueryBuilder(
            ThreeWayMatch,
            "t",
        )
        .select(
            "t.estatus",
            "estatus",
        )
        .addSelect(
            "COUNT(*)",
            "total",
        )
        .groupBy(
            "t.estatus",
        )
        .getRawMany();

    await logs.log(
        executionId,
        "INFO",
        "TWM_RESUMEN_FINAL",
        summary,
    );
}

async function executeSteps(
    runner: QueryRunner,
    executionId: string,
    fechaBase: Date,
): Promise<void> {
    await repo.deleteNotPaid(fechaBase);

    const step1 = await publishBaseReceptions(
        runner,
        executionId,
        fechaBase,
    );

    await saveControlFigures(
        runner.manager,
        executionId,
        1,
        step1.total,
        step1.amount,
    );

    const step2 = await relateInvoices(
        runner,
        executionId,
    );

    await saveControlFigures(
        runner.manager,
        executionId,
        2,
        step2.withInvoice +
        step2.withoutInvoice,
        step2.amount,
    );

    const step3 = await accountInvoices(
        runner,
        executionId,
    );

    await saveControlFigures(
        runner.manager,
        executionId,
        3,
        step3.total,
        step3.amount,
    );

    const step4 = await registerPayments(
        runner,
        executionId,
    );

    await logFinalSummary(
        runner,
        executionId,
    );

    await saveControlFigures(
        runner.manager,
        executionId,
        4,
        step4.total,
        step4.amount,
    );
}

async function handleRunError(
    runner: QueryRunner,
    executionId: string,
    error: unknown,
): Promise<never> {
    await runner.rollbackTransaction();

    await ejec.closeRun(
        executionId,
        "ERROR",
    );

    const errorMessage =
        error instanceof Error
            ? error.message
            : String(error);

    await logs.log(
        executionId,
        "CRITICAL",
        "TWM_EXEC_ERROR",
        {
            error: errorMessage,
        },
    );

    throw error;
}

export async function runThreeWayMatch(
    fechaBase: Date,
    intento: number = 1,
): Promise<void> {
    const dataSource = getDataSource();
    const runner =
        dataSource.createQueryRunner();

    await runner.connect();
    await runner.startTransaction();

    const execution = await ejec.createRun(
        fechaBase,
        intento,
    );

    try {
        await executeSteps(
            runner,
            execution.id,
            fechaBase,
        );

        await ejec.closeRun(
            execution.id,
            "COMPLETA",
        );

        await runner.commitTransaction();
    } catch (error: unknown) {
        await handleRunError(
            runner,
            execution.id,
            error,
        );
    } finally {
        await runner.release();
    }
}