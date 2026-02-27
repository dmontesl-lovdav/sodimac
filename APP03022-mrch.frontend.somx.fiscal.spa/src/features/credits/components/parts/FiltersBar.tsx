
import { GenericButton, GenericInput, GenericSelect, GenericSelectFloating } from "@shared/components/ui";
import { GenericDateRangePicker } from "@shared/components/ui/date";
import type { ChangeEvent, ReactElement } from "react";
import { useEffect, useState } from "react";
import { CreditsStatusOptions, CreditNoteFilters, CreditsMockProviderOptions, REQUIRED_CREDIT_NOTE_KEYS } from "../../interfaces";
import ErrorMessage from "@/shared/components/ui/alerts/ErrorMessage";

interface Props {
  onSearch: (filters: CreditNoteFilters) => void;
  initialFilters: CreditNoteFilters;
}

export default function FiltersBar({ onSearch, initialFilters }: Props): ReactElement {
  const catalogs_api = process.env.CATALOGS_API_URL || '';
  const [filters, setFilters] = useState<CreditNoteFilters>(initialFilters);
  const [missingFilter, setMissingFilter] = useState("");
  const [providers, setProviders] = useState<Array<{ label: string; value: string }>>([]);
  const [range, setRange] = useState<Date[]>(
    [
      new Date(initialFilters.fechaInicioRecepcion),
      new Date(initialFilters.fechaFinalRecepcion),
    ]
  );

  useEffect(() => {
    const fetchProviders = async () => {
      try {
        const response = await fetch(catalogs_api);
        if (response.ok) {
          const data = await response.json();
          const mappedProviders = data.map((provider: any) => ({
            label: `${provider.businessName} (${provider.rfc})`,
            value: provider.rfc=="LOSJ780126"?"JOH120507FU9":provider.rfc,
          }));
          setProviders([
            {
              label: "Todos los proveedores",
              value: ""
            }, 
            ...mappedProviders
          ]);
        }
      } catch (error) {
        console.error('Error:', error);
      }
    };
    fetchProviders();
  }, []);
    



  const handleField =
    <K extends keyof CreditNoteFilters>(key: K) =>
    (event: ChangeEvent<HTMLInputElement>) => {
      setFilters(prev => ({ ...prev, [key]: event.target.value as CreditNoteFilters[K] }));
    };

  const handleDates = (dates: Date[]) => {
    setRange(dates);
    setFilters(prev => ({
      ...prev,
      fechaInicioRecepcion: dates?.[0]?.toISOString() ?? prev.fechaInicioRecepcion,
      fechaFinalRecepcion: dates?.[1]?.toISOString() ?? prev.fechaFinalRecepcion,
    }));
  };

  const handleSubmit = () => {
    setMissingFilter("")
    for (const req of REQUIRED_CREDIT_NOTE_KEYS) {
      const val = filters[req.key]; 
      if (typeof val === "string" && val.trim() === "") {
        setMissingFilter(`${req.label} es un valor requerido`);
        return;
      }
    }
    onSearch({
      ...filters,
      tipoDocumento: "E",
      page: filters.page ?? 0,
      size: filters.size ?? 10,
    });
  };

  return (
    <div>
      <div className="somx-flex somx-flex-wrap somx-items-end somx-gap-4">
      <div>
        <GenericDateRangePicker
          value={range}
          onChange={handleDates}
          placeholder="Fecha de Recepción"
        />
      </div>

      <div style={{width: "350px"}}>
        <GenericSelectFloating
          label="Proveedor"
          value={filters.rfcEmisor}
          onChange={handleField("rfcEmisor")}
          options={providers}
          placeholder=""
        />
      </div>

      <div>
        <GenericInput
          label="Serie"
          value={filters.serie}
          onChange={handleField("serie")}
          placeholder="XXXX000XXX"
        />
      </div>

      <div>
        <GenericInput
          label="Folio"
          value={filters.folio}
          onChange={handleField("folio")}
          placeholder="000000"
        />
      </div>

      <div>
        <GenericInput
          label="UUID"
          value={filters.uuid}
          onChange={handleField("uuid")}
          placeholder="000000-0000-0000"
        />
      </div>

      <div>
        <GenericSelect
          label="Estatus de la nota"
          value={filters.status ?? ""}
          onChange={handleField("status")}
          placeholder="Estado"
          options={CreditsStatusOptions}
        />
      </div>

      <div>
        <GenericButton variant="outlineFill" className="somx-h-11" onClick={handleSubmit}>
          Buscar
        </GenericButton>
      </div>
    </div>
    <div className="somx-mt-2">
      {missingFilter != "" && <ErrorMessage message={missingFilter} />}
    </div>
    </div>
  );
}
