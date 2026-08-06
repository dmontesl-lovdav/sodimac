import {
    ReactElement,
    useEffect,
    useRef,
    useState,
} from "react";
import downloadIconUrl from "@assets/download.svg";

import {
    Breadcrumb,
    GenericModal,
    GenericButton,
} from "@shared/components/ui";
import { withFinanceBreadcrumb } from "@shared/components/ui/navigation/financeBreadcrumb";
import {
    Title,
    Divider,
} from "@/shared/components/ui/misc";

import FiltersBar from "./components/FiltersBar";
import ResultsTable from "./components/ResultsTable";
import {
    APP_EVENT,
    PermissionGate,
} from "@shared/security";

import { paymentsService } from "./api/paymentsService";
import { PaymentRecord } from "./interfaces";
import { PaymentFiltersValues } from "./components/FiltersBar";
import { authenticator } from "@/configuration/ConfigurationBuilder";
import { getErrorMessage } from "@/utils/errorMessage";
import { fetchProviders } from "@/utils/utils";

import "./styles/PaymentsContainer.css";
import {
    FINANCE_LIST_KEYS,
    useFinanceListScreenSession,
    useFinanceListRefetchOnReturn,
} from "@/shared/hooks";

type ModalSeverity =
    | "success"
    | "error"
    | "warning"
    | "info";

