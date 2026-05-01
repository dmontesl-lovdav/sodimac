import ExcelJS from 'exceljs';
import { randomUUID } from 'node:crypto';
import * as headerRepo from '@/repositories/catalogHeader.repo.js';
import * as detailRepo from '@/repositories/catalogDetail.repo.js';
import type { LayoutValidationError, LayoutValidationResponse } from '@/dto/layoutValidation.dto.js';

const ERROR_THRESHOLD = 20;
const REQUIRED_HEADERS = ['tipoCatalogo', 'elemento', 'valor', 'fechaInicioVigencia', 'fechaFinVigencia', 'idPadre'];
const COL = ['A', 'B', 'C', 'D', 'E', 'F', 'G'];
const MAX_TIPO = 20;
const MAX_ELEM = 512;
const MAX_VAL = 100;
const MAX_ID = 10;
const MAX_EXTKEY = 50;
const EXTKEY_PATTERN = /^[a-zA-Z0-9._-]+$/;

const reportCache = new Map<string, string>();

function isoDate(d: Date): string {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
}

function parseD(v: string | null | undefined): Date | null {
    if (!v || v.trim() === '') return null;
    const trimmed = v.trim();

    let match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(trimmed);
    if (match) {
        const d = new Date(Number(match[1]), Number(match[2]) - 1, Number(match[3]));
        return isNaN(d.getTime()) ? null : d;
    }

    match = /^(\d{2})-(\d{2})-(\d{4})$/.exec(trimmed);
    if (match) {
        const d = new Date(Number(match[3]), Number(match[2]) - 1, Number(match[1]));
        return isNaN(d.getTime()) ? null : d;
    }
    return null;
}

