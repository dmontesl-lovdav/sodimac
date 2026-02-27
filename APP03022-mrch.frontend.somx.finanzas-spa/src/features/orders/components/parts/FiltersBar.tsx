
import { GenericButton, GenericSelectFloating } from "@shared/components/ui";
import { GenericDateRangePicker } from "@shared/components/ui/date";
import type { ChangeEvent, ReactElement } from "react";
import { useEffect, useState, useRef } from "react";
import { ReceptionStatusOptions, ProvidersOptions, OrdersFilters } from "../../interfaces";
import { fetchProvidersAsCatalog, getFiltersFromLocalStorage, saveFiltersToLocalStorage } from "@/utils/utils";

interface Props {
  onSearch: (filters: OrdersFilters) => void;
}

export default function FiltersBar({ onSearch }: Props): ReactElement {
  const [range, setRange] = useState<Date[]>([]);
  const [status, setStatus] = useState(0);
  const [provider, setProvider] = useState(0);
  const [providers, setProviders] = useState<ProvidersOptions[]>([]);
  const hasLoadedRef = useRef(false);

  useEffect(() => {
    const fetchProviders = async () => {
      const response = await fetchProvidersAsCatalog();
      if (response) {
        setProviders(response);
      }
    };
    fetchProviders();
  }, []);

  useEffect(() => {
    if (providers.length > 0 && !hasLoadedRef.current) {
      const savedFilters = getFiltersFromLocalStorage<OrdersFilters>("receptions_filters");
      if (savedFilters) {
        setRange([
          new Date(savedFilters.purchaseOrderDateAtInitial),
          new Date(savedFilters.purchaseOrderDateAtEnd)
        ]);
        setProvider(savedFilters.supplierNumber || 0);
        setStatus(savedFilters.status || 0);
        hasLoadedRef.current = true;
      }
    }
  }, [providers]);

  const handleSubmit = () => {
    const oneMonthAgo = new Date();
    oneMonthAgo.setMonth(oneMonthAgo.getMonth() - 1);
    
    const filterData = {
      purchaseOrderDateAtInitial: range && range.length > 0 && range[0] ? range[0].toISOString() : oneMonthAgo.toISOString(),
      purchaseOrderDateAtEnd: range && range.length > 1 && range[1] ? range[1].toISOString() : new Date().toISOString(),
      supplierNumber: provider || undefined,
      status: status || undefined,
      pageNumber: 1,
      pageSize: 10
    };
    saveFiltersToLocalStorage("receptions_filters", filterData);
    onSearch(filterData);
  };



  return (
    <div className="somx-flex somx-flex-wrap somx-items-end somx-gap-4">
      <div className="somx-w-2xs">
        <GenericSelectFloating
          label="Proveedor"
          value={provider}
          onChange={(event: ChangeEvent<HTMLInputElement>) => setProvider(parseInt(event.target.value))}
          placeholder="Proveedor"
          options={providers}
        />
      </div>
      <div className="somx-w-2xs">
        <GenericSelectFloating
          label="Estatus"
          value={status}
          onChange={(event: ChangeEvent<HTMLInputElement>) => setStatus( parseInt(event.target.value))}
          placeholder="Estado"
          options={ReceptionStatusOptions}
        />
      </div>
      <div className="somx-w-2xs">
        <GenericDateRangePicker
          value={range}
          onChange={(dates: Date[]) => setRange(dates)}
          placeholder="Rango de Fecha Recepción"
        />
      </div>
      <GenericButton variant="outlineFill" className="somx-h-11" onClick={handleSubmit}>
        Buscar
      </GenericButton>
    </div>
  );
}
