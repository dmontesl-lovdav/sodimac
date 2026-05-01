import { type ChangeEvent, useEffect, useMemo, useState } from 'react';
import GenericButton from '@shared/components/ui/button/GenericButton';
import GenericModal from '@shared/components/ui/modal/GenericModal';
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
import './styles/SecurityCommon.css';

const USER_ATTRIBUTES_PAGE_SIZE = 1000;

const initialFilters: SecurityFilters = {
  startDate: '',
  endDate: '',
  entityId: '',
  entityName: '',
  status: '',
};

export function UserAttributePage() {
  const [filters, setFilters] = useState<SecurityFilters>(initialFilters);
  const [hasSearched, setHasSearched] = useState(false);
  const [selectedUser, setSelectedUser] = useState<SecurityRow | null>(null);
  const [detailWarning, setDetailWarning] = useState('');
  const [valueOptionsByType, setValueOptionsByType] = useState<Record<number, AttributeValueOption[]>>({});
  const [selectedValueByType, setSelectedValueByType] = useState<Record<number, string>>({});
  const [valuesLoading, setValuesLoading] = useState(false);

  const searchQuery = useUserAttributeSearch(filters, hasSearched && Boolean(filters.startDate && filters.endDate));
  const attributeTypesQuery = useAttributeTypes();
  const attributesQuery = useUserAttributes(
    selectedUser?.id ?? 0,
    1,
    USER_ATTRIBUTES_PAGE_SIZE,
    Boolean(selectedUser),
  );
  const createAttributeMutation = useCreateUserAttribute();
  const deleteAttributeMutation = useDeleteUserAttribute();

  const rows = searchQuery.data?.items ?? [];
  const warning = !filters.startDate || !filters.endDate
    ? (hasSearched ? 'Fecha inicio y fecha fin son obligatorias.' : '')
    : searchQuery.data?.warningMessage ?? '';
  const attributeTypes = attributeTypesQuery.data ?? [];
  const attributes = attributesQuery.data?.items ?? [];
  const loading =
    searchQuery.isFetching
    || attributeTypesQuery.isFetching
    || attributesQuery.isFetching
    || valuesLoading
    || createAttributeMutation.isPending
    || deleteAttributeMutation.isPending;

  const search = async () => {
    setHasSearched(true);
  };

  const openUser = (row: SecurityRow) => {
    setSelectedUser(row);
    setDetailWarning('');
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
    if (!selectedUser || attributeTypes.length === 0) {
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
          setDetailWarning('No fue posible cargar los catálogos configurados para los atributos seleccionados.');
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

    setDetailWarning('');
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
      setDetailWarning(message);
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

  if (selectedUser) {
    return (
      <div className="security-layout">
        <SecurityBreadcrumb items={['Inicio', 'Herramientas y Utilerias', 'Control de Acceso', 'Usuario Atributo', selectedUser.name]} />
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
              setDetailWarning('');
              setSelectedValueByType({});
              setValueOptionsByType({});
            }}
          />
          {detailWarning && <div className="security-warning">{detailWarning}</div>}
        </div>
        {loading && <GenericModal visible variant="loading" message="Procesando informacion..." />}
      </div>
    );
  }

  return (
    <div className="security-layout">
      <SecurityBreadcrumb items={['Inicio', 'Herramientas y Utilerias', 'Control de Acceso', 'Usuario Atributo']} />
      <div className="security-box">
        <h1 className="security-title">Usuario Atributo</h1>
        <p className="security-subtitle">Consulta usuarios activos y administra sus atributos de acceso.</p>
        <SecuritySearchFilters filters={filters} onChange={setFilters} />
        <div className="security-actions">
          <div>
            <GenericButton type="button" variant="primary" onClick={search}>Buscar</GenericButton>
            <GenericButton type="button" variant="link" onClick={() => { setFilters(initialFilters); setHasSearched(false); }}>Limpiar</GenericButton>
          </div>
        </div>
        {warning && <div className="security-warning">{warning}</div>}
        {!hasSearched && <div className="security-empty">Ejecuta una busqueda para consultar informacion.</div>}
        {hasSearched && rows.length > 0 && (
          <SecurityGrid
            title="Usuario Atributo"
            items={rows}
            actionLabel="Administrar atributos"
            onAction={openUser}
          />
        )}
      </div>
      {loading && <GenericModal visible variant="loading" message="Procesando informacion..." />}
    </div>
  );
}
