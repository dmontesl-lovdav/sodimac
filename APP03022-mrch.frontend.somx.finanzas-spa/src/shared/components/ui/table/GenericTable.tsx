// ✅ FILE: src/shared/components/ui/table/GenericTable.tsx
import { useNavigate, NavigateFunction } from 'react-router-dom';
import { useEffect, useState, useCallback, useMemo } from 'react';
import './styles/GenericTable.css';

/* ========================================================= */
/* TYPES                               */
/* ========================================================= */

type Align = 'left' | 'center' | 'right';

export interface SwitchProps {
    on: boolean;
    onClick: () => void;
}

export interface Column<T = any> {
    header: string;
    align?: Align;
    render: (row: T, nav: NavigateFunction) => React.ReactNode;
}

export interface RowAction<T = any> {
    title: string;
    icon: string;
    onClick: (row: T, nav: NavigateFunction) => void;
    isDisabled?: (row: T) => boolean;
}

// Mantenemos Action como alias por si otros componentes ya lo están importando
export type Action<T = any> = RowAction<T>;

export interface GenericTableProps<T = any> {
    rows?: T[];
    columns?: Column<T>[];
    actions?: RowAction<T>[];
    emptyLabel?: string;
    perPage?: number;
    page?: number;
    totalPages?: number;
    onChangePerPage?: (value: number) => void;
    onChangePage?: (value: number) => void;
    enableSelection?: boolean;
    selectedIds?: any[];
    onSelectRow?: (id: any, selected: boolean) => void;
    /** Total de registros (catálogo completo o servidor). Si es mayor que `rows.length`, no se pagina en cliente. */
    totalItems?: number;
}

/* ========================================================= */
/* SWITCH                              */
/* ========================================================= */

export function Switch({ on, onClick }: SwitchProps) {
    return (
        <span
            onClick={onClick}
            className={`switch ${on ? 'switch-on' : 'switch-off'}`}
        >
            <span className={`switch-thumb ${on ? 'thumb-on' : 'thumb-off'}`}>
                {on ? (
                    <svg viewBox="0 0 12 9" className="switch-check">
                        <path d="M4.3 8.6 0 4.3 1.4 2.9l2.9 2.9 5.7-5.7 1.4 1.4z" />
                    </svg>
                ) : (
                    <svg viewBox="0 0 10 10" className="switch-x">
                        <line x1="1" y1="1" x2="9" y2="9" />
                        <line x1="9" y1="1" x2="1" y2="9" />
                    </svg>
                )}
            </span>
        </span>
    );
}

/* ========================================================= */
/* TABLA GENÉRICA                            */
/* ========================================================= */

function getRowId(row: any) {
    // ✅ Backward compatible: prioriza id si existe
    return row?.id ?? row?.activity_logs_uuid ?? row?.uuid ?? JSON.stringify(row);
}

