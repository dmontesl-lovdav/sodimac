import * as React from 'react';
import { useNavigate } from 'react-router-dom';
import './DataTable.css'
/* --------------------------------- Types --------------------------------- */
export type Align = 'left' | 'center' | 'right';

export interface Column<T> {
    /** Encabezado visible */
    header: string | React.ReactNode;
    /** Alineación (default: left) */
    align?: Align;
    /** Render por celda (nav es opcional para permitir (row)=>... ) */
    render: (row: T, nav?: ReturnType<typeof useNavigate>) => React.ReactNode;
}

export interface RowAction<T> {
    /** Tooltip / título accesible del botón */
    title: string;
    /** Ícono (ruta a imagen/svg) */
    icon: string;
    /** Handler de click (recibe fila y nav) */
    onClick: (row: T, nav: ReturnType<typeof useNavigate>) => void;
    /** Deshabilitar acción por fila (opcional) */
    isDisabled?: (row: T) => boolean;
}

export interface GenericTableProps<T> {
    /* -- data -------------- */
    rows: T[];
    columns: Column<T>[];

    /* -- acciones por fila -- */
    actions?: RowAction<T>[];

    /* -- textos -- */
    emptyLabel?: string;

    /* -- paginación -------- */
    perPage?: number;
    page?: number;
    totalPages?: number;
    onChangePerPage?: (n: number) => void;
    onChangePage?: (n: number) => void;

    /* -- total global ------ */
    /** totalItems: total de registros **en el dataset completo** (no solo en esta página).
     *  Si no se pasa, se usa rows.length. */
    totalItems?: number;
}

/* ---------- util: mini-switch opcional ---------- */
export function Switch({
    on,
    onClick,
}: {
    on: boolean;
    onClick?: () => void;
}) {
    return (
        <span onClick={onClick} className={`gt-switch ${on ? 'on' : 'off'}`}>
            <span className={`gt-switch-thumb ${on ? 'on' : 'off'}`}>
                {on ? (
                    <svg viewBox="0 0 12 9" className="gt-switch-icon" fill="#002d4c">
                        <path d="M4.3 8.6 0 4.3 1.4 2.9l2.9 2.9 5.7-5.7 1.4 1.4z" />
                    </svg>
                ) : (
                    <svg
                        viewBox="0 0 10 10"
                        className="gt-switch-icon"
                        stroke="#ffffff"
                        strokeWidth={2}
                    >
                        <line x1="1" y1="1" x2="9" y2="9" />
                        <line x1="9" y1="1" x2="1" y2="9" />
                    </svg>
                )}
            </span>
        </span>
    );
}

