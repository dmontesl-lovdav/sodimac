import * as React from 'react';
import { createPortal } from 'react-dom';
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
    onClick: (row: T, nav: ReturnType<typeof useNavigate>) => void | Promise<void>;
    /** Deshabilitar acción por fila (opcional) */
    isDisabled?: (row: T) => boolean;
}

function KebabIcon() {
    return (
        <svg
            viewBox="0 0 24 24"
            width="20"
            height="20"
            aria-hidden="true"
            className="fiscal-table-kebab-icon"
        >
            <circle cx="12" cy="5" r="1.75" fill="currentColor" />
            <circle cx="12" cy="12" r="1.75" fill="currentColor" />
            <circle cx="12" cy="19" r="1.75" fill="currentColor" />
        </svg>
    );
}

function RowActionsMenu<T>({
    row,
    actions,
    nav,
}: {
    row: T;
    actions: RowAction<T>[];
    nav: ReturnType<typeof useNavigate>;
}) {
    const [open, setOpen] = React.useState(false);
    const triggerRef = React.useRef<HTMLButtonElement>(null);
    const menuRef = React.useRef<HTMLDivElement>(null);
    const [pos, setPos] = React.useState({ top: 0, right: 0 });

    const updatePosition = React.useCallback(() => {
        const rect = triggerRef.current?.getBoundingClientRect();
        if (!rect) return;
        setPos({
            top: rect.bottom + 4,
            right: Math.max(8, window.innerWidth - rect.right),
        });
    }, []);

    const close = React.useCallback(() => setOpen(false), []);

    const toggle = () => {
        if (open) {
            close();
            return;
        }
        updatePosition();
        setOpen(true);
    };

    React.useEffect(() => {
        if (!open) return;

        const onPointerDown = (e: MouseEvent) => {
            const target = e.target as Node;
            if (menuRef.current?.contains(target) || triggerRef.current?.contains(target)) {
                return;
            }
            close();
        };

        const onKeyDown = (e: KeyboardEvent) => {
            if (e.key === 'Escape') close();
        };

        const onReposition = () => {
            updatePosition();
        };

        document.addEventListener('mousedown', onPointerDown);
        document.addEventListener('keydown', onKeyDown);
        window.addEventListener('resize', onReposition);
        window.addEventListener('scroll', onReposition, true);

        return () => {
            document.removeEventListener('mousedown', onPointerDown);
            document.removeEventListener('keydown', onKeyDown);
            window.removeEventListener('resize', onReposition);
            window.removeEventListener('scroll', onReposition, true);
        };
    }, [open, close, updatePosition]);

    return (
        <div className="fiscal-table-actions">
            <button
                ref={triggerRef}
                type="button"
                title="Acciones"
                aria-label="Acciones"
                aria-haspopup="menu"
                aria-expanded={open}
                className="fiscal-table-action-btn fiscal-table-kebab-btn"
                onClick={toggle}
            >
                <KebabIcon />
            </button>

            {open &&
                createPortal(
                    <div
                        ref={menuRef}
                        role="menu"
                        className="fiscal-table-actions-menu"
                        style={{ top: pos.top, right: pos.right }}
                    >
                        {actions.map(({ title, icon, onClick, isDisabled }) => {
                            const disabled = isDisabled?.(row) ?? false;
                            return (
                                <button
                                    key={title}
                                    type="button"
                                    role="menuitem"
                                    title={title}
                                    disabled={disabled}
                                    className="fiscal-table-actions-menu-item"
                                    onClick={() => {
                                        if (disabled) return;
                                        close();
                                        void onClick(row, nav);
                                    }}
                                >
                                    <img
                                        src={icon}
                                        className="fiscal-table-action-icon"
                                        alt=""
                                    />
                                    <span>{title}</span>
                                </button>
                            );
                        })}
                    </div>,
                    document.body
                )}
        </div>
    );
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

    className?: string;
}

