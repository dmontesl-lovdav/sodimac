// ✅ FILE: src/features/account-statement/containers/AccountStatementContainer.tsx
import { useEffect, useRef, useState } from "react";
import {
    FINANCE_LIST_KEYS,
    useFinanceListScreenSession,
    useFinanceListRefetchOnReturn,
} from "@/shared/hooks";
import { Breadcrumb, GenericModal } from "@shared/components/ui";

import { Title, Divider } from "@/shared/components/ui/misc";

import { AccountStatementService } from "./api/accountStatementService";
import { RequestsClientService } from "./api/requestsClient";
import { buildAccountStatementReviewClarificationPayload } from "./api/buildAccountStatementClarificationPayload";
import { openAccountStatementPdfPreview } from "./pdf/openAccountStatementPdf";
import type {
    AccountStatementRecord,
    AccountStatementFilters,
} from "./interfaces";

import FiltersBar from "./components/FiltersBar";
import AccountStatementGrid from "./components/AccountStatementGrid";

import { ConfirmDialog } from "@shared/components/ui/modal";

import "./styles/AccountStatementContainer.css";
import { withFinanceBreadcrumb } from "@shared/components/ui/navigation/financeBreadcrumb";
import { getErrorMessage } from "@/utils/errorMessage";
import {
    ACCOUNT_STATEMENT_STATUS,
    withAccountStatementStatus,
} from "./accountStatementActions";
import { fetchCatalogAsSelectableOptions, fetchCatalogDetails, fetchProvidersAsCatalog } from "@/utils/utils";

const breadcrumb = withFinanceBreadcrumb([{ label: "Estado de Cuenta" }]);

