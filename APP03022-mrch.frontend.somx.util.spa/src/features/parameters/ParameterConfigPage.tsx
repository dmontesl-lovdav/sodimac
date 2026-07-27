import { useMemo, useState, useEffect, useCallback } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { Breadcrumb } from './components/Breadcrumb';
import { ToolbarFilters } from './components/ToolbarFilters';
import { EmptyState } from './components/EmptyState';
import { ParameterGrid } from './components/ParameterGrid';
import { ParameterCreateForm } from './components/ParameterCreateForm';
import { ParameterEditForm } from './components/ParameterEditForm';
import { useParameters, useCatalogs, parameterKeys } from './hooks';
import type { Parameter, ParameterFilters, ParameterListParams } from './types';
import { parameterService } from './services/parameterService';
import GenericButton from '@shared/components/ui/button/GenericButton';
import GenericModal from '@shared/components/ui/modal/GenericModal';
import { useModalNotification } from '@shared/components/ui/modal';
import { extractApiErrorMessage } from '@shared/utils/errorMessage';
import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';
import documentIconUrl from '@/shared/icons/document.svg';

import './styles/ParameterContainer.css';

const INITIAL_FILTERS: ParameterFilters = {
  parameterId: '',
  parameterName: '',
  module: '',
  parameterType: '',
  status: '',
  includeHistory: false,
};

const hasExpired = (p: Parameter): boolean => {
  if (!p.endDate) return false;
  const today = new Date();
  const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;
  const endDateStr = p.endDate.split('T')[0];
  return (endDateStr ?? '') < todayStr;
};

const formatDateForExport = (value: string | null | undefined): string => {
  if (!value) return '-';
  const parts = value.split('T')[0]?.split('-');
  if (!parts || parts.length !== 3) return value;
  return `${parts[2]}-${parts[1]}-${parts[0]}`;
};

const buildApiParams = (
  filters: ParameterFilters,
  currentPage: number,
  pageSize: number,
): ParameterListParams => {
  const needsLargeLimit = filters.includeHistory &&
    (filters.parameterId || filters.parameterName);
  const params: ParameterListParams = {
    page: needsLargeLimit ? 1 : currentPage,
    limit: needsLargeLimit ? 5000 : pageSize,
    includeHistory: filters.includeHistory,
  };
  if (filters.module) params.idModule = Number(filters.module);
  if (filters.parameterType) params.idType = Number(filters.parameterType);
  if (filters.parameterName && !filters.includeHistory) {
    params.name = filters.parameterName;
  }
  if (filters.status !== '') params.status = Number(filters.status);
  return params;
};

const applyClientFilters = (
  items: Parameter[],
  filters: ParameterFilters,
): Parameter[] => {
  let result = items;
  if (filters.parameterId) {
    const searchId = filters.parameterId.toLowerCase();
    result = result.filter(p => p.id.toLowerCase().includes(searchId) || p.idParameter.toString().includes(searchId));
  }
  if (filters.parameterName) {
    const searchName = filters.parameterName.toLowerCase();
    result = result.filter(p => p.name.toLowerCase().includes(searchName));
  }
  if (!filters.includeHistory) {
    result = result.filter(p => !hasExpired(p));
  }
  if (filters.status !== '') {
    result = result.filter(p => p.status === Number(filters.status));
  }
  return [...result].sort((a, b) => {
    const dateA = a.createdAt ? new Date(a.createdAt).getTime() : 0;
    const dateB = b.createdAt ? new Date(b.createdAt).getTime() : 0;
    return dateB - dateA;
  });
};

const fetchAllForExport = async (
  filters: ParameterFilters,
  selectedIds: Set<string>,
): Promise<Parameter[]> => {
  const exportParams: ParameterListParams = { page: 1, limit: 5000, includeHistory: true };
  if (filters.module) exportParams.idModule = Number(filters.module);
  if (filters.parameterType) exportParams.idType = Number(filters.parameterType);
  if (filters.status !== '') exportParams.status = Number(filters.status);
  const allData = await parameterService.list(exportParams);
  let data = allData.data;
  if (filters.parameterId) data = data.filter(p => p.id.toLowerCase().includes(filters.parameterId.toLowerCase()));
  if (filters.parameterName) data = data.filter(p => p.name.toLowerCase().includes(filters.parameterName.toLowerCase()));
  if (selectedIds.size > 0) data = data.filter(item => selectedIds.has(item.id));
  return data;
};

const computeLatestVersionIds = (data: Parameter[] | undefined): Set<string> => {
  if (!data) return new Set();
  const latestByName = new Map<string, Parameter>();
  data.forEach((p) => {
    const existing = latestByName.get(p.name);
    if (!existing || parseFloat(p.version) > parseFloat(existing.version)) {
      latestByName.set(p.name, p);
    }
  });
  return new Set(Array.from(latestByName.values()).map((p) => p.id));
};

