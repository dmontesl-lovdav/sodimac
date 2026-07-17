import { logger } from "@/utils/logger.js";

export interface MigoCsvRow {
    Nro_OC: string;
    Nro_Recepcion: string;
    Numero_Proveedor: string;
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

type RawCsvRow = Record<string, string>;

interface OcGroup {
    sumImporte: number;
    montoOc: number;
}

export const REQUIRED_HEADERS = [
    'Nro_OC',
    'Nro_Recepcion',
    'Numero_Proveedor',
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

export function parseCsv(
    content: string,
): { headers: string[]; rows: RawCsvRow[] } {
    const lines = content
        .replace(/\r\n/g, '\n')
        .replace(/\r/g, '\n')
        .split('\n')
        .filter((line) => line.trim().length > 0);

    if (lines.length === 0) {
        return { headers: [], rows: [] };
    }

    const separator = lines[0]!.includes(';') ? ';' : ',';
    const headers = lines[0]!
        .split(separator)
        .map((header) => header.trim().replace(/^"|"$/g, ''));

    const rows: RawCsvRow[] = [];

    for (let index = 1; index < lines.length; index++) {
        const values = lines[index]!
            .split(separator)
            .map((value) => value.trim().replace(/^"|"$/g, ''));

        const row: RawCsvRow = {};

        headers.forEach((header, headerIndex) => {
            row[header] = values[headerIndex] ?? '';
        });

        rows.push(row);
    }

    return { headers, rows };
}

function isNumeric(value: string): boolean {
    if (!value || value.trim() === '') {
        return false;
    }

    return !Number.isNaN(Number(value.replace(',', '.')));
}

function toNum(value: string): number {
    return Number(value.replace(',', '.'));
}

export function parseLayoutDate(value: string): Date | null {
    if (!value) {
        return null;
    }

    const trimmedValue = value.trim();

    if (trimmedValue === '') {
        return null;
    }

    const isoDate = parseIsoDate(trimmedValue);
    if (isoDate !== null) {
        return isoDate;
    }

    return parseDayMonthYearDate(trimmedValue);
}

function parseIsoDate(value: string): Date | null {
    const match = /^(\d{4})-(\d{2})-(\d{2})/.exec(value);

    if (!match) {
        return null;
    }

    const year = Number(match[1]);
    const month = Number(match[2]) - 1;
    const day = Number(match[3]);
    const hasTime =
        value.includes('T') ||
        /\d{2}:\d{2}/.test(value);

    if (hasTime) {
        const parsed = new Date(value);
        return Number.isNaN(parsed.getTime()) ? null : parsed;
    }

    const localDate = new Date(year, month, day);

    return isValidYmd(localDate, year, month, day)
        ? localDate
        : null;
}

function parseDayMonthYearDate(value: string): Date | null {
    const match = /^(\d{1,2})[/-](\d{1,2})[/-](\d{4})$/.exec(value);

    if (!match) {
        return null;
    }

    const day = Number(match[1]);
    const month = Number(match[2]) - 1;
    const year = Number(match[3]);
    const localDate = new Date(year, month, day);

    return isValidYmd(localDate, year, month, day)
        ? localDate
        : null;
}

function isValidYmd(
    date: Date,
    year: number,
    month: number,
    day: number,
): boolean {
    return (
        !Number.isNaN(date.getTime()) &&
        date.getFullYear() === year &&
        date.getMonth() === month &&
        date.getDate() === day
    );
}

function isValidDate(value: string): boolean {
    return parseLayoutDate(value) !== null;
}

function buildEmptyLayoutResult(): ValidationResult {
    return {
        valid: false,
        globalError: {
            code: 'WRN7018',
            message: 'Layout de recepciones está vacío, favor de revisar.',
        },
        parsedRows: [],
        totalRows: 0,
        totalValid: 0,
        totalInvalid: 0,
    };
}

function buildInvalidHeadersResult(totalRows: number): ValidationResult {
    return {
        valid: false,
        globalError: {
            code: 'WRN7020',
            message: 'La cabecera del layout esta incorrecta, favor de validar.',
        },
        parsedRows: [],
        totalRows,
        totalValid: 0,
        totalInvalid: totalRows,
    };
}

function headersAreValid(headers: string[]): boolean {
    const allHeaders = [
        ...REQUIRED_HEADERS,
        ...OPTIONAL_HEADERS,
    ];

    const requiredMatch = REQUIRED_HEADERS.every(
        (header, index) => headers[index] === header,
    );

    const hasOnlyKnownHeaders = headers.every(
        (header) => allHeaders.includes(header),
    );

    return (
        requiredMatch &&
        hasOnlyKnownHeaders &&
        headers.length >= REQUIRED_HEADERS.length
    );
}

function addNumericValidationErrors(
    row: RawCsvRow,
    errors: string[],
    hasMontoOc: boolean,
): void {
    const numericFields: Array<{
        key: string;
        message: string;
    }> = [
            {
                key: 'Nro_OC',
                message: 'Nro_OC debe ser numérico',
            },
            {
                key: 'Nro_Recepcion',
                message: 'Nro_Recepcion debe ser numérico',
            },
            {
                key: 'Sucursal',
                message: 'Sucursal debe ser numérico',
            },
            {
                key: 'Importe_sin_impuesto',
                message: 'Importe_sin_impuesto debe ser numérico',
            },
            {
                key: 'Cantidad',
                message: 'Cantidad debe ser numérico',
            },
            {
                key: 'Importe_Unitario',
                message: 'Importe_Unitario debe ser numérico',
            },
            {
                key: 'Importe_SinImpuesto',
                message: 'Importe_SinImpuesto debe ser numérico',
            },
        ];

    for (const field of numericFields) {
        if (!isNumeric(row[field.key] ?? '')) {
            errors.push(field.message);
        }
    }

    if (!isValidDate(row['Fecha_Recepcion'] ?? '')) {
        errors.push('Fecha_Recepcion debe ser una fecha válida');
    }

    const montoOc = row['MontoOC'] ?? '';
    const mustValidateMontoOc =
        hasMontoOc &&
        montoOc.trim() !== '';

    if (mustValidateMontoOc && !isNumeric(montoOc)) {
        errors.push('MontoOC debe ser numérico');
    }
}

function addPositiveValueErrors(
    row: RawCsvRow,
    errors: string[],
): void {
    validatePositiveValue(
        row['Cantidad'] ?? '',
        'Cantidad debe ser mayor a 0',
        errors,
    );

    validatePositiveValue(
        row['Importe_Unitario'] ?? '',
        'Importe_Unitario debe ser mayor a 0',
        errors,
    );

    validatePositiveValue(
        row['Importe_SinImpuesto'] ?? '',
        'Importe_SinImpuesto debe ser mayor a 0',
        errors,
    );
}

function validatePositiveValue(
    value: string,
    message: string,
    errors: string[],
): void {
    if (isNumeric(value) && toNum(value) <= 0) {
        errors.push(message);
    }
}

function addCalculatedAmountError(
    row: RawCsvRow,
    errors: string[],
): void {
    const unitAmount = row['Importe_Unitario'] ?? '';
    const quantity = row['Cantidad'] ?? '';
    const totalAmount = row['Importe_SinImpuesto'] ?? '';

    if (
        !isNumeric(unitAmount) ||
        !isNumeric(quantity) ||
        !isNumeric(totalAmount)
    ) {
        return;
    }

    const calculated =
        Math.round(
            toNum(unitAmount) *
            toNum(quantity) *
            100,
        ) / 100;

    const expected =
        Math.round(toNum(totalAmount) * 100) / 100;

    if (Math.abs(calculated - expected) <= 0.01) {
        return;
    }

    errors.push(
        `Importe_Unitario (${unitAmount}) * ` +
        `Cantidad (${quantity}) = ${calculated}, ` +
        `pero Importe_SinImpuesto es ${expected}`,
    );
}

function buildParsedRow(
    row: RawCsvRow,
    rowNumber: number,
    hasMontoOc: boolean,
): ParsedRow {
    const errors: string[] = [];

    addNumericValidationErrors(
        row,
        errors,
        hasMontoOc,
    );

    addPositiveValueErrors(
        row,
        errors,
    );

    addCalculatedAmountError(
        row,
        errors,
    );

    return {
        Nro_OC: row['Nro_OC']?.trim() ?? '',
        Nro_Recepcion: row['Nro_Recepcion']?.trim() ?? '',
        Numero_Proveedor: row['Numero_Proveedor']?.trim() ?? '',
        Sucursal: row['Sucursal']?.trim() ?? '',
        Nro_Guia: row['Nro_Guia']?.trim() ?? '',
        Origen: row['Origen']?.trim() ?? '',
        Fecha_Recepcion: row['Fecha_Recepcion']?.trim() ?? '',
        Importe_sin_impuesto:
            row['Importe_sin_impuesto']?.trim() ?? '',
        SKU: row['SKU']?.trim() ?? '',
        Descripcion_Sku:
            row['Descripcion_Sku']?.trim() ?? '',
        Cantidad: row['Cantidad']?.trim() ?? '',
        Importe_Unitario:
            row['Importe_Unitario']?.trim() ?? '',
        Importe_SinImpuesto:
            row['Importe_SinImpuesto']?.trim() ?? '',
        MontoOC:
            hasMontoOc
                ? row['MontoOC']?.trim() ?? ''
                : '',
        rowNumber,
        isValid: errors.length === 0,
        errors,
    };
}

function parseRows(
    rows: RawCsvRow[],
    hasMontoOc: boolean,
): ParsedRow[] {
    return rows.map(
        (row, index) =>
            buildParsedRow(
                row,
                index + 2,
                hasMontoOc,
            ),
    );
}

function canBeGroupedByOc(row: ParsedRow): boolean {
    return (
        row.isValid &&
        isNumeric(row.Nro_OC) &&
        isNumeric(row.Importe_SinImpuesto) &&
        !!row.MontoOC &&
        isNumeric(row.MontoOC)
    );
}

function buildOcGroups(
    parsedRows: ParsedRow[],
): Map<string, OcGroup> {
    const groups = new Map<string, OcGroup>();

    for (const row of parsedRows) {
        if (!canBeGroupedByOc(row)) {
            continue;
        }

        const key = row.Nro_OC;
        const currentGroup = groups.get(key) ?? {
            sumImporte: 0,
            montoOc: toNum(row.MontoOC),
        };

        currentGroup.sumImporte =
            Math.round(
                (
                    currentGroup.sumImporte +
                    toNum(row.Importe_SinImpuesto)
                ) * 100,
            ) / 100;

        groups.set(key, currentGroup);
    }

    return groups;
}

function invalidateRowsWithMontoDifference(
    parsedRows: ParsedRow[],
    ocKey: string,
    group: OcGroup,
): void {
    const montoOcRounded =
        Math.round(group.montoOc * 100) / 100;

    if (
        Math.abs(
            group.sumImporte - montoOcRounded,
        ) <= 0.01
    ) {
        return;
    }

    const message =
        `Suma Importe_SinImpuesto de OC ${ocKey} = ` +
        `${group.sumImporte}, pero MontoOC = ${montoOcRounded}`;

    for (const row of parsedRows) {
        if (row.Nro_OC !== ocKey || !row.isValid) {
            continue;
        }

        row.isValid = false;
        row.errors.push(message);
    }
}

function applyCrossRowValidation(
    parsedRows: ParsedRow[],
    hasMontoOc: boolean,
): void {
    if (!hasMontoOc) {
        return;
    }

    const groups = buildOcGroups(parsedRows);

    for (const [ocKey, group] of groups) {
        invalidateRowsWithMontoDifference(
            parsedRows,
            ocKey,
            group,
        );
    }
}

function buildValidationResult(
    parsedRows: ParsedRow[],
): ValidationResult {
    const totalValid = parsedRows.filter(
        (row) => row.isValid,
    ).length;

    const totalInvalid =
        parsedRows.length - totalValid;

    logger.info(
        `[MIGO VALIDATION] Rows=${parsedRows.length}, ` +
        `valid=${totalValid}, invalid=${totalInvalid}`,
    );

    return {
        valid: totalValid > 0,
        parsedRows,
        totalRows: parsedRows.length,
        totalValid,
        totalInvalid,
    };
}

export function validateLayout(
    content: string,
): ValidationResult {
    const { headers, rows } = parseCsv(content);

    if (headers.length === 0 || rows.length === 0) {
        return buildEmptyLayoutResult();
    }

    if (!headersAreValid(headers)) {
        return buildInvalidHeadersResult(rows.length);
    }

    const hasMontoOc = headers.includes('MontoOC');
    const parsedRows = parseRows(
        rows,
        hasMontoOc,
    );

    applyCrossRowValidation(
        parsedRows,
        hasMontoOc,
    );

    return buildValidationResult(parsedRows);
}