function hasForbidden(v: string, allowSpaces: boolean): boolean {
    const pattern = allowSpaces
        ? /[!?,¡¿:;.@#%^&*(){}[\]<>/'"\\`]/
        : /[!?,¡¿:;.@#%^&*(){}[\]<>/'"\\` ]/;
    return pattern.test(v);
}

function hasSpecial(v: string): boolean {
    return /[@#%^&*(){}[\]<>/'"\\`]/.test(v);
}

function err(rn: number, cl: string, desc: string): LayoutValidationError {
    const cell = `${cl}${rn}`;
    return {
        row: rn,
        cell,
        column: cl,
        message: `Error en la celda ${cell}: ${desc}`
    };
}

function cellValue(cell: ExcelJS.Cell | undefined): string | null {
    if (!cell || cell.value == null) return null;
    const v = cell.value as any;

    if (v instanceof Date) return isoDate(v);
    if (typeof v === 'number') {
        if (v === Math.floor(v)) return String(v);
        return String(v);
    }
    if (typeof v === 'string') return v.trim();
    if (typeof v === 'boolean') return String(v);

    if (v && typeof v === 'object') {
        if ('text' in v && typeof v.text === 'string') return v.text.trim();
        if ('richText' in v && Array.isArray(v.richText)) {
            return v.richText.map((r: any) => r.text ?? '').join('').trim();
        }
        if ('result' in v) {
            const result = v.result;
            if (result instanceof Date) return isoDate(result);
            if (typeof result === 'number') return String(result);
            if (typeof result === 'string') return result.trim();
        }
        if ('formula' in v && 'value' in v) {
            if (v.value instanceof Date) return isoDate(v.value);
            return v.value != null ? String(v.value) : null;
        }
    }

    return String(v);
}

function rowIsEmpty(row: ExcelJS.Row | undefined, hasColG: boolean): boolean {
    if (!row) return true;
    const cols = hasColG ? 7 : 6;
    for (let i = 1; i <= cols; i++) {
        const v = cellValue(row.getCell(i));
        if (v != null && v !== '') return false;
    }
    return true;
}

function hasColumnG(headerRow: ExcelJS.Row | undefined): boolean {
    if (!headerRow) return false;
    const g = cellValue(headerRow.getCell(7));
    return !!g && g.trim().toLowerCase() === 'valorconversion';
}

function validateHeaders(sheet: ExcelJS.Worksheet, errs: LayoutValidationError[]): boolean {
    const headerRow = sheet.getRow(1);
    if (!headerRow || !headerRow.hasValues) {
        errs.push(
            err(
                1,
                'A',
                'Falta la fila de encabezados. Las columnas obligatorias son: tipoCatalogo, elemento, valor, fechaInicioVigencia, fechaFinVigencia, idPadre.'
            )
        );
        return false;
    }

    let ok = true;
    for (let i = 0; i < 6; i++) {
        const v = cellValue(headerRow.getCell(i + 1));
        const cr = `${COL[i]}1`;
        if (!v || v.trim() === '') {
            errs.push({
                row: 1,
                cell: cr,
                column: REQUIRED_HEADERS[i]!,
                message: `Error en la celda ${cr}: Falta la columna '${REQUIRED_HEADERS[i]}'. Las columnas obligatorias son: tipoCatalogo, elemento, valor, fechaInicioVigencia, fechaFinVigencia, idPadre.`
            });
            ok = false;
        } else if (v.trim().toLowerCase() !== REQUIRED_HEADERS[i]!.toLowerCase()) {
            errs.push({
                row: 1,
                cell: cr,
                column: REQUIRED_HEADERS[i]!,
                message: `Error en la celda ${cr}: El nombre de columna '${v.trim()}' es incorrecto. Debe ser '${REQUIRED_HEADERS[i]}'.`
            });
            ok = false;
        }
    }

    const gv = cellValue(headerRow.getCell(7));
    if (gv && gv.trim() !== '' && gv.trim().toLowerCase() !== 'valorconversion') {
        errs.push({
            row: 1,
            cell: 'G1',
            column: 'valorConversion',
            message: `Error en la celda G1: El nombre de columna '${gv.trim()}' es incorrecto. Debe ser 'valorConversion'.`
        });
        ok = false;
    }

    return ok;
}

function build(errs: LayoutValidationError[], rows: number): LayoutValidationResponse {
    let reportId: string | null = null;
    let reportAvailable = false;
    if (errs.length > ERROR_THRESHOLD) {
        reportId = randomUUID();
        reportAvailable = true;
        const lines: string[] = [`=== REPORTE DE ERRORES ===`, `Total: ${errs.length}`, ''];
        for (const e of errs) {
            lines.push(`Fila:${e.row} Celda:${e.cell} ${e.message}`);
        }
        reportCache.set(reportId, lines.join('\n'));
    }

    return {
        isValid: errs.length === 0,
        errorCount: errs.length,
        errors: errs,
        reportAvailable,
        reportId,
        rowsProcessed: rows
    };
}

export async function validateLayout(
    buffer: Buffer,
    tipoSel: string,
    nombre: string
): Promise<LayoutValidationResponse> {
    const errs: LayoutValidationError[] = [];
    const elemFirstRow = new Map<string, number>();
    let rows = 0;

    try {
        const wb = new ExcelJS.Workbook();
        await wb.xlsx.load(buffer as unknown as ArrayBuffer);
        const sheet = wb.worksheets[0];
        if (!sheet) {
            errs.push({
                row: 0,
                cell: 'N/A',
                column: 'archivo',
                message: 'El archivo no contiene hojas.'
            });
            return build(errs, rows);
        }

        const merges = (sheet as any).model?.merges as string[] | undefined;
        if (merges && Array.isArray(merges)) {
            for (const m of merges) {
                const match = /^([A-Z]+)(\d+):/.exec(m);
                if (match) {
                    const firstRow = Number(match[2]);
                    const colLetter = match[1]!;
                    const colIdx = Math.min(
                        COL.indexOf(colLetter.length === 1 ? colLetter : 'F'),
                        6
                    );
                    const safeCol = colIdx >= 0 ? COL[colIdx]! : 'A';
                    errs.push(
                        err(
                            firstRow,
                            safeCol,
                            'La celda está combinada con otras celdas. No se permiten celdas combinadas en el archivo.'
                        )
                    );
                }
            }
        }

        if (!validateHeaders(sheet, errs)) return build(errs, rows);

        if ((await headerRepo.findByName(nombre)).length > 0) {
            errs.push({
                row: 0,
                cell: 'N/A',
                column: 'nombre',
                message: `El catálogo '${nombre}' ya existe en el sistema. No se permite duplicar nombres de catálogos.`
            });
        }

        const headerRow = sheet.getRow(1);
        const hasColG = hasColumnG(headerRow);
        const elems = new Set<string>();

        let lastData = -1;
        const totalRows = sheet.rowCount;
        for (let i = totalRows; i >= 2; i--) {
            const r = sheet.getRow(i);
            if (r && !rowIsEmpty(r, hasColG)) {
                lastData = i - 1;
                break;
            }
        }

        for (let i = 2; i <= Math.max(lastData + 1, 1); i++) {
            const r = sheet.getRow(i);
            if (!r || rowIsEmpty(r, hasColG)) continue;
            const t = cellValue(r.getCell(1));
            if (t && t.trim() !== '') {

            }
        }

        for (let i = 2; i <= Math.max(lastData + 1, 1); i++) {
            const r = sheet.getRow(i);
            const rn = i;

            if (!r || rowIsEmpty(r, hasColG)) {
                if (i - 1 < lastData) {
                    errs.push({
                        row: rn,
                        cell: `A${rn}`,
                        column: 'fila',
                        message:
                            'La fila está completamente vacía. No se permiten filas vacías entre registros válidos. Las filas vacías solo se permiten al final del archivo.'
                    });
                }
                continue;
            }
            rows++;

            const tipo = cellValue(r.getCell(1));
            const elem = cellValue(r.getCell(2));
            const val = cellValue(r.getCell(3));
            const fi = cellValue(r.getCell(4));
            const ff = cellValue(r.getCell(5));
            const padre = cellValue(r.getCell(6));

            if (!tipo || tipo.trim() === '')
                errs.push(err(rn, 'A', "El campo 'tipoCatalogo' es obligatorio y no puede estar vacío."));
            if (!elem || elem.trim() === '')
                errs.push(err(rn, 'B', "El campo 'elemento' es obligatorio y no puede estar vacío."));
            if (!fi || fi.trim() === '')
                errs.push(err(rn, 'D', "El campo 'fechaInicioVigencia' es obligatorio y no puede estar vacío."));

            if (tipo && tipo.trim() !== '') {
                const tn = tipo.trim().toUpperCase();
                if (tn !== 'PRIMARIO' && tn !== 'SECUNDARIO')
                    errs.push(err(rn, 'A', `El tipo de catálogo '${tipo}' no es válido. Los valores permitidos son: 'primario' o 'secundario'.`));
                else if (tn !== tipoSel.toUpperCase())
                    errs.push(err(rn, 'A', `El tipo de catálogo del archivo ('${tipo}') no coincide con el tipo seleccionado en el formulario ('${tipoSel}'). Verifique que el archivo corresponda al tipo de catálogo que está creando.`));
                if (tipo.length > MAX_TIPO)
                    errs.push(err(rn, 'A', `El tipo de catálogo excede la longitud máxima permitida de ${MAX_TIPO} caracteres.`));
            }

            if (elem && elem.trim() !== '') {
                if (elem !== elem.trim())
                    errs.push(err(rn, 'B', 'El nombre del elemento no puede tener espacios al inicio o al final.'));
                if (hasForbidden(elem, true))
                    errs.push(err(rn, 'B', 'El valor contiene caracteres no permitidos. Caracteres prohibidos: ! ? , ¡ ¿ : ; .'));
                if (hasSpecial(elem))
                    errs.push(err(rn, 'B', 'El valor contiene caracteres especiales no permitidos: @ # % ^ & * ( ) { } < > / \' "'));
                if (elem.length > MAX_ELEM)
                    errs.push(err(rn, 'B', `El nombre del elemento excede la longitud máxima permitida de ${MAX_ELEM} caracteres.`));
                const normalizedKey = elem.trim().toLowerCase();
                if (elems.has(normalizedKey)) {
                    const prevRow = elemFirstRow.get(normalizedKey);
                    errs.push(err(rn, 'B', `El elemento '${elem}' está duplicado en el archivo. Ya aparece en la fila ${prevRow ?? 'anterior'}. Cada elemento debe ser único dentro del catálogo.`));
                } else {
                    elems.add(normalizedKey);
                    elemFirstRow.set(normalizedKey, rn);
                }
                if (await detailRepo.existsByKeyIgnoreCase(elem)) {
                    errs.push(err(rn, 'B', `El elemento '${elem}' ya existe en el sistema. No se pueden registrar elementos duplicados.`));
                }
            }

            if (val && val.trim() !== '') {
                if (val.includes(' '))
                    errs.push(err(rn, 'C', "El valor no puede contener espacios. Los espacios solo están permitidos en la columna 'elemento'."));
                if (hasForbidden(val, false))
                    errs.push(err(rn, 'C', 'El valor contiene caracteres no permitidos. Caracteres prohibidos: ! ? , ¡ ¿ : ; .'));
                if (hasSpecial(val))
                    errs.push(err(rn, 'C', 'El valor contiene caracteres especiales no permitidos: @ # % ^ & * ( ) { } < > / \' "'));
                if (val.length > MAX_VAL)
                    errs.push(err(rn, 'C', `El valor excede la longitud máxima permitida de ${MAX_VAL} caracteres.`));
            }

            const di = parseD(fi);
            const df = parseD(ff);
            if (fi && fi.trim() !== '' && !di)
                errs.push(err(rn, 'D', `La fecha '${fi}' no tiene un formato válido. Use el formato ISO-8601: yyyy-mm-dd (ejemplo: 2026-01-15).`));
            if (ff && ff.trim() !== '' && !df)
                errs.push(err(rn, 'E', `La fecha '${ff}' no tiene un formato válido. Use el formato ISO-8601: yyyy-mm-dd (ejemplo: 2026-01-15).`));
            if (di && df && di > df)
                errs.push(err(rn, 'D', `La fecha de inicio de vigencia (${fi}) no puede ser posterior a la fecha de fin de vigencia (${ff}).`));
            const todayD = new Date();
            todayD.setHours(0, 0, 0, 0);
            if (df && df <= todayD)
                errs.push(err(rn, 'E', `La fecha de fin de vigencia (${ff}) debe ser mayor a la fecha actual (${isoDate(todayD)}).`));

            if (padre && padre.trim() !== '') {
                const pid = Number.parseInt(padre.trim(), 10);
                if (Number.isNaN(pid)) {
                    errs.push(err(rn, 'F', `El valor '${padre}' no es un número entero válido. El campo idPadre debe ser numérico.`));
                } else if (tipoSel.toUpperCase() === 'PRIMARIO') {
                    errs.push(err(rn, 'F', 'Los catálogos primarios no deben tener idPadre. El campo debe estar vacío.'));
                } else if (tipoSel.toUpperCase() === 'SECUNDARIO') {
                    const parent = await detailRepo.findById(pid);
                    if (!parent) {
                        errs.push(err(rn, 'F', `El idPadre '${pid}' no existe en el sistema. Verifique que el ID sea correcto.`));
                    } else if (parent.header) {
                        const parentType = parent.header.catalogType?.toUpperCase();
                        if (parentType !== 'PRIMARIO' && parentType !== 'HIERARCHICAL') {
                            errs.push(err(rn, 'F', `El idPadre '${pid}' debe corresponder a un elemento de un catálogo de tipo primario o HIERARCHICAL.`));
                        }
                    }
                }
                if (padre.length > MAX_ID)
                    errs.push(err(rn, 'F', `El idPadre excede la longitud máxima permitida de ${MAX_ID} caracteres.`));
            }

            if (hasColG) {
                const extKey = cellValue(r.getCell(7));
                if (extKey && extKey.trim() !== '') {
                    if (extKey.length > MAX_EXTKEY)
                        errs.push(err(rn, 'G', `El valor de conversión no puede exceder ${MAX_EXTKEY} caracteres (actual: ${extKey.length} caracteres).`));
                    else if (!EXTKEY_PATTERN.test(extKey))
                        errs.push(err(rn, 'G', 'El valor de conversión solo puede contener letras, números, guiones, guiones bajos y puntos.'));
                }
            }
        }
    } catch (e) {
        errs.push({
            row: 0,
            cell: 'N/A',
            column: 'archivo',
            message: `Error al procesar el archivo: ${(e as Error).message}`
        });
    }

    return build(errs, rows);
}

export function getValidationReport(id: string): string | null {
    return reportCache.get(id) ?? null;
}