const buildExportFilename = (format: 'csv' | 'xlsx'): string => {
  const now = new Date();
  const timestamp = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}_${String(now.getHours()).padStart(2, '0')}${String(now.getMinutes()).padStart(2, '0')}`;
  return `parametros_${timestamp}.${format}`;
};

const formatParameterForExport = (
  item: Parameter,
  catalogData: { modules: any[]; parameterTypes: any[]; statuses: any[] },
) => ({
  'ID Parámetro': item.id,
  'Nombre Parámetro': item.name,
  'Descripción': item.description || '',
  'Módulo': item.module,
  'Tipo Parámetro': catalogData.parameterTypes.find((c) => c.value === item.parameterType)?.label || item.parameterType,
  'Valor': item.value,
  'Versión': parseFloat(String(item.version)).toFixed(1),
  'Fecha Inicio Vigencia': formatDateForExport(item.startDate),
  'Fecha Fin Vigencia': formatDateForExport(item.endDate),
  'Estatus': catalogData.statuses.find((c) => String(c.value) === String(item.status))?.label || String(item.status),
  'Usuario Registro': item.createdBy || '',
  'Fecha Registro': formatDateForExport(item.createdAt),
  'Usuario Modificación': item.updatedBy ?? '-',
  'Fecha Modificación': item.updatedAt ? formatDateForExport(item.updatedAt) : '-',
});

const writeParametersWorkbook = (
  formattedData: Record<string, unknown>[],
  format: 'csv' | 'xlsx',
  filename: string,
) => {
  const worksheet = XLSX.utils.json_to_sheet(formattedData);
  if (format === 'csv') {
    const blob = new Blob([XLSX.utils.sheet_to_csv(worksheet)], { type: 'text/csv;charset=utf-8' });
    saveAs(blob, filename);
    return;
  }
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, 'Parámetros');
  const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
  saveAs(new Blob([buffer], { type: 'application/octet-stream' }), filename);
};

export const ParameterConfigPage = () => {
  const [filters, setFilters] = useState<ParameterFilters>(INITIAL_FILTERS);
  const [appliedFilters, setAppliedFilters] = useState<ParameterFilters>(INITIAL_FILTERS);
  const [hasSearched, setHasSearched] = useState(false);
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());
  const [isExportDropdownOpen, setIsExportDropdownOpen] = useState(false);

  const [pageSize, setPageSize] = useState(10);
  const [currentPage, setCurrentPage] = useState(1);

  const [isCreating, setIsCreating] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [editingParameter, setEditingParameter] = useState<Parameter | null>(null);

  const [isStatusModalOpen, setIsStatusModalOpen] = useState(false);
  const [statusChangeParameter, setStatusChangeParameter] = useState<Parameter | null>(null);
  const [isUpdatingStatus, setIsUpdatingStatus] = useState(false);

  const [isExporting, setIsExporting] = useState(false);

  const queryClient = useQueryClient();
  const { showError, showSuccess, ModalNode } = useModalNotification();

  const { data: catalogs, isLoading: isLoadingCatalogs, error: catalogsError } = useCatalogs();

  const apiParams: ParameterListParams = useMemo(() => {
    if (!hasSearched) return {};
    return buildApiParams(appliedFilters, currentPage, pageSize);
  }, [hasSearched, currentPage, pageSize, appliedFilters]);

  const { data: parametersData, isLoading: isLoadingParameters, error: parametersError, refetch: refetchParameters } = useParameters(apiParams, hasSearched);

  const filteredItems: Parameter[] = useMemo(() => {
    if (!hasSearched || !parametersData?.data) return [];
    return applyClientFilters(parametersData.data, appliedFilters);
  }, [hasSearched, parametersData, appliedFilters]);

  const latestVersionIds: Set<string> = useMemo(
    () => computeLatestVersionIds(parametersData?.data),
    [parametersData],
  );

  const needsFrontendPagination = !appliedFilters.includeHistory || appliedFilters.parameterId || appliedFilters.parameterName;
  const totalItems = needsFrontendPagination ? filteredItems.length : (parametersData?.total ?? filteredItems.length);
  const totalPages = needsFrontendPagination
    ? Math.max(1, Math.ceil(filteredItems.length / pageSize))
    : (parametersData?.totalPages ?? Math.max(1, Math.ceil(totalItems / pageSize)));

  useEffect(() => { setCurrentPage(1); }, [pageSize]);
  const clampedCurrentPage = Math.min(currentPage, totalPages);

  const pageItems = useMemo(() => {
    if (needsFrontendPagination) {
      const startIndex = (clampedCurrentPage - 1) * pageSize;
      return filteredItems.slice(startIndex, startIndex + pageSize);
    }
    return filteredItems;
  }, [filteredItems, needsFrontendPagination, clampedCurrentPage, pageSize]);

  const handleSearch = () => {
    setAppliedFilters({ ...filters });
    setHasSearched(true);
    setSelectedIds(new Set());
    setCurrentPage(1);
    queryClient.invalidateQueries({ queryKey: parameterKeys.lists() });
    setTimeout(() => refetchParameters(), 100);
  };

  const handleClear = () => {
    setFilters(INITIAL_FILTERS);
    setAppliedFilters(INITIAL_FILTERS);
    setHasSearched(false);
    setSelectedIds(new Set());
    setCurrentPage(1);
  };

  const handleCreateSuccess = useCallback(() => {
    setIsCreating(false);
    queryClient.invalidateQueries({ queryKey: parameterKeys.lists() });
    if (hasSearched) {
      setTimeout(() => refetchParameters(), 100);
    } else {
      setAppliedFilters({ ...INITIAL_FILTERS });
      setHasSearched(true);
      setSelectedIds(new Set());
      setCurrentPage(1);
    }
  }, [hasSearched, queryClient, refetchParameters]);

  const handleEditSuccess = useCallback(() => {
    setIsEditing(false);
    setEditingParameter(null);
    queryClient.invalidateQueries({ queryKey: parameterKeys.lists() });
    if (hasSearched) setTimeout(() => refetchParameters(), 100);
  }, [hasSearched, queryClient, refetchParameters]);

  const handleConfirmStatusChange = async () => {
    if (!statusChangeParameter) return;
    setIsUpdatingStatus(true);
    try {
      const newStatus = statusChangeParameter.status === 1 ? 0 : 1;
      await parameterService.updateStatus(statusChangeParameter.idParameter, newStatus);
      setIsStatusModalOpen(false);
      setStatusChangeParameter(null);
      showSuccess('El estatus del parámetro se actualizó correctamente.', 'Operación exitosa');
      queryClient.invalidateQueries({ queryKey: parameterKeys.lists() });
      if (hasSearched) setTimeout(() => refetchParameters(), 100);
    } catch (err) {
      console.error('Error al actualizar estatus de parámetro:', err);
      setIsStatusModalOpen(false);
      setStatusChangeParameter(null);
      showError(
        extractApiErrorMessage(err, {
          fallback: 'No fue posible actualizar el estatus del parámetro. Inténtalo nuevamente.',
        }),
        'No se pudo actualizar el estatus',
      );
    } finally {
      setIsUpdatingStatus(false);
    }
  };

  const handleBack = () => {
    if (isCreating) { setIsCreating(false); return; }
    if (isEditing) { setIsEditing(false); setEditingParameter(null); return; }
    if (hasSearched) { handleClear(); return; }
    if (window.history.length > 1) { window.history.back(); return; }
    window.location.href = '/herramientas-utilerias';
  };

  const exportData = (dataToExport: Parameter[], format: 'csv' | 'xlsx') => {
    if (dataToExport.length === 0) {
      showError('No hay datos para exportar con los filtros aplicados.', 'Exportación');
      return;
    }
    if (dataToExport.length > 5000) {
      showError('El máximo de registros a exportar es de 5,000. Aplica filtros para reducir el resultado.', 'Exportación');
      return;
    }
    const catalogData = catalogs ?? { modules: [], parameterTypes: [], statuses: [] };
    const formattedData = dataToExport.map((item) => formatParameterForExport(item, catalogData));
    writeParametersWorkbook(formattedData, format, buildExportFilename(format));
  };

  const handleExport = async (format: 'csv' | 'xlsx') => {
    setIsExportDropdownOpen(false);
    if (appliedFilters.includeHistory && totalPages > 1) {
      setIsExporting(true);
      try {
        const data = await fetchAllForExport(appliedFilters, selectedIds);
        exportData(data, format);
      } catch (err) {
        console.error('Error al exportar parámetros:', err);
        showError(
          extractApiErrorMessage(err, {
            fallback: 'No fue posible obtener los datos para exportar. Inténtalo nuevamente.',
          }),
          'Error al exportar',
        );
      } finally {
        setIsExporting(false);
      }
      return;
    }
    const data = selectedIds.size > 0
      ? filteredItems.filter(item => selectedIds.has(item.id))
      : filteredItems;
    exportData(data, format);
  };

  const catalogData = catalogs ?? { modules: [], parameterTypes: [], statuses: [] };
  const getCatalogsErrorMessage = (): string | null => {
    if (catalogsError instanceof Error) return catalogsError.message;
    if (catalogsError) return 'Error al cargar catálogos';
    return null;
  };
  const catalogsErrorMessage = getCatalogsErrorMessage();

  return (
    <div className="param-layout">
      <Breadcrumb
        items={[
          'Inicio',
          'Herramientas y Utilerías',
          'Configuración de Parámetros',
          ...(isCreating ? ['Nuevo Parámetro'] : []),
          ...(isEditing ? ['Editar Parámetro'] : []),
        ]}
      />

      <div className="param-box">
        {(() => {
          if (isEditing && editingParameter) {
            return (
              <ParameterEditForm
                parameter={editingParameter}
                onSuccess={handleEditSuccess}
                onCancel={() => { setIsEditing(false); setEditingParameter(null); }}
                catalogs={catalogData}
              />
            );
          }
          if (isCreating) {
            return (
              <ParameterCreateForm
                onSuccess={handleCreateSuccess}
                onCancel={() => setIsCreating(false)}
                catalogs={catalogData}
              />
            );
          }
          return (
          <>
            <div className="param-header">
              <div>
                <div className="param-title-group">
                  <img src={documentIconUrl} alt="" className="param-title-icon" />
                  <h1 className="param-title">Configuración de Parámetros</h1>
                </div>
                <p className="param-description">
                  Busca, gestiona y consulta parámetros junto con sus valores. Además,
                  obtén información detallada sobre los cambios realizados en cada uno de ellos.
                </p>
              </div>
              <GenericButton variant="primary" onClick={() => setIsCreating(true)}>
                + Nuevo Parámetro
              </GenericButton>
            </div>

            <ToolbarFilters
              filters={filters}
              onFiltersChange={setFilters}
              onSearch={handleSearch}
              onClear={handleClear}
              catalogs={catalogData}
              isLoading={isLoadingCatalogs || isLoadingParameters}
              error={catalogsErrorMessage}
            />

            {(() => {
              if (!hasSearched) return <EmptyState />;
              if (isLoadingParameters) {
                return <GenericModal visible variant="loading" message="Cargando parámetros..." />;
              }
              if (parametersError) {
                return (
                  <GenericModal
                    visible
                    variant="alert"
                    severity="error"
                    title="Error"
                    message={parametersError instanceof Error ? parametersError.message : 'Error desconocido'}
                    buttonText="Aceptar"
                    onClose={() => {}}
                  />
                );
              }
              return (
                <ParameterGrid
                  items={pageItems}
                  totalItems={totalItems}
                  catalogs={catalogData}
                  selectedIds={selectedIds}
                  onSelectionChange={setSelectedIds}
                  onEdit={(p) => { setEditingParameter(p); setIsEditing(true); }}
                  onStatusChange={(p) => { setStatusChangeParameter(p); setIsStatusModalOpen(true); }}
                  latestVersionIds={latestVersionIds}
                  pageSize={pageSize}
                  currentPage={clampedCurrentPage}
                  totalPages={totalPages}
                  onPageSizeChange={(s) => { setPageSize(s); setCurrentPage(1); }}
                  onPageChange={setCurrentPage}
                />
              );
            })()}

            <div className="param-footer">
              <div style={{ flex: 1 }} />
              <div className="param-footer-right">
                <GenericButton variant="link" onClick={handleBack}>
                  Volver
                </GenericButton>
                {hasSearched && filteredItems.length > 0 && (
                  <div className="param-export-dropdown">
                    <GenericButton
                      variant="outline"
                      disabled={isExporting}
                      onClick={() => !isExporting && setIsExportDropdownOpen(!isExportDropdownOpen)}
                    >
                      {isExporting ? 'Exportando...' : 'Exportar como ▾'}
                    </GenericButton>
                    {isExportDropdownOpen && (
                      <div className="param-export-menu">
                        <button className="param-export-item" onClick={() => handleExport('csv')}>Archivo CSV</button>
                        <button className="param-export-item" onClick={() => handleExport('xlsx')}>Archivo XLSX</button>
                      </div>
                    )}
                  </div>
                )}
              </div>
            </div>
          </>
          );
        })()}
      </div>

      <GenericModal
        visible={isStatusModalOpen && !!statusChangeParameter}
        variant="confirm"
        severity="warning"
        title="Confirmar cambio de estatus"
        message={`¿Está seguro de cambiar el estatus del parámetro ${statusChangeParameter?.name ?? ''}?`}
        confirmText={isUpdatingStatus ? 'Actualizando...' : 'Confirmar'}
        cancelText="Cancelar"
        onConfirm={handleConfirmStatusChange}
        onCancel={() => { setIsStatusModalOpen(false); setStatusChangeParameter(null); }}
      />

      {isExporting && <GenericModal visible variant="loading" message="Exportando datos..." />}

      {ModalNode}
    </div>
  );
};
