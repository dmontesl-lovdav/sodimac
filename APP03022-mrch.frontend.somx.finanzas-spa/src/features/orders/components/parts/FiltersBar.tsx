import {
  GenericSelectSearchable,
  GenericDateRangePicker,
  GenericButton,
  GenericModal,
} from "@shared/components/ui";
import type { ReactElement } from "react";
import { useEffect, useRef, useState } from "react";
import {
  ReceptionStatusOptions,
  ProvidersOptions,
  OrdersFilters,
} from "../../interfaces";
import {
  fetchProvidersAsCatalog,
  getFiltersFromLocalStorage,
  saveFiltersToLocalStorage,
} from "@/utils/utils";

import "./FiltersBar.css";

interface Props {
  onSearch: (filters: OrdersFilters) => void;
}

export default function FiltersBar({ onSearch }: Props): ReactElement {
  const [providers, setProviders] = useState<ProvidersOptions[]>([]);
  const [provider, setProvider] = useState<string>(""); // string para Searchable
  const [status, setStatus] = useState<string>(""); // string para Searchable
  const [dateRange, setDateRange] = useState<[Date | null, Date | null]>([
    null,
    null,
  ]);

  const [rangeErrorModal, setRangeErrorModal] = useState<boolean>(false);
  const hasLoadedRef = useRef(false);

  useEffect(() => {
    const fetchProviders = async () => {
      const response = await fetchProvidersAsCatalog();
      if (response) setProviders(response);
    };
    fetchProviders();
  }, []);

  useEffect(() => {
    if (providers.length > 0 && !hasLoadedRef.current) {
      const saved = getFiltersFromLocalStorage<OrdersFilters>(
        "receptions_filters"
      );

      if (saved) {
        const start = saved.purchaseOrderDateAtInitial
          ? new Date(saved.purchaseOrderDateAtInitial)
          : null;

        const end = saved.purchaseOrderDateAtEnd
          ? new Date(saved.purchaseOrderDateAtEnd)
          : null;

        setDateRange([start, end]);
        setProvider(saved.supplierNumber ? String(saved.supplierNumber) : "");
        setStatus(saved.status ? String(saved.status) : "");
      }

      hasLoadedRef.current = true;
    }
  }, [providers]);

  const validateRange = (): boolean => {
    const [d1, d2] = dateRange;
    if (!d1 || !d2) return true;

    const diffMonths =
      (d2.getFullYear() - d1.getFullYear()) * 12 +
      (d2.getMonth() - d1.getMonth());

    return diffMonths <= 6;
  };

  const handleSearch = (): void => {
    if (!validateRange()) {
      setRangeErrorModal(true);
      return;
    }

    const today = new Date();
    const oneMonthAgo = new Date(today);
    oneMonthAgo.setMonth(today.getMonth() - 1);

    const [start, end] = dateRange;

    const filterData: OrdersFilters = {
      purchaseOrderDateAtInitial: (start ?? oneMonthAgo).toISOString(),
      purchaseOrderDateAtEnd: (end ?? today).toISOString(),
      supplierNumber: provider ? Number(provider) : undefined,
      status: status ? Number(status) : undefined,
      pageNumber: 1,
      pageSize: 10,
    };

    saveFiltersToLocalStorage("receptions_filters", filterData);
    onSearch(filterData);
  };

  return (
    <>
      <div className="rc-filters">
        <div className="rc-row">
          <GenericSelectSearchable
            value={provider}
            onChange={(e: { target: { value: string } }) =>
              setProvider(e.target.value)
            }
            options={providers}
            placeholder="Proveedor"
            widthClass="gs-width-md"
          />

          <GenericSelectSearchable
            value={status}
            onChange={(e: { target: { value: string } }) =>
              setStatus(e.target.value)
            }
            options={ReceptionStatusOptions as any}
            placeholder="Estatus"
            widthClass="gs-width-md"
          />

          <GenericDateRangePicker
            value={dateRange}
            onChange={(dates) => setDateRange(dates)}
            placeholder="Rango de fecha recepción"
            size="md"
          />

          <GenericButton variant="outline" onClick={handleSearch}>
            Buscar
          </GenericButton>
        </div>
      </div>

      <GenericModal
        visible={rangeErrorModal}
        variant="alert"
        severity="warning"
        title="Rango inválido"
        message="El rango máximo permitido es 6 meses."
        buttonText="Aceptar"
        onClose={() => setRangeErrorModal(false)}
      />
    </>
  );
}