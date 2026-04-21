// ✅ FILE: src/features/account-statement/containers/AccountStatementContainer.tsx
import { useState } from "react";
import { Breadcrumb, GenericModal } from "@shared/components/ui";
import type { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";

import { Title, Divider } from "@/shared/components/ui/misc";

import { AccountStatementService } from "./api/accountStatementService";
import type {
    AccountStatementRecord,
    AccountStatementFilters,
} from "./interfaces";

import FiltersBar from "./components/FiltersBar";
import AccountStatementGrid from "./components/AccountStatementGrid";

import { ConfirmDialog } from "@shared/components/ui/modal";

import "./styles/AccountStatementContainer.css";

const breadcrumb: BreadcrumbItem[] = [
    { label: "Finanzas", to: "/finanzas" },
    { label: "Estado de Cuenta" },
];

export default function AccountStatementContainer() {
    const [loading, setLoading] = useState(false);
    const [rows, setRows] = useState<AccountStatementRecord[]>([]);

    const [page, setPage] = useState<number>(1);
    const [perPage, setPerPage] = useState<number>(25);
    const [totalPages, setTotalPages] = useState<number>(1);
    const [totalItems, setTotalItems] = useState<number>(0);

    const [confirmReview, setConfirmReview] =
        useState<AccountStatementRecord | null>(null);
    const [confirmReject, setConfirmReject] =
        useState<AccountStatementRecord | null>(null);

    const [isAdmin] = useState(true);
    const [lastCriteria, setLastCriteria] =
        useState<AccountStatementFilters | null>(null);

    const [infoModal, setInfoModal] = useState({
        type: "info" as "info" | "success" | "warning" | "error",
        visible: false,
        message: "",
    });

    const fetchData = async (
        criteria: AccountStatementFilters,
        p: number,
        size: number
    ) => {
        setLoading(true);
        try {
            setLastCriteria(criteria);

            // NOTE: si tu API no pagina, esto igual funciona (page=1 totalPages=1)
            const result: any = await AccountStatementService.search(criteria, p, size);

            const items = result?.items ?? result?.data ?? [];
            const total = Number(result?.total ?? items.length ?? 0);
            const limit = Number(result?.limit ?? size ?? 25);

            setRows(items);
            setTotalItems(total);
            setTotalPages(Math.max(1, Math.ceil(total / Math.max(1, limit))));
            setPage(p);
            setPerPage(size);
        } catch (err) {
            console.error("Error al obtener estados de cuenta:", err);
            setRows([]);
            setTotalItems(0);
            setTotalPages(1);
            setPage(1);
        } finally {
            setLoading(false);
        }
    };

    const onFilter = async (criteria: AccountStatementFilters) => {
        setPage(1);
        await fetchData(criteria, 1, perPage);
    };

    const handleChangePage = async (nextPage: number) => {
        const p = Math.max(1, Number(nextPage || 1));
        setPage(p);
        if (lastCriteria) await fetchData(lastCriteria, p, perPage);
    };

    const handleChangePerPage = async (n: number) => {
        const size = Math.max(1, Number(n || 25));
        setPerPage(size);
        setPage(1);
        if (lastCriteria) await fetchData(lastCriteria, 1, size);
    };

    const handleView = async (row: AccountStatementRecord) => {
        try {
            const blob = await AccountStatementService.getPdf(
                row.accountStatementUuid
            );
            const blobUrl = URL.createObjectURL(blob);
            window.open(blobUrl, "_blank", "noopener,noreferrer");
            setTimeout(() => URL.revokeObjectURL(blobUrl), 60000);
        } catch (err) {
            setInfoModal({
                type: "error",
                visible: true,
                message: "No se pudo obtener el PDF",
            });
        }
    };

    const handleConfirmReview = async () => {
        if (!confirmReview) return;
        setLoading(true);
        try {
            await AccountStatementService.confirmReview(
                confirmReview.accountStatementUuid
            );
            setConfirmReview(null);
            setInfoModal({
                type: "success",
                visible: true,
                message: "Revisión confirmada exitosamente",
            });
            if (lastCriteria) await fetchData(lastCriteria, page, perPage);
        } catch (err) {
            setInfoModal({
                type: "error",
                visible: true,
                message: "No se pudo confirmar la revisión",
            });
        } finally {
            setLoading(false);
        }
    };

    const handleConfirmReject = async () => {
        if (!confirmReject) return;
        setLoading(true);
        try {
            await AccountStatementService.requestReview(
                confirmReject.accountStatementUuid
            );
            setConfirmReject(null);
            setInfoModal({
                type: "success",
                visible: true,
                message: "Revisión solicitada exitosamente",
            });
            if (lastCriteria) await fetchData(lastCriteria, page, perPage);
        } catch (err) {
            setInfoModal({
                type: "error",
                visible: true,
                message: "No se pudo solicitar la revisión",
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
                    <FiltersBar onSearch={onFilter} isAdmin={isAdmin} />
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