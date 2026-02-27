/**
 * ============================================================================
 * SERVICE: rebateFiscal.service
 * ============================================================================
 * Servicio para gestión de rebates con integración fiscal
 *
 * RESPONSABILIDADES:
 * - STM-973: Relacionar descuento comercial con NC (Nota de Crédito)
 * - STM-875: Buscar descuentos comerciales con filtros avanzados
 *
 * ARQUITECTURA:
 * - Sigue el patrón Express del proyecto (funciones exportadas)
 * - Usa repositorios directos (no decoradores NestJS)
 * - Integra con fiscal-api para validación de XML
 *
 * @author Sodimac Tech Team
 * @version 2.0 (Refactorizado para Express)
 * @since 2025
 */

import { getDataSource } from "@/config/typeorm-datasource.js";
import { StampedRebate } from "@/entities/StampedRebate.entity.js";
import { Rebate } from "@/entities/Rebate.entity.js";
import { FiscalApiClient } from "@/clients/fiscal-api.client.js";
import type {
    RebateRelationRequestDto,
    RebateRelationResponseDto
} from "@/schemas/rebateFiscal/rebate-relation.dto.js";
import type {
    RebateSearchRequestDto,
    RebateSearchResponseDto,
    RebateSearchPageResponseDto
} from "@/schemas/rebateFiscal/rebate-search.dto.js";
import { HttpError } from "@/utils/HttpError.js";

// Instancia del cliente HTTP para fiscal-api
const fiscalApiClient = new FiscalApiClient();

/**
 * STM-973: Relacionar Descuento Comercial con NC
 *
 * FLUJO:
 * 1. Validar XML de NC contra fiscal-api
 * 2. Verificar duplicados (documentNumber único)
 * 3. Crear StampedRebate con invoice_fiscal_uuid
 * 4. Crear Rebate (datos financieros)
 * 5. Retornar UUIDs creados
 *
 * @param request Datos del rebate (numeroDocumento, referenciaDocumento, etc.)
 * @param xmlFile Buffer del archivo XML de la NC
 * @returns Response con UUIDs creados y estado
 */
export async function relateRebate(
    request: RebateRelationRequestDto,
    xmlFile: Buffer
): Promise<RebateRelationResponseDto> {
    console.log(`[relateRebate] Iniciando relación. Documento: ${request.numeroDocumento}`);

    // 1. Validar NC contra fiscal-api
    console.log('[relateRebate] Validando NC con fiscal-api...');
    const validation = await fiscalApiClient.validateNCRelation(
        { invoiceFiscalUuid: request.uuid },
        xmlFile
    );

    if (!validation.valid) {
        console.error('[relateRebate] Validación fallida:', validation.errorMessage);
        return {
            success: false,
            message: validation.errorMessage || 'Validación de NC fallida',
            businessCode: validation.businessCode || 'BUS4001'
        };
    }

    console.log(`[relateRebate] Validación exitosa. NC UUID: ${validation.ncFiscalUuid}`);

    const stampedRebateRepo = getDataSource().getRepository(StampedRebate);
    const rebateRepo = getDataSource().getRepository(Rebate);

    // 2. Verificar duplicados
    console.log('[relateRebate] Verificando duplicados...');
    const existing = await stampedRebateRepo.findOne({
        where: { documentNumber: request.numeroDocumento }
    });

    if (existing) {
        console.warn(`[relateRebate] Duplicado encontrado: ${request.numeroDocumento}`);
        return {
            success: false,
            message: `El rebate con número de documento ${request.numeroDocumento} ya existe`,
            businessCode: 'BUS4002'
        };
    }

    // 3. Crear StampedRebate (datos fiscales)
    console.log('[relateRebate] Creando StampedRebate...');
    const stampedRebate = stampedRebateRepo.create({
        documentNumber: request.numeroDocumento,
        referenceNumber: request.referenciaDocumento,
        invoiceFiscalUuid: request.uuid, // UUID de la factura original
        status: 1,
        createdBy: parseInt(request.usuario),
        createdAt: new Date()
    });

    const savedStampedRebate = await stampedRebateRepo.save(stampedRebate);
    console.log(`[relateRebate] StampedRebate creado: ${savedStampedRebate.stampedRebateUuid}`);

    // 4. Crear Rebate (datos financieros)
    console.log('[relateRebate] Creando Rebate...');
    const rebate = rebateRepo.create({
        documentNumber: request.numeroDocumento,
        documentReference: request.referenciaDocumento,
        sapDocument: '', // Se llenará después
        supplierNumber: parseInt(request.numeroProveedor),
        amount: 0, // Se llenará después
        originId: 1, // Default
        periodId: parseInt(new Date().toISOString().slice(0, 7).replace('-', '')), // YYYYMM
        dueDate: new Date(),
        postingDate: new Date(),
        status: 1,
        createdBy: parseInt(request.usuario),
        createdAt: new Date()
    });

    const savedRebate = await rebateRepo.save(rebate);
    console.log(`[relateRebate] Rebate creado: ${savedRebate.rebateId}`);

    // 5. Retornar respuesta exitosa
    return {
        success: true,
        message: 'Rebate relacionado exitosamente con NC',
        businessCode: 'BUS2000',
        stampedRebateUuid: savedStampedRebate.stampedRebateUuid,
        rebateUuid: savedRebate.rebateId
    };
}

