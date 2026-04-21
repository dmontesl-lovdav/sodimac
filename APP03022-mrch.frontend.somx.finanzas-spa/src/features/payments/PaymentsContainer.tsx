// FILE: src/features/payments/PaymentsContainer.tsx
import { ReactElement, useEffect, useRef, useState } from "react";
import { useLocation } from "react-router-dom";
import downloadIconUrl from "@assets/download.svg";

import { Breadcrumb, GenericModal, GenericButton } from "@shared/components/ui";
import { Title, Divider } from "@/shared/components/ui/misc";

import FiltersBar from "./components/FiltersBar";
import ResultsTable from "./components/ResultsTable";

import { paymentsService } from "./api/paymentsService";
import { PaymentRecord, PaymentSearchParams } from "./interfaces";
import { PaymentFiltersValues } from "./components/FiltersBar";
import { authenticator } from "@/configuration/ConfigurationBuilder";

import "./styles/PaymentsContainer.css";

type ModalSeverity = "success" | "error" | "warning" | "info";

export default function PaymentsContainer(): ReactElement {
    const location = useLocation();
    const restoredFilters = (location.state as any)?.filters as
        | PaymentFiltersValues
        | undefined;
    const hasRestored = useRef(false);

    const [payments, setPayments] = useState<PaymentRecord[]>([]);
    const [allFilteredPayments, setAllFilteredPayments] = useState<PaymentRecord[]>(
        []
    );

    const [loading, setLoading] = useState(false);
    const [searchApplied, setSearchApplied] = useState(false);
    const [error, setError] = useState<string>("");
    const [messages, setMessages] = useState<Record<string, string>>({});
    const [lastFilters, setLastFilters] = useState<PaymentFiltersValues | null>(
        null
    );

    const [page, setPage] = useState(1);
    const [perPage, setPerPage] = useState(10);
    const [totalPages, setTotalPages] = useState(1);
    const [totalItems, setTotalItems] = useState(0);

    const [isAdmin, setIsAdmin] = useState(false);

    // Modal state for errors/info
    const [modalTitle, setModalTitle] = useState<string>("");
    const [modalSeverity, setModalSeverity] = useState<ModalSeverity>("error");

    useEffect(() => {
        loadMessages();
        checkAdmin();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    useEffect(() => {
        if (restoredFilters && !hasRestored.current) {
            hasRestored.current = true;
            handleSearch(restoredFilters, 1, perPage);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [restoredFilters]);

    const checkAdmin = async () => {
        try {
            const admin = await authenticator.isAdmin();
            setIsAdmin(admin);
        } catch {
            setIsAdmin(false);
        }
    };

    const loadMessages = async () => {
        try {
            const msgs = await paymentsService.getMessages();
            setMessages(msgs);
        } catch (err) {
            console.error("Error loading messages:", err);
        }
    };

    const handleSearch = async (
        filters: PaymentFiltersValues,
        nextPage?: number,
        nextPerPage?: number
    ) => {
        const p = nextPage ?? page;
        const s = nextPerPage ?? perPage;

        setLoading(true);
        setError("");
        setSearchApplied(true);
        setLastFilters(filters);
        setAllFilteredPayments([]);

        try {
            const useLocalFiltering =
                !!filters.paymentReference || !!filters.paymentYear;

            const params: PaymentSearchParams = {
                startDate: filters.startDate,
                endDate: filters.endDate,
                providerId: filters.providerId,
                page: p,
                size: useLocalFiltering ? 10000 : s,
            };

            const result = await paymentsService.searchPayments(params);

            let filteredItems = result.items;

            if (filters.paymentReference) {
                const searchTerm = filters.paymentReference.toLowerCase();
                filteredItems = filteredItems.filter((item) =>
                    item.documentReference.toLowerCase().includes(searchTerm)
                );
            }

            if (filters.paymentYear) {
                filteredItems = filteredItems.filter(
                    (item) => item.paymentYear === filters.paymentYear
                );
            }

            if (filteredItems.length === 0) {
                setModalSeverity("info");
                setModalTitle("Sin resultados");
                setError(
                    messages["INF6000"] ||
                    "No existe información con los criterios establecidos."
                );
                setPayments([]);
                setAllFilteredPayments([]);
                setPage(1);
                setPerPage(s);
                setTotalPages(1);
                setTotalItems(0);
                return;
            }

            if (useLocalFiltering) {
                setAllFilteredPayments(filteredItems);
                setPayments(filteredItems.slice(0, s));
                setPage(1);
                setPerPage(s);
                setTotalPages(Math.ceil(filteredItems.length / s));
                setTotalItems(filteredItems.length);
                return;
            }

            setAllFilteredPayments([]);
            setPayments(filteredItems);
            setPage(result.currentPage);
            setPerPage(s);
            setTotalPages(result.totalPages);
            setTotalItems(result.totalItems);
        } catch (err: any) {
            console.error("Error searching payments:", err);
            const detail = err?.response
                ? `Status ${err.response.status}: ${JSON.stringify(
                    err.response.data ?? err.message
                )}`
                : err?.message || "Error desconocido";

            setModalSeverity("error");
            setModalTitle("Error");
            setError(`Error al buscar pagos: ${detail}`);

            setPayments([]);
            setAllFilteredPayments([]);
            setPage(1);
            setTotalPages(1);
            setTotalItems(0);
        } finally {
            setLoading(false);
        }
    };

    const handlePageChange = async (newPage: number) => {
        if (!lastFilters) return;

        setPage(newPage);

        if (allFilteredPayments.length > 0) {
            const startIndex = (newPage - 1) * perPage;
            const endIndex = startIndex + perPage;
            setPayments(allFilteredPayments.slice(startIndex, endIndex));
            return;
        }

        await handleSearch(lastFilters, newPage, perPage);
    };

    const handlePerPageChange = async (newPerPage: number) => {
        setPerPage(newPerPage);
        setPage(1);
        if (!lastFilters) return;

        if (allFilteredPayments.length > 0) {
            setPayments(allFilteredPayments.slice(0, newPerPage));
            setTotalPages(Math.ceil(allFilteredPayments.length / newPerPage));
            setTotalItems(allFilteredPayments.length);
            return;
        }

        await handleSearch(lastFilters, 1, newPerPage);
    };

    const handleExportCsv = () => {
        if (payments.length === 0) return;

        const blob = paymentsService.exportPaymentsCsv(payments);
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");

        const now = new Date();
        const pad2 = (value: number) => value.toString().padStart(2, "0");

        const fileName = `pagos_${now.getFullYear()}_${pad2(
            now.getMonth() + 1
        )}_${pad2(now.getDate())}_${pad2(now.getHours())}:${pad2(
            now.getMinutes()
        )}:${pad2(now.getSeconds())}.csv`;

        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
    };

    const handleClearSearch = () => {
        setPayments([]);
        setAllFilteredPayments([]);
        setError("");
        setSearchApplied(false);
        setLastFilters(null);
        setPage(1);
        setPerPage(10);
        setTotalPages(1);
        setTotalItems(0);
    };

    return (
        <div className="pay-layout">
            <Breadcrumb
                items={[
                    { label: "Finanzas", to: "/finanzas" },
                    { label: "Pagos" },
                ]}
            />

            <div className="pay-box">
                <div className="pay-header">
                    <div>
                        <Title title="Pagos" />
                        <p className="pay-description">
                            Busca y consulta los pagos realizados.
                        </p>
                    </div>

                    <div className="pay-header-actions">
                        <GenericButton
                            onClick={handleExportCsv}
                            disabled={loading || payments.length === 0}
                        >
                            <span
                                style={{
                                    display: "flex",
                                    alignItems: "center",
                                    gap: 6,
                                }}
                            >
                                <span
                                    className="pay-download-ico"
                                    aria-hidden="true"
                                    style={{
                                        WebkitMaskImage: `url(${downloadIconUrl})`,
                                        maskImage: `url(${downloadIconUrl})`,
                                    }}
                                />
                                Descargar CSV
                            </span>
                        </GenericButton>
                    </div>
                </div>

                <div className="pay-filters-section">
                    <FiltersBar
                        onSearch={(criteria) => {
                            setPage(1);
                            handleSearch({ ...criteria }, 1, perPage);
                        }}
                        onClear={handleClearSearch}
                        isAdmin={isAdmin}
                        messages={messages}
                        initialValues={restoredFilters || null}
                    />
                </div>

                <Divider />

                <div className="pay-grid-section">
                    <ResultsTable
                        rows={payments}
                        loading={loading}
                        isAdmin={isAdmin}
                        page={page}
                        perPage={perPage}
                        totalPages={totalPages}
                        totalItems={totalItems}
                        onPageChange={handlePageChange}
                        onPerPageChange={handlePerPageChange}
                        onExport={handleExportCsv}
                        backPath="/finanzas"
                        lastFilters={lastFilters}
                    />
                </div>

                <GenericModal
                    visible={!!error}
                    variant="alert"
                    title={modalTitle}
                    severity={modalSeverity}
                    message={error}
                    buttonText="Aceptar"
                    onClose={() => setError("")}
                />

                {loading && (
                    <GenericModal visible variant="loading" message="Cargando…" />
                )}
            </div>
        </div>
    );
}