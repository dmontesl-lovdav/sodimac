export type ParameterStatus = 'ACTIVE' | 'INACTIVE';

export interface Parameter {
  id: string;
  name: string;
  description: string;
  module: string;
  parameterType: string;
  value: string;
  version: number;
  startDate: string;
  endDate: string;
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

