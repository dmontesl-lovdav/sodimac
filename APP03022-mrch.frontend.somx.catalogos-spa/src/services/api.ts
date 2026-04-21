import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8083';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export interface SupplierType {
  id: number;
  code: string;
  description: string;
}

export interface PaymentCondition {
  id: number;
  conditionName: string;
  days: number;
}

export interface Supplier {
  id: number;
  supplierNumber: string;
  rfc: string;
  businessName: string;
  supplierType: SupplierType | null;
  logo: string | null;
  paymentCondition: PaymentCondition | null;
  status: number;
}

export interface SupplierBlock {
  id: number;
  supplierNumber: string;
  validFrom: string;
  validTo: string;
  blockReason: string;
  status: number;
  currentlyBlocked: boolean;
  createdAt: string;
  createdBy: string;
  updatedAt: string;
  updatedBy: string;
}

export interface SupplierCreateDto {
  supplierNumber: string;
  rfc: string;
  businessName: string;
  supplierTypeId?: number;
  logo?: string;
  paymentConditionId?: number;
}

export interface SupplierUpdateDto {
  rfc?: string;
  businessName?: string;
  supplierTypeId?: number;
  logo?: string;
  paymentConditionId?: number;
  status?: number;
}

export interface SupplierBlockCreateDto {
  supplierNumber: string;
  validFrom: string;
  validTo: string;
  blockReason?: string;
}

export interface SupplierBlockUpdateDto {
  validFrom?: string;
  validTo?: string;
  blockReason?: string;
  status?: number;
}

export const supplierService = {
  getAll: async (status?: number): Promise<Supplier[]> => {
    const params = status !== undefined ? { status } : {};
    const response = await api.get('/suppliers', { params });
    return response.data;
  },

  getById: async (id: number): Promise<Supplier> => {
    const response = await api.get(`/suppliers/${id}`);
    return response.data;
  },

  getByNumber: async (supplierNumber: string): Promise<Supplier> => {
    const response = await api.get(`/suppliers/number/${supplierNumber}`);
    return response.data;
  },

  create: async (data: SupplierCreateDto, userId: string = 'system'): Promise<Supplier> => {
    const response = await api.post('/suppliers', data, {
      headers: { 'X-User-Id': userId },
    });
    return response.data;
  },

  update: async (id: number, data: SupplierUpdateDto, userId: string = 'system'): Promise<Supplier> => {
    const response = await api.put(`/suppliers/${id}`, data, {
      headers: { 'X-User-Id': userId },
    });
    return response.data;
  },

  delete: async (id: number, userId: string = 'system'): Promise<void> => {
    await api.delete(`/suppliers/${id}`, {
      headers: { 'X-User-Id': userId },
    });
  },

  getTypes: async (): Promise<SupplierType[]> => {
    const response = await api.get('/suppliers/types');
    return response.data;
  },

  getPaymentConditions: async (): Promise<PaymentCondition[]> => {
    const response = await api.get('/suppliers/payment-conditions');
    return response.data;
  },
};

export const supplierBlockService = {
  getAll: async (status?: number): Promise<SupplierBlock[]> => {
    const params = status !== undefined ? { status } : {};
    const response = await api.get('/supplier-blocks', { params });
    return response.data;
  },

  getById: async (id: number): Promise<SupplierBlock> => {
    const response = await api.get(`/supplier-blocks/${id}`);
    return response.data;
  },

  getBySupplierNumber: async (supplierNumber: string): Promise<SupplierBlock[]> => {
    const response = await api.get(`/supplier-blocks/supplier/${supplierNumber}`);
    return response.data;
  },

  create: async (data: SupplierBlockCreateDto, userId: string = 'system'): Promise<SupplierBlock> => {
    const response = await api.post('/supplier-blocks', data, {
      headers: { 'X-User-Id': userId },
    });
    return response.data;
  },

  update: async (id: number, data: SupplierBlockUpdateDto, userId: string = 'system'): Promise<SupplierBlock> => {
    const response = await api.put(`/supplier-blocks/${id}`, data, {
      headers: { 'X-User-Id': userId },
    });
    return response.data;
  },

  delete: async (id: number, userId: string = 'system'): Promise<void> => {
    await api.delete(`/supplier-blocks/${id}`, {
      headers: { 'X-User-Id': userId },
    });
  },
};

export interface CatalogResponse {
  id: number;
  code: string;
  prefix: string;
  name: string;
  description: string;
  module: string;
  catalogType: string;
  status: number;
  createdBy: string;
  createdAt: string;
  updatedBy: string;
  updatedAt: string;
  elementCount: number;
}

