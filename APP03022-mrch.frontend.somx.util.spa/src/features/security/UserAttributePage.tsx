import { type ChangeEvent, useCallback, useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import GenericButton from '@shared/components/ui/button/GenericButton';
import GenericModal from '@shared/components/ui/modal/GenericModal';
import { getErrorMessage, useAlertModal } from '@shared/hooks/useAlertModal';
import { GenericSelect } from '@shared/components/ui/select';
import { SecurityBreadcrumb } from './components/SecurityBreadcrumb';
import { DualTransferList } from './components/DualTransferList';
import { SecuritySearchFilters } from './components/SecuritySearchFilters';
import { SecurityGrid } from './components/SecurityGrid';
import {
  useAttributeTypes,
  useCreateUserAttribute,
  useDeleteUserAttribute,
  useUserAttributes,
  useUserAttributeSearch,
} from './hooks/useSecurity';
import { securityService } from './services/securityService';
import type { AssignableItem, AttributeValueOption, SecurityFilters, SecurityRow } from './types';
import { exportSecurityRowsAsCsv } from './utils/csvExport';
import './styles/SecurityCommon.css';

const USER_ATTRIBUTES_PAGE_SIZE = 1000;

const initialFilters: SecurityFilters = {
  startDate: '',
  endDate: '',
  entityId: '',
  entityName: '',
  status: '',
};

type UserAttributeListRestore = {
  restoreUserAttributeList: {
    draftFilters: SecurityFilters;
    appliedFilters: SecurityFilters | null;
  };
};

export function UserAttributePage() {
  const location = useLocation();
  const navigate = useNavigate();
  const [draftFilters, setDraftFilters] = useState<SecurityFilters>(initialFilters);
  const [appliedFilters, setAppliedFilters] = useState<SecurityFilters | null>(null);
  const [selectedUser, setSelectedUser] = useState<SecurityRow | null>(null);
  const [valueOptionsByType, setValueOptionsByType] = useState<Record<number, AttributeValueOption[]>>({});
  const [selectedValueByType, setSelectedValueByType] = useState<Record<number, string>>({});
  const [valuesLoading, setValuesLoading] = useState(false);
  const [selectedRowIds, setSelectedRowIds] = useState<number[]>([]);
  const { showAlert, alertModal } = useAlertModal();

  const handleSelectionChange = useCallback((ids: number[]) => {
    setSelectedRowIds(ids);
  }, []);

  useEffect(() => {
    const st = location.state as UserAttributeListRestore | null | undefined;
    const payload = st?.restoreUserAttributeList;
    if (!payload) return;

    setSelectedUser(null);
    setDraftFilters(payload.draftFilters);
    setAppliedFilters(payload.appliedFilters);
    setSelectedValueByType({});
    setValueOptionsByType({});
    navigate(location.pathname, { replace: true, state: {} });
  }, [location.state, location.pathname, navigate]);

  const isAppliedValid = Boolean(appliedFilters?.startDate && appliedFilters?.endDate);

  const searchQuery = useUserAttributeSearch(appliedFilters ?? initialFilters, appliedFilters !== null && isAppliedValid);
  const attributeTypesQuery = useAttributeTypes();
  const attributesQuery = useUserAttributes(
    selectedUser?.id ?? 0,
    1,
    USER_ATTRIBUTES_PAGE_SIZE,
    Boolean(selectedUser),
  );
  const createAttributeMutation = useCreateUserAttribute();
  const deleteAttributeMutation = useDeleteUserAttribute();

  const rows = useMemo(
    () => searchQuery.data?.items ?? [],
    [searchQuery.data?.items],
  );
  const warning = useMemo(() => searchQuery.data?.warningMessage ?? '', [searchQuery.data?.warningMessage]);
  useEffect(() => {
    if (!warning || !searchQuery.isSuccess) return;
    showAlert({ title: 'Aviso', message: warning, severity: 'warning' });
  }, [warning, searchQuery.isSuccess, showAlert]);

  useEffect(() => {
    if (!searchQuery.isSuccess || !appliedFilters || !isAppliedValid) return;
    if (rows.length > 0) return;
    showAlert({
      title: 'Sin resultados',
      message: 'No hay usuarios para los filtros indicados.',
      severity: 'info',
    });
  }, [searchQuery.isSuccess, appliedFilters, isAppliedValid, rows.length, showAlert]);

  useEffect(() => {
    if (!searchQuery.isError) return;
    showAlert({
      title: 'Error',
      message: getErrorMessage(searchQuery.error, 'No fue posible consultar usuarios.'),
      severity: 'error',
    });
  }, [searchQuery.isError, searchQuery.error, showAlert]);

  const attributeTypes = useMemo(() => attributeTypesQuery.data ?? [], [attributeTypesQuery.data]);
  const attributes = useMemo(() => attributesQuery.data?.items ?? [], [attributesQuery.data?.items]);
  const loading =
    searchQuery.isFetching
    || attributeTypesQuery.isFetching
    || attributesQuery.isFetching
    || valuesLoading
    || createAttributeMutation.isPending
    || deleteAttributeMutation.isPending;

  const search = () => {
    if (!draftFilters.startDate || !draftFilters.endDate) {
      showAlert({
        title: 'Atención',
        message: 'Fecha inicio y fecha fin son obligatorias.',
        severity: 'warning',
      });
      return;
    }
    setAppliedFilters({ ...draftFilters });
  };

  const openUser = (row: SecurityRow) => {
    setSelectedUser(row);
    setValueOptionsByType({});
    setSelectedValueByType({});
  };

  const assignmentLists = useMemo(() => {
    const assignedAttributeTypeIds = new Set(attributes.map((item) => item.attributeTypeId));
    const available = attributeTypes
      .filter((item) => !assignedAttributeTypeIds.has(item.id))
      .map((item) => ({ id: item.id, title: item.name }));

    const assigned = attributeTypes
      .filter((item) => assignedAttributeTypeIds.has(item.id))
      .map((item) => ({ id: item.id, title: item.name }));

    available.sort((a, b) => a.title.localeCompare(b.title));
    assigned.sort((a, b) => a.title.localeCompare(b.title));

    return { available, assigned };
  }, [attributeTypes, attributes]);
  useEffect(() => {
    if (!selectedUser) return;
    if (attributeTypes.length === 0) {
      setValueOptionsByType({});
      return;
    }

    let cancelled = false;
    const loadOptions = async () => {
      setValuesLoading(true);
      try {
        const attributeTypeIds = Array.from(new Set(attributeTypes.map((item) => item.id)));
        const resolved = await Promise.all(
          attributeTypeIds.map(async (attributeTypeId) => ({
            attributeTypeId,
            options: await securityService.getAttributeValueCatalog(attributeTypeId),
          })),
        );

        if (cancelled) return;

        const optionsMap: Record<number, AttributeValueOption[]> = {};
        for (const row of resolved) {
          optionsMap[row.attributeTypeId] = row.options;
        }
        setValueOptionsByType(optionsMap);

        setSelectedValueByType((prev) => {
          const next: Record<number, string> = {};
          for (const attributeTypeId of attributeTypeIds) {
            const existing = attributes.find((item) => item.attributeTypeId === attributeTypeId)?.attributeValueId;
            const prevValue = prev[attributeTypeId];
            const firstOption = optionsMap[attributeTypeId]?.[0]?.id;
            const chosen = existing ?? (prevValue ? Number(prevValue) : undefined) ?? firstOption;
            if (chosen) {
              next[attributeTypeId] = String(chosen);
            }
          }
          return next;
        });
      } catch {
        if (!cancelled) {
          showAlert({
            title: 'Catálogos',
            message: 'No fue posible cargar los catálogos configurados para los atributos seleccionados.',
            severity: 'error',
          });
        }
      } finally {
        if (!cancelled) {
          setValuesLoading(false);
        }
      }
    };

    loadOptions();

    return () => {
      cancelled = true;
    };
  }, [selectedUser, attributeTypes, attributes]);

  const updateAssignedValue = (attributeTypeId: number, selectedValueId: string) => {
    setSelectedValueByType((prev) => ({
      ...prev,
      [attributeTypeId]: selectedValueId,
    }));
  };

  const saveAssignments = async (selectedIds: number[]) => {
    if (!selectedUser) return false;

    const toDelete = attributes.filter((item) => !selectedIds.includes(item.attributeTypeId));

    try {
      for (const attributeTypeId of selectedIds) {
        const options = valueOptionsByType[attributeTypeId] ?? [];
        if (options.length === 0) {
          const attributeType = attributeTypes.find((item) => item.id === attributeTypeId);
          const attributeTypeName = attributeType?.name ?? `Atributo ${attributeTypeId}`;
          throw new Error(`No hay catálogo configurable para "${attributeTypeName}".`);
        }
        const selectedValueId = Number(selectedValueByType[attributeTypeId]);
        if (!Number.isInteger(selectedValueId) || selectedValueId <= 0) {
          const attributeType = attributeTypes.find((item) => item.id === attributeTypeId);
          const attributeTypeName = attributeType?.name ?? `Atributo ${attributeTypeId}`;
          throw new Error(`Selecciona un valor de catálogo para "${attributeTypeName}".`);
        }
      }

      await Promise.all(
        selectedIds.map((attributeTypeId) =>
          createAttributeMutation.mutateAsync({
            userId: selectedUser.id,
            attributeTypeId,
            attributeValueId: Number(selectedValueByType[attributeTypeId]),
          }),
        ),
      );

      await Promise.all(
        toDelete.map((item) =>
          deleteAttributeMutation.mutateAsync({ userId: selectedUser.id, attributeId: item.id }),
        ),
      );
      return true;
    } catch (err) {
      const message = err instanceof Error ? err.message : 'No se pudieron guardar los atributos del usuario.';
      showAlert({ title: 'Error al guardar', message, severity: 'error' });
      return false;
    }
  };

  const renderAssignedItemExtra = (item: AssignableItem) => {
    const options = valueOptionsByType[item.id] ?? [];
    if (options.length === 0) {
      return (
        <small className="security-attribute-hint">
          Sin catálogo configurable
        </small>
      );
    }

    return (
      <div className="security-attribute-value-row">
        <small>Valor de catálogo</small>
        <GenericSelect
          value={selectedValueByType[item.id] ?? ''}
          onChange={(event: ChangeEvent<HTMLSelectElement>) => updateAssignedValue(item.id, event.target.value)}
          options={options.map((option) => ({
            value: String(option.id),
            label: `${option.catalogKey} - ${option.name}`,
          }))}
          placeholder="Selecciona valor"
          containerClassName="security-attribute-value-select"
          widthClass="gs-width-full"
        />
      </div>
    );
  };

  const renderAvailableItemExtra = (item: AssignableItem) => renderAssignedItemExtra(item);

  const userAttributeListRestoreState: Record<string, unknown> | undefined = selectedUser
    ? {
        'Usuario Atributo': {
          restoreUserAttributeList: { draftFilters, appliedFilters },
        },
      }
    : undefined;

  if (selectedUser) {
    return (
      <div className="security-layout">
        <SecurityBreadcrumb
          items={['Inicio', 'Herramientas y Utilerias', 'Control de Acceso', 'Usuario Atributo', selectedUser.name]}
          linkStateByLabel={userAttributeListRestoreState}
        />
        <div className="security-box">
          <DualTransferList
            title={`Usuario Atributo: ${selectedUser.name}`}
            leftTitle="Atributos disponibles"
            rightTitle="Atributos asignados"
            leftItems={assignmentLists.available}
            rightItems={assignmentLists.assigned}
            onSave={saveAssignments}
            renderAvailableItemExtra={renderAvailableItemExtra}
            renderAssignedItemExtra={renderAssignedItemExtra}
            onBack={() => {
              setSelectedUser(null);
              setSelectedValueByType({});
              setValueOptionsByType({});
            }}
          />
        </div>
        {loading && <GenericModal visible variant="loading" message="Procesando informacion..." />}
        {alertModal}
      </div>
    );
  }

  const handleExport = () => {
    const sourceRows = selectedRowIds.length
      ? rows.filter((row) => selectedRowIds.includes(row.id))
      : rows;
    if (sourceRows.length === 0) return;
    exportSecurityRowsAsCsv(sourceRows, 'Usuario Atributo');
  };

  return (
    <div className="security-layout">
      <SecurityBreadcrumb items={['Inicio', 'Herramientas y Utilerias', 'Control de Acceso', 'Usuario Atributo']} />
      <div className="security-box">
        <div className="security-page-header">
          <div>
            <h1 className="security-title">Usuario Atributo</h1>
            <p className="security-subtitle">Consulta usuarios activos y administra sus atributos de acceso.</p>
          </div>
          <GenericButton variant="primary" type="button" onClick={handleExport} disabled={rows.length === 0}>
            Exportar a CSV
          </GenericButton>
        </div>
        <div className="security-toolbar">
          <SecuritySearchFilters filters={draftFilters} onChange={setDraftFilters} />
          <div className="security-actions">
            <GenericButton variant="outlineFill" type="button" onClick={search}>
              Buscar
            </GenericButton>
            <GenericButton
              variant="outlineFill"
              type="button"
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
          title="Usuario Atributo"
          items={rows}
          actionLabel="Administrar atributos"
          onAction={openUser}
          onSelectionChange={handleSelectionChange}
        />
      </div>
      {loading && <GenericModal visible variant="loading" message="Procesando informacion..." />}
      {alertModal}
    </div>
  );
}
