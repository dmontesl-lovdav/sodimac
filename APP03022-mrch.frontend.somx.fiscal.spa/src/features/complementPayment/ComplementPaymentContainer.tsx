import {
    Breadcrumb,
    GenericButton,
    GenericDateRangePicker,
    GenericInput,
    GenericModal,
    GenericSelect,
    GenericSelectFloating,
} from '@shared/components/ui';
import ComplementPaymentTable from './components/ComplementPaymentTable';
import { useComplementPayment } from './components/useComplementPayment';
import type { ComplementPayment } from './components/ComplementPaymentService';
import { useEffect, useState } from 'react';
import { downloadXML, exportToCSV, downloadPDF } from '@/utils/utils';
import { CreditsClient } from '../credits/api/CreditsClient';
import { decorate } from '@/shared/components/ui/decorator/SimpleDecorator';

const styles = {
    container: {
        padding: '0.5rem 1.5rem 1rem 1.5rem',
    },
    breadcrumbWrapper: {
        marginLeft: '-1.5rem',
        marginTop: '-0.25rem',
    },
    title: {
        fontSize: '1.125rem',
        fontWeight: 600,
        marginBottom: '1.5rem',
        color: '#1f2937',
    },
    filtersSection: {
        marginBottom: '1.5rem',
    },
    filterRow: {
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(12rem, 1fr))',
        gap: '1rem',
        alignItems: 'end',
    },
    dateRow: {
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(18rem, 1fr))',
        gap: '1rem',
        alignItems: 'end',
        marginTop: '1.5rem',
    },
    buttonRow: {
        display: 'flex',
        gap: '0.75rem',
        justifyContent: 'flex-end',
        gridColumn: 'span 2',
    },
    label: {
        display: 'block',
        fontSize: '0.875rem',
        fontWeight: 500,
        color: '#374151',
        marginBottom: '0.25rem',
    },
    downloadRow: {
        display: 'flex',
        alignItems: 'flex-end',
        marginTop: '0.75rem',
    },
    footer: {
        display: 'flex',
        justifyContent: 'flex-end',
        borderTop: '1px solid #e5e7eb',
        paddingTop: '1rem',
    },
    backLink: {
        fontSize: '0.875rem',
        color: '#003865',
        background: 'none',
        border: 'none',
        cursor: 'pointer',
        textDecoration: 'none',
    },
};

