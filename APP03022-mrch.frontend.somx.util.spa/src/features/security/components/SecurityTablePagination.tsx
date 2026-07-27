interface Props {
  currentPage: number;
  totalPages: number;
  totalItems: number;
  pageSize: number;
  onPrev: () => void;
  onNext: () => void;
  onPageSizeChange: (value: number) => void;
  showPageSizeSelector?: boolean;
}

export function SecurityTablePagination({
  currentPage,
  totalPages,
  totalItems,
  pageSize,
  onPrev,
  onNext,
  onPageSizeChange,
  showPageSizeSelector = true,
}: Readonly<Props>) {
  return (
    <nav className="security-pagination" aria-label="Paginación">
      <div className="security-pagination__info">
        {showPageSizeSelector ? (
          <>
            <label htmlFor="security-page-size">Filas por página</label>
            <select
              id="security-page-size"
              className="security-pagination__select"
              value={pageSize}
              onChange={(event) => onPageSizeChange(Number(event.target.value))}
            >
              {[5, 10, 20, 50].map((size) => (
                <option key={size} value={size}>
                  {size}
                </option>
              ))}
            </select>
          </>
        ) : null}
        <span>Total: {totalItems}</span>
      </div>
      <div className="security-pagination__controls">
        <button type="button" className="security-pagination__btn" onClick={onPrev} disabled={currentPage <= 1}>
          Anterior
        </button>
        <span>
          Página {currentPage} de {totalPages}
        </span>
        <button
          type="button"
          className="security-pagination__btn"
          onClick={onNext}
          disabled={currentPage >= totalPages}
        >
          Siguiente
        </button>
      </div>
    </nav>
  );
}
