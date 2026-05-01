import apiClient from '@/shared/api/apiClient';
import type {
  AssignmentResponse,
  AttributeValueOption,
  AttributeTypeOption,
  CreateUserAttributePayload,
  PagedUserAttributes,
  SearchResponse,
  SecurityFilters,
  SecurityRow,
  UserAttribute,
} from '../types';

interface ApiEnvelope<T> {
  success: boolean;
  data: T;
  count?: number;
  message?: string;
}

interface BackendSearchRow {
  id: number;
  catalogKey: string;
  name: string;
  description: string;
  status: number;
  totalAssigned: number;
  updatedAt?: string | null;
}

interface BackendSearchResponse {
  items: BackendSearchRow[];
  warningCode?: string;
  warningMessage?: string;
}

interface BackendAssignableItem {
  id: number;
  title: string;
  subtitle?: string | null;
  tags?: string[];
}

interface BackendUserAttribute {
  id: number;
  userId: number;
  name: string;
  attributeTypeId: number;
  attributeTypeName: string;
  attributeValueId?: number | null;
  attributeValueName?: string | null;
  attributeValueKey?: string | null;
  status: number;
  createdBy: number;
  createdAt: string;
  updatedBy?: number | null;
  updatedAt?: string | null;
}

interface BackendAttributeValueOption {
  id: number;
  catalogKey: string;
  name: string;
}

const mapSearchRow = (row: BackendSearchRow): SecurityRow => ({
  id: row.id,
  catalogKey: row.catalogKey ?? '',
  name: row.name,
  description: row.description,
  status: Number(row.status) === 1 ? 1 : 0,
  totalAssigned: row.totalAssigned,
  updatedAt: row.updatedAt ? row.updatedAt.split('T')[0] : '',
});

const mapUserAttribute = (row: BackendUserAttribute): UserAttribute => ({
  id: row.id,
  userId: row.userId,
  name: row.name,
  attributeTypeId: row.attributeTypeId,
  attributeTypeName: row.attributeTypeName,
  attributeValueId: row.attributeValueId == null ? undefined : Number(row.attributeValueId),
  attributeValueName: row.attributeValueName ?? undefined,
  attributeValueKey: row.attributeValueKey ?? undefined,
  status: Number(row.status) === 1 ? 1 : 0,
  createdBy: String(row.createdBy),
  createdAt: row.createdAt?.split('T')[0] ?? '',
  updatedBy: row.updatedBy == null ? undefined : String(row.updatedBy),
  updatedAt: row.updatedAt ? row.updatedAt.split('T')[0] : undefined,
});

function toParams(filters: SecurityFilters) {
  return {
    startDate: filters.startDate,
    endDate: filters.endDate,
    entityId: filters.entityId || undefined,
    entityName: filters.entityName || undefined,
    status: filters.status === '' ? undefined : Number(filters.status),
  };
}

async function fetchSearch(path: string, filters: SecurityFilters): Promise<SearchResponse<SecurityRow>> {
  const response = await apiClient.request<ApiEnvelope<BackendSearchResponse>>(
    path,
    'get',
    undefined,
    { params: toParams(filters) },
  );

  return {
    items: response.data.items.map(mapSearchRow),
    warningCode: response.data.warningCode,
    warningMessage: response.data.warningMessage,
  };
}

