import { datasource } from '@/config/typeorm-datasource.js';
import { SupplierType } from '@/entities/SupplierType.entity.js';

export const repo = () => datasource.getRepository(SupplierType);

export async function findById(id: number): Promise<SupplierType | null> {
    return repo().findOne({ where: { id } });
}

export async function findByCode(code: string): Promise<SupplierType | null> {
    return repo().findOne({ where: { code } });
}

export async function findByStatus(status: number): Promise<SupplierType[]> {
    return repo().find({ where: { status } });
}

export async function save(entity: SupplierType): Promise<SupplierType> {
    return repo().save(entity);
}
export interface SupplierTypeCatalogRow {
    id: number;
    code: string;
    description: string;
}

const SUPPLIER_TYPE_CODE_TO_CATALOG_KEY: Record<string, string> = {
    MERCANCIA: 'TPR001',
    TRANSPORTE: 'TPR002',
    INDIRECTOS: 'TPR003',
    SERVICIOS: 'TPR004',
};

export async function findActiveFromCatTipoProveedor(): Promise<SupplierTypeCatalogRow[]> {
    const sql = `
        SELECT
            st.id,
            st.code,
            COALESCE(dl.description, st.description) AS description
        FROM shared_catalogs.supplier_type st
        LEFT JOIN shared_catalogs.catalog_header ch
            ON ch.code = 'CatTipoProveedor'
        LEFT JOIN shared_catalogs.catalog_detail cd
            ON cd.header_id = ch.id
            AND cd.status = 1
            AND cd.key = CASE st.code
                WHEN 'MERCANCIA'  THEN 'TPR001'
                WHEN 'TRANSPORTE' THEN 'TPR002'
                WHEN 'INDIRECTOS' THEN 'TPR003'
                WHEN 'SERVICIOS'  THEN 'TPR004'
            END
        LEFT JOIN shared_catalogs.dictionary_lang dl
            ON dl.dict_id = cd.dict_id
            AND dl.lang_id = 1
        WHERE st.status = $1
        ORDER BY st.id
    `;
    const rows = await datasource.query(sql, [SupplierType.STATUS_ACTIVE]);
    return (rows ?? []).map((r: { id: number | string; code: string; description: string }) => ({
        id: Number(r.id),
        code: r.code,
        description: r.description,
    }));
}

export { SUPPLIER_TYPE_CODE_TO_CATALOG_KEY };