export default function GenericTable<T = any>({
    rows = [],
    columns = [],
    actions = [],
    emptyLabel = 'Sin resultados',
    perPage = 10,
    page = 1,
    totalPages = 1,
    onChangePerPage = () => { },
    onChangePage = () => { },
    enableSelection = false,
    selectedIds = [],
    onSelectRow = () => { },
    totalItems,
}: GenericTableProps<T>) {

    const nav = useNavigate();
    const [focusedIndex, setFocusedIndex] = useState<number | null>(null);

    const safePerPage = Math.max(1, Number(perPage || 1));

    /**
     * Servidor: el padre envía solo la página actual (`totalItems` > filas visibles).
     * Cliente: el padre envía el conjunto completo y la tabla pagina en memoria.
     */
    const serverPaginated = useMemo(() => {
        if (totalItems == null || !Number.isFinite(totalItems)) return false;
        return Number(totalItems) > rows.length;
    }, [totalItems, rows.length]);

    const effectiveTotalItems = serverPaginated
        ? Number(totalItems)
        : rows.length;

    const effectiveTotalPages = serverPaginated
        ? Math.max(1, Number(totalPages || 1))
        : Math.max(1, Math.ceil(rows.length / safePerPage) || 1);

    const safePage = Math.min(
        Math.max(1, Number(page || 1)),
        effectiveTotalPages
    );

    const displayRows = useMemo(() => {
        if (serverPaginated || rows.length === 0) return rows;
        const start = (safePage - 1) * safePerPage;
        return rows.slice(start, start + safePerPage);
    }, [rows, serverPaginated, safePage, safePerPage]);

    const N = effectiveTotalItems;
    const firstIdx = N === 0 ? 0 : (safePage - 1) * safePerPage + 1;
    const lastIdx = serverPaginated
        ? Math.min(safePage * safePerPage, N)
        : Math.min(safePage * safePerPage, rows.length);

    useEffect(() => {
        setFocusedIndex(null);
    }, [rows, safePage, safePerPage]);

    useEffect(() => {
        if (!serverPaginated && page > effectiveTotalPages) {
            onChangePage(effectiveTotalPages);
        }
    }, [serverPaginated, page, effectiveTotalPages, onChangePage]);

    /* ========================================================= */
    /* NAVEGACIÓN CON TECLADO                        */
    /* ========================================================= */

    const handleKeyDown = useCallback(
        (e: KeyboardEvent) => {
            if (!enableSelection || displayRows.length === 0) return;

            if (e.key === 'ArrowDown') {
                e.preventDefault();
                setFocusedIndex(prev => {
                    const next = prev === null ? 0 : Math.min(prev + 1, displayRows.length - 1);
                    return next;
                });
            }

            if (e.key === 'ArrowUp') {
                e.preventDefault();
                setFocusedIndex(prev => {
                    const next = prev === null ? displayRows.length - 1 : Math.max(prev - 1, 0);
                    return next;
                });
            }

            if (e.key === 'Enter' && focusedIndex !== null) {
                e.preventDefault();
                const id = getRowId((displayRows as any)[focusedIndex]);
                const isSelected = selectedIds.includes(id);
                onSelectRow(id, !isSelected);
            }
        },
        [displayRows, selectedIds, focusedIndex, enableSelection, onSelectRow]
    );

    useEffect(() => {
        window.addEventListener('keydown', handleKeyDown);
        return () => window.removeEventListener('keydown', handleKeyDown);
    }, [handleKeyDown]);

    /* ========================================================= */
    /* SELECT ALL (PAGE)                         */
    /* ========================================================= */

    const pageIds = displayRows.map((r: any) => getRowId(r));
    const hasRows = pageIds.length > 0;
    const selectedCountOnPage = pageIds.filter((id) => selectedIds.includes(id)).length;
    const allSelectedOnPage = hasRows && selectedCountOnPage === pageIds.length;
    const someSelectedOnPage = selectedCountOnPage > 0 && !allSelectedOnPage;

    const handlePerPageChange = (value: number) => {
        onChangePerPage(value);
        if (!serverPaginated) {
            onChangePage(1);
        }
    };

    /* ========================================================= */
    /* RENDER                            */
    /* ========================================================= */

    return (
        <div className="table-wrapper">

            <table className="table">
                <thead>
                    <tr>
                        {enableSelection && (
                            <th className="text-center" style={{ width: 40 }}>
                                <input
                                    type="checkbox"
                                    className="checkbox"
                                    checked={allSelectedOnPage}
                                    ref={(el) => {
                                        if (el) el.indeterminate = someSelectedOnPage;
                                    }}
                                    onChange={(e) => {
                                        const checked = e.target.checked;
                                        displayRows.forEach((row) => {
                                            const id = getRowId(row);
                                            onSelectRow(id, checked);
                                        });
                                    }}
                                    aria-label="Seleccionar todo"
                                    title="Seleccionar todo"
                                />
                            </th>
                        )}

                        {columns.map(({ header, align = 'left' }) => (
                            <th key={header} style={{ textAlign: align }}>
                                {header}
                            </th>
                        ))}

                        {actions.length > 0 && (
                            <th className="text-center">Acciones</th>
                        )}
                    </tr>
                </thead>

                <tbody>
                    {displayRows.length === 0 ? (
                        <tr>
                            <td
                                className="text-center"
                                colSpan={
                                    columns.length +
                                    (actions.length > 0 ? 1 : 0) +
                                    (enableSelection ? 1 : 0)
                                }
                            >
                                <div style={{ padding: "1.5rem", color: "#6b7280" }}>
                                    {emptyLabel}
                                </div>
                            </td>
                        </tr>
                    ) : (
                        displayRows.map((row: any, index) => {
                            const rowId = getRowId(row);

                            return (
                                <tr
                                    key={rowId}
                                    className={`table-row ${focusedIndex === index ? 'focused' : ''}`}
                                >
                                    {enableSelection && (
                                        <td className="text-center" style={{ width: 40 }}>
                                            <input
                                                type="checkbox"
                                                checked={selectedIds.includes(rowId)}
                                                onChange={(e) =>
                                                    onSelectRow(rowId, e.target.checked)
                                                }
                                                className="checkbox"
                                            />
                                        </td>
                                    )}

                                    {columns.map(({ header, align = 'left', render }) => (
                                        <td key={header} style={{ textAlign: align }}>
                                            {render(row, nav)}
                                        </td>
                                    ))}

                                    {actions.length > 0 && (
                                        <td>
                                            <div className="table-actions">
                                                {actions.map(({ title, icon, onClick, isDisabled }) => {
                                                    const disabled = isDisabled ? isDisabled(row) : false;
                                                    return (
                                                        <button
                                                            key={title}
                                                            title={title}
                                                            onClick={() => !disabled && onClick(row, nav)}
                                                            disabled={disabled}
                                                            style={{
                                                                cursor: disabled ? 'not-allowed' : 'pointer',
                                                                opacity: disabled ? 0.4 : 1
                                                            }}
                                                        >
                                                            <img src={icon} alt={title} width={20} height={20} />
                                                        </button>
                                                    )
                                                })}
                                            </div>
                                        </td>
                                    )}
                                </tr>
                            );
                        })
                    )}
                </tbody>
            </table>

            <div className="pagination">
                <label style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                    Filas por página:
                    <div style={{ position: 'relative' }}>
                        <select
                            value={safePerPage}
                            onChange={(e) => handlePerPageChange(+e.target.value)}
                        >
                            {[5, 10, 25, 50].map((n) => (
                                <option key={n} value={n}>{n}</option>
                            ))}
                        </select>
                        <span style={{
                            position: 'absolute',
                            right: 0,
                            top: 0,
                            pointerEvents: 'none',
                            color: '#002d4c'
                        }}>▾</span>
                    </div>
                </label>

                <label style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                    Ir a:
                    <input
                        type="number"
                        min={1}
                        max={effectiveTotalPages}
                        value={safePage}
                        onChange={(e) => {
                            const next = Math.min(
                                effectiveTotalPages,
                                Math.max(1, +e.target.value || 1)
                            );
                            onChangePage(next);
                        }}
                    />
                </label>

                <span>{firstIdx}-{lastIdx} de {N}</span>

                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <button
                        type="button"
                        onClick={() => onChangePage(Math.max(1, safePage - 1))}
                        disabled={safePage === 1}
                        className={`pagination-arrow ${safePage === 1 ? 'disabled' : ''}`}
                    >
                        ‹
                    </button>

                    {[...Array(effectiveTotalPages).keys()]
                        .map((i) => i + 1)
                        .slice(
                            Math.max(0, Math.min(safePage - 3, effectiveTotalPages - 5)),
                            Math.max(0, Math.min(safePage - 3, effectiveTotalPages - 5)) + 5
                        )
                        .map((n) => (
                            <button
                                type="button"
                                key={n}
                                onClick={() => onChangePage(n)}
                                className={`page-btn ${safePage === n ? 'active' : ''}`}
                            >
                                {n}
                            </button>
                        ))}

                    <button
                        type="button"
                        onClick={() => onChangePage(Math.min(effectiveTotalPages, safePage + 1))}
                        disabled={safePage === effectiveTotalPages}
                        className={`pagination-arrow ${safePage === effectiveTotalPages ? 'disabled' : ''}`}
                    >
                        ›
                    </button>
                </div>
            </div>

            <div className="table-divider" />
        </div>
    );
}
