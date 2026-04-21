import { logger } from "@/utils/logger.js";

export interface MigoCsvRow {
    Nro_OC: string;
    Nro_Recepcion: string;
    Sucursal: string;
    Nro_Guia: string;
    Origen: string;
    Fecha_Recepcion: string;
    Importe_sin_impuesto: string;
    SKU: string;
    Descripcion_Sku: string;
    Cantidad: string;
    Importe_Unitario: string;
    Importe_SinImpuesto: string;
    MontoOC: string;
}

export interface ParsedRow extends MigoCsvRow {
    rowNumber: number;
    isValid: boolean;
    errors: string[];
}

export interface ValidationResult {
    valid: boolean;
    globalError?: { code: string; message: string };
    parsedRows: ParsedRow[];
    totalRows: number;
    totalValid: number;
    totalInvalid: number;
}

const REQUIRED_HEADERS = [
    'Nro_OC',
    'Nro_Recepcion',
    'Sucursal',
    'Nro_Guia',
    'Origen',
    'Fecha_Recepcion',
    'Importe_sin_impuesto',
    'SKU',
    'Descripcion_Sku',
    'Cantidad',
    'Importe_Unitario',
    'Importe_SinImpuesto',
];

const OPTIONAL_HEADERS = ['MontoOC'];

export function parseCsv(content: string): { headers: string[]; rows: Record<string, string>[] } {
    const lines = content
        .replace(/\r\n/g, '\n')
        .replace(/\r/g, '\n')
        .split('\n')
        .filter(l => l.trim().length > 0);

    if (lines.length === 0) return { headers: [], rows: [] };

    const separator = lines[0]!.includes(';') ? ';' : ',';
    const headers = lines[0]!.split(separator).map(h => h.trim().replace(/^"|"$/g, ''));

    const rows: Record<string, string>[] = [];
    for (let i = 1; i < lines.length; i++) {
        const values = lines[i]!.split(separator).map(v => v.trim().replace(/^"|"$/g, ''));
        const row: Record<string, string> = {};
        headers.forEach((h, idx) => { row[h] = values[idx] ?? ''; });
        rows.push(row);
    }

    return { headers, rows };
}

function isNumeric(val: string): boolean {
    if (!val || val.trim() === '') return false;
    return !isNaN(Number(val.replace(',', '.')));
}

function toNum(val: string): number {
    return Number(val.replace(',', '.'));
}

function isValidDate(val: string): boolean {
    if (!val || val.trim() === '') return false;
    const d = new Date(val);
    return !isNaN(d.getTime());
}

