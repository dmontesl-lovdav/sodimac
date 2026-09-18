import { useCallback, useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import GenericButton from '@shared/components/ui/button/GenericButton';
import GenericModal from '@shared/components/ui/modal/GenericModal';
import { getErrorMessage, useAlertModal } from '@shared/hooks/useAlertModal';
import { MultiValueSelect } from './components/MultiValueSelect';
import { SecurityBreadcrumb } from './components/SecurityBreadcrumb';
import { DualTransferList } from './components/DualTransferList';
import { SecuritySearchFilters } from './components/SecuritySearchFilters';
import { SecurityGrid } from './components/SecurityGrid';
import {
  useAttributeTypes,
  useCreateRoleAttribute,
  useDeleteRoleAttribute,
  useRoleAttributes,
  useRoleAttributeSearch,
} from './hooks/useSecurity';
import { securityService } from './services/securityService';
import type { AssignableItem, AttributeValueOption, SecurityFilters, SecurityRow } from './types';
import { exportSecurityRowsAsCsv } from './utils/csvExport';
import './styles/SecurityCommon.css';

const ROLE_ATTRIBUTES_PAGE_SIZE = 1000;

const initialFilters: SecurityFilters = {
  startDate: '',
  endDate: '',
  entityId: '',
  entityName: '',
  status: '',
};

type RoleAttributeListRestore = {
  restoreRoleAttributeList: {
    draftFilters: SecurityFilters;
    appliedFilters: SecurityFilters | null;
  };
};

const buildSelectedValuesMap = (
  attributes: Array<{ attributeTypeId: number; attributeValueId?: number }>,
): Record<number, number[]> => {
  const next: Record<number, number[]> = {};
  for (const item of attributes) {
    if (item.attributeValueId == null) continue;
    const current = next[item.attributeTypeId] ?? [];
    if (!current.includes(item.attributeValueId)) {
      current.push(item.attributeValueId);
    }
    next[item.attributeTypeId] = current;
  }
  return next;
};

export function RoleAttributePage() {
  const location = useLocation();
  const navigate = useNavigate();
  const [draftFilters, setDraftFilters] = useState<SecurityFilters>(initialFilters);
  const [appliedFilters, setAppliedFilters] = useState<SecurityFilters | null>(null);
  const [selectedRole, setSelectedRole] = useState<SecurityRow | null>(null);
  const [valueOptionsByType, setValueOptionsByType] = useState<Record<number, AttributeValueOption[]>>({});
  const [selectedValuesByType, setSelectedValuesByType] = useState<Record<number, number[]>>({});
  const [valuesLoading, setValuesLoading] = useState(false);
  const [selectedRowIds, setSelectedRowIds] = useState<number[]>([]);
  const { showAlert, alertModal } = useAlertModal();

  const handleSelectionChange = useCallback((ids: number[]) => {
    setSelectedRowIds(ids);
  }, []);

  useEffect(() => {
    const st = location.state as RoleAttributeListRestore | null | undefined;
    const payload = st?.restoreRoleAttributeList;
    if (!payload) return;

    setSelectedRole(null);
    setDraftFilters(payload.draftFilters);
    setAppliedFilters(payload.appliedFilters);
    setSelectedValuesByType({});
    setValueOptionsByType({});
    navigate(location.pathname, { replace: true, state: {} });
  }, [location.state, location.pathname, navigate]);

  const isAppliedValid = Boolean(appliedFilters?.startDate && appliedFilters?.endDate);

  const searchQuery = useRoleAttributeSearch(appliedFilters ?? initialFilters, appliedFilters !== null && isAppliedValid);
  const attributeTypesQuery = useAttributeTypes();
  const attributesQuery = useRoleAttributes(
    selectedRole?.id ?? 0,
    1,
    ROLE_ATTRIBUTES_PAGE_SIZE,
    Boolean(selectedRole),
  );
  const createAttributeMutation = useCreateRoleAttribute();
  const deleteAttributeMutation = useDeleteRoleAttribute();

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
      message: 'No hay roles para los filtros indicados.',
      severity: 'info',
    });
  }, [searchQuery.isSuccess, appliedFilters, isAppliedValid, rows.length, showAlert]);

  useEffect(() => {
    if (!searchQuery.isError) return;
    showAlert({
      title: 'Error',
      message: getErrorMessage(searchQuery.error, 'No fue posible consultar roles.'),
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

  const openRole = (row: SecurityRow) => {
    setSelectedRole(row);
    setValueOptionsByType({});
    setSelectedValuesByType({});
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
    if (!selectedRole) return;
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

        setSelectedValuesByType(buildSelectedValuesMap(attributes));
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
  }, [selectedRole, attributeTypes, attributes]);

  const updateAssignedValues = (attributeTypeId: number, values: number[]) => {
    setSelectedValuesByType((prev) => ({
      ...prev,
      [attributeTypeId]: values,
    }));
  };

  const saveAssignments = async (selectedIds: number[]) => {
    if (!selectedRole) return false;

    const unassignedRows = attributes.filter((item) => !selectedIds.includes(item.attributeTypeId));

    try {
      for (const attributeTypeId of selectedIds) {
        const attributeType = attributeTypes.find((item) => item.id === attributeTypeId);
        const attributeTypeName = attributeType?.name ?? `Atributo ${attributeTypeId}`;
        const options = valueOptionsByType[attributeTypeId] ?? [];
        if (options.length === 0) {
          throw new Error(`No hay catálogo configurable para "${attributeTypeName}".`);
        }
        const values = selectedValuesByType[attributeTypeId] ?? [];
        if (values.length === 0) {
          throw new Error(`Selecciona al menos un valor de catálogo para "${attributeTypeName}".`);
        }
      }

      for (const attributeTypeId of selectedIds) {
        const values = selectedValuesByType[attributeTypeId] ?? [];
        const existingRows = attributes.filter((item) => item.attributeTypeId === attributeTypeId);
        const existingValueIds = existingRows
          .map((item) => item.attributeValueId)
          .filter((value): value is number => value != null);

        const valuesToAdd = values.filter((value) => !existingValueIds.includes(value));
        const rowsToRemove = existingRows.filter(
          (item) => item.attributeValueId != null && !values.includes(item.attributeValueId),
        );

        await Promise.all(
          valuesToAdd.map((attributeValueId) =>
            createAttributeMutation.mutateAsync({
              roleId: selectedRole.id,
              attributeTypeId,
              attributeValueId,
            }),
          ),
        );

        await Promise.all(
          rowsToRemove.map((row) =>
            deleteAttributeMutation.mutateAsync({ roleId: selectedRole.id, attributeId: row.id }),
          ),
        );
      }

      await Promise.all(
        unassignedRows.map((item) =>
          deleteAttributeMutation.mutateAsync({ roleId: selectedRole.id, attributeId: item.id }),
        ),
      );
      return true;
    } catch (err) {
      const message = err instanceof Error ? err.message : 'No se pudieron guardar los atributos del rol.';
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
        <small>Valores de catálogo</small>
        <MultiValueSelect
          options={options}
          selected={selectedValuesByType[item.id] ?? []}
          onChange={(values) => updateAssignedValues(item.id, values)}
        />
      </div>
    );
  };

  const renderAvailableItemExtra = (item: AssignableItem) => renderAssignedItemExtra(item);

  const roleAttributeListRestoreState: Record<string, unknown> | undefined = selectedRole
    ? {
        'Rol Atributo': {
          restoreRoleAttributeList: { draftFilters, appliedFilters },
        },
      }
    : undefined;

  if (selectedRole) {
    return (
      <div className="security-layout">
        <SecurityBreadcrumb
          items={['Inicio', 'Herramientas y Utilerias', 'Control de Acceso', 'Rol Atributo', selectedRole.name]}
          linkStateByLabel={roleAttributeListRestoreState}
        />
        <div className="security-box">
          <DualTransferList
            title={`Rol Atributo: ${selectedRole.name}`}
            leftTitle="Atributos disponibles"
            rightTitle="Atributos asignados"
            leftItems={assignmentLists.available}
            rightItems={assignmentLists.assigned}
            onSave={saveAssignments}
            renderAvailableItemExtra={renderAvailableItemExtra}
            renderAssignedItemExtra={renderAssignedItemExtra}
            onBack={() => {
              setSelectedRole(null);
              setSelectedValuesByType({});
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
    exportSecurityRowsAsCsv(sourceRows, 'Rol Atributo');
  };

  return (
    <div className="security-layout">
      <SecurityBreadcrumb items={['Inicio', 'Herramientas y Utilerias', 'Control de Acceso', 'Rol Atributo']} />
      <div className="security-box">
        <div className="security-page-header">
          <div>
            <h1 className="security-title">Rol Atributo</h1>
            <p className="security-subtitle">Consulta roles y administra sus atributos de acceso.</p>
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
          title="Rol Atributo"
          items={rows}
          actionLabel="Administrar atributos"
          onAction={openRole}
          onSelectionChange={handleSelectionChange}
        />
      </div>
      {loading && <GenericModal visible variant="loading" message="Procesando informacion..." />}
      {alertModal}
    </div>
  );
}