/* ---------- util: mini-switch opcional ---------- */
export function Switch({
    on,
    onClick,
    className = '',
}: {
    on: boolean;
    onClick?: () => void;
    className?: string;
}) {
    return (
        <button
            type="button"
            onClick={onClick}
            className={`fiscal-table-switch ${on ? 'on' : 'off'} ${className}`.trim()}
        >
            <span className={`fiscal-table-switch-thumb ${on ? 'on' : 'off'}`}>
                {on ? (
                    <svg viewBox="0 0 12 9" className="fiscal-table-switch-icon" fill="#002d4c">
                        <path d="M4.3 8.6 0 4.3 1.4 2.9l2.9 2.9 5.7-5.7 1.4 1.4z" />
                    </svg>
                ) : (
                    <svg
                        viewBox="0 0 10 10"
                        className="fiscal-table-switch-icon"
                        stroke="#ffffff"
                        strokeWidth={2}
                    >
                        <line x1="1" y1="1" x2="9" y2="9" />
                        <line x1="9" y1="1" x2="1" y2="9" />
                    </svg>
                )}
            </span>
        </button>
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
        className = '',
    } = props;

    const nav = useNavigate();

    // Totales para el texto "X–Y de N"
    const N: number = Number.isFinite(totalItems as number)
        ? Number(totalItems)
        : rows.length;

    const safePerPage = Math.max(1, Number(perPage ?? 1));
    const safeTotalPages = Math.max(1, Number(totalPages ?? 1));
    const safePage = Math.min(Math.max(1, Number(page ?? 1)), safeTotalPages);

    const firstIdx = N === 0 ? 0 : (safePage - 1) * safePerPage + 1;
    const lastIdx = Math.min(safePage * safePerPage, N);

    const pageWindowStart = Math.max(0, Math.min(safePage - 3, safeTotalPages - 5));
    const visiblePages = Array.from(
        { length: Math.min(5, safeTotalPages) },
        (_, i) => i + 1 + pageWindowStart
    );

    function alignClass(align: Align): string {
        if (align === 'center') return 'fiscal-table-align-center';
        if (align === 'right') return 'fiscal-table-align-right';
        return 'fiscal-table-align-left';
    }

    return (
        <div className={`fiscal-table-container fiscal-mt-4 ${className}`.trim()}>
            {/* ------------------ tabla ------------------ */}
            <table className="fiscal-table-table">
                {/* ---------- encabezados ---------- */}
                <thead className="fiscal-table-thead">
                    <tr>
                        {columns.map(({ header, align = 'left' }, index) => (
                            <th
                                key={typeof header === 'string' ? header : `col-${index}`}
                                className={`fiscal-table-th ${alignClass(align)}`}
                            >
                                {header}
                            </th>
                        ))}
                        {actions.length > 0 && (
                            <th className="fiscal-table-th fiscal-table-align-center fiscal-table-lg">Acciones</th>
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
                                className="fiscal-table-empty"
                            >
                                {emptyLabel}
                            </td>
                        </tr>
                    )}

                    {/* datos */}
                    {rows.map((row, index) => (
                        <tr
                            key={(row as any)?.id ?? (row as any)?.key ?? index}
                            className="fiscal-table-row"
                        >
                            {columns.map(({ header, align = 'left', render }, index) => (
                                <td
                                    key={typeof header === 'string' ? header : `col-${index}`}
                                    className={`fiscal-table-td ${alignClass(align)}`}
                                >
                                    {render(row, nav)}
                                </td>
                            ))}

                            {/* acciones */}
                            {actions.length > 0 && (
                                <td className="fiscal-table-td fiscal-table-align-center fiscal-table-actions-cell">
                                    <RowActionsMenu row={row} actions={actions} nav={nav} />
                                </td>
                            )}
                        </tr>
                    ))}
                </tbody>
            </table>

            {/* ---------------- paginación ---------------- */}
            <div className="fiscal-table-pagination">
                {/* per-page */}
                <label>
                    Filas por página:
                    <div className="fiscal-table-select-wrapper">
                        <select
                            value={perPage}
                            onChange={(e) => onChangePerPage(Number(e.target.value))}
                            className="fiscal-table-select"
                        >
                            {[5, 10, 25, 50].map((n) => (
                                <option key={n} value={n}>
                                    {n}
                                </option>
                            ))}
                        </select>
                        <span className="fiscal-table-select-caret">
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
                                Math.max(1, Math.min(safeTotalPages, ((n) => (Number.isFinite(n) && n !== 0 ? n : 1))(Number(e.target.value))))
                            )
                        }
                        className="fiscal-table-input"
                    />
                </label>

                {/* rango */}
                <span className="fiscal-table-range">
                    {firstIdx}-{lastIdx} de {N}
                </span>

                {/* flechas + números */}
                <div className="fiscal-table-pages">
                    <button
                        onClick={() => onChangePage(Math.max(1, safePage - 1))}
                        disabled={safePage === 1}
                        className="fiscal-table-page-btn fiscal-table-arrow"
                        title="Anterior"
                    >
                        ‹
                    </button>

                    {visiblePages.map((n) => (
                        <button
                            key={n}
                            onClick={() => onChangePage(n)}
                            disabled={safePage === n}
                            className={`fiscal-table-page-btn ${safePage === n ? 'is-current' : ''}`}
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
                        className="fiscal-table-page-btn fiscal-table-arrow"
                        title="Siguiente"
                    >
                        ›
                    </button>
                </div>
            </div>

            {/* línea final */}
            <div className="fiscal-table-footer-line" />
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
