import { datasource } from '@/config/typeorm-datasource.js';
import { CatalogConversion } from '@/entities/CatalogConversion.entity.js';
import * as convRepo from '@/repositories/catalogConversion.repo.js';
import * as detailRepo from '@/repositories/catalogDetail.repo.js';
import { GenericException } from '@/exceptions/GenericException.js';
import type {
    ConversionCreateDto,
    ConversionUpdateDto,
    ConversionDto,
    ConversionPageResponse
} from '@/dto/conversion.dto.js';

export interface ConversionSearchParams {
    sourceElementId?: number | null;
    targetElementId?: number | null;
    elemento?: string | null;
    valor?: string | null;
    catalogoOrigen?: string | null;
    estatus?: number | null;
    page: number;
    pageSize: number;
    sortBy: string;
    sortDir: 'ASC' | 'DESC';
}

function statusLabel(status: number | null | undefined): 'Activo' | 'Inactivo' {
    return status === 1 ? 'Activo' : 'Inactivo';
}

function toDto(c: CatalogConversion): ConversionDto {
    const src = c.sourceElement;
    const tgt = c.targetElement;
    return {
        idConversion: c.id,
        idElementoOrigen: src?.id ?? null,
        elementoOrigen: src?.key ?? null,
        valorElementoOrigen: src?.value ?? null,
        estatusElementoOrigen: src ? statusLabel(src.status) : null,
        catalogoElementoOrigen: src?.header?.name ?? null,
        idElemento: tgt?.id ?? null,
        elemento: tgt?.key ?? null,
        valor: tgt?.value ?? null,
        catalogoOrigen: tgt?.header?.name ?? null,
        fechaInicioVigencia: c.validFrom ?? null,
        fechaFinVigencia: c.validTo ?? null,
        estatus: statusLabel(c.status),
        esPrincipal: c.isPrincipal,
        idUsuarioRegistro: c.createdBy ?? null,
        fechaRegistro: c.createdAt ?? null,
        idUsuarioActualizacion: c.updatedBy ?? null,
        fechaActualizacion: c.updatedAt ?? null
    };
}

export async function search(params: ConversionSearchParams): Promise<ConversionPageResponse> {
    const result = await convRepo.searchPaged(
        {
            sourceElementId: params.sourceElementId,
            targetElementId: params.targetElementId,
            elemento: params.elemento,
            valor: params.valor,
            catalogoOrigen: params.catalogoOrigen,
            estatus: params.estatus
        },
        {
            page: Math.max(0, params.page - 1),
            pageSize: params.pageSize,
            sortBy: params.sortBy,
            sortDir: params.sortDir
        }
    );

    return {
        items: result.items.map(toDto),
        page: result.page + 1,
        pageSize: result.pageSize,
        total: result.total,
        totalPages: result.totalPages
    };
}

export async function getById(id: number): Promise<ConversionDto> {
    const conv = await convRepo.findById(id);
    if (!conv) throw new GenericException(404, `Conversión no encontrada: ${id}`);
    return toDto(conv);
}

