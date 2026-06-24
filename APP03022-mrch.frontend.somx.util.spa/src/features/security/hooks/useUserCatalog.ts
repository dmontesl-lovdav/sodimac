import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { useMemo } from 'react';
import { securityService } from '../services/securityService';
import type { UserCatalogDetailQuery, UserCatalogSearchFilters } from '../types';

function traceId() {
  return typeof crypto !== 'undefined' && crypto.randomUUID ? crypto.randomUUID() : String(Date.now());
}

export const userCatalogKeys = {
  search: (filters: UserCatalogSearchFilters, page: number, limit: number, sortBy: string, sortDir: string) =>
    ['security', 'user-catalog', filters, page, limit, sortBy, sortDir] as const,
  detail: (userId: number, q: UserCatalogDetailQuery) => ['security', 'user-catalog-detail', userId, q] as const,
  events: (userId: number, moduleId: number) => ['security', 'user-catalog-events', userId, moduleId] as const,
};

export function useUserCatalogSearch(
  filters: UserCatalogSearchFilters,
  page: number,
  limit: number,
  sortBy: string,
  sortDir: 'ASC' | 'DESC',
  enabled: boolean,
) {
  return useQuery({
    queryKey: userCatalogKeys.search(filters, page, limit, sortBy, sortDir),
    queryFn: () => securityService.searchUserCatalog(filters, page, limit, sortBy, sortDir, traceId()),
    enabled:
      enabled && Boolean(filters.startDate && filters.endDate && filters.endDate >= filters.startDate),
    placeholderData: keepPreviousData,
  });
}

export function useUserCatalogDetail(userId: number, query: UserCatalogDetailQuery, enabled: boolean) {
  const stableQuery = useMemo(
    () => ({
      rolesPage: query.rolesPage,
      applicationsPage: query.applicationsPage,
      attributesPage: query.attributesPage,
      matrixPage: query.matrixPage,
      matrixPageSize: query.matrixPageSize,
    }),
    [
      query.rolesPage,
      query.applicationsPage,
      query.attributesPage,
      query.matrixPage,
      query.matrixPageSize,
    ],
  );

  return useQuery({
    queryKey: userCatalogKeys.detail(userId, stableQuery),
    queryFn: () => securityService.getUserCatalogDetail(userId, stableQuery, traceId()),
    enabled: enabled && userId > 0,
    placeholderData: keepPreviousData,
  });
}

export function useUserCatalogApplicationEvents(userId: number, moduleId: number, enabled: boolean) {
  return useQuery({
    queryKey: userCatalogKeys.events(userId, moduleId),
    queryFn: () => securityService.getUserApplicationEventsCatalog(userId, moduleId, traceId()),
    enabled: enabled && userId > 0 && moduleId > 0,
  });
}
