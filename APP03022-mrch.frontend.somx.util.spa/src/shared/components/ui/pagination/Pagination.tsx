import { type FC, useMemo, useState, useEffect } from 'react';

export interface PaginationProps {
    currentPage: number;
    totalPages: number;
    totalItems: number;
    pageSize: number;
    onPageChange: (page: number) => void;
    onPageSizeChange?: (size: number) => void;
    pageSizeOptions?: number[];
    maxVisiblePages?: number;
    showPageSize?: boolean;
    showGoTo?: boolean;
    showRange?: boolean;
    disabled?: boolean;
}

export const Pagination: FC<PaginationProps> = ({
    currentPage,
    totalPages,
    totalItems,
    pageSize,
    onPageChange,
    onPageSizeChange,
    pageSizeOptions = [10, 20, 50],
    maxVisiblePages = 5,
    showPageSize = true,
    showGoTo = true,
    showRange = true,
    disabled = false,
}) => {
    const [goToInput, setGoToInput] = useState<string>(String(currentPage));
    useEffect(() => {
        setGoToInput(String(currentPage));
    }, [currentPage]);

    const safeTotalPages = Math.max(1, totalPages);
    const safeCurrentPage = Math.max(1, Math.min(currentPage, safeTotalPages));

    const rangeStart = totalItems === 0 ? 0 : (safeCurrentPage - 1) * pageSize + 1;
    const rangeEnd = totalItems === 0 ? 0 : Math.min(safeCurrentPage * pageSize, totalItems);

    const visiblePages = useMemo(() => {
        const max = Math.max(1, maxVisiblePages);
        if (safeTotalPages <= max) {
            return Array.from({ length: safeTotalPages }, (_, i) => i + 1);
        }
        const half = Math.floor(max / 2);
        let start = safeCurrentPage - half;
        let end = safeCurrentPage + (max - half - 1);
        if (start < 1) {
            end += 1 - start;
            start = 1;
        }
        if (end > safeTotalPages) {
            start -= end - safeTotalPages;
            end = safeTotalPages;
        }
        start = Math.max(1, start);
        const pages: number[] = [];
        for (let p = start; p <= end; p += 1) pages.push(p);
        return pages;
    }, [safeCurrentPage, safeTotalPages, maxVisiblePages]);

    const commitGoTo = () => {
        const parsed = Number(goToInput);
        if (!Number.isFinite(parsed)) {
            setGoToInput(String(safeCurrentPage));
            return;
        }
        const clamped = Math.max(1, Math.min(safeTotalPages, Math.floor(parsed)));
        if (clamped !== safeCurrentPage) onPageChange(clamped);
        else setGoToInput(String(safeCurrentPage));
    };

    return (
        <div
            role="navigation"
            aria-label="Paginación"
            style={styles.container}
        >
            {showPageSize && onPageSizeChange && (
                <span style={styles.group}>
                    <label htmlFor="pagination-page-size" style={styles.label}>
                        Items por página:
                    </label>
                    <select
                        id="pagination-page-size"
                        value={pageSize}
                        onChange={(e) => onPageSizeChange(Number(e.target.value))}
                        disabled={disabled}
                        style={styles.select}
                    >
                        {pageSizeOptions.map((n) => (
                            <option key={n} value={n}>
                                {n}
                            </option>
                        ))}
                    </select>
                </span>
            )}

            {showGoTo && (
                <span style={styles.group}>
                    <label htmlFor="pagination-go-to" style={styles.label}>
                        Ir a:
                    </label>
                    <input
                        id="pagination-go-to"
                        type="number"
                        min={1}
                        max={safeTotalPages}
                        value={goToInput}
                        disabled={disabled}
                        onChange={(e) => setGoToInput(e.target.value)}
                        onBlur={commitGoTo}
                        onKeyDown={(e) => {
                            if (e.key === 'Enter') {
                                e.preventDefault();
                                commitGoTo();
                            }
                        }}
                        style={styles.input}
                    />
                </span>
            )}

            {showRange && (
                <span aria-live="polite" style={styles.range}>
                    {rangeStart}-{rangeEnd} de {totalItems}
                </span>
            )}

            <div style={styles.pageButtons} aria-label="Navegación de páginas">
                <button
                    type="button"
                    onClick={() => onPageChange(Math.max(1, safeCurrentPage - 1))}
                    disabled={disabled || safeCurrentPage <= 1}
                    aria-label="Página anterior"
                    style={{
                        ...styles.navBtn,
                        ...((disabled || safeCurrentPage <= 1) ? styles.navBtnDisabled : {}),
                    }}
                >
                    ‹
                </button>

                {visiblePages.map((page) => {
                    const active = page === safeCurrentPage;
                    return (
                        <button
                            key={page}
                            type="button"
                            onClick={() => onPageChange(page)}
                            disabled={disabled}
                            aria-current={active ? 'page' : undefined}
                            style={{
                                ...styles.pageBtn,
                                ...(active ? styles.pageBtnActive : {}),
                            }}
                        >
                            {page}
                        </button>
                    );
                })}

                <button
                    type="button"
                    onClick={() => onPageChange(Math.min(safeTotalPages, safeCurrentPage + 1))}
                    disabled={disabled || safeCurrentPage >= safeTotalPages}
                    aria-label="Página siguiente"
                    style={{
                        ...styles.navBtn,
                        ...((disabled || safeCurrentPage >= safeTotalPages) ? styles.navBtnDisabled : {}),
                    }}
                >
                    ›
                </button>
            </div>
        </div>
    );
};

const styles: Record<string, React.CSSProperties> = {
    container: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: '1rem',
        padding: '0.75rem 0',
        flexWrap: 'wrap',
        fontSize: '0.8125rem',
        color: '#374151',
    },
    pageButtons: {
        display: 'flex',
        alignItems: 'center',
        gap: '0.25rem',
    },
    group: {
        display: 'inline-flex',
        alignItems: 'center',
        gap: '0.375rem',
    },
    label: {
        fontSize: '0.8125rem',
        color: '#374151',
    },
    select: {
        border: '1px solid #d1d5db',
        borderRadius: '0.25rem',
        padding: '0.25rem 0.5rem',
        fontSize: '0.8125rem',
        background: '#fff',
    },
    input: {
        width: '52px',
        border: '1px solid #d1d5db',
        borderRadius: '0.25rem',
        padding: '0.25rem 0.375rem',
        fontSize: '0.8125rem',
        textAlign: 'center',
    },
    range: {
        fontSize: '0.8125rem',
        color: '#374151',
    },
    navBtn: {
        minWidth: 32,
        height: 32,
        padding: '0 0.5rem',
        background: '#fff',
        border: '1px solid #d1d5db',
        borderRadius: '0.25rem',
        cursor: 'pointer',
        fontSize: '0.875rem',
        color: '#0066CC',
    },
    navBtnDisabled: {
        cursor: 'not-allowed',
        color: '#9ca3af',
        background: '#f9fafb',
    },
    pageBtn: {
        minWidth: 32,
        height: 32,
        padding: '0 0.5rem',
        background: '#fff',
        border: '1px solid #d1d5db',
        borderRadius: '0.25rem',
        cursor: 'pointer',
        fontSize: '0.8125rem',
        color: '#0066CC',
    },
    pageBtnActive: {
        background: '#003865',
        color: '#fff',
        borderColor: '#003865',
        fontWeight: 600,
    },
};

export default Pagination;
