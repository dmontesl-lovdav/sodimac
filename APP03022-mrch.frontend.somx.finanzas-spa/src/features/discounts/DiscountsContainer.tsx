import { ReactElement, useState } from "react";
import { Breadcrumb, GenericModal } from "@shared/components/ui";
import { DiscountsClient } from "./api/DiscountsClient";
import type { Rebate, RebateFilters } from "./interfaces";
import { RebateStatusOptions } from "./interfaces";
import FiltersBar from "./components/FiltersBar";
import { Title, Divider } from "@/shared/components/ui/misc";

import DiscountsGridTable from "./components/DiscountsGridTable";

import "./styles/DiscountsContainer.css";

/* ---------------------- Status Renderer ---------------------- */
const renderStatus = (status: number) => {
  const selected = RebateStatusOptions.filter((item) => item.value == status);

  if (selected.length === 1) return selected[0];

  return {
    value: "-1",
    type: "error",
    label: "Desconocido",
  };
};

export default function DiscountsContainer(): ReactElement {
  const [loading, setLoading] = useState(false);
  const [rows, setRows] = useState<Rebate[]>([]);

  const [page, setPage] = useState<number>(1);
  const [perPage, setPerPage] = useState<number>(10);
  const [totalPages, setTotalPages] = useState<number>(1);
  const [totalItems, setTotalItems] = useState<number>(0);

  const [filters, setFilters] = useState<RebateFilters | null>(null);

  const runSearch = async (
    criteria: RebateFilters,
    nextPage?: number,
    nextPerPage?: number
  ) => {
    try {
      setLoading(true);

      const p = nextPage ?? page;
      const s = nextPerPage ?? perPage;

      const finalCriteria: any = {
        ...criteria,
        pageNumber: p,
        pageSize: s,
      };

      setFilters(finalCriteria);

      const data = (await DiscountsClient.get(finalCriteria)) || [];
      setRows(data);

      setTotalItems(data.length);
      setTotalPages(1);
      setPage(p);
      setPerPage(s);
    } catch (err) {
      console.error("Error al obtener descuentos:", err);
      setRows([]);
      setTotalItems(0);
      setTotalPages(1);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="dc-layout">
      <Breadcrumb
        items={[
          { label: "Finanzas", to: "/finanzas" },
          { label: "Descuentos Comerciales" },
        ]}
      />

      <div className="dc-box">
        <div className="dc-header">
          <div>
            <Title title="Listado de Descuentos Comerciales" />
            <p className="dc-description">
              Consulta y seguimiento de rebates/documentos comerciales.
            </p>
          </div>
        </div>

        <div className="dc-filters-section">
          <FiltersBar
            onSearch={(criteria) => {
              setPage(1);
              runSearch({ ...criteria } as any, 1, perPage);
            }}
          />
        </div>

        <Divider />

        <div className="dc-grid-section">
          <DiscountsGridTable
            rows={rows}
            page={page}
            perPage={perPage}
            totalPages={totalPages}
            totalItems={totalItems}
            loading={loading}
            onChangePage={(p) => {
              setPage(p);
              if (filters) runSearch(filters, p, perPage);
            }}
            onChangePerPage={(s) => {
              setPerPage(s);
              setPage(1);
              if (filters) runSearch(filters, 1, s);
            }}
            renderStatus={renderStatus}
          />
        </div>

        {loading && <GenericModal visible variant="loading" message="Cargando…" />}
      </div>
    </div>
  );
}