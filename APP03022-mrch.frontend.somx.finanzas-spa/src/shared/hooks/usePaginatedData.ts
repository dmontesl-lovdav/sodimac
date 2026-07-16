import { useState, useCallback, useEffect, useRef, useMemo } from "react";

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface UsePaginatedDataOptions<T, F> {
  fetchFn: (filters: F & { page: number; size: number }) => Promise<PaginatedResponse<T>>;
  initialFilters: F;
  initialPage?: number;
  initialSize?: number;
  onError?: (err: unknown) => void;
  /** When false, skip fetches (hook still called unconditionally by the consumer). */
  enabled?: boolean;
}

export function usePaginatedData<T, F extends Record<string, any>>({
  fetchFn,
  initialFilters,
  initialPage = 0,
  initialSize = 10,
  onError,
  enabled = true,
}: UsePaginatedDataOptions<T, F>) {
  const [loading, setLoading] = useState<boolean>(false);
  const [rows, setRows] = useState<T[]>([]);
  const [filters, setFilters] = useState<F>(initialFilters);
  const [page, setPage] = useState<number>(initialPage);
  const [size, setSize] = useState<number>(initialSize);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [totalItems, setTotalItems] = useState<number>(0);
  
  // Inicializar ref con filtros sin page y size
  const getFiltersWithoutPagination = (f: F) => {
    const filtersWithoutPagination: any = { ...f };
    delete filtersWithoutPagination.page;
    delete filtersWithoutPagination.size;
    return JSON.stringify(filtersWithoutPagination);
  };
  
  const previousFiltersRef = useRef<string>(getFiltersWithoutPagination(initialFilters));

  const fetchData = useCallback(async (currentFilters: F, currentPage: number, currentSize: number) => {
    if (!enabled) return;
    try {
      setLoading(true);
      const result = await fetchFn({
        ...currentFilters,
        page: currentPage,
        size: currentSize,
      });
      // Manejar diferentes formatos de respuesta
      const content = result?.content ?? result?.data?.content ?? result ?? [];
      const totalElements = result?.totalElements ?? result?.data?.totalElements ?? content.length;
      const totalPages = result?.totalPages ?? result?.data?.totalPages ?? Math.ceil(totalElements / currentSize);
      const pageResult = result?.page ?? result?.data?.page ?? currentPage;
      
      setRows(Array.isArray(content) ? content : []);
      setTotalPages(totalPages);
      setTotalItems(totalElements);
      setPage(pageResult);
    } catch (err) {
      onError?.(err);
      console.error("Error al obtener datos:", err);
      setRows([]);
      setTotalPages(0);
      setTotalItems(0);
    } finally {
      setLoading(false);
    }
  }, [fetchFn, onError, enabled]);

  const search = useCallback((newFilters: F) => {
    setFilters(newFilters);
    fetchData(newFilters, 0, size);
  }, [fetchData, size]);

  const changePage = useCallback((newPage: number) => {
    const pageIndex = newPage - 1; // Convertir de 1-indexed a 0-indexed
    setPage(pageIndex);
    fetchData(filters, pageIndex, size);
  }, [filters, size, fetchData]);

  const changePerPage = useCallback((newSize: number) => {
    setSize(newSize);
    setPage(0);
    fetchData(filters, 0, newSize);
  }, [filters, fetchData]);

  // Cargar datos iniciales
  useEffect(() => {
    if (!enabled) return;
    fetchData(initialFilters, initialPage, initialSize);
    setFilters(initialFilters);
    setPage(initialPage);
    setSize(initialSize);
    previousFiltersRef.current = getFiltersWithoutPagination(initialFilters);
  }, [enabled]); // Solo al montar / cuando se habilita

  const fetchDataRef = useRef(fetchData);
  fetchDataRef.current = fetchData;

  // Sincronizar filtros externos cuando cambian (detectar cambios en initialFilters)
  useEffect(() => {
    if (!enabled) return;
    const currentFiltersStr = getFiltersWithoutPagination(initialFilters);
    
    // Solo actualizar si los filtros realmente cambiaron (ignorando page y size)
    if (currentFiltersStr !== previousFiltersRef.current) {
      previousFiltersRef.current = currentFiltersStr;
      const newSize = initialSize !== undefined ? initialSize : size;
      setFilters(initialFilters);
      setPage(initialPage);
      setSize(newSize);
      fetchDataRef.current(initialFilters, initialPage, newSize);
    }
  }, [initialFilters, initialPage, initialSize, size, enabled]);

  return {
    loading,
    rows,
    filters,
    page: page + 1, // Convertir a 1-indexed para la UI
    size,
    totalPages,
    totalItems,
    search,
    changePage,
    changePerPage,
    refresh: () => fetchData(filters, page, size),
  };
}
