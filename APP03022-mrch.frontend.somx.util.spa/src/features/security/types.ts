export type StatusValue = '' | '1' | '0';

export interface SecurityFilters {
  startDate: string;
  endDate: string;
  entityId: string;
  entityName: string;
  status: StatusValue;
}

export interface SelectOption {
  value: string;
  label: string;
}

export interface CatalogItem {
  id: number;
  name: string;
  status: 0 | 1;
}

export interface UserAttribute extends CatalogItem {
  userId: number;
  attributeTypeId: number;
  attributeTypeName: string;
  attributeValueId?: number;
  attributeValueName?: string;
  attributeValueKey?: string;
  createdBy: string;
  createdAt: string;
  updatedBy?: string;
  updatedAt?: string;
}

export interface AssignableItem {
  id: number;
  title: string;
  subtitle?: string;
  tags?: string[];
}

export interface SecurityRow {
  id: number;
  /** Clave logica del catalog_detail (`key`), ej. PERM_PAGO_APROBAR */
  catalogKey: string;
  name: string;
  description: string;
  status: 0 | 1;
  totalAssigned: number;
  updatedAt: string;
}

export interface TransferPayload {
  containerId: number;
  selectedIds: number[];
}

export interface SearchResponse<T> {
  items: T[];
  warningCode?: string;
  warningMessage?: string;
}

export interface AssignmentResponse {
  available: AssignableItem[];
  assigned: AssignableItem[];
}

export interface PagedUserAttributes {
  items: UserAttribute[];
  total: number;
}

export interface AttributeTypeOption {
  id: number;
  name: string;
}

export interface AttributeValueOption {
  id: number;
  catalogKey: string;
  name: string;
}

export interface CreateUserAttributePayload {
  userId: number;
  attributeTypeId: number;
  attributeValueId: number;
}

export interface RoleAttribute extends CatalogItem {
  roleId: number;
  attributeTypeId: number;
  attributeTypeName: string;
  attributeValueId?: number;
  attributeValueName?: string;
  attributeValueKey?: string;
  createdBy: string;
  createdAt: string;
  updatedBy?: string;
  updatedAt?: string;
}

export interface PagedRoleAttributes {
  items: RoleAttribute[];
  total: number;
}

export interface CreateRoleAttributePayload {
  roleId: number;
  attributeTypeId: number;
  attributeValueId: number;
}

export interface UserCatalogSearchFilters {
  startDate: string;
  endDate: string;
  email: string;
  name: string;
  status: StatusValue;
}

export interface UserCatalogRow {
  id: number;
  username: string;
  fullName: string;
  email: string | null;
  status: number;
  createdAt: string;
  modifiedAt: string;
}

export interface UserCatalogSearchResult {
  items: UserCatalogRow[];
  total: number;
  page: number;
  limit: number;
  sortBy: string;
  sortDir: string;
  warningCode?: string;
  warningMessage?: string;
}

export interface CatalogDetailGridRow {
  id: number;
  name: string;
  description: string;
}

export interface UserApplicationEventRow {
  moduleProcessId: number;
  processId: number;
  name: string;
  description: string;
  assigned: boolean;
}

/** Aplicación en catálogo con eventos anidados (solo lectura) */
export type UserCatalogApplicationRow = CatalogDetailGridRow & {
  events: UserApplicationEventRow[];
};

export interface PagedSection<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
}

export interface PermissionEventMatrixRow {
  permissionId: number;
  permissionName: string;
  permissionKey: string;
  roleId: number;
  roleName: string;
  moduleId: number;
  moduleName: string;
  processId: number;
  processName: string;
  processKey: string;
  effective: boolean;
}

export type UserCatalogDetailResponse = {
  header: UserCatalogRow;
  profile: CatalogDetailGridRow | null;
  multipleProfilesDetected: boolean;
  roles: PagedSection<CatalogDetailGridRow>;
  applications: PagedSection<UserCatalogApplicationRow>;
  permissionEventMatrix: PagedSection<PermissionEventMatrixRow>;
  attributes: PagedSection<CatalogDetailGridRow>;
  userLookupKey: string;
};

export type UserCatalogDetailQuery = {
  rolesPage?: number;
  applicationsPage?: number;
  attributesPage?: number;
  matrixPage?: number;
  matrixPageSize?: number;
};

export type UserApplicationEventsResponse = {
  application: CatalogDetailGridRow;
  events: UserApplicationEventRow[];
};