/**
 * STM-875: Buscar Descuentos Comerciales
 *
 * FILTROS:
 * - idProveedor (OBLIGATORIO): Número de proveedor
 * - numeroDocumento (opcional): Número de documento exacto
 * - referenciaDocumento (opcional): Referencia de documento
 * - uuid (opcional): UUID fiscal de la factura relacionada
 * - page, size: Paginación
 *
 * @param searchRequest Filtros de búsqueda
 * @returns Resultados paginados
 */
export async function searchRebates(
    searchRequest: RebateSearchRequestDto
): Promise<RebateSearchPageResponseDto> {
    console.log(`[searchRebates] Buscando rebates. Proveedor: ${searchRequest.idProveedor}`);

    const rebateRepo = getDataSource().getRepository(Rebate);
    const queryBuilder = rebateRepo.createQueryBuilder('rebate');

    // Filtro OBLIGATORIO: supplier_number
    queryBuilder.where('rebate.supplierNumber = :supplierNumber', {
        supplierNumber: parseInt(searchRequest.idProveedor)
    });

    // Filtros OPCIONALES
    if (searchRequest.numeroDocumento) {
        queryBuilder.andWhere('rebate.documentNumber = :docNum', {
            docNum: searchRequest.numeroDocumento
        });
    }

    if (searchRequest.referenciaDocumento) {
        queryBuilder.andWhere('rebate.documentReference = :refNum', {
            refNum: searchRequest.referenciaDocumento
        });
    }

    if (searchRequest.uuid) {
        // Buscar por UUID fiscal (join con stamped_rebate)
        queryBuilder
            .leftJoinAndSelect('rebate.stampedRebate', 'stampedRebate')
            .andWhere('stampedRebate.invoiceFiscalUuid = :uuid', {
                uuid: searchRequest.uuid
            });
    } else {
        // Siempre traer stampedRebate para tener invoice_fiscal_uuid
        queryBuilder.leftJoinAndSelect('rebate.stampedRebate', 'stampedRebate');
    }

    // Paginación
    const page = searchRequest.page || 0;
    const size = searchRequest.size || 20;

    queryBuilder
        .skip(page * size)
        .take(size)
        .orderBy('rebate.createdAt', 'DESC');

    // Ejecutar query
    const [rebates, total] = await queryBuilder.getManyAndCount();

    console.log(`[searchRebates] Búsqueda completada. Encontrados: ${rebates.length} de ${total} totales`);

    // Mapear a DTOs
    const data: RebateSearchResponseDto[] = rebates.map(rebate => {
        const dto: RebateSearchResponseDto = {
            rebateUuid: rebate.rebateId,
            documentNumber: rebate.documentNumber,
            referenceNumber: rebate.documentReference || '',
            sapDocument: rebate.sapDocument || '',
            vendorNumber: rebate.supplierNumber || 0,
            amount: rebate.amount || 0,
            status: rebate.status || 0,
            statusName: getStatusName(rebate.status || 0),
            createdAt: rebate.createdAt
        };

        // Add optional properties only if they have values
        if (rebate.stampedRebate?.invoiceFiscalUuid) {
            dto.invoiceFiscalUuid = rebate.stampedRebate.invoiceFiscalUuid;
        }
        if (rebate.createdBy) {
            dto.createdBy = rebate.createdBy;
        }
        if (rebate.updatedBy) {
            dto.updatedBy = rebate.updatedBy;
        }
        if (rebate.updatedAt) {
            dto.updatedAt = rebate.updatedAt;
        }

        return dto;
    });

    // Calcular total de páginas
    const totalPages = Math.ceil(total / size);

    return {
        data,
        total,
        page,
        size,
        totalPages
    };
}

/**
 * Helper: Mapear status numérico a nombre
 */
function getStatusName(status: number): string {
    const statusMap: Record<number, string> = {
        0: 'Inactivo',
        1: 'Activo',
        2: 'Procesado',
        3: 'Cancelado'
    };
    return statusMap[status] || 'Desconocido';
}
