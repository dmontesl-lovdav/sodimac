import { useState, useCallback, useEffect, useRef } from "react";

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

/** Respuesta posible del fetch (directa o envuelta en .data) */
interface FetchResultShape<T> {
  content?: T[];
  totalElements?: number;
  totalPages?: number;
  page?: number;
  size?: number;
  data?: FetchResultShape<T>;
}

export interface ApiErrorPayload {
  errorCode?: string;
  message?: string;
}

/** Forma del error con response (ej. Axios) o data */
interface ErrWithPayload {
  response?: { data?: Record<string, unknown> };
  data?: Record<string, unknown>;
}

/** Objeto filtros sin page/size para comparar cambios */
interface FiltersSnapshot {
  [key: string]: unknown;
}

export function parseFetchError(err: unknown): ApiErrorPayload | null {
  if (!err || typeof err !== "object") return null;
  const e = err as ErrWithPayload;
  const data = e.response?.data ?? e.data;
  if (!data || typeof data !== "object") return null;
  const errorCode = typeof data.errorCode === "string" ? data.errorCode : undefined;
  const message = typeof data.message === "string" ? data.message : undefined;
  if (!errorCode && !message) return null;
  return { errorCode, message };
}

export interface UsePaginatedDataOptions<T, F> {
  fetchFn: (filters: F & { page: number; size: number }) => Promise<PaginatedResponse<T>>;
  initialFilters: F;
  initialPage?: number;
  initialSize?: number;
  onError?: (err: unknown) => void;
  /** Si es false, no consulta hasta que el usuario dispare búsqueda (p. ej. clic en Buscar). */
  fetchEnabled?: boolean;
  /** Incrementar en cada búsqueda explícita para re-consultar aunque los filtros no cambien. */
  searchToken?: number;
}

export function usePaginatedData<T, F = Record<string, unknown>>({
  fetchFn,
  initialFilters,
  initialPage = 0,
  initialSize = 10,
  onError,
  fetchEnabled = false,
  searchToken,
}: UsePaginatedDataOptions<T, F>) {
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<ApiErrorPayload | null>(null);
  const [rows, setRows] = useState<T[]>([]);
  const [filters, setFilters] = useState<F>(initialFilters);
  const [page, setPage] = useState<number>(initialPage);
  const [size, setSize] = useState<number>(initialSize);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [totalItems, setTotalItems] = useState<number>(0);

  const getFiltersWithoutPagination = (f: F): string => {
    const snapshot: FiltersSnapshot = { ...(f as FiltersSnapshot) };
    delete snapshot.page;
    delete snapshot.size;
    return JSON.stringify(snapshot);
  };

  const previousFiltersRef = useRef<string>(getFiltersWithoutPagination(initialFilters));
  const prevFetchEnabledRef = useRef(false);
  const prevSearchTokenRef = useRef<number | undefined>(searchToken);

  const fetchData = useCallback(
    async (currentFilters: F, currentPage: number, currentSize: number) => {
      try {
        setError(null);
        setLoading(true);
        const result = await fetchFn({
          ...currentFilters,
          page: currentPage,
          size: currentSize,
        });
        const raw = result as FetchResultShape<T>;
        const data = raw?.data ?? raw;
        const content = raw?.content ?? data?.content ?? [];
        const totalElements =
          raw?.totalElements ?? data?.totalElements ?? (Array.isArray(content) ? content.length : 0);
        const totalPages = raw?.totalPages ?? data?.totalPages ?? Math.ceil(totalElements / currentSize);
        const pageResult = raw?.page ?? data?.page ?? currentPage;

        setRows(Array.isArray(content) ? content : []);
        setTotalPages(totalPages);
        setTotalItems(totalElements);
        setPage(pageResult);
      } catch (err) {
        onError?.(err);
        console.error("Error al obtener datos:", err);
        const payload = parseFetchError(err) ?? { message: "Error al obtener los datos" };
        setError(payload);
        setRows([]);
        setTotalPages(0);
        setTotalItems(0);
      } finally {
        setLoading(false);
      }
    },
    [fetchFn, onError]
  );

  const search = useCallback(
    (newFilters: F) => {
      setFilters(newFilters);
      fetchData(newFilters, 0, size);
    },
    [fetchData, size]
  );

  const changePage = useCallback(
    (newPage: number) => {
      if (!fetchEnabled) return;
      const pageIndex = newPage - 1;
      setPage(pageIndex);
      fetchData(filters, pageIndex, size);
    },
    [filters, size, fetchData, fetchEnabled]
  );

  const changePerPage = useCallback(
    (newSize: number) => {
      if (!fetchEnabled) return;
      setSize(newSize);
      setPage(0);
      fetchData(filters, 0, newSize);
    },
    [filters, fetchData, fetchEnabled]
  );

  const fetchDataRef = useRef(fetchData);
  fetchDataRef.current = fetchData;

  // Sin búsqueda activa: sincronizar filtros y vaciar resultados previos
  useEffect(() => {
    if (fetchEnabled) return;
    setFilters(initialFilters);
    previousFiltersRef.current = getFiltersWithoutPagination(initialFilters);
    setRows([]);
    setTotalPages(0);
    setTotalItems(0);
    setError(null);
    setLoading(false);
  }, [initialFilters, fetchEnabled]);

  // Consultar solo cuando fetchEnabled (Buscar) o al cambiar filtros tras una búsqueda
  useEffect(() => {
    if (!fetchEnabled) {
      prevFetchEnabledRef.current = false;
      prevSearchTokenRef.current = searchToken;
      return;
    }

    const enabledJustNow = !prevFetchEnabledRef.current;
    prevFetchEnabledRef.current = true;

    const currentFiltersStr = getFiltersWithoutPagination(initialFilters);
    const filtersChanged = currentFiltersStr !== previousFiltersRef.current;
    const searchTriggered =
      searchToken !== undefined && searchToken !== prevSearchTokenRef.current;

    if (enabledJustNow || filtersChanged || searchTriggered) {
      previousFiltersRef.current = currentFiltersStr;
      prevSearchTokenRef.current = searchToken;
      setFilters(initialFilters);
      setPage(initialPage);
      const newSize = initialSize ?? size;
      setSize(newSize);
      fetchDataRef.current(initialFilters, initialPage, newSize);
    }
  }, [initialFilters, initialPage, initialSize, fetchEnabled, size, searchToken]);

  return {
    loading,
    error,
    rows,
    filters,
    page: page + 1,
    size,
    totalPages,
    totalItems,
    search,
    changePage,
    changePerPage,
    refresh: () => {
      if (!fetchEnabled) return Promise.resolve();
      return fetchData(filters, page, size);
    },
  };
}
