export interface SystemParameter {
  idParameter: number;
  idModule: number;
  idType: number;
  name: string;
  description: string | null;
  value: string | number;
  version: string;
  startDate: string;
  endDate: string | null;
  status: string | number;
  createdBy: number;
  createdAt: string;
  updatedBy: number | null;
  updatedAt: string | null;
}

export interface SystemParametersResponse {
  success: boolean;
  data: SystemParameter[];
  total: number;
  page: number;
  limit: number;
  totalPages: number;
}

export interface SystemParameterCheckResult {
  value: string | number;
  isEnabled: boolean;
}