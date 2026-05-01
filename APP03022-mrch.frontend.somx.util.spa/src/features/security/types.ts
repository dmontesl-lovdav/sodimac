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