export interface CatalogPageResponse {
  items: CatalogResponse[];
  page: number;
  pageSize: number;
  total: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface CatalogCreateDto {
  code?: string;
  prefix?: string;
  name: string;
  description?: string;
  catalogType: string;
  module?: string;
}

export interface CatalogUpdateDto {
  name?: string;
  description?: string;
  catalogType?: string;
  status?: number;
  module?: string;
}

export interface CatalogSearchParams {
  id?: number;
  nombre?: string;
  descripcion?: string;
  tipo?: string;
  estatus?: number;
  code?: string;
  prefix?: string;
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortDir?: string;
}

export interface CatalogElement {
  id: number;
  key?: string;
  element: string;
  value: string;
  externalKey: string | null;
  validFrom: string;
  validTo: string;
  status: number;
  parentCatalogId: number | null;
  parentCatalogName: string | null;
  parentElementId: number | null;
  parentElementName: string | null;
  createdBy: string;
  createdAt: string;
  updatedBy: string;
  updatedAt: string;
}

export interface CatalogElementPageResponse {
  items: CatalogElement[];
  page: number;
  pageSize: number;
  total: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface CatalogElementCreateDto {
  element: string;
  value?: string;
  externalKey?: string;
  validFrom: string;
  validTo?: string;
  parentCatalogId?: number;
  parentElementId?: number;
}

export interface CatalogElementUpdateDto {
  element?: string;
  value?: string;
  externalKey?: string;
  validFrom?: string;
  validTo?: string;
  parentCatalogId?: number;
  parentElementId?: number;
  status?: number;
}

export interface CatalogElementSearchParams {
  idElemento?: number;
  clave?: string;
  elemento?: string;
  valor?: string;
  idCatalogoPadre?: number;
  idElementoPadre?: number;
  estatus?: number;
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortDir?: string;
}

export interface CatalogSimple {
  id: number;
  name: string;
  catalogType?: string;
}

export const catalogService = {
  search: async (params: CatalogSearchParams = {}): Promise<CatalogPageResponse> => {
    const response = await api.get('/catalogos', { params });
    return response.data;
  },

  getById: async (id: number): Promise<CatalogResponse> => {
    const response = await api.get(`/catalogos/${id}`);
    return response.data;
  },

  create: async (data: CatalogCreateDto, userId: string = 'system'): Promise<CatalogResponse> => {
    const response = await api.post('/catalogos', data, {
      headers: { 'X-User-Id': userId },
    });
    return response.data;
  },

  update: async (id: number, data: CatalogUpdateDto, userId: string = 'system'): Promise<CatalogResponse> => {
    const response = await api.put(`/catalogos/${id}`, data, {
      headers: { 'X-User-Id': userId },
    });
    return response.data;
  },

  getPrimaryCatalogs: async (): Promise<CatalogSimple[]> => {
    const response = await api.get('/catalogos/primarios');
    return response.data;
  },
};

export const catalogElementService = {
  search: async (catalogId: number, params: CatalogElementSearchParams = {}): Promise<CatalogElementPageResponse> => {
    const response = await api.get(`/catalogos/${catalogId}/elementos`, { params });
    return response.data;
  },

  getById: async (catalogId: number, elementId: number): Promise<CatalogElement> => {
    const response = await api.get(`/catalogos/${catalogId}/elementos/${elementId}`);
    return response.data;
  },

  create: async (catalogId: number, data: CatalogElementCreateDto, userId: string = 'system'): Promise<CatalogElement> => {
    const response = await api.post(`/catalogos/${catalogId}/elementos`, data, {
      headers: { 'X-User-Id': userId },
    });
    return response.data;
  },

  update: async (catalogId: number, elementId: number, data: CatalogElementUpdateDto, userId: string = 'system'): Promise<CatalogElement> => {
    const response = await api.put(`/catalogos/${catalogId}/elementos/${elementId}`, data, {
      headers: { 'X-User-Id': userId },
    });
    return response.data;
  },

  changeStatus: async (elementId: number, status: number, userId: string = 'system'): Promise<CatalogElement> => {
    const response = await api.patch(`/catalogos/elementos/${elementId}/estatus`, { status }, {
      headers: { 'X-User-Id': userId },
    });
    return response.data;
  },

  getActiveElements: async (catalogId: number): Promise<CatalogElement[]> => {
    const response = await api.get(`/catalogos/${catalogId}/elementos/activos`);
    return response.data;
  },

  getCatalogDetail: async (catalogId: number): Promise<CatalogSimple> => {
    const response = await api.get(`/catalogos/${catalogId}/detalle`);
    return response.data;
  },
};

export interface SourceElementInfo {
  id: number;
  nombre: string;
  valor: string;
  estatus: string;
  fechaRegistro: string;
  catalogoOrigen: string;
}

export interface Conversion {
  id?: number;
  idConversion: number;
  idElementoOrigen: number;
  elementoOrigen: string;
  valorElementoOrigen: string;
  estatusElementoOrigen: string;
  fechaRegistroElementoOrigen: string;
  catalogoElementoOrigen: string;
  idElementoDestino: number;
  idElemento: number; 
  elemento: string;
  valor: string;
  idCatalogoOrigen: number;
  catalogoOrigen: string;
  fechaInicioVigencia: string | null;
  fechaFinVigencia: string | null;
  estatus: string;
  conversionPrincipal: boolean; 
  esPrincipal: boolean; 
  idUsuarioRegistro: string;
  fechaRegistro: string;
  idUsuarioActualizacion: string | null;
  fechaActualizacion: string | null;
  version?: number;
}

export interface ConversionPageResponse {
  items: Conversion[];
  page: number;
  pageSize: number;
  total: number;
  totalPages: number;
  sourceElement: SourceElementInfo;
}

export interface ConversionSearchParams {
  idElementoOrigen: number;
  idElemento?: number;
  elemento?: string;
  valorElemento?: string;
  catalogoOrigen?: string;
  estatus?: number;
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortDir?: string;
}

export interface ConversionCreateDto {
  sourceElementId: number;
  targetElementId: number;
  validFrom?: string;
  validTo?: string;
  isPrincipal?: boolean;
}

export interface ConversionPrincipalDto {
  conversionPrincipal: boolean; 
}

export interface ConversionDeleteRequest {
  ids: number[];
}

export interface ConversionExportRequest {
  sourceElementId: number;
  scope: 'ALL' | 'SELECTED';
  format: 'XLSX' | 'CSV';
  filters?: {
    idElemento?: number;
    elemento?: string;
    valorElemento?: string;
    catalogoOrigen?: string;
    estatus?: number;
  };
  selectedIds?: number[];
}

export interface ConversionUpdateDto {
  targetElementId: number;
  validFrom?: string;
  validTo?: string;
}

const CONVERSIONS_BASE_PATH = '/api/v1/conversions';

export const conversionService = {
  search: async (params: ConversionSearchParams): Promise<ConversionPageResponse> => {
    const response = await api.get(CONVERSIONS_BASE_PATH, { params });
    return response.data;
  },

  getById: async (id: number): Promise<Conversion> => {
    const response = await api.get(`${CONVERSIONS_BASE_PATH}/${id}`);
    return response.data;
  },

  create: async (data: ConversionCreateDto, userId: string = 'system'): Promise<Conversion> => {
    const response = await api.post(CONVERSIONS_BASE_PATH, data, {
      headers: { 'X-User-Id': userId },
    });
    return response.data;
  },

  updatePrincipal: async (id: number, data: ConversionPrincipalDto, userId: string = 'system'): Promise<Conversion> => {
    const response = await api.patch(`${CONVERSIONS_BASE_PATH}/${id}/principal`, data, {
      headers: { 'X-User-Id': userId },
    });
    return response.data;
  },

  delete: async (id: number, userId: string = 'system'): Promise<void> => {
    await api.delete(`${CONVERSIONS_BASE_PATH}/${id}`, {
      headers: { 'X-User-Id': userId },
    });
  },

  deleteMultiple: async (ids: number[], userId: string = 'system'): Promise<void> => {
    await api.delete(CONVERSIONS_BASE_PATH, {
      data: { ids },
      headers: { 'X-User-Id': userId },
    });
  },

  export: async (request: ConversionExportRequest, userId: string = 'system'): Promise<Blob> => {
    const response = await api.post(`${CONVERSIONS_BASE_PATH}/export`, request, {
      headers: { 
        'X-User-Id': userId,
      },
      responseType: 'blob',
    });
    return response.data;
  },

  update: async (id: number, data: ConversionUpdateDto, userId: string = 'system'): Promise<Conversion> => {
    const response = await api.put(`${CONVERSIONS_BASE_PATH}/${id}`, data, {
      headers: { 'X-User-Id': userId },
    });
    return response.data;
  },
};

export default api;