export const securityService = {
  searchProfileUsers: (filters: SecurityFilters) => fetchSearch('/security/profile-users', filters),

  getProfileUserAssignment: async (id: number): Promise<AssignmentResponse> => {
    const response = await apiClient.request<ApiEnvelope<AssignmentResponse>>(
      `/security/profiles/${id}/users`,
      'get',
    );
    return response.data;
  },

  saveProfileUserAssignment: async (id: number, selectedIds: number[]): Promise<void> => {
    await apiClient.request(`/security/profiles/${id}/users`, 'put', { selectedIds });
  },

  searchRoleUsers: (filters: SecurityFilters) => fetchSearch('/security/role-users', filters),

  getRoleUserAssignment: async (id: number): Promise<AssignmentResponse> => {
    const response = await apiClient.request<ApiEnvelope<AssignmentResponse>>(
      `/security/roles/${id}/users`,
      'get',
    );
    return response.data;
  },

  saveRoleUserAssignment: async (id: number, selectedIds: number[]): Promise<void> => {
    await apiClient.request(`/security/roles/${id}/users`, 'put', { selectedIds });
  },

  searchRolePermissions: (filters: SecurityFilters) => fetchSearch('/security/role-permissions', filters),

  getRolePermissionAssignment: async (id: number): Promise<AssignmentResponse> => {
    const response = await apiClient.request<ApiEnvelope<AssignmentResponse>>(
      `/security/roles/${id}/permissions`,
      'get',
    );
    return {
      available: response.data.available.map((item: BackendAssignableItem) => ({
        ...item,
        subtitle: item.subtitle ?? undefined,
        tags: item.tags ?? [],
      })),
      assigned: response.data.assigned.map((item: BackendAssignableItem) => ({
        ...item,
        subtitle: item.subtitle ?? undefined,
        tags: item.tags ?? [],
      })),
    };
  },

  saveRolePermissionAssignment: async (id: number, selectedIds: number[]): Promise<void> => {
    await apiClient.request(`/security/roles/${id}/permissions`, 'put', { selectedIds });
  },

  searchUserAttributes: (filters: SecurityFilters) => fetchSearch('/security/user-attributes', filters),

  getUserAttributes: async (userId: number, page: number, pageSize: number): Promise<PagedUserAttributes> => {
    const response = await apiClient.request<ApiEnvelope<{ items: BackendUserAttribute[]; total: number }>>(
      `/security/users/${userId}/attributes`,
      'get',
      undefined,
      { params: { page, limit: pageSize } },
    );

    return {
      items: response.data.items.map(mapUserAttribute),
      total: response.data.total,
    };
  },

  createUserAttribute: async (payload: CreateUserAttributePayload): Promise<void> => {
    await apiClient.request(`/security/users/${payload.userId}/attributes`, 'post', {
      attributeTypeId: payload.attributeTypeId,
      attributeValueId: payload.attributeValueId,
    });
  },

  deleteUserAttribute: async (userId: number, attributeId: number): Promise<void> => {
    await apiClient.request(`/security/users/${userId}/attributes/${attributeId}`, 'delete');
  },

  getAttributeCatalog: async (): Promise<AttributeTypeOption[]> => {
    const response = await apiClient.request<ApiEnvelope<AttributeTypeOption[]>>(
      '/security/catalogs/attribute-types',
      'get',
    );
    return response.data;
  },

  getAttributeValueCatalog: async (attributeTypeId: number): Promise<AttributeValueOption[]> => {
    const response = await apiClient.request<ApiEnvelope<{ items: BackendAttributeValueOption[] }>>(
      '/security/catalogs/attribute-values',
      'get',
      undefined,
      { params: { attributeTypeId } },
    );
    return (response.data.items ?? []).map((item) => ({
      id: Number(item.id),
      catalogKey: item.catalogKey,
      name: item.name,
    }));
  },

  searchProfileModules: (filters: SecurityFilters) => fetchSearch('/security/profile-modules', filters),

  getProfileModuleAssignment: async (id: number): Promise<AssignmentResponse> => {
    const response = await apiClient.request<ApiEnvelope<AssignmentResponse>>(
      `/security/profiles/${id}/modules`,
      'get',
    );
    return response.data;
  },

  saveProfileModuleAssignment: async (id: number, selectedIds: number[]): Promise<void> => {
    await apiClient.request(`/security/profiles/${id}/modules`, 'put', { selectedIds });
  },

  searchProfileModuleProcesses: (filters: SecurityFilters) =>
    fetchSearch('/security/profile-module-processes', filters),

  getProfileModuleProcessAssignment: async (id: number): Promise<AssignmentResponse> => {
    const response = await apiClient.request<ApiEnvelope<AssignmentResponse>>(
      `/security/profiles/${id}/module-processes`,
      'get',
    );
    return response.data;
  },

  saveProfileModuleProcessAssignment: async (id: number, selectedIds: number[]): Promise<void> => {
    await apiClient.request(`/security/profiles/${id}/module-processes`, 'put', { selectedIds });
  },

  searchApplicationEvents: (filters: SecurityFilters) => fetchSearch('/security/application-events', filters),

  getApplicationEventAssignment: async (id: number): Promise<AssignmentResponse> => {
    const response = await apiClient.request<ApiEnvelope<AssignmentResponse>>(
      `/security/modules/${id}/processes`,
      'get',
    );
    return response.data;
  },

  saveApplicationEventAssignment: async (id: number, selectedIds: number[]): Promise<void> => {
    await apiClient.request(`/security/modules/${id}/processes`, 'put', { selectedIds });
  },
};
