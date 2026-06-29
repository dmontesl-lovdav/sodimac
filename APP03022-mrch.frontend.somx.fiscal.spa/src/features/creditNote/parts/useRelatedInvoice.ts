import { useEffect, useMemo, useState } from "react";
import { createInvoicesClient } from "../../invoice/api/InvoiceClient";
import { EMPTY_INVOICE, type Invoice } from "../../invoice/interfaces";
import { formatLocalDateStr, startOfLocalDay } from "@/utils/utils";

function invoiceSearchRange(): { start: string; end: string } {
  const end = startOfLocalDay(new Date());
  const start = new Date(end);
  start.setMonth(start.getMonth() - 6);
  return {
    start: formatLocalDateStr(start),
    end: formatLocalDateStr(end),
  };
}

export function useRelatedInvoice(relatedInvoiceUuid: string) {
  const invoiceClient = useMemo(
    () =>
      createInvoicesClient<{
        content: Invoice[];
        totalElements: number;
      }>(),
    []
  );
  const [invoice, setInvoice] = useState<Invoice | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const uuid = relatedInvoiceUuid.trim();
    if (!uuid) {
      setInvoice(null);
      return;
    }

    let cancelled = false;
    (async () => {
      setLoading(true);
      try {
        const range = invoiceSearchRange();
        const result = await invoiceClient.getInvoices({
          ...EMPTY_INVOICE,
          uuid,
          fechaInicioRecepcion: range.start,
          fechaFinalRecepcion: range.end,
          page: 0,
          size: 1,
        });
        if (!cancelled) setInvoice(result?.content?.[0] ?? null);
      } catch {
        if (!cancelled) setInvoice(null);
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [relatedInvoiceUuid, invoiceClient]);

  return { invoice, loading };
}
