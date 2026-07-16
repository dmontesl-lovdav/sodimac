import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { withFinanceBreadcrumb } from "@/shared/components/ui/navigation/financeBreadcrumb";
import GenericModal from "@shared/components/ui/modal/GenericModal";
import {
    GenericButton,
    GenericInput,
    GenericSelect,
} from "@/shared/components/ui";
import { ChangeEvent, ReactElement, useEffect, useMemo, useState } from "react";
import { useLocation, useParams } from "react-router-dom";
import { shippingGuideService } from "../api/ShippingGuideClient";
import {
    ShippingGuide,
    ShippingGuideStatusHistory,
    getNumericGuideStatus,
} from "../interfaces";
import { getErrorMessage } from "@/utils/errorMessage";
import { formatDateTime } from "@/utils/utils";
import { getShippingGuideStatusCode } from "../utils/shippingGuideStatus";
import { FINANCE_LIST_KEYS } from "@/shared/hooks";
import { APP_EVENT, PermissionGate } from "@shared/security";
import "../styles/shippingGuides.css";

type StatusOption = { value: number; label: string };

const statusOptions: StatusOption[] = [
    { value: 1, label: "Disponible" },
    { value: 3, label: "Consumida manual" },
    { value: 4, label: "Cancelada" },
];

const reasonCatalog = [
    { value: 1, label: "Corrección operativa" },
    { value: 2, label: "Solicitud proveedor" },
    { value: 3, label: "Error en captura" },
    { value: 4, label: "Otro" },
];

const allowedTargets = (current?: number) => {
    if (current === 1) return [3, 4];
    if (current === 4) return [1];
    return [];
};

/** Catálogo de estatus en fila (objeto) o número en detalle API */
const getGuideStatusNum = (g?: ShippingGuide): number | undefined => {
    if (g?.status == null) return undefined;
    const n = getShippingGuideStatusCode(g);
    return Number.isFinite(n) ? n : undefined;
};

const getGuideBreadcrumbLabel = (
    guideId?: string,
    guideNumber?: string
): string => {
    if (guideNumber) return `Guía ${guideNumber}`;
    if (guideId) return "Detalle guía";
    return "Guía";
};

const buildBreadcrumb = (
    guideId?: string,
    guideNumber?: string
): BreadcrumbItem[] =>
    withFinanceBreadcrumb([
        { label: "Guías de Embarque", to: "/finanzas/guias" },
        {
            label: getGuideBreadcrumbLabel(guideId, guideNumber),
            to: guideId ? `/finanzas/guias/${guideId}` : undefined,
        },
        { label: "Actualizar estatus" },
    ]);

const styles = {
    container: {
        display: "flex",
        flexDirection: "column" as const,
        gap: "1.25rem",
    },
    formVertical: {
        display: "flex",
        flexDirection: "column" as const,
        gap: "1rem",
        maxWidth: "560px",
    },
    fieldLabel: {
        fontSize: "12px",
        fontWeight: 600,
    },
};