/* ------------------------------ GenericTable ------------------------------ */
export default function GenericTable<T>(
    props: GenericTableProps<T>
) {

    const {
        rows,
        columns,
        actions = [],
        emptyLabel = 'Sin resultados',

        // paginación
        perPage = 10,
        page = 1,
        totalPages = 1,
        onChangePerPage = () => undefined,
        onChangePage = () => undefined,

        totalItems,
    } = props;

    const nav = useNavigate();

    // Totales para el texto "X–Y de N"
    const N: number = Number.isFinite(totalItems as number)
        ? Number(totalItems)
        : rows.length;

    const safePerPage = Math.max(1, Number(perPage || 1));
    const safeTotalPages = Math.max(1, Number(totalPages || 1));
    const safePage = Math.min(Math.max(1, Number(page || 1)), safeTotalPages);

    const firstIdx = N === 0 ? 0 : (safePage - 1) * safePerPage + 1;
    const lastIdx = Math.min(safePage * safePerPage, N);

    const pageWindowStart = Math.max(0, Math.min(safePage - 3, safeTotalPages - 5));
    const visiblePages = Array.from(
        { length: Math.min(5, safeTotalPages) },
        (_, i) => i + 1 + pageWindowStart
    );

    const alignClass = (align: Align) =>
        align === 'center' ? 'gt-align-center' : align === 'right' ? 'gt-align-right' : 'gt-align-left';

    return (
        <div className="gt-container">
            {/* ------------------ tabla ------------------ */}
            <table className="gt-table">
                {/* ---------- encabezados ---------- */}
                <thead className="gt-thead">
                    <tr>
                        {columns.map(({ header, align = 'left' }, index) => (
                            <th
                                key={typeof header === 'string' ? header : `col-${index}`}
                                className={`gt-th ${alignClass(align)}`}
                            >
                                {header}
                            </th>
                        ))}
                        {actions.length > 0 && (
                            <th className="gt-th gt-align-center gt-lg">Acciones</th>
                        )}
                    </tr>
                </thead>

                {/* ---------- cuerpo ---------- */}
                <tbody>
                    {/* vacía */}
                    {rows.length === 0 && (
                        <tr>
                            <td
                                colSpan={columns.length + (actions.length > 0 ? 1 : 0)}
                                className="gt-empty"
                            >
                                {emptyLabel}
                            </td>
                        </tr>
                    )}

                    {/* datos */}
                    {rows.map((row, index) => (
                        <tr
                            key={index}
                            className="gt-row"
                        >
                            {columns.map(({ header, align = 'left', render }, index) => (
                                <td
                                    key={typeof header === 'string' ? header : `col-${index}`}
                                    className={`gt-td ${alignClass(align)}`}
                                >
                                    {render(row, nav)}
                                </td>
                            ))}

                            {/* acciones */}
                            {actions.length > 0 && (
                                <td className="gt-td gt-align-center">
                                    <div className="gt-actions">
                                        {actions.map(({ title, icon, onClick, isDisabled }) => {
                                            const disabled = isDisabled?.(row) ?? false;
                                            return (
                                                <button
                                                    key={title}
                                                    title={title}
                                                    onClick={() => !disabled && onClick(row, nav)}
                                                    className="gt-action-btn"
                                                    disabled={disabled}
                                                >
                                                    <img src={icon} className="gt-action-icon" alt={title} />
                                                </button>
                                            );
                                        })}
                                    </div>
                                </td>
                            )}
                        </tr>
                    ))}
                </tbody>
            </table>

            {/* ---------------- paginación ---------------- */}
            <div className="gt-pagination">
                {/* per-page */}
                <label>
                    Filas por página:
                    <div className="gt-select-wrapper">
                        <select
                            value={perPage}
                            onChange={(e) => onChangePerPage(Number(e.target.value))}
                            className="gt-select"
                        >
                            {[5, 10, 25, 50].map((n) => (
                                <option key={n} value={n}>
                                    {n}
                                </option>
                            ))}
                        </select>
                        <span className="gt-select-caret">
                            ▾
                        </span>
                    </div>
                </label>

                {/* go-to page */}
                <label>
                    Ir a:
                    <input
                        type="number"
                        min={1}
                        max={safeTotalPages}
                        value={safePage}
                        onChange={(e) =>
                            onChangePage(
                                Math.max(1, Math.min(safeTotalPages, Number(e.target.value) || 1))
                            )
                        }
                        className="gt-input"
                    />
                </label>

                {/* rango */}
                <span className="gt-range">
                    {firstIdx}-{lastIdx} de {N}
                </span>

                {/* flechas + números */}
                <div className="gt-pages">
                    <button
                        onClick={() => onChangePage(Math.max(1, safePage - 1))}
                        disabled={safePage === 1}
                        className="gt-page-btn"
                        title="Anterior"
                    >
                        ‹
                    </button>

                    {visiblePages.map((n) => (
                        <button
                            key={n}
                            onClick={() => onChangePage(n)}
                            disabled={safePage === n}
                            className={`gt-page-btn ${safePage === n ? 'is-current' : ''}`}
                            title={`Ir a ${n}`}
                        >
                            {n}
                        </button>
                    ))}

                    <button
                        onClick={() =>
                            onChangePage(Math.min(safeTotalPages, safePage + 1))
                        }
                        disabled={safePage === safeTotalPages}
                        className="gt-page-btn"
                        title="Siguiente"
                    >
                        ›
                    </button>
                </div>
            </div>

            {/* línea final */}
            <div className="gt-footer-line" />
        </div>
    );
}

/* --------------------------- Helper buildTable --------------------------- */
/** Compatibilidad con uso existente: buildTable(data, columns, options) */
export function buildTable<T extends { id?: string | number }>(
    rows: T[],
    columns: Column<T>[],
    options: Partial<Omit<GenericTableProps<T>, 'rows' | 'columns'>> = {}
) {
    return <GenericTable<T> rows={rows} columns={columns} {...options} />;
}
