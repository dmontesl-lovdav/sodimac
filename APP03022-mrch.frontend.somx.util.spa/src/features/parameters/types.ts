export type ParameterStatus = 0 | 1; // 0: Inactivo, 1: Activo (valores del backend)

export interface Parameter {
  id: string;
  idParameter: number; // ID numérico para API calls
  idModule: number; // ID del módulo para edición
  idType: number; // ID del tipo para edición
  name: string;
  description: string;
  module: string;
  parameterType: string;
  value: string;
  version: string;
  startDate: string;
  endDate: string | null;
  status: ParameterStatus;
  createdBy: string;
  createdAt: string;
  updatedBy?: string | null;
  updatedAt?: string | null;
}

export interface ParameterFilters {
  parameterId: string;
  parameterName: string;
  module: string;
  parameterType: string;
  status: string;
  includeHistory: boolean;
}

// Backend API Response Types
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
}

export interface PaginatedResponse<T> {
  success: boolean;
  data: T[];
  total: number;
  page: number;
  limit: number;
  totalPages: number;
}

// Backend Parameter Entity (matches backend structure)
export interface BackendParameter {
  idParameter: number;
  idModule: number;
  idType: number;
  name: string;
  description?: string;
  value: string;
  version: string;
  startDate: string;
  endDate: string | null;
  status: number; // 1 = Active, 0 = Inactive
  createdBy?: number | string;
  createdAt: string;
  updatedBy?: number | string | null;
  updatedAt?: string | null;
  // Relations
  module?: { idModule: number; name: string };
  type?: { idType: number; name: string };
}

// DTOs for API calls
export interface CreateParameterDto {
  idModule: number;
  idType: number;
  name: string;
  description?: string;
  value: string;
  startDate?: string;
  endDate?: string;
}

export interface UpdateParameterDto {
  idModule?: number;
  idType?: number;
  description?: string;
  startDate?: string;
  endDate?: string;
}

export interface CreateVersionDto {
  value: string;
  startDate?: string;
  endDate?: string;
  changeReason?: string;
}

export interface ParameterListParams {
  idModule?: number;
  idType?: number;
  name?: string;
  status?: number;
  includeHistory?: boolean;
  page?: number;
  limit?: number;
  sort?: 'idParameter' | 'name' | 'createdAt' | 'updatedAt';
  order?: 'asc' | 'desc';
}