export default function ShippingGuideStatusUpdate(): ReactElement {
    const { guideId } = useParams<{ guideId: string }>();
    const location = useLocation();

    const guideFromState = (location.state as any)?.guide as
        | ShippingGuide
        | undefined;

    const [guideCard, setGuideCard] = useState<ShippingGuide | null>(
        guideFromState ?? null
    );

    const [loading, setLoading] = useState<boolean>(false);
    const [history, setHistory] = useState<ShippingGuideStatusHistory[]>([]);
    const initialStatus = getGuideStatusNum(guideFromState);

    const [currentStatus, setCurrentStatus] = useState<number | undefined>(
        initialStatus
    );
    const [targetStatus, setTargetStatus] = useState<number | undefined>(
        initialStatus
    );
    const [reasonId, setReasonId] = useState<string>("");
    const [series, setSeries] = useState<string>("");
    const [folio, setFolio] = useState<string>("");
    const [uuid, setUuid] = useState<string>("");
    const [comment, setComment] = useState<string>("");
    const [alertModal, setAlertModal] = useState<{
        visible: boolean;
        title: string;
        message: string;
        severity: "success" | "error" | "info" | "warning";
    }>({ visible: false, title: "", message: "", severity: "info" });

    useEffect(() => {
        const fetchDetail = async (id: string) => {
            setLoading(true);
            try {
                const d = await shippingGuideService.getDetail(id);
                const sn = getNumericGuideStatus(d.status);
                setCurrentStatus(sn);
                setTargetStatus(sn);
            } finally {
                setLoading(false);
            }
        };

        const fetchHistory = async (id: string) => {
            try {
                const h = await shippingGuideService.getStatusHistory(id);
                setHistory(h);
            } catch {
                setHistory([]);
            }
        };

        if (guideId) {
            if (!guideFromState) {
                fetchDetail(guideId);
            }
            fetchHistory(guideId);
        }
    }, [guideId, guideFromState]);

    useEffect(() => {
        if (guideFromState) {
            setGuideCard(guideFromState);
        }
    }, [guideFromState]);

    useEffect(() => {
        if (guideFromState || !guideId) return;
        let cancelled = false;
        (async () => {
            const end = new Date();
            const start = new Date();
            start.setFullYear(start.getFullYear() - 1);
            try {
                const rows = await shippingGuideService.get({
                    from: start.toISOString().slice(0, 10),
                    to: end.toISOString().slice(0, 10),
                });
                const found = Array.isArray(rows)
                    ? rows.find((g) => g.shippingGuideId === guideId)
                    : undefined;
                if (!cancelled) setGuideCard(found || null);
            } catch {
                if (!cancelled) setGuideCard(null);
            }
        })();
        return () => {
            cancelled = true;
        };
    }, [guideId, guideFromState]);

    const allowedTargetsForCurrent = useMemo(
        () => allowedTargets(currentStatus),
        [currentStatus]
    );

    const targetStatusSelectOptions = useMemo(
        () =>
            statusOptions.filter((s) =>
                allowedTargetsForCurrent.includes(s.value)
            ),
        [allowedTargetsForCurrent]
    );

    useEffect(() => {
        if (
            targetStatus !== undefined &&
            allowedTargetsForCurrent.length > 0 &&
            !allowedTargetsForCurrent.includes(targetStatus)
        ) {
            setTargetStatus(undefined);
        }
    }, [allowedTargetsForCurrent, targetStatus]);

    const resetFields = () => {
        setReasonId("");
        setSeries("");
        setFolio("");
        setUuid("");
        setComment("");
    };

    const handleStatusChange = (value: number) => {
        setTargetStatus(value);
        resetFields();
    };

    const validate = () => {
        if (!guideId) {
            setAlertModal({
                visible: true,
                title: "Sin guía",
                message: "No se encontró la guía a actualizar.",
                severity: "error",
            });
            return false;
        }
        if (currentStatus === undefined || allowedTargetsForCurrent.length === 0) {
            setAlertModal({
                visible: true,
                title: "Estatus no permitido",
                message: "Solo puedes actualizar guías en estatus 1 o 4.",
                severity: "warning",
            });
            return false;
        }
        if (!targetStatus) {
            setAlertModal({
                visible: true,
                title: "Selecciona estatus",
                message: "Elige el estatus objetivo para continuar.",
                severity: "warning",
            });
            return false;
        }
        if (!reasonId) {
            setAlertModal({
                visible: true,
                title: "Motivo requerido",
                message: "Selecciona un motivo de cambio.",
                severity: "warning",
            });
            return false;
        }

        if (targetStatus === 3) {
            if (!series || !folio || !uuid || !comment.trim()) {
                setAlertModal({
                    visible: true,
                    title: "Campos obligatorios",
                    message:
                        "Serie, Folio, UUID y Comentario son obligatorios para Consumida manual.",
                    severity: "warning",
                });
                return false;
            }
            if (
                series.length > 100 ||
                folio.length > 100 ||
                uuid.length > 36 ||
                comment.length > 254
            ) {
                setAlertModal({
                    visible: true,
                    title: "Longitudes excedidas",
                    message:
                        "Revisa la longitud: Serie/Folio (100), UUID (36), Comentario (254).",
                    severity: "warning",
                });
                return false;
            }
        } else {
            if (!comment.trim()) {
                setAlertModal({
                    visible: true,
                    title: "Comentario requerido",
                    message: "El comentario es obligatorio.",
                    severity: "warning",
                });
                return false;
            }
            if (comment.length > 254) {
                setAlertModal({
                    visible: true,
                    title: "Longitud excedida",
                    message:
                        "El comentario no puede exceder 254 caracteres.",
                    severity: "warning",
                });
                return false;
            }
        }

        const allowed = allowedTargetsForCurrent.includes(targetStatus);
        if (!allowed) {
            setAlertModal({
                visible: true,
                title: "Transición no permitida",
                message: "Solo se permite: 1→3, 1→4 o 4→1.",
                severity: "warning",
            });
            return false;
        }

        return true;
    };

    const handleSubmit = async () => {
        if (!validate() || !guideId || !targetStatus) return;

        setLoading(true);
        try {
            await shippingGuideService.updateStatus({
                shippingGuideId: guideId,
                targetStatus,
                reasonId: Number(reasonId),
                series: targetStatus === 3 ? series : undefined,
                folio: targetStatus === 3 ? folio : undefined,
                uuid: targetStatus === 3 ? uuid : "",
                comment,
            });

            setAlertModal({
                visible: true,
                title: "Actualización exitosa",
                message:
                    "La guía de embarque ha cambiado de estatus con éxito.",
                severity: "success",
            });

            setCurrentStatus(targetStatus);
        } catch (error: unknown) {
            const detail = getErrorMessage(
                error,
                "No fue posible actualizar el estatus de la guía."
            );
            setAlertModal({
                visible: true,
                title: "Error al cambiar estatus",
                message: detail,
                severity: "error",
            });
        } finally {
            setLoading(false);
        }
    };

    const renderHistory = () => {
        if (!history || history.length === 0) {
            return (
                <div className="sg-divider">
                    Sin histórico disponible
                </div>
            );
        }

        return (
            <div className="sg-divider">
                <div className="sg-title" style={{ fontSize: "14px" }}>
                    Historial de estatus
                </div>
                <table className="gt-table" style={{ marginTop: 8 }}>
                    <thead className="gt-thead">
                        <tr>
                            <th className="gt-th gt-align-left">
                                Fecha registro
                            </th>
                            <th className="gt-th gt-align-left">
                                Estatus
                            </th>
                            <th className="gt-th gt-align-left">
                                Usuario
                            </th>
                            <th className="gt-th gt-align-left">
                                Comentario
                            </th>
                        </tr>
                    </thead>
                    <tbody>
                        {history.map((h) => (
                            <tr
                                key={`${h.registeredAt}-${h.status}-${h.userId ?? ""}-${h.comment ?? ""}`}
                                className="gt-row"
                            >
                                <td className="gt-td">
                                    {formatDateTime(h.registeredAt, { seconds: true })}
                                </td>
                                <td className="gt-td">
                                    {statusOptions.find(
                                        (s) =>
                                            s.value === h.status
                                    )?.label || h.status}
                                </td>
                                <td className="gt-td">
                                    {h.userId || "N/D"}
                                </td>
                                <td className="gt-td">
                                    {h.comment || "N/D"}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        );
    };

    return decorate(
        buildBreadcrumb(
            guideId,
            guideCard?.guideNumber ?? guideFromState?.guideNumber
        ),
        guideId ? `/finanzas/guias/${guideId}` : "/finanzas/guias",
        <div style={styles.container}>
            <div className="sg-status-header-card">
                <div className="sg-title" style={{ marginBottom: "12px" }}>
                    Actualizar estatus
                </div>
                <div className="sg-status-header-grid">
                    <div>
                        <div className="sg-status-field-label">
                            Número proveedor
                        </div>
                        <div className="sg-status-field-value">
                            {guideCard?.vendorNumber ??
                                guideCard?.supplier?.supplierNumber ??
                                "N/D"}
                        </div>
                    </div>
                    <div>
                        <div className="sg-status-field-label">
                            Nombre proveedor
                        </div>
                        <div className="sg-status-field-value">
                            {guideCard?.supplier?.businessName ?? "N/D"}
                        </div>
                    </div>
                    <div>
                        <div className="sg-status-field-label">
                            Orden Compra
                        </div>
                        <div className="sg-status-field-value">
                            {guideCard?.orderNumber?.trim()
                                ? guideCard.orderNumber
                                : "N/D"}
                        </div>
                    </div>
                    <div>
                        <div className="sg-status-field-label">
                            Guía embarque
                        </div>
                        <div className="sg-status-field-value">
                            {guideCard?.guideNumber || guideId || "N/D"}
                        </div>
                    </div>
                </div>
            </div>

            <div className="sg-form-vertical" style={styles.formVertical}>
                <div>
                    <div
                        className="sg-title"
                        style={styles.fieldLabel}
                    >
                        Estatus actual
                    </div>
                    <div>
                        {statusOptions.find((s) => s.value === currentStatus)
                            ?.label || "N/D"}
                    </div>
                </div>

                <div>
                    <label
                        htmlFor="sg-status-target"
                        className="sg-title"
                        style={styles.fieldLabel}
                    >
                        Nuevo estatus
                    </label>
                    <GenericSelect
                        id="sg-status-target"
                        value={
                            targetStatus !== undefined
                                ? String(targetStatus)
                                : ""
                        }
                        onChange={(
                            e: ChangeEvent<HTMLSelectElement>
                        ) => {
                            const v = e.target.value;
                            if (v === "") {
                                setTargetStatus(undefined);
                                resetFields();
                                return;
                            }
                            handleStatusChange(Number(v));
                        }}
                        placeholder="Selecciona estatus"
                        options={targetStatusSelectOptions.map(
                            (s) => ({
                                value: String(s.value),
                                label: s.label,
                            })
                        )}
                        containerClassName="sg-generic-select-wrap"
                    />
                </div>

                <div>
                    <label
                        htmlFor="sg-status-reason"
                        className="sg-title"
                        style={styles.fieldLabel}
                    >
                        Motivo de cambio
                    </label>
                    <GenericSelect
                        id="sg-status-reason"
                        value={reasonId}
                        onChange={(
                            e: ChangeEvent<HTMLSelectElement>
                        ) =>
                            setReasonId(e.target.value)
                        }
                        placeholder="Selecciona motivo"
                        options={reasonCatalog.map((r) => ({
                            value: String(r.value),
                            label: r.label,
                        }))}
                        containerClassName="sg-generic-select-wrap"
                    />
                </div>

                {targetStatus === 3 && (
                    <>
                        <GenericInput
                            label="Serie (100)"
                            value={series}
                            maxLength={100}
                            onChange={(
                                e: ChangeEvent<HTMLInputElement>
                            ) => setSeries(e.target.value)}
                            placeholder="Serie"
                        />
                        <GenericInput
                            label="Folio (100)"
                            value={folio}
                            maxLength={100}
                            onChange={(
                                e: ChangeEvent<HTMLInputElement>
                            ) => setFolio(e.target.value)}
                            placeholder="Folio"
                        />
                        <GenericInput
                            label="UUID (36)"
                            value={uuid}
                            maxLength={36}
                            onChange={(
                                e: ChangeEvent<HTMLInputElement>
                            ) => setUuid(e.target.value)}
                            placeholder="UUID"
                        />
                    </>
                )}

                <div>
                    <label
                        htmlFor="sg-status-comment"
                        className="sg-title"
                        style={styles.fieldLabel}
                    >
                        Comentario (254)
                    </label>
                    <textarea
                        id="sg-status-comment"
                        value={comment}
                        maxLength={254}
                        onChange={(e) => setComment(e.target.value)}
                        className="sg-textarea"
                        placeholder="Describe el motivo del cambio"
                    />
                </div>
            </div>

            <div className="sg-status-actions-row">
                <PermissionGate appEvent={APP_EVENT.CARTA_PORTE.UPDATE_STATUS}>
                    <GenericButton
                        variant="primary"
                        type="button"
                        onClick={handleSubmit}
                        disabled={loading}
                    >
                        {loading ? "Actualizando..." : "Actualizar estatus"}
                    </GenericButton>
                </PermissionGate>
            </div>

            {renderHistory()}

            <GenericModal
                variant="alert"
                visible={alertModal.visible}
                title={alertModal.title}
                message={alertModal.message}
                severity={alertModal.severity}
                buttonText="Aceptar"
                onClose={() =>
                    setAlertModal({
                        ...alertModal,
                        visible: false,
                    })
                }
            />
        </div>,
        loading,
        undefined,
        {
            actionsAlign: "end",
            financeListSession: FINANCE_LIST_KEYS.shippingGuides,
        }
    );
}