export async function create(dto: ConversionCreateDto, userId: string): Promise<ConversionDto> {
    const source = await detailRepo.findById(dto.sourceElementId);
    if (!source) throw new GenericException(404, `Elemento origen no encontrado: ${dto.sourceElementId}`);
    const target = await detailRepo.findById(dto.targetElementId);
    if (!target) throw new GenericException(404, `Elemento destino no encontrado: ${dto.targetElementId}`);

    if (await convRepo.existsBySourceElementIdAndTargetElementId(dto.sourceElementId, dto.targetElementId)) {
        throw new GenericException(
            409,
            `El elemento ${target.key} del catálogo ${target.header?.name ?? ''} ya se encuentra agregado como conversión, seleccione otro elemento.`
        );
    }

    if (dto.validFrom && dto.validTo && new Date(dto.validTo) < new Date(dto.validFrom)) {
        throw new GenericException(400, 'La fecha de fin de vigencia no puede ser anterior a la de inicio.');
    }

    let shouldBePrincipal = dto.isPrincipal === true;
    const existingPrincipal = await convRepo.findBySourceElementIdAndIsPrincipalTrue(dto.sourceElementId);
    if (!existingPrincipal) {
        shouldBePrincipal = true;
    }

    if (shouldBePrincipal) {
        await convRepo.clearPrincipalBySourceElement(dto.sourceElementId, userId);
    }

    const repoInstance = datasource.getRepository(CatalogConversion);
    const conv = repoInstance.create({
        sourceElement: source,
        sourceElementId: source.id,
        targetElement: target,
        targetElementId: target.id,
        validFrom: dto.validFrom ?? null,
        validTo: dto.validTo ?? null,
        status: shouldBePrincipal ? CatalogConversion.STATUS_ACTIVE : CatalogConversion.STATUS_INACTIVE,
        isPrincipal: shouldBePrincipal,
        createdBy: userId
    });

    const saved = await repoInstance.save(conv);
    const full = await convRepo.findById(saved.id);
    return toDto(full!);
}

export async function update(id: number, dto: ConversionUpdateDto, userId: string): Promise<ConversionDto> {
    const conv = await convRepo.findById(id);
    if (!conv) throw new GenericException(404, `Conversión no encontrada: ${id}`);

    const newTarget = await detailRepo.findById(dto.targetElementId);
    if (!newTarget) throw new GenericException(404, `Elemento destino no encontrado: ${dto.targetElementId}`);

    const sourceId = conv.sourceElement.id;
    if (conv.targetElement.id !== dto.targetElementId) {
        if (await convRepo.existsBySourceElementIdAndTargetElementId(sourceId, dto.targetElementId)) {
            throw new GenericException(
                409,
                `El elemento ${newTarget.key} del catálogo ${newTarget.header?.name ?? ''} ya se encuentra agregado como conversión, seleccione otro elemento.`
            );
        }
    }

    if (dto.validFrom && dto.validTo && new Date(dto.validTo) < new Date(dto.validFrom)) {
        throw new GenericException(400, 'La fecha de fin de vigencia no puede ser anterior a la de inicio.');
    }

    conv.targetElement = newTarget;
    conv.targetElementId = newTarget.id;
    if (dto.validFrom != null) conv.validFrom = dto.validFrom;
    if (dto.validTo != null) conv.validTo = dto.validTo;
    if (dto.status != null) conv.status = dto.status;

    if (conv.isPrincipal === true) {
        conv.status = CatalogConversion.STATUS_ACTIVE;
    }
    conv.updatedBy = userId;

    const saved = await convRepo.save(conv);
    const full = await convRepo.findById(saved.id);
    return toDto(full!);
}

export async function setPrincipal(id: number, isPrincipal: boolean, userId: string): Promise<ConversionDto> {
    const conv = await convRepo.findById(id);
    if (!conv) throw new GenericException(404, `Conversión no encontrada: ${id}`);

    if (isPrincipal === true) {
        await convRepo.clearPrincipalBySourceElement(conv.sourceElement.id, userId);
        conv.isPrincipal = true;
        conv.status = CatalogConversion.STATUS_ACTIVE;
    } else {
        conv.isPrincipal = false;
        conv.status = CatalogConversion.STATUS_INACTIVE;
    }
    conv.updatedBy = userId;

    const saved = await convRepo.save(conv);
    const full = await convRepo.findById(saved.id);
    return toDto(full!);
}

export async function deleteById(id: number): Promise<void> {
    if (!(await convRepo.existsById(id))) {
        throw new GenericException(404, `Conversión no encontrada: ${id}`);
    }
    await convRepo.deleteById(id);
}

export async function deleteMultiple(ids: number[]): Promise<void> {
    await convRepo.deleteAllByIdInBatch(ids);
}