export function validateLayout(content: string): ValidationResult {
    const { headers, rows } = parseCsv(content);

    // WRN7018: archivo vacío
    if (headers.length === 0 || rows.length === 0) {
        return {
            valid: false,
            globalError: { code: 'WRN7018', message: 'Layout de recepciones está vacío, favor de revisar.' },
            parsedRows: [],
            totalRows: 0,
            totalValid: 0,
            totalInvalid: 0,
        };
    }

    // WRN7020: cabecera incorrecta (las 12 obligatorias deben estar en orden)
    const allHeaders = [...REQUIRED_HEADERS, ...OPTIONAL_HEADERS];
    const requiredMatch = REQUIRED_HEADERS.every((h, i) => headers[i] === h);
    const hasOnlyKnownHeaders = headers.every(h => allHeaders.includes(h));
    const headersMatch = requiredMatch && hasOnlyKnownHeaders && headers.length >= REQUIRED_HEADERS.length;
    const hasMontoOc = headers.includes('MontoOC');
    if (!headersMatch) {
        return {
            valid: false,
            globalError: { code: 'WRN7020', message: 'La cabecera del layout esta incorrecta, favor de validar.' },
            parsedRows: [],
            totalRows: rows.length,
            totalValid: 0,
            totalInvalid: rows.length,
        };
    }

    const parsedRows: ParsedRow[] = [];

    for (let i = 0; i < rows.length; i++) {
        const row = rows[i]!;
        const rowNum = i + 2;
        const errors: string[] = [];

        // Tipo de dato: numéricos
        if (!isNumeric(row['Nro_OC'] ?? '')) errors.push('Nro_OC debe ser numérico');
        if (!isNumeric(row['Nro_Recepcion'] ?? '')) errors.push('Nro_Recepcion debe ser numérico');
        if (!isNumeric(row['Sucursal'] ?? '')) errors.push('Sucursal debe ser numérico');
        if (!isValidDate(row['Fecha_Recepcion'] ?? '')) errors.push('Fecha_Recepcion debe ser una fecha válida');
        if (!isNumeric(row['Importe_sin_impuesto'] ?? '')) errors.push('Importe_sin_impuesto debe ser numérico');
        if (!isNumeric(row['Cantidad'] ?? '')) errors.push('Cantidad debe ser numérico');
        if (!isNumeric(row['Importe_Unitario'] ?? '')) errors.push('Importe_Unitario debe ser numérico');
        if (!isNumeric(row['Importe_SinImpuesto'] ?? '')) errors.push('Importe_SinImpuesto debe ser numérico');
        if (hasMontoOc && row['MontoOC'] && row['MontoOC'].trim() !== '' && !isNumeric(row['MontoOC'])) {
            errors.push('MontoOC debe ser numérico');
        }

        // Valores > 0
        if (isNumeric(row['Cantidad'] ?? '') && toNum(row['Cantidad']!) <= 0) {
            errors.push('Cantidad debe ser mayor a 0');
        }
        if (isNumeric(row['Importe_Unitario'] ?? '') && toNum(row['Importe_Unitario']!) <= 0) {
            errors.push('Importe_Unitario debe ser mayor a 0');
        }
        if (isNumeric(row['Importe_SinImpuesto'] ?? '') && toNum(row['Importe_SinImpuesto']!) <= 0) {
            errors.push('Importe_SinImpuesto debe ser mayor a 0');
        }

        // Importe_Unitario * Cantidad == Importe_SinImpuesto
        if (
            isNumeric(row['Importe_Unitario'] ?? '') &&
            isNumeric(row['Cantidad'] ?? '') &&
            isNumeric(row['Importe_SinImpuesto'] ?? '')
        ) {
            const calc = Math.round(toNum(row['Importe_Unitario']!) * toNum(row['Cantidad']!) * 100) / 100;
            const expected = Math.round(toNum(row['Importe_SinImpuesto']!) * 100) / 100;
            if (Math.abs(calc - expected) > 0.01) {
                errors.push(`Importe_Unitario (${row['Importe_Unitario']}) * Cantidad (${row['Cantidad']}) = ${calc}, pero Importe_SinImpuesto es ${expected}`);
            }
        }

        parsedRows.push({
            Nro_OC: row['Nro_OC']?.trim() ?? '',
            Nro_Recepcion: row['Nro_Recepcion']?.trim() ?? '',
            Sucursal: row['Sucursal']?.trim() ?? '',
            Nro_Guia: row['Nro_Guia']?.trim() ?? '',
            Origen: row['Origen']?.trim() ?? '',
            Fecha_Recepcion: row['Fecha_Recepcion']?.trim() ?? '',
            Importe_sin_impuesto: row['Importe_sin_impuesto']?.trim() ?? '',
            SKU: row['SKU']?.trim() ?? '',
            Descripcion_Sku: row['Descripcion_Sku']?.trim() ?? '',
            Cantidad: row['Cantidad']?.trim() ?? '',
            Importe_Unitario: row['Importe_Unitario']?.trim() ?? '',
            Importe_SinImpuesto: row['Importe_SinImpuesto']?.trim() ?? '',
            MontoOC: hasMontoOc ? (row['MontoOC']?.trim() ?? '') : '',
            rowNumber: rowNum,
            isValid: errors.length === 0,
            errors,
        });
    }

    // Cross-row validation: sum of Importe_SinImpuesto per OC == MontoOC (solo si MontoOC existe en el CSV)
    if (hasMontoOc) {
        const ocGroups = new Map<string, { sumImporte: number; montoOc: number }>();
        for (const pr of parsedRows) {
            if (!pr.isValid) continue;
            if (!isNumeric(pr.Nro_OC) || !isNumeric(pr.Importe_SinImpuesto)) continue;
            if (!pr.MontoOC || !isNumeric(pr.MontoOC)) continue;

            const key = pr.Nro_OC;
            if (!ocGroups.has(key)) {
                ocGroups.set(key, { sumImporte: 0, montoOc: toNum(pr.MontoOC) });
            }
            const g = ocGroups.get(key)!;
            g.sumImporte = Math.round((g.sumImporte + toNum(pr.Importe_SinImpuesto)) * 100) / 100;
        }

        for (const [ocKey, g] of ocGroups) {
            const montoOcRounded = Math.round(g.montoOc * 100) / 100;
            if (Math.abs(g.sumImporte - montoOcRounded) > 0.01) {
                for (const pr of parsedRows) {
                    if (pr.Nro_OC === ocKey && pr.isValid) {
                        pr.isValid = false;
                        pr.errors.push(
                            `Suma Importe_SinImpuesto de OC ${ocKey} = ${g.sumImporte}, pero MontoOC = ${montoOcRounded}`
                        );
                    }
                }
            }
        }
    }

    const totalValid = parsedRows.filter(r => r.isValid).length;
    const totalInvalid = parsedRows.filter(r => !r.isValid).length;

    logger.info(`[MIGO VALIDATION] Rows=${parsedRows.length}, valid=${totalValid}, invalid=${totalInvalid}`);

    return {
        valid: totalValid > 0,
        parsedRows,
        totalRows: parsedRows.length,
        totalValid,
        totalInvalid,
    };
}
