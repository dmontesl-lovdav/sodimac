import { useEffect, useMemo, useState } from 'react';
import GenericButton from '@shared/components/ui/button/GenericButton';
import GenericModal from '@shared/components/ui/modal/GenericModal';
import { SecurityBreadcrumb } from './SecurityBreadcrumb';
import { SecuritySearchFilters } from './SecuritySearchFilters';
import { SecurityGrid } from './SecurityGrid';
import { DualTransferList } from './DualTransferList';
import type { UseMutationResult, UseQueryResult } from '@tanstack/react-query';
import type { AssignmentResponse, SecurityFilters, SecurityRow, SearchResponse } from '../types';
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
  const [filters, setFilters] = useState<SecurityFilters>(initialFilters);
  const [hasSearched, setHasSearched] = useState(false);
  const [currentRow, setCurrentRow] = useState<SecurityRow | null>(null);

  const isFiltersInvalid = useMemo(
    () => !filters.startDate || !filters.endDate,
    [filters.startDate, filters.endDate],
  );

  const searchQuery = useSearchHook(filters, hasSearched && !isFiltersInvalid);
  const assignmentQuery = useAssignmentHook(currentRow?.id ?? 0, Boolean(currentRow));
  const saveMutation = useSaveHook();

  useEffect(() => {
    if (searchQuery.error) {
      console.error(searchQuery.error);
    }
  }, [searchQuery.error]);

  useEffect(() => {
    if (assignmentQuery.error) {
      console.error(assignmentQuery.error);
    }
  }, [assignmentQuery.error]);

  const handleSearch = async () => {
    setHasSearched(true);
  };

  const openAssign = (row: SecurityRow) => {
    setCurrentRow(row);
  };

  const rows = searchQuery.data?.items ?? [];
  const warning = isFiltersInvalid && hasSearched
    ? 'Fecha inicio y fecha fin son obligatorias.'
    : searchQuery.data?.warningMessage ?? '';
  const loading = searchQuery.isFetching || assignmentQuery.isFetching || saveMutation.isPending;

  useEffect(() => {
    if (assignmentQuery.error) {
      setCurrentRow(null);
    }
  }, [assignmentQuery.error]);

  if (currentRow) {
    return (
      <div className="security-layout">
        <SecurityBreadcrumb items={[...breadcrumb, currentRow.name]} />
        <div className="security-box">
          <DualTransferList
            title={`${title}: ${currentRow.name}`}
            leftTitle={leftTitle}
            rightTitle={rightTitle}
            leftItems={assignmentQuery.data?.available ?? []}
            rightItems={assignmentQuery.data?.assigned ?? []}
            onSave={(ids) => saveMutation.mutateAsync({ id: currentRow.id, selectedIds: ids }).then(() => setCurrentRow(null))}
            onBack={() => setCurrentRow(null)}
          />
        </div>
      </div>
    );
  }

  return (
    <div className="security-layout">
      <SecurityBreadcrumb items={breadcrumb} />
      <div className="security-box">
        <h1 className="security-title">{title}</h1>
        <p className="security-subtitle">{subtitle}</p>
        <div className="security-toolbar">
          <SecuritySearchFilters filters={filters} onChange={setFilters} />
          <div className="security-actions">
            <GenericButton variant="outline" onClick={handleSearch}>
              Buscar
            </GenericButton>
            <GenericButton
              variant="outline"
              onClick={() => {
                setFilters(initialFilters);
                setHasSearched(false);
              }}
            >
              Limpiar
            </GenericButton>
          </div>
        </div>
        {warning && <div className="security-warning">{warning}</div>}
        {!hasSearched && <div className="security-empty">Ejecuta una busqueda para consultar informacion.</div>}
        {hasSearched && rows.length > 0 && (
          <SecurityGrid title={title} items={rows} actionLabel="Asignar" onAction={openAssign} />
        )}
      </div>
      {loading && <GenericModal visible variant="loading" message="Procesando informacion..." />}
    </div>
  );
}