export default function ComplementPaymentContainer() {
    const invoice = new CreditsClient();
    const {
        filters,
        setFilters,
        searchResults,
        page,
        setPage,
        perPage,
        setPerPage,
        totalPages,
        totalItems,
        loading,
        selectedIds,
        alert,
        setAlert,
        handleFilterChange,
        handleSearch,
        handleClear,
        handleSelectRow,
        handleDownload,
    } = useComplementPayment();

    const [actionSelected, setActionSelected] = useState("");
    const catalogs_api = process.env.CATALOGS_API_URL || '';
    const [providers, setProviders] = useState<Array<{ label: string; value: string }>>([]);

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

    function escapeCSV(value: unknown): string {
        if (value === null || value === undefined) return "";
        const str = String(value);
        const needsQuotes = /[",\r\n]/.test(str);
        const escaped = str.replace(/"/g, '""');
        return needsQuotes ? `"${escaped}"` : escaped;
    }

    function paymentToRow(p: ComplementPayment): string[] {
        return [
            p.series,
            p.folio,
            p.createdAt,           
            p.issuerRfc,
            p.issuerName,
            p.receiverRfc,
            p.receiverName,
            p.subtotal?.toFixed(2),       
            p.totalAmount?.toFixed(2),
            p.paymentDate,        
            p.statusDescription,
            p.paymentsUuid,
        ].map(escapeCSV);
    }

    
    useEffect(() => {
    if (actionSelected === "csv") {
        const headers = [
        "Serie",
        "Folio",
        "Fecha Emisión",
        "RFC Emisor",
        "Nombre Emisor",
        "RFC Receptor",
        "Nombre Receptor",
        "Subtotal",
        "Total",
        "Fecha Pago",
        "Estatus",
        "UUID",
        ];

        const exportableItems =
        selectedIds.length > 0
            ? searchResults.filter(item => selectedIds.includes(item.paymentsUuid))
            : searchResults;
        const rows = exportableItems.map(paymentToRow);
        exportToCSV(headers, rows, "Complementos de pago");
        setActionSelected("");
    }
    }, [actionSelected, selectedIds, searchResults]);

    const getFilename = (r: ComplementPayment) => {
        const serie = r.series || "";
        const folio = r.folio || "";
        const now = new Date();
        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, "0");
        const day = String(now.getDate()).padStart(2, "0");
        const hours = String(now.getHours()).padStart(2, "0");
        const minutes = String(now.getMinutes()).padStart(2, "0");
        const timestamp = `${year}${month}${day}.${hours}${minutes}`;
        return `${serie}-${folio}-${timestamp}`;
    }


    const handleDownloadXML = async (r: ComplementPayment) => {
        const uuid = "cccccccc-1111-2222-3333-444444444444";
        const {data} = await invoice.getXmlDocument(uuid);
        downloadXML(data, getFilename(r)+".xml");
    };

    const handleDownloadPDF = async (r: ComplementPayment) => {
        const uuid = "cccccccc-1111-2222-3333-444444444444";
        const {data} = await invoice.getPdfDocument(uuid);
        downloadPDF(data, getFilename(r)+".pdf");
    };

    const handleView = (r: ComplementPayment) => {
        setAlert({
            visible: true,
            title: 'Ver facturas',
            message: `Facturas relacionadas con el folio ${r.folio}`,
            severity: 'info',
        });
    };

    const hasFilters = Object.values(filters).some((v) => v.trim() !== '');
    const canSearch = hasFilters && !loading;
    const canClear = hasFilters || selectedIds.length > 0;
    const canDownload = selectedIds.length > 0;

    const breadcrumbItems = [
        { label: 'Inicio', to: '/' },
        { label: 'Fiscal', to: '/fiscal' },
        { label: 'Consulta complemento pago' },
    ];

    return decorate(
        breadcrumbItems,
        "/",
        <>
            <div>
                    <h3 style={styles.title}>
                        Consulta complemento pago
                    </h3>

                    <div style={styles.filtersSection}>
                        <div style={styles.filterRow}>
                            <div>
                                <label style={styles.label}>
                                    Rango de fechas de pago
                                </label>
                                <GenericDateRangePicker
                                    value={[
                                        filters.fechaPagoInicio ? new Date(filters.fechaPagoInicio) : null,
                                        filters.fechaPagoFin ? new Date(filters.fechaPagoFin) : null,
                                    ]}
                                    onChange={([start, end]: [Date | null, Date | null]) =>
                                        setFilters({
                                            ...filters,
                                            fechaPagoInicio: start ? start.toISOString().split('T')[0] : '',
                                            fechaPagoFin: end ? end.toISOString().split('T')[0] : '',
                                        })
                                    }
                                />
                            </div>

                            <div>
                                <label style={styles.label}>
                                    Rango de fechas de emisión
                                </label>
                                <GenericDateRangePicker
                                    value={[
                                        filters.fechaEmisionInicio ? new Date(filters.fechaEmisionInicio) : null,
                                        filters.fechaEmisionFin ? new Date(filters.fechaEmisionFin) : null,
                                    ]}
                                    onChange={([start, end]: [Date | null, Date | null]) =>
                                        setFilters({
                                            ...filters,
                                            fechaEmisionInicio: start ? start.toISOString().split('T')[0] : '',
                                            fechaEmisionFin: end ? end.toISOString().split('T')[0] : '',
                                        })
                                    }
                                />
                            </div>
                            <GenericSelectFloating
                                label="Proveedor"
                                value={filters.rfcEmisor}
                                onChange={handleFilterChange("rfcEmisor")}
                                options={providers}
                                placeholder=""
                            />
                            <GenericSelectFloating
                                label="Estatus"
                                value={filters.status}
                                onValueChange={(e: { target: { value: string } }) =>
                                    setFilters({ ...filters, status: e.target.value })
                                }
                                options={[
                                    { value: '1', label: 'Vigente' },
                                    { value: '0', label: 'Cancelado' },
                                ]}
                            />
                        </div>
                        <div style={styles.filterRow}>
                            
                            
                            <GenericInput label="Serie" value={filters.serie} onChange={handleFilterChange('serie')} />
                            <GenericInput label="Folio" value={filters.folio} onChange={handleFilterChange('folio')} />
                            <GenericInput label="UUID" value={filters.uuid} onChange={handleFilterChange('uuid')} />
                            
                            <div style={styles.buttonRow}>
                                <GenericButton variant="outline" disabled={!canClear} onClick={handleClear}>
                                    Limpiar filtros
                                </GenericButton>
                                <GenericButton onClick={handleSearch}>
                                    Consultar
                                </GenericButton>
                            </div>
                        </div>

                        
                    </div>

                    <ComplementPaymentTable
                        rows={searchResults}
                        page={page}
                        perPage={perPage}
                        totalPages={totalPages}
                        totalItems={totalItems}
                        loading={loading}
                        onChangePage={setPage}
                        onChangePerPage={setPerPage}
                        onView={handleView}
                        onDownloadPDF={handleDownloadPDF}
                        onDownloadXML={handleDownloadXML}
                        enableSelection
                        selectedIds={selectedIds}
                        onSelectRow={handleSelectRow}
                    />

                    <div style={styles.footer}>
                        <GenericSelect
                            label="Acción"
                            placeholder="Selecciona una acción"
                            value={actionSelected}
                            onChange={(e:any)=>setActionSelected(e.target.value)}
                            options={
                                [{ label: "Exportar CSV", value: "csv" }]
                            }
                        />
                    </div>
            </div>
            <GenericModal
                visible={alert.visible}
                variant="alert"
                title={alert.title}
                message={alert.message}
                severity={alert.severity}
                onClose={() => setAlert({ ...alert, visible: false })}
            />

            <GenericModal
                visible={loading}
                variant="loading"
                message="Cargando información, por favor espera..."
            />
        </>
    );
}