export default function AccountStatementContainer() {
    const [loading, setLoading] = useState(false);
    const [rows, setRows] = useState<AccountStatementRecord[]>([]);

    const [receptionStatuses, setReceptionStatuses] = useState<any[]>([]);

    const [page, setPage] = useState<number>(1);
    const [perPage, setPerPage] = useState<number>(25);
    const [totalPages, setTotalPages] = useState<number>(1);
    const [totalItems, setTotalItems] = useState<number>(0);

    const perPageRef = useRef(perPage);

    const [confirmReview, setConfirmReview] =
        useState<AccountStatementRecord | null>(null);
    const [confirmReject, setConfirmReject] =
        useState<AccountStatementRecord | null>(null);

    const [isAdmin] = useState(true);
    const [lastCriteria, setLastCriteria] =
        useState<AccountStatementFilters | null>(null);

    const [providers, setProviders] = useState<{ label: string; value: string }[]>([]);

    const returningFromDetail = useFinanceListScreenSession(
        FINANCE_LIST_KEYS.accountStatement
    );

    const [infoModal, setInfoModal] = useState({
        type: "info" as "info" | "success" | "warning" | "error",
        visible: false,
        message: "",
    });

    useEffect(() => {
        perPageRef.current = perPage;
    }, [perPage]);

    useEffect(() => {
        const fetchProviders = async () => {
            const list = await fetchProvidersAsCatalog("supplierNumber");
            if (list) setProviders(list);
        };
        const fetchReceptionStatuses = async () => {
            const tipoRecepcionRes = await fetchCatalogDetails("CatEstatusRecepcion");
            if (tipoRecepcionRes) {
                const mappedStatus = fetchCatalogAsSelectableOptions(tipoRecepcionRes, "Todos los estatus");
                setReceptionStatuses(mappedStatus.filter((item: any) => item.value !== "8"));
            }
        };
        
        fetchProviders();
        fetchReceptionStatuses();
    }, []);
    const fetchData = async (
        criteria: AccountStatementFilters,
        p: number,
        size: number
    ) => {
        setLoading(true);

        try {
            setLastCriteria(criteria);

            const result = await AccountStatementService.search(criteria, p, size);

            const mapped = result.items.map((item) => ({
                ...item
            }));

            setRows(mapped);
            setTotalItems(result.totalItems);
            setTotalPages(result.totalPages);
            setPage(result.currentPage);

            perPageRef.current = size;
            setPerPage(size);

            if (mapped.length === 0) {
                setInfoModal({
                    type: "info",
                    visible: true,
                    message: "No se encontraron estados de cuenta con los criterios establecidos.",
                });
            }
        } catch (err) {
            setRows([]);
            setTotalItems(0);
            setTotalPages(1);
            setPage(1);

            setInfoModal({
                type: "error",
                visible: true,
                message: getErrorMessage(
                    err,
                    "No fue posible obtener los estados de cuenta. Intenta nuevamente."
                ),
            });
        } finally {
            setLoading(false);
        }
    };

    const onFilter = async (criteria: AccountStatementFilters) => {
        const size = perPageRef.current;

        setPage(1);
        await fetchData(criteria, 1, size);
    };

    useFinanceListRefetchOnReturn(
        FINANCE_LIST_KEYS.accountStatement,
        returningFromDetail,
        (criteria) => onFilter(criteria as AccountStatementFilters)
    );

    const handleChangePage = async (nextPage: number) => {
        const p = Math.max(1, Number(nextPage || 1));
        const size = perPageRef.current;

        setPage(p);

        if (lastCriteria) {
            await fetchData(lastCriteria, p, size);
        }
    };

    const handleChangePerPage = async (n: number) => {
        const size = Math.max(1, Number(n || 25));

        perPageRef.current = size;
        setPerPage(size);
        setPage(1);

        if (lastCriteria) {
            await fetchData(lastCriteria, 1, size);
        }
    };

    const handleClearList = () => {
        setRows([]);
        setTotalItems(0);
        setTotalPages(1);
        setPage(1);
        setLastCriteria(null);
    };

    const handleView = async (row: AccountStatementRecord) => {
        setLoading(true);

        try {
            const reportData = await AccountStatementService.getReportData(
                row.accountStatementUuid
            );

            openAccountStatementPdfPreview(reportData, receptionStatuses);
        } catch (err) {
            setInfoModal({
                type: "error",
                visible: true,
                message: getErrorMessage(
                    err,
                    "No se pudo generar el estado de cuenta."
                ),
            });
        } finally {
            setLoading(false);
        }
    };

    const handleConfirmReview = async () => {
        if (!confirmReview) return;

        const targetId = confirmReview.accountStatementUuid;

        setLoading(true);

        try {
            await AccountStatementService.confirmReview(targetId);

            setConfirmReview(null);

            setRows((prev) =>
                prev.map((row) =>
                    row.accountStatementUuid === targetId
                        ? withAccountStatementStatus(
                            row,
                            ACCOUNT_STATEMENT_STATUS.REVIEWED,
                            "Revisado",
                            new Date().toISOString()
                        )
                        : row
                )
            );

            setInfoModal({
                type: "success",
                visible: true,
                message: "Revisión confirmada exitosamente",
            });

            if (lastCriteria) {
                await fetchData(lastCriteria, page, perPageRef.current);
            }
        } catch (err) {
            setInfoModal({
                type: "error",
                visible: true,
                message: getErrorMessage(err, "No se pudo confirmar la revisión."),
            });
        } finally {
            setLoading(false);
        }
    };

    const handleConfirmReject = async () => {
        if (!confirmReject) return;

        const targetId = confirmReject.accountStatementUuid;
        const rowForClarification = confirmReject;

        setLoading(true);

        try {
            await AccountStatementService.requestReview(targetId);

            let clarificationWarning: string | null = null;

            try {
                await RequestsClientService.createClarification(
                    buildAccountStatementReviewClarificationPayload(rowForClarification)
                );
            } catch (clarErr) {
                clarificationWarning = getErrorMessage(
                    clarErr,
                    "No se pudo registrar la aclaración en el sistema de solicitudes."
                );
            }

            setConfirmReject(null);

            setRows((prev) =>
                prev.map((row) =>
                    row.accountStatementUuid === targetId
                        ? withAccountStatementStatus(
                            row,
                            ACCOUNT_STATEMENT_STATUS.REJECTED,
                            "Rechazado",
                            new Date().toISOString()
                        )
                        : row
                )
            );

            setInfoModal({
                type: clarificationWarning ? "warning" : "success",
                visible: true,
                message: clarificationWarning
                    ? `Revisión solicitada. ${clarificationWarning}`
                    : "Revisión solicitada exitosamente",
            });

            if (lastCriteria) {
                await fetchData(lastCriteria, page, perPageRef.current);
            }
        } catch (err) {
            setInfoModal({
                type: "error",
                visible: true,
                message: getErrorMessage(err, "No se pudo solicitar la revisión."),
            });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="al-layout">
            <Breadcrumb items={breadcrumb} />

            <div className="al-box">
                <div className="al-header">
                    <div>
                        <Title title="Estado de Cuenta" />
                        <p className="al-description">
                            Descarga los estados de cuenta que has tenido a lo largo del tiempo
                        </p>
                    </div>
                </div>

                <div className="al-filters-section">
                    <FiltersBar
                        onSearch={onFilter}
                        onClear={handleClearList}
                        isAdmin={isAdmin}
                        providers={providers}
                    />
                </div>

                <Divider />

                <div className="al-grid-section">
                    <AccountStatementGrid
                        rows={rows}
                        loading={loading}
                        page={page}
                        perPage={perPage}
                        totalPages={totalPages}
                        totalItems={totalItems}
                        onPageChange={handleChangePage}
                        onPerPageChange={handleChangePerPage}
                        onView={handleView}
                        onReview={(r) => setConfirmReview(r)}
                        onReject={(r) => setConfirmReject(r)}
                    />
                </div>

                {loading && <GenericModal visible variant="loading" message="Cargando…" />}

                <ConfirmDialog
                    visible={confirmReview != null}
                    title="Confirmar revisión"
                    message="¿Desea confirmar la revisión del estado de cuenta?"
                    onConfirm={handleConfirmReview}
                    onCancel={() => setConfirmReview(null)}
                />

                <ConfirmDialog
                    visible={confirmReject != null}
                    title="Solicitar revisión"
                    message="¿Desea solicitar la revisión del estado de cuenta con el equipo financiero?"
                    onConfirm={handleConfirmReject}
                    onCancel={() => setConfirmReject(null)}
                />

                <GenericModal
                    visible={infoModal.visible}
                    variant="alert"
                    severity={infoModal.type}
                    title={infoModal.type === "error" ? "Error" : "Atención"}
                    message={infoModal.message}
                    buttonText="Aceptar"
                    onClose={() => setInfoModal({ visible: false, message: "", type: "info" })}
                />
            </div>
        </div>
    );
}