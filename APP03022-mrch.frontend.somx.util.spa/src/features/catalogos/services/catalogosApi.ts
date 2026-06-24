import apiClient from '@/services/apiClient';

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
  emailPrincipal?: string | null;
  emailFinancial?: string | null;
  emailCommercial?: string | null;
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
  emailPrincipal: string;
  emailFinancial: string;
  emailCommercial?: string;
}

export interface SupplierUpdateDto {
  rfc?: string;
  businessName?: string;
  supplierTypeId?: number;
  logo?: string;
  paymentConditionId?: number;
  emailPrincipal?: string;
  emailFinancial?: string;
  emailCommercial?: string;
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

const userHeaders = (userId: string) => ({ headers: { 'X-User-Id': userId } });

export const supplierService = {
  getAll: async (status?: number): Promise<Supplier[]> => {
    const params = status !== undefined ? { status } : undefined;
    return apiClient.request<Supplier[]>('/suppliers', 'get', undefined, params ? { params } : undefined);
  },

  getById: async (id: number): Promise<Supplier> => {
    return apiClient.request<Supplier>(`/suppliers/${id}`, 'get');
  },

  getByNumber: async (supplierNumber: string): Promise<Supplier> => {
    return apiClient.request<Supplier>(`/suppliers/number/${supplierNumber}`, 'get');
  },

  create: async (data: SupplierCreateDto, userId: string = 'system'): Promise<Supplier> => {
    return apiClient.request<Supplier>('/suppliers', 'post', data, userHeaders(userId));
  },

  update: async (id: number, data: SupplierUpdateDto, userId: string = 'system'): Promise<Supplier> => {
    return apiClient.request<Supplier>(`/suppliers/${id}`, 'put', data, userHeaders(userId));
  },

  delete: async (id: number, userId: string = 'system'): Promise<void> => {
    await apiClient.request<void>(`/suppliers/${id}`, 'delete', undefined, userHeaders(userId));
  },

  getTypes: async (): Promise<SupplierType[]> => {
    return apiClient.request<SupplierType[]>('/suppliers/types', 'get');
  },

  getPaymentConditions: async (): Promise<PaymentCondition[]> => {
    return apiClient.request<PaymentCondition[]>('/suppliers/payment-conditions', 'get');
  },
};

export const supplierBlockService = {
  getAll: async (status?: number): Promise<SupplierBlock[]> => {
    const params = status !== undefined ? { status } : undefined;
    return apiClient.request<SupplierBlock[]>('/supplier-blocks', 'get', undefined, params ? { params } : undefined);
  },

  getById: async (id: number): Promise<SupplierBlock> => {
    return apiClient.request<SupplierBlock>(`/supplier-blocks/${id}`, 'get');
  },

  getBySupplierNumber: async (supplierNumber: string): Promise<SupplierBlock[]> => {
    return apiClient.request<SupplierBlock[]>(`/supplier-blocks/supplier/${supplierNumber}`, 'get');
  },

  create: async (data: SupplierBlockCreateDto, userId: string = 'system'): Promise<SupplierBlock> => {
    return apiClient.request<SupplierBlock>('/supplier-blocks', 'post', data, userHeaders(userId));
  },

  update: async (id: number, data: SupplierBlockUpdateDto, userId: string = 'system'): Promise<SupplierBlock> => {
    return apiClient.request<SupplierBlock>(`/supplier-blocks/${id}`, 'put', data, userHeaders(userId));
  },

  delete: async (id: number, userId: string = 'system'): Promise<void> => {
    await apiClient.request<void>(`/supplier-blocks/${id}`, 'delete', undefined, userHeaders(userId));
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

export interface LayoutValidationError {
  row: number;
  cell: string;
  column: string;
  message: string;
}

export interface LayoutValidationResponse {
  isValid: boolean;
  errorCount: number;
  errors: LayoutValidationError[];
  reportAvailable: boolean;
  reportId?: string | null;
  rowsProcessed: number;
}

export const catalogService = {
  search: async (params: CatalogSearchParams = {}): Promise<CatalogPageResponse> => {
    return apiClient.request<CatalogPageResponse>('/catalogos', 'get', undefined, { params });
  },

  getById: async (id: number): Promise<CatalogResponse> => {
    return apiClient.request<CatalogResponse>(`/catalogos/${id}`, 'get');
  },

  create: async (data: CatalogCreateDto, userId: string = 'system'): Promise<CatalogResponse> => {
    return apiClient.request<CatalogResponse>('/catalogos', 'post', data, userHeaders(userId));
  },

  update: async (id: number, data: CatalogUpdateDto, userId: string = 'system'): Promise<CatalogResponse> => {
    return apiClient.request<CatalogResponse>(`/catalogos/${id}`, 'put', data, userHeaders(userId));
  },

  getPrimaryCatalogs: async (): Promise<CatalogSimple[]> => {
    return apiClient.request<CatalogSimple[]>('/catalogos/primarios', 'get');
  },
  validateLayout: async (
    file: File,
    tipoCatalogoSeleccionado: 'PRIMARIO' | 'SECUNDARIO',
    nombreCatalogo: string,
    modoCarga: 'NUEVO_CATALOGO' | 'IMPORTAR_ELEMENTOS' = 'NUEVO_CATALOGO',
  ): Promise<LayoutValidationResponse> => {
    const payload = new FormData();
    payload.append('file', file);
    payload.append('tipoCatalogoSeleccionado', tipoCatalogoSeleccionado);
    payload.append('nombreCatalogo', nombreCatalogo);
    payload.append('modoCarga', modoCarga);
    return apiClient.request<LayoutValidationResponse>(
      '/catalogos/validate-layout',
      'post',
      payload,
    );
  },
};

export const catalogElementService = {
  search: async (catalogId: number, params: CatalogElementSearchParams = {}): Promise<CatalogElementPageResponse> => {
    return apiClient.request<CatalogElementPageResponse>(`/catalogos/${catalogId}/elementos`, 'get', undefined, { params });
  },

  getById: async (catalogId: number, elementId: number): Promise<CatalogElement> => {
    return apiClient.request<CatalogElement>(`/catalogos/${catalogId}/elementos/${elementId}`, 'get');
  },

  create: async (catalogId: number, data: CatalogElementCreateDto, userId: string = 'system'): Promise<CatalogElement> => {
    return apiClient.request<CatalogElement>(`/catalogos/${catalogId}/elementos`, 'post', data, userHeaders(userId));
  },

  update: async (catalogId: number, elementId: number, data: CatalogElementUpdateDto, userId: string = 'system'): Promise<CatalogElement> => {
    return apiClient.request<CatalogElement>(`/catalogos/${catalogId}/elementos/${elementId}`, 'put', data, userHeaders(userId));
  },

  changeStatus: async (elementId: number, status: number, userId: string = 'system'): Promise<CatalogElement> => {
    return apiClient.request<CatalogElement>(`/catalogos/elementos/${elementId}/estatus`, 'patch', { status }, userHeaders(userId));
  },

  getActiveElements: async (catalogId: number): Promise<CatalogElement[]> => {
    return apiClient.request<CatalogElement[]>(`/catalogos/${catalogId}/elementos/activos`, 'get');
  },

  getCatalogDetail: async (catalogId: number): Promise<CatalogSimple> => {
    return apiClient.request<CatalogSimple>(`/catalogos/${catalogId}/detalle`, 'get');
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

const CONVERSIONS_BASE_PATH = '/conversions';

export const conversionService = {
  search: async (params: ConversionSearchParams): Promise<ConversionPageResponse> => {
    return apiClient.request<ConversionPageResponse>(CONVERSIONS_BASE_PATH, 'get', undefined, { params });
  },

  getById: async (id: number): Promise<Conversion> => {
    return apiClient.request<Conversion>(`${CONVERSIONS_BASE_PATH}/${id}`, 'get');
  },

  create: async (data: ConversionCreateDto, userId: string = 'system'): Promise<Conversion> => {
    return apiClient.request<Conversion>(CONVERSIONS_BASE_PATH, 'post', data, userHeaders(userId));
  },

  updatePrincipal: async (id: number, data: ConversionPrincipalDto, userId: string = 'system'): Promise<Conversion> => {
    return apiClient.request<Conversion>(`${CONVERSIONS_BASE_PATH}/${id}/principal`, 'patch', data, userHeaders(userId));
  },

  delete: async (id: number, userId: string = 'system'): Promise<void> => {
    await apiClient.request<void>(`${CONVERSIONS_BASE_PATH}/${id}`, 'delete', undefined, userHeaders(userId));
  },

  deleteMultiple: async (ids: number[], userId: string = 'system'): Promise<void> => {
    await apiClient.request<void>(CONVERSIONS_BASE_PATH, 'delete', { ids }, userHeaders(userId));
  },

  export: async (request: ConversionExportRequest, userId: string = 'system'): Promise<Blob> => {
    return apiClient.request<Blob>(`${CONVERSIONS_BASE_PATH}/export`, 'post', request, {
      headers: { 'X-User-Id': userId },
      responseType: 'blob',
    });
  },

  update: async (id: number, data: ConversionUpdateDto, userId: string = 'system'): Promise<Conversion> => {
    return apiClient.request<Conversion>(`${CONVERSIONS_BASE_PATH}/${id}`, 'put', data, userHeaders(userId));
  },
};