export default function PaymentsContainer(): ReactElement {
    const [payments, setPayments] = useState<
        PaymentRecord[]
    >([]);

    const [
        allFilteredPayments,
        setAllFilteredPayments,
    ] = useState<PaymentRecord[]>([]);

    const [providers, setProviders] =
        useState<any[]>([]);

    const [loading, setLoading] =
        useState(false);

    const [error, setError] =
        useState<string>("");

    const [messages, setMessages] =
        useState<Record<string, string>>({});

    const [lastFilters, setLastFilters] =
        useState<PaymentFiltersValues | null>(
            null
        );

    const [page, setPage] = useState(1);
    const [perPage, setPerPage] =
        useState(10);
    const [totalPages, setTotalPages] =
        useState(1);
    const [totalItems, setTotalItems] =
        useState(0);

    const [isAdmin, setIsAdmin] =
        useState(false);

    const warnIfEmptyRef = useRef(false);

    const returningFromDetail =
        useFinanceListScreenSession(
            FINANCE_LIST_KEYS.payments
        );

    const [modalTitle, setModalTitle] =
        useState<string>("");

    const [modalSeverity, setModalSeverity] =
        useState<ModalSeverity>("error");

    useEffect(() => {
        loadMessages();
        checkAdmin();
        loadProviders();
    }, []);

    const checkAdmin = async () => {
        try {
            const admin =
                await authenticator.isAdmin();

            setIsAdmin(admin);
        } catch {
            setIsAdmin(false);
        }
    };

    const loadProviders = async () => {
        const list = await fetchProviders();

        setProviders(list ?? []);
    };

    const loadMessages = async () => {
        try {
            const loadedMessages =
                await paymentsService.getMessages();

            setMessages(loadedMessages);
        } catch (err) {
            warnIfEmptyRef.current = false;
            setModalSeverity("warning");
            setModalTitle("Atención");
            setError(
                getErrorMessage(
                    err,
                    "No se pudieron cargar los mensajes del catálogo."
                )
            );
        }
    };

    const handleSearch = async (
        filters: PaymentFiltersValues,
        nextPage?: number,
        nextPerPage?: number
    ) => {
        const requestedPage =
            nextPage ?? page;

        const requestedPageSize =
            nextPerPage ?? perPage;

        setLoading(true);
        setError("");
        setLastFilters(filters);
        setAllFilteredPayments([]);

        try {
            const supplierTypeId = Number(
                filters.providerType
            );

            const hasSupplierType =
                filters.providerType != null &&
                filters.providerType !== "" &&
                Number.isFinite(
                    supplierTypeId
                ) &&
                supplierTypeId > 0;

            /*
             * Estos filtros no están resueltos directamente por el
             * endpoint actual, por lo que se aplican localmente.
             */
            const useLocalFiltering =
                Boolean(
                    filters.paymentReference
                ) ||
                Boolean(filters.paymentYear) ||
                hasSupplierType;

            /*
             * Para filtrado local obtenemos todas las páginas
             * respetando el máximo de 200 permitido por el backend.
             *
             * Para una búsqueda normal conservamos exactamente la
             * paginación solicitada por el usuario.
             */
            const result = useLocalFiltering
                ? await paymentsService.searchAllPayments(
                    {
                        startDate:
                            filters.startDate,
                        endDate:
                            filters.endDate,
                        providerId:
                            filters.providerId,
                        page: 1,
                        size: 200,
                    }
                )
                : await paymentsService.searchPayments(
                    {
                        startDate:
                            filters.startDate,
                        endDate:
                            filters.endDate,
                        providerId:
                            filters.providerId,
                        page: requestedPage,
                        size: requestedPageSize,
                    }
                );

            let filteredItems = result.items;

            if (filters.paymentReference) {
                const searchTerm =
                    filters.paymentReference.toLowerCase();

                filteredItems =
                    filteredItems.filter(
                        (item) =>
                            item.documentReference
                                .toLowerCase()
                                .includes(
                                    searchTerm
                                )
                    );
            }

            if (filters.paymentYear) {
                filteredItems =
                    filteredItems.filter(
                        (item) =>
                            item.paymentYear ===
                            filters.paymentYear
                    );
            }

            if (hasSupplierType) {
                const vendorNumbers = new Set(
                    providers
                        .filter(
                            (item) =>
                                item.supplierType
                                    ?.id ==
                                supplierTypeId
                        )
                        .map((item) =>
                            String(
                                item.supplierNumber
                            )
                        )
                );

                filteredItems =
                    filteredItems.filter(
                        (item) =>
                            vendorNumbers.has(
                                String(
                                    item.providerNumber
                                )
                            )
                    );
            }

            if (
                filteredItems.length === 0 &&
                warnIfEmptyRef.current
            ) {
                setModalSeverity("info");
                setModalTitle("Sin resultados");
                setError(
                    messages["INF6000"] ??
                    "No existe información con los criterios establecidos."
                );

                setPayments([]);
                setAllFilteredPayments([]);
                setPage(1);
                setPerPage(requestedPageSize);
                setTotalPages(1);
                setTotalItems(0);

                return;
            }

            if (useLocalFiltering) {
                setAllFilteredPayments(
                    filteredItems
                );

                setPayments(
                    filteredItems.slice(
                        0,
                        requestedPageSize
                    )
                );

                setPage(1);
                setPerPage(
                    requestedPageSize
                );
                setTotalPages(
                    Math.max(
                        1,
                        Math.ceil(
                            filteredItems.length /
                            requestedPageSize
                        )
                    )
                );
                setTotalItems(
                    filteredItems.length
                );

                return;
            }

            setAllFilteredPayments([]);
            setPayments(filteredItems);
            setPage(result.currentPage);
            setPerPage(requestedPageSize);
            setTotalPages(result.totalPages);
            setTotalItems(result.totalItems);
        } catch (err: any) {
            setModalSeverity("error");
            setModalTitle("Error");
            setError(
                getErrorMessage(
                    err,
                    "Error al buscar pagos."
                )
            );

            setPayments([]);
            setAllFilteredPayments([]);
            setPage(1);
            setTotalPages(1);
            setTotalItems(0);
        } finally {
            warnIfEmptyRef.current = false;
            setLoading(false);
        }
    };

    useFinanceListRefetchOnReturn(
        FINANCE_LIST_KEYS.payments,
        returningFromDetail,
        (filters) => {
            handleSearch(
                filters as PaymentFiltersValues,
                1,
                perPage
            );
        }
    );

    const handlePageChange = async (
        newPage: number
    ) => {
        if (!lastFilters) {
            return;
        }

        setPage(newPage);

        if (
            allFilteredPayments.length > 0
        ) {
            const startIndex =
                (newPage - 1) * perPage;

            const endIndex =
                startIndex + perPage;

            setPayments(
                allFilteredPayments.slice(
                    startIndex,
                    endIndex
                )
            );

            return;
        }

        await handleSearch(
            lastFilters,
            newPage,
            perPage
        );
    };

    const handlePerPageChange = async (
        newPerPage: number
    ) => {
        setPerPage(newPerPage);
        setPage(1);

        if (!lastFilters) {
            return;
        }

        if (
            allFilteredPayments.length > 0
        ) {
            setPayments(
                allFilteredPayments.slice(
                    0,
                    newPerPage
                )
            );

            setTotalPages(
                Math.ceil(
                    allFilteredPayments.length /
                    newPerPage
                )
            );

            setTotalItems(
                allFilteredPayments.length
            );

            return;
        }

        await handleSearch(
            lastFilters,
            1,
            newPerPage
        );
    };

    const handleExportCsv = () => {
        if (payments.length === 0) {
            return;
        }

        const blob =
            paymentsService.exportPaymentsCsv(
                payments
            );

        const url =
            window.URL.createObjectURL(blob);

        const anchor =
            document.createElement("a");

        const now = new Date();

        const pad2 = (value: number) =>
            value
                .toString()
                .padStart(2, "0");

        const ymd = `${now.getFullYear()}${pad2(
            now.getMonth() + 1
        )}${pad2(now.getDate())}`;

        const hms = `${pad2(
            now.getHours()
        )}.${pad2(
            now.getMinutes()
        )}.${pad2(now.getSeconds())}`;

        const fileName =
            `pagos_${ymd}_${hms}.csv`;

        anchor.href = url;
        anchor.download = fileName;

        document.body.appendChild(anchor);
        anchor.click();
        document.body.removeChild(anchor);

        window.URL.revokeObjectURL(url);
    };

    const handleClearSearch = () => {
        warnIfEmptyRef.current = false;

        setPayments([]);
        setAllFilteredPayments([]);
        setError("");
        setLastFilters(null);
        setPage(1);
        setPerPage(10);
        setTotalPages(1);
        setTotalItems(0);
    };

    return (
        <div className="pay-layout">
            <Breadcrumb
                items={withFinanceBreadcrumb([
                    {
                        label: "Pagos",
                    },
                ])}
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
                        <PermissionGate
                            appEvent={
                                APP_EVENT.PAYMENTS
                                    .DOWNLOAD_CSV
                            }
                        >
                            <GenericButton
                                onClick={
                                    handleExportCsv
                                }
                                disabled={
                                    loading ||
                                    payments.length ===
                                    0
                                }
                            >
                                <span
                                    style={{
                                        display:
                                            "flex",
                                        alignItems:
                                            "center",
                                        gap: 6,
                                    }}
                                >
                                    <span
                                        className="pay-download-ico"
                                        aria-hidden="true"
                                        style={{
                                            WebkitMaskImage:
                                                `url(${downloadIconUrl})`,
                                            maskImage:
                                                `url(${downloadIconUrl})`,
                                        }}
                                    />

                                    Exportar CSV
                                </span>
                            </GenericButton>
                        </PermissionGate>
                    </div>
                </div>

                <div className="pay-filters-section">
                    <FiltersBar
                        onSearch={(
                            criteria
                        ) => {
                            warnIfEmptyRef.current =
                                true;

                            setPage(1);

                            handleSearch(
                                {
                                    ...criteria,
                                },
                                1,
                                perPage
                            );
                        }}
                        onClear={
                            handleClearSearch
                        }
                        isAdmin={isAdmin}
                        messages={messages}
                    />
                </div>

                <Divider />

                <div className="pay-grid-section">
                    <ResultsTable
                        rows={payments}
                        providers={providers}
                        loading={loading}
                        isAdmin={isAdmin}
                        page={page}
                        perPage={perPage}
                        totalPages={
                            totalPages
                        }
                        totalItems={totalItems}
                        onPageChange={
                            handlePageChange
                        }
                        onPerPageChange={
                            handlePerPageChange
                        }
                        onExport={
                            handleExportCsv
                        }
                        backPath="/finanzas"
                        lastFilters={
                            lastFilters
                        }
                    />
                </div>

                <GenericModal
                    visible={Boolean(error)}
                    variant="alert"
                    title={modalTitle}
                    severity={modalSeverity}
                    message={error}
                    buttonText="Aceptar"
                    onClose={() =>
                        setError("")
                    }
                />

                {loading && (
                    <GenericModal
                        visible
                        variant="loading"
                        message="Cargando…"
                    />
                )}
            </div>
        </div>
    );
}