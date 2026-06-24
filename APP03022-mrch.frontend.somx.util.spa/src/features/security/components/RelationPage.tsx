import { useCallback, useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import GenericButton from '@shared/components/ui/button/GenericButton';
import GenericModal from '@shared/components/ui/modal/GenericModal';
import { getErrorMessage, useAlertModal } from '@shared/hooks/useAlertModal';
import { SecurityBreadcrumb } from './SecurityBreadcrumb';
import { SecuritySearchFilters } from './SecuritySearchFilters';
import { SecurityGrid } from './SecurityGrid';
import { DualTransferList } from './DualTransferList';
import type { UseMutationResult, UseQueryResult } from '@tanstack/react-query';
import type { AssignmentResponse, SecurityFilters, SecurityRow, SearchResponse } from '../types';
import { exportSecurityRowsAsCsv } from '../utils/csvExport';
import '../styles/SecurityCommon.css';

const initialFilters: SecurityFilters = {
  startDate: '',
  endDate: '',
  entityId: '',
  entityName: '',
  status: '',
};

interface Props {
  title: string;
  subtitle: string;
  breadcrumb: string[];
  useSearchHook: (filters: SecurityFilters, enabled: boolean) => UseQueryResult<SearchResponse<SecurityRow>, Error>;
  useAssignmentHook: (id: number, enabled: boolean) => UseQueryResult<AssignmentResponse, Error>;
  useSaveHook: () => UseMutationResult<void, Error, { id: number; selectedIds: number[] }>;
  leftTitle: string;
  rightTitle: string;
}

type RelationListRestore = {
  restoreRelationList: {
    draftFilters: SecurityFilters;
    appliedFilters: SecurityFilters | null;
  };
};

export function RelationPage({
  title,
  subtitle,
  breadcrumb,
  useSearchHook,
  useAssignmentHook,
  useSaveHook,
  leftTitle,
  rightTitle,
}: Props) {
  const location = useLocation();
  const navigate = useNavigate();
  const { showAlert, alertModal } = useAlertModal();
  const [draftFilters, setDraftFilters] = useState<SecurityFilters>(initialFilters);
  const [appliedFilters, setAppliedFilters] = useState<SecurityFilters | null>(null);
  const [currentRow, setCurrentRow] = useState<SecurityRow | null>(null);
  const [selectedRowIds, setSelectedRowIds] = useState<number[]>([]);

  const handleSelectionChange = useCallback((ids: number[]) => {
    setSelectedRowIds(ids);
  }, []);

  useEffect(() => {
    const st = location.state as RelationListRestore | null | undefined;
    const payload = st?.restoreRelationList;
    if (!payload) return;

    setCurrentRow(null);
    setDraftFilters(payload.draftFilters);
    setAppliedFilters(payload.appliedFilters);
    navigate(location.pathname, { replace: true, state: {} });
  }, [location.state, location.pathname, navigate]);

  const isAppliedValid = Boolean(appliedFilters?.startDate && appliedFilters?.endDate);

  const searchQuery = useSearchHook(appliedFilters ?? initialFilters, isAppliedValid);
  const assignmentQuery = useAssignmentHook(currentRow?.id ?? 0, Boolean(currentRow));
  const saveMutation = useSaveHook();

  useEffect(() => {
    if (!searchQuery.error) return;
    console.error(searchQuery.error);
    showAlert({
      title: 'Error de consulta',
      message: getErrorMessage(searchQuery.error, 'No fue posible obtener los datos. Intente de nuevo.'),
      severity: 'error',
    });
  }, [searchQuery.error, showAlert]);

  useEffect(() => {
    if (!assignmentQuery.error) return;
    console.error(assignmentQuery.error);
    showAlert({
      title: 'Error de asignación',
      message: getErrorMessage(
        assignmentQuery.error,
        'No fue posible cargar las listas de asignación. Intente de nuevo.',
      ),
      severity: 'error',
    });
  }, [assignmentQuery.error, showAlert]);

  const handleSearch = () => {
    setAppliedFilters({ ...draftFilters });
  };

  const openAssign = (row: SecurityRow) => {
    setCurrentRow(row);
  };

  const rows = useMemo(
    () => searchQuery.data?.items ?? [],
    [searchQuery.data?.items],
  );
  const warning = useMemo(() => {
    if (appliedFilters && (!appliedFilters.startDate || !appliedFilters.endDate)) {
      return 'Fecha inicio y fecha fin son obligatorias.';
    }
    return searchQuery.data?.warningMessage ?? '';
  }, [appliedFilters, searchQuery.data?.warningMessage]);
  const loading = searchQuery.isFetching || assignmentQuery.isFetching || saveMutation.isPending;

  useEffect(() => {
    if (!warning) return;
    showAlert({ title: 'Atención', message: warning, severity: 'warning' });
  }, [warning, showAlert]);

  useEffect(() => {
    if (!searchQuery.isSuccess || !appliedFilters || !isAppliedValid) return;
    if (rows.length > 0) return;
    showAlert({
      title: 'Sin resultados',
      message: 'No hay registros para los filtros y fechas indicados.',
      severity: 'info',
    });
  }, [searchQuery.isSuccess, appliedFilters, isAppliedValid, rows.length, showAlert]);

  useEffect(() => {
    if (assignmentQuery.error) {
      setCurrentRow(null);
    }
  }, [assignmentQuery.error]);

  const listPageLabel = breadcrumb[breadcrumb.length - 1];
  const listRestoreLinkState: Record<string, unknown> | undefined =
    currentRow && appliedFilters
      ? {
          [listPageLabel]: {
            restoreRelationList: { draftFilters, appliedFilters },
          },
        }
      : undefined;

  if (currentRow) {
    return (
      <div className="security-layout">
        <SecurityBreadcrumb items={[...breadcrumb, currentRow.name]} linkStateByLabel={listRestoreLinkState} />
        <div className="security-box">
          <DualTransferList
            title={`${title}: ${currentRow.name}`}
            leftTitle={leftTitle}
            rightTitle={rightTitle}
            leftItems={assignmentQuery.data?.available ?? []}
            rightItems={assignmentQuery.data?.assigned ?? []}
            onSave={async (ids) => {
              try {
                await saveMutation.mutateAsync({ id: currentRow.id, selectedIds: ids });
                setCurrentRow(null);
              } catch (e) {
                showAlert({
                  title: 'Error al guardar',
                  message: getErrorMessage(e, 'No se pudo guardar la asignación.'),
                  severity: 'error',
                });
              }
            }}
            onBack={() => setCurrentRow(null)}
          />
        </div>
        {loading ? <GenericModal visible variant="loading" message="Procesando informacion..." /> : null}
        {alertModal}
      </div>
    );
  }

  const handleExport = () => {
    const sourceRows = selectedRowIds.length
      ? rows.filter((row) => selectedRowIds.includes(row.id))
      : rows;
    if (sourceRows.length === 0) return;
    exportSecurityRowsAsCsv(sourceRows, title);
  };

  return (
    <div className="security-layout">
      <SecurityBreadcrumb items={breadcrumb} />
      <div className="security-box">
        <div className="security-page-header">
          <div>
            <h1 className="security-title">{title}</h1>
            <p className="security-subtitle">{subtitle}</p>
          </div>
          <GenericButton variant="primary" onClick={handleExport} disabled={rows.length === 0}>
            Exportar a CSV
          </GenericButton>
        </div>
        <div className="security-toolbar">
          <SecuritySearchFilters filters={draftFilters} onChange={setDraftFilters} />
          <div className="security-actions">
            <GenericButton variant="outlineFill" onClick={handleSearch}>
              Buscar
            </GenericButton>
            <GenericButton
              variant="outlineFill"
              onClick={() => {
                setDraftFilters(initialFilters);
                setAppliedFilters(null);
              }}
            >
              Limpiar
            </GenericButton>
          </div>
        </div>
        <SecurityGrid
          title={title}
          items={rows}
          actionLabel="Asignar"
          onAction={openAssign}
          onSelectionChange={handleSelectionChange}
        />
        <div className="security-footer-back">
          <GenericButton variant="back" onClick={() => navigate('/seguridad')}>
            Volver
          </GenericButton>
        </div>
      </div>
      {loading && <GenericModal visible variant="loading" message="Procesando informacion..." />}
      {alertModal}
    </div>
  );
}

