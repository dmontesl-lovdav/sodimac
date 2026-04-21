import { GenericButton, GenericSelect } from "@shared/components/ui";
import { GenericDateRangePicker } from "@shared/components/ui/date";
import type { ChangeEvent, ReactElement } from "react";
import { useEffect, useRef, useState } from "react";
import {
  RebateStatusOptions,
  ProvidersOptions,
  RebateFilters,
} from "../interfaces";
import {
  fetchProvidersAsCatalog,
  getFiltersFromLocalStorage,
  saveFiltersToLocalStorage,
} from "@/utils/utils";

import "../styles/DiscountsFiltersBar.css";

type DateRange = [Date | null, Date | null];

interface Props {
  onSearch: (filters: RebateFilters) => void;
}

export default function FiltersBar({ onSearch }: Props): ReactElement {
  const [dateRange, setDateRange] = useState<DateRange>([null, null]);
  const [status, setStatus] = useState<number | undefined>(undefined);
  const [provider, setProvider] = useState<number | undefined>(undefined);
  const [providers, setProviders] = useState<ProvidersOptions[]>([]);
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
      const saved = getFiltersFromLocalStorage<RebateFilters>("rebates_filters");

      if (saved) {
        const start = saved.postingDate ? new Date(saved.postingDate) : null;
        const end = saved.dueDate ? new Date(saved.dueDate) : null;

        setDateRange([start, end]);
        setProvider(saved.supplierNumber ?? undefined);
        setStatus(saved.status ?? undefined);
      }

      hasLoadedRef.current = true;
    }
  }, [providers]);

  const handleSubmit = () => {
    const today = new Date();
    const oneMonthAgo = new Date(today);
    oneMonthAgo.setMonth(oneMonthAgo.getMonth() - 1);

    const [start, end] = dateRange;

    const filterData: RebateFilters = {
      postingDate: (start ?? oneMonthAgo).toISOString(),
      dueDate: (end ?? today).toISOString(),
      supplierNumber: provider,
      status: status,
      pageNumber: 1,
      pageSize: 10,
    };

    saveFiltersToLocalStorage("rebates_filters", filterData);
    onSearch(filterData);
  };

  return (
    <div className="dc-filters">
      <div className="dc-row">
        <div className="somx-w-2xs">
          <GenericSelect
            label="Proveedor"
            value={(provider ?? "") as any}
            onChange={(event: ChangeEvent<HTMLInputElement>) => {
              const v = event.target.value;
              setProvider(v === "" ? undefined : Number(v));
            }}
            placeholder="Proveedor"
            options={providers}
          />
        </div>

        <div className="somx-w-2xs">
          <GenericSelect
            label="Estatus"
            value={(status ?? "") as any}
            onChange={(event: ChangeEvent<HTMLInputElement>) => {
              const v = event.target.value;
              setStatus(v === "" ? undefined : Number(v));
            }}
            placeholder="Estatus"
            options={RebateStatusOptions as any}
          />
        </div>

        <div className="somx-w-2xs">
          <GenericDateRangePicker
            value={dateRange}
            onChange={(dates) => setDateRange(dates)}
            placeholder="Rango de fechas"
            size="md"
          />
        </div>

        <GenericButton
          variant="outlineFill"
          className="somx-h-11"
          onClick={handleSubmit}
        >
          Buscar
        </GenericButton>
      </div>
    </div>
  );
}