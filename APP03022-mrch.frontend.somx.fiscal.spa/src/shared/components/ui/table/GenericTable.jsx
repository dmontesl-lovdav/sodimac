import { useNavigate } from 'react-router-dom';
import { useEffect, useState, useCallback } from 'react';

const styles = {
    container: {
        overflowX: 'auto',
    },
    table: {
        minWidth: '100%',
        fontSize: '0.875rem',
        borderCollapse: 'collapse',
    },
    thead: {
        backgroundColor: '#eaf5fc',
        color: '#002d4c',
        fontWeight: '500',
    },
    th: {
        padding: '0.75rem 1rem',
        textAlign: 'left',
    },
    thCenter: {
        textAlign: 'center',
    },
    thRight: {
        textAlign: 'right',
    },
    tr: {
        borderTop: '1px solid #e5e7eb',
    },
    trHover: {
        backgroundColor: '#f9fafb',
    },
    trFocused: {
        backgroundColor: '#f3faff',
        boxShadow: 'inset 0 0 0 1px #0073e6',
    },
    td: {
        padding: '0.75rem 1rem',
    },
    tdCenter: {
        textAlign: 'center',
        width: '40px',
    },
    emptyCell: {
        padding: '1.5rem',
        textAlign: 'center',
        color: '#6b7280',
    },
    checkbox: {
        width: '1.25rem',
        height: '1.25rem',
        accentColor: '#002d4c',
        cursor: 'pointer',
        transition: 'transform 150ms',
    },
    actionsCell: {
        display: 'flex',
        justifyContent: 'center',
        gap: '1rem',
        color: '#002d4c',
    },
    actionButton: {
        cursor: 'pointer',
        background: 'none',
        border: 'none',
        padding: 0,
        opacity: 1,
        transition: 'opacity 150ms',
    },
    actionIcon: {
        width: '1.25rem',
        height: '1.25rem',
    },
    footer: {
        display: 'flex',
        flexWrap: 'wrap',
        alignItems: 'center',
        justifyContent: 'center',
        gap: '1.5rem',
        padding: '1rem',
        fontSize: '0.875rem',
    },
    footerLabel: {
        display: 'flex',
        alignItems: 'center',
        gap: '0.25rem',
    },
    selectWrapper: {
        position: 'relative',
    },
    select: {
        appearance: 'none',
        paddingLeft: 0,
        paddingRight: '1rem',
        backgroundColor: 'transparent',
        border: 'none',
        borderBottom: '1px solid #002d4c',
        width: '3rem',
        textAlign: 'center',
    },
    selectCaret: {
        position: 'absolute',
        right: 0,
        top: 0,
        color: '#002d4c',
        pointerEvents: 'none',
    },
    pageInput: {
        width: '3rem',
        backgroundColor: 'transparent',
        border: 'none',
        borderBottom: '1px solid #002d4c',
        textAlign: 'center',
    },
    paginationButtons: {
        display: 'flex',
        alignItems: 'center',
        gap: '0.5rem',
        color: '#002d4c',
    },
    pageButton: {
        cursor: 'pointer',
        background: 'none',
        border: 'none',
        padding: '0.125rem 0.5rem',
        borderRadius: '0.25rem',
        transition: 'all 150ms',
    },
    pageButtonActive: {
        backgroundColor: '#002d4c',
        color: '#ffffff',
    },
    pageButtonDisabled: {
        color: '#9ca3af',
        cursor: 'default',
    },
    divider: {
        borderTop: '1px solid #e5e7eb',
    },
    switchTrack: {
        position: 'relative',
        display: 'inline-block',
        width: '38px',
        height: '20px',
        borderRadius: '9999px',
        cursor: 'pointer',
    },
    switchThumb: {
        position: 'absolute',
        top: '3px',
        width: '14px',
        height: '14px',
        borderRadius: '9999px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
    },
};

