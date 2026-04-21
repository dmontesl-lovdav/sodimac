import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import GenericModal from "@shared/components/ui/modal/GenericModal";
import { ChangeEvent, ReactElement, useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { shippingGuideService } from "../api/ShippingGuideClient";
import {
    ShippingGuide,
    ShippingGuideDetail,
    ShippingGuideStatusHistory,
} from "../interfaces";
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

const buildBreadcrumb = (guideId?: string): BreadcrumbItem[] => [
    { label: "Finanzas", to: "/" },
    { label: "Guías de Embarque", to: "/guias" },
    { label: guideId ? `Guía ${guideId}` : "Detalle" },
    { label: "Actualizar estatus" },
];

const styles = {
    container: {
        display: "flex",
        flexDirection: "column" as const,
        gap: "1rem",
    },
    formGrid: {
        display: "grid",
        gridTemplateColumns: "repeat(auto-fit, minmax(240px, 1fr))",
        gap: "1rem",
    },
    fieldLabel: {
        fontSize: "12px",
        fontWeight: 600,
    },
};

export default function ShippingGuideStatusUpdate(): ReactElement {
    const { guideId } = useParams<{ guideId: string }>();
    const nav = useNavigate();
    const location = useLocation();

    const guideFromState = (location.state as any)?.guide as ShippingGuide | undefined;

    const [loading, setLoading] = useState<boolean>(false);
    const [detail, setDetail] = useState<ShippingGuideDetail | null>(null);
    const [history, setHistory] = useState<ShippingGuideStatusHistory[]>([]);
    const [currentStatus, setCurrentStatus] = useState<number | undefined>(
        guideFromState?.status
    );
    const [targetStatus, setTargetStatus] = useState<number | undefined>(
        guideFromState?.status
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
                setDetail(d);
                setCurrentStatus(d.status);
                setTargetStatus(d.status);
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

    const allowedTargetsForCurrent = useMemo(
        () => allowedTargets(currentStatus),
        [currentStatus]
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
        if (!currentStatus || allowedTargetsForCurrent.length === 0) {
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
        } catch (error: any) {
            const code = error?.response?.status ?? "N/D";
            const reason =
                error?.response?.data?.message ??
                "Motivo no disponible";
            setAlertModal({
                visible: true,
                title: "Error al cambiar estatus",
                message: `Error al intentar cambiar de estatus la guía seleccionada, código de error [${code}] - Motivo [${reason}]`,
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
                        {history.map((h, idx) => (
                            <tr key={idx} className="gt-row">
                                <td className="gt-td">
                                    {new Date(
                                        h.registeredAt
                                    ).toLocaleString("es-MX")}
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
        buildBreadcrumb(guideId),
        "/guias",
        <div style={styles.container}>
            <div className="sg-header">
                <div className="sg-title">
                    Actualizar estatus
                </div>
            </div>

            <div className="sg-divider" />

            <div className="sg-title" style={{ fontSize: "14px" }}>
                Guía: {guideId || "N/D"}
            </div>

            <div className="sg-divider" />

            <div style={styles.formGrid}>
                <div>
                    <label
                        className="sg-title"
                        style={styles.fieldLabel}
                    >
                        Estatus actual
                    </label>
                    <div>
                        {statusOptions.find(
                            (s) =>
                                s.value === currentStatus
                        )?.label || "N/D"}
                    </div>
                </div>

                <div>
                    <label
                        className="sg-title"
                        style={styles.fieldLabel}
                    >
                        Nuevo estatus
                    </label>
                    <select
                        value={targetStatus ?? ""}
                        onChange={(
                            e: ChangeEvent<HTMLSelectElement>
                        ) =>
                            handleStatusChange(
                                Number(e.target.value)
                            )
                        }
                        className="sg-select"
                    >
                        <option value="">
                            Selecciona estatus
                        </option>
                        {statusOptions
                            .filter((s) =>
                                allowedTargetsForCurrent.includes(
                                    s.value
                                )
                            )
                            .map((s) => (
                                <option
                                    key={s.value}
                                    value={s.value}
                                >
                                    {s.label}
                                </option>
                            ))}
                    </select>
                </div>

                <div>
                    <label
                        className="sg-title"
                        style={styles.fieldLabel}
                    >
                        Motivo de cambio
                    </label>
                    <select
                        value={reasonId}
                        onChange={(
                            e: ChangeEvent<HTMLSelectElement>
                        ) =>
                            setReasonId(e.target.value)
                        }
                        className="sg-select"
                    >
                        <option value="">
                            Selecciona motivo
                        </option>
                        {reasonCatalog.map((r) => (
                            <option
                                key={r.value}
                                value={r.value}
                            >
                                {r.label}
                            </option>
                        ))}
                    </select>
                </div>
            </div>

            {targetStatus === 3 && (
                <div style={styles.formGrid}>
                    <div>
                        <label
                            className="sg-title"
                            style={styles.fieldLabel}
                        >
                            Serie (100)
                        </label>
                        <input
                            value={series}
                            maxLength={100}
                            onChange={(e) =>
                                setSeries(e.target.value)
                            }
                            className="sg-input"
                            placeholder="Serie"
                        />
                    </div>

                    <div>
                        <label
                            className="sg-title"
                            style={styles.fieldLabel}
                        >
                            Folio (100)
                        </label>
                        <input
                            value={folio}
                            maxLength={100}
                            onChange={(e) =>
                                setFolio(e.target.value)
                            }
                            className="sg-input"
                            placeholder="Folio"
                        />
                    </div>

                    <div>
                        <label
                            className="sg-title"
                            style={styles.fieldLabel}
                        >
                            UUID (36)
                        </label>
                        <input
                            value={uuid}
                            maxLength={36}
                            onChange={(e) =>
                                setUuid(e.target.value)
                            }
                            className="sg-input"
                            placeholder="UUID"
                        />
                    </div>
                </div>
            )}

            <div>
                <label
                    className="sg-title"
                    style={styles.fieldLabel}
                >
                    Comentario (254)
                </label>
                <textarea
                    value={comment}
                    maxLength={254}
                    onChange={(e) =>
                        setComment(e.target.value)
                    }
                    className="sg-textarea"
                    placeholder="Describe el motivo del cambio"
                />
            </div>

            <div className="sg-modal-footer">
                <button
                    className="sg-btn sg-btn-outline"
                    onClick={() => nav(-1)}
                >
                    Regresar
                </button>
                <button
                    className="sg-btn sg-btn-primary"
                    onClick={handleSubmit}
                    disabled={loading}
                >
                    {loading
                        ? "Actualizando..."
                        : "Actualizar estatus"}
                </button>
            </div>

            {renderHistory()}

            <GenericModal
                variant="alert"
                visible={alertModal.visible}
                title={alertModal.title}
                message={alertModal.message}
                severity={alertModal.severity}
                onClose={() =>
                    setAlertModal({
                        ...alertModal,
                        visible: false,
                    })
                }
            />
        </div>,
        loading
    );
}
