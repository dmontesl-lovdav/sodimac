import { type FC, useMemo } from 'react';

interface ParameterPaginationProps {
    pageSize: number;
    pageSizeInput: string;
    currentPage: number;
    goToPage: string;
    totalItems: number;
    totalPages: number;
    onPageSizeChange: (value: string) => void;
    onPageSizeCommit: () => void;
    onGoToPageChange: (value: string) => void;
    onGoToPageCommit: () => void;
    onPrevPage: () => void;
    onNextPage: () => void;
    onPageClick: (page: number) => void;
}

export const ParameterPagination: FC<ParameterPaginationProps> = ({
    pageSize,
    pageSizeInput,
    currentPage,
    goToPage,
    totalItems,
    totalPages,
    onPageSizeChange,
    onPageSizeCommit,
    onGoToPageChange,
    onGoToPageCommit,
    onPrevPage,
    onNextPage,
    onPageClick,
}) => {
    const rangeStart = totalItems === 0 ? 0 : (currentPage - 1) * pageSize + 1;
    const rangeEnd = totalItems === 0 ? 0 : Math.min(currentPage * pageSize, totalItems);

    const visiblePages = useMemo(() => {
        const maxVisible = 5;
        const pages: number[] = [];
        const start = Math.max(
            1,
            Math.min(currentPage, Math.max(1, totalPages - maxVisible + 1)),
        );
        const end = Math.min(totalPages, start + maxVisible - 1);

        for (let page = start; page <= end; page += 1) {
            pages.push(page);
        }

        return pages;
    }, [currentPage, totalPages]);

    return (
        <div
            className="parameters-grid__pagination"
            role="navigation"
            aria-label="Paginación"
            style={{ padding: 0, marginTop: 0 }}
        >
            <div className="pagination__left">
                <label className="pagination__label" htmlFor="pageSize" style={{ fontSize: '13px' }}>
                    Items por página:
                </label>
                <input
                    id="pageSize"
                    className="pagination__input pagination__input--pagesize"
                    type="number"
                    inputMode="numeric"
                    min={1}
                    max={100}
                    value={pageSizeInput}
                    onChange={(e) => onPageSizeChange(e.target.value)}
                    onBlur={onPageSizeCommit}
                    onKeyDown={(e) => e.key === 'Enter' && onPageSizeCommit()}
                />

                <label className="pagination__label" htmlFor="goToPage" style={{ fontSize: '13px' }}>
                    Ir a:
                </label>
                <input
                    id="goToPage"
                    className="pagination__input"
                    type="number"
                    inputMode="numeric"
                    min={1}
                    max={totalPages}
                    value={goToPage}
                    onChange={(e) => onGoToPageChange(e.target.value)}
                    onBlur={onGoToPageCommit}
                    onKeyDown={(e) => e.key === 'Enter' && onGoToPageCommit()}
                />

                <div className="pagination__range" aria-live="polite" style={{ fontSize: '13px' }}>
                    {rangeStart}-{rangeEnd} de {totalItems}
                </div>
            </div>

            <div className="pagination__right" aria-label="Navegación de páginas">
                <button
                    type="button"
                    className="pagination__nav"
                    onClick={onPrevPage}
                    disabled={currentPage <= 1}
                    aria-label="Página anterior"
                >
                    ‹
                </button>

                <div className="pagination__pages">
                    {visiblePages.map((page) => (
                        <button
                            key={page}
                            type="button"
                            className={`pagination__page${page === currentPage ? ' pagination__page--active' : ''}`}
                            onClick={() => onPageClick(page)}
                            aria-current={page === currentPage ? 'page' : undefined}
                        >
                            {page}
                        </button>
                    ))}
                </div>

                <button
                    type="button"
                    className="pagination__nav"
                    onClick={onNextPage}
                    disabled={currentPage >= totalPages}
                    aria-label="Página siguiente"
                >
                    ›
                </button>
            </div>
        </div>
    );
};