export function Switch({ on, onClick }) {
    return (
        <span
            onClick={onClick}
            style={{
                ...styles.switchTrack,
                backgroundColor: on ? '#002d4c' : '#d1d5db',
            }}
        >
            <span
                style={{
                    ...styles.switchThumb,
                    ...(on ? { right: '3px', backgroundColor: '#ffffff' } : { left: '3px', backgroundColor: '#002d4c' }),
                }}
            >
                {on ? (
                    <svg viewBox="0 0 12 9" style={{ width: '10px', fill: '#002d4c' }}>
                        <path d="M4.3 8.6 0 4.3 1.4 2.9l2.9 2.9 5.7-5.7 1.4 1.4z" />
                    </svg>
                ) : (
                    <svg viewBox="0 0 10 10" style={{ width: '10px', stroke: '#ffffff', strokeWidth: 2 }}>
                        <line x1="1" y1="1" x2="9" y2="9" />
                        <line x1="9" y1="1" x2="1" y2="9" />
                    </svg>
                )}
            </span>
        </span>
    );
}

export default function GenericTable({
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
}) {
    const nav = useNavigate();
    const [focusedIndex, setFocusedIndex] = useState(null);
    const [hoveredIndex, setHoveredIndex] = useState(null);

    const N = Number.isFinite(totalItems) ? Number(totalItems) : rows.length;
    const safePerPage = Math.max(1, Number(perPage || 1));
    const safePage = Math.min(Math.max(1, Number(page || 1)), Math.max(1, Number(totalPages || 1)));
    const firstIdx = N === 0 ? 0 : (safePage - 1) * safePerPage + 1;
    const lastIdx = Math.min(safePage * safePerPage, N);

    const handleKeyDown = useCallback(
        (e) => {
            if (!enableSelection || rows.length === 0) return;

            if (e.key === 'ArrowDown') {
                e.preventDefault();
                setFocusedIndex((prev) => {
                    const next = prev === null ? 0 : Math.min(prev + 1, rows.length - 1);
                    return next;
                });
            }

            if (e.key === 'ArrowUp') {
                e.preventDefault();
                setFocusedIndex((prev) => {
                    const next = prev === null ? rows.length - 1 : Math.max(prev - 1, 0);
                    return next;
                });
            }

            if (e.key === 'Enter' && focusedIndex !== null) {
                e.preventDefault();
                const id = rows[focusedIndex].id;
                const isSelected = selectedIds.includes(id);
                onSelectRow(id, !isSelected);
            }
        },
        [rows, selectedIds, focusedIndex, enableSelection, onSelectRow]
    );

    useEffect(() => {
        window.addEventListener('keydown', handleKeyDown);
        return () => window.removeEventListener('keydown', handleKeyDown);
    }, [handleKeyDown]);

    const getRowStyle = (index) => {
        if (focusedIndex === index) {
            return { ...styles.tr, ...styles.trFocused };
        }
        if (hoveredIndex === index) {
            return { ...styles.tr, ...styles.trHover };
        }
        return styles.tr;
    };

    const getTdStyle = (align) => {
        const base = { ...styles.td };
        if (align === 'center') return { ...base, textAlign: 'center' };
        if (align === 'right') return { ...base, textAlign: 'right' };
        return base;
    };

    return (
        <div style={styles.container}>
            <table style={styles.table}>
                <thead style={styles.thead}>
                    <tr>
                        {enableSelection && <th style={{ ...styles.th, ...styles.thCenter }}></th>}
                        {columns.map(({ header, align = 'left' }) => (
                            <th
                                key={header}
                                style={{
                                    ...styles.th,
                                    ...(align === 'center' ? styles.thCenter : align === 'right' ? styles.thRight : {}),
                                }}
                            >
                                {header}
                            </th>
                        ))}
                        {actions.length > 0 && <th style={{ ...styles.th, ...styles.thCenter }}>Acciones</th>}
                    </tr>
                </thead>

                <tbody>
                    {rows.length === 0 ? (
                        <tr>
                            <td
                                colSpan={
                                    columns.length +
                                    (actions.length > 0 ? 1 : 0) +
                                    (enableSelection ? 1 : 0)
                                }
                                style={styles.emptyCell}
                            >
                                {emptyLabel}
                            </td>
                        </tr>
                    ) : (
                        rows.map((row, index) => (
                            <tr
                                key={row.id ?? JSON.stringify(row)}
                                style={getRowStyle(index)}
                                onMouseEnter={() => setHoveredIndex(index)}
                                onMouseLeave={() => setHoveredIndex(null)}
                            >
                                {enableSelection && (
                                    <td style={{ ...styles.td, ...styles.tdCenter }}>
                                        <div style={{ display: 'flex', justifyContent: 'center' }}>
                                            <input
                                                type="checkbox"
                                                checked={selectedIds.includes(row.id)}
                                                onChange={(e) =>
                                                    onSelectRow(row.id, e.target.checked)
                                                }
                                                style={styles.checkbox}
                                            />
                                        </div>
                                    </td>
                                )}

                                {columns.map(({ header, align = 'left', render }) => (
                                    <td key={header} style={getTdStyle(align)}>
                                        {render(row, nav)}
                                    </td>
                                ))}

                                {actions.length > 0 && (
                                    <td style={styles.td}>
                                        <div style={styles.actionsCell}>
                                            {actions.map(({ title, icon, onClick }) => (
                                                <button
                                                    key={title}
                                                    title={title}
                                                    onClick={() => onClick(row, nav)}
                                                    style={styles.actionButton}
                                                    onMouseEnter={(e) => e.currentTarget.style.opacity = '0.8'}
                                                    onMouseLeave={(e) => e.currentTarget.style.opacity = '1'}
                                                >
                                                    <img src={icon} style={styles.actionIcon} alt={title} />
                                                </button>
                                            ))}
                                        </div>
                                    </td>
                                )}
                            </tr>
                        ))
                    )}
                </tbody>
            </table>

            <div style={styles.footer}>
                <label style={styles.footerLabel}>
                    Filas por página:
                    <div style={styles.selectWrapper}>
                        <select
                            value={perPage}
                            onChange={(e) => onChangePerPage(+e.target.value)}
                            style={styles.select}
                        >
                            {[5, 10, 25, 50].map((n) => (
                                <option key={n}>{n}</option>
                            ))}
                        </select>
                        <span style={styles.selectCaret}>▾</span>
                    </div>
                </label>

                <label style={styles.footerLabel}>
                    Ir a:
                    <input
                        type="number"
                        min={1}
                        max={totalPages}
                        value={page}
                        onChange={(e) => onChangePage(+e.target.value || 1)}
                        style={styles.pageInput}
                    />
                </label>

                <span>
                    {firstIdx}-{lastIdx} de {N}
                </span>

                <div style={styles.paginationButtons}>
                    <button
                        onClick={() => onChangePage(Math.max(1, page - 1))}
                        disabled={page === 1}
                        style={{
                            ...styles.pageButton,
                            ...(page === 1 ? styles.pageButtonDisabled : {}),
                        }}
                        title="Anterior"
                    >
                        ‹
                    </button>

                    {[...Array(totalPages).keys()]
                        .map((i) => i + 1)
                        .slice(
                            Math.max(0, Math.min(page - 3, totalPages - 5)),
                            Math.max(0, Math.min(page - 3, totalPages - 5)) + 5
                        )
                        .map((n) => (
                            <button
                                key={n}
                                onClick={() => onChangePage(n)}
                                style={{
                                    ...styles.pageButton,
                                    ...(page === n ? styles.pageButtonActive : {}),
                                }}
                                title={`Ir a ${n}`}
                            >
                                {n}
                            </button>
                        ))}

                    <button
                        onClick={() => onChangePage(Math.min(totalPages, page + 1))}
                        disabled={page === totalPages}
                        style={{
                            ...styles.pageButton,
                            ...(page === totalPages ? styles.pageButtonDisabled : {}),
                        }}
                        title="Siguiente"
                    >
                        ›
                    </button>
                </div>
            </div>

            <div style={styles.divider} />
        </div>
    );
}
