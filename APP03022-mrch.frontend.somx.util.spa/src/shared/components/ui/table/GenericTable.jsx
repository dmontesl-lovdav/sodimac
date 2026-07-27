import { useNavigate } from 'react-router-dom';
import { useEffect, useState, useCallback } from 'react';
import PropTypes from 'prop-types';
import './GenericTable.css';

export function Switch({ on, onClick }) {
    return (
        <button
            type="button"
            onClick={onClick}
            className={`switch ${on ? 'switch-on' : 'switch-off'}`}
            style={{ appearance: 'none', border: 'none', padding: 0, background: 'transparent', font: 'inherit', cursor: 'pointer' }}
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
        </button>
    );
}

Switch.propTypes = {
    on: PropTypes.bool,
    onClick: PropTypes.func,
};


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
    selectionHeader = null,
    totalItems,
    showPagination = true,
    showPageSizeSelector = true,
}) {

    const nav = useNavigate();
    const [focusedIndex, setFocusedIndex] = useState(null);

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
                setFocusedIndex(prev => {
                    const next = prev === null ? 0 : Math.min(prev + 1, rows.length - 1);
                    return next;
                });
            }

            if (e.key === 'ArrowUp') {
                e.preventDefault();
                setFocusedIndex(prev => {
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

    return (
        <div className="table-wrapper">

            <table className="table">
                <thead>
                    <tr>
                        {enableSelection && (
                            <th className="text-center" style={{ width: 40 }}>
                                {selectionHeader}
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
                    {rows.length === 0 ? (
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
                        rows.map((row, index) => (
                            <tr
                                key={row.id ?? JSON.stringify(row)}
                                className={`table-row ${focusedIndex === index ? 'focused' : ''}`}
                            >
                                {enableSelection && (
                                    <td className="text-center" style={{ width: 40 }}>
                                        <input
                                            type="checkbox"
                                            checked={selectedIds.includes(row.id)}
                                            onChange={(e) =>
                                                onSelectRow(row.id, e.target.checked)
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
                                            {actions.map(({ title, icon, onClick }) => (
                                                <button
                                                    key={title}
                                                    title={title}
                                                    onClick={() => onClick(row, nav)}
                                                >
                                                    <img src={icon} alt={title} width={20} height={20} />
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

            {showPagination ? (
                <div className="pagination">

                    {showPageSizeSelector ? (
                        <label style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                            Filas por página:
                            <div style={{ position: 'relative' }}>
                                <select
                                    value={perPage}
                                    onChange={(e) => onChangePerPage(+e.target.value)}
                                >
                                    {[5, 10, 25, 50].map((n) => (
                                        <option key={n}>{n}</option>
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
                    ) : null}

                    <label style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                        <span>Ir a:</span>
                        <input
                            type="number"
                            min={1}
                            max={totalPages}
                            value={page}
                            onChange={(e) => onChangePage(+e.target.value || 1)}
                        />
                    </label>

                    <span>{firstIdx}-{lastIdx} de {N}</span>

                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <button
                            onClick={() => onChangePage(Math.max(1, page - 1))}
                            disabled={page === 1}
                            className={`pagination-arrow ${page === 1 ? 'disabled' : ''}`}
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
                                    className={`page-btn ${page === n ? 'active' : ''}`}
                                >
                                    {n}
                                </button>
                            ))}

                        <button
                            onClick={() => onChangePage(Math.min(totalPages, page + 1))}
                            disabled={page === totalPages}
                            className={`pagination-arrow ${page === totalPages ? 'disabled' : ''}`}
                        >
                            ›
                        </button>
                    </div>
                </div>
            ) : null}

            <div className="table-divider" />
        </div>
    );
}

GenericTable.propTypes = {
    rows: PropTypes.array,
    columns: PropTypes.array,
    actions: PropTypes.array,
    emptyLabel: PropTypes.node,
    perPage: PropTypes.number,
    page: PropTypes.number,
    totalPages: PropTypes.number,
    onChangePerPage: PropTypes.func,
    onChangePage: PropTypes.func,
    enableSelection: PropTypes.bool,
    selectedIds: PropTypes.array,
    onSelectRow: PropTypes.func,
    selectionHeader: PropTypes.node,
    totalItems: PropTypes.number,
    showPagination: PropTypes.bool,
    showPageSizeSelector: PropTypes.bool,
};
