import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { GenericButton } from "@shared/components/ui/button";
import { ReactElement, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { shippingGuideService } from "../api/ShippingGuideClient";
import { ShippingGuideDetail } from "../interfaces";

const styles = {
    container: {
        display: 'flex',
        flexDirection: 'column' as const,
        gap: '1.5rem',
    },
    header: {
        display: 'flex',
        flexDirection: 'column' as const,
        gap: '0.5rem',
    },
    title: {
        fontSize: '1.25rem',
        fontWeight: 700,
    },
    subtitle: {
        fontSize: '0.875rem',
        color: '#4b5563',
    },
    grid: {
        display: 'grid',
        gridTemplateColumns: 'repeat(2, 1fr)',
        gap: '1.5rem',
        backgroundColor: '#ffffff',
        border: '1px solid #e5e7eb',
        borderRadius: '0.5rem',
        padding: '1rem',
    },
    label: {
        color: '#6b7280',
        fontSize: '0.75rem',
        textTransform: 'uppercase' as const,
    },
    value: {
        fontSize: '0.875rem',
        fontWeight: 600,
    },
    footer: {
        display: 'flex',
        justifyContent: 'flex-end',
    },
};

const formatDateTime = (value?: string) => {
    if (!value) return "N/D";
    return new Date(value).toLocaleString("es-MX", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
        hour12: false,
    });
};

const DEFAULT_DETAIL: ShippingGuideDetail = {
    shippingGuideId: "",
    originType: "",
    reasonId: 0,
    reasonDescription: "",
    relationDate: "",
    comment: "",
    userId: "",
    userName: "",
};

export default function ShippingGuideDetailView(): ReactElement {
    const { guideId } = useParams<{ guideId: string }>();
    const nav = useNavigate();

    const [detail, setDetail] = useState<ShippingGuideDetail>(DEFAULT_DETAIL);
    const [loading, setLoading] = useState<boolean>(false);

    const fetchDetail = async (id: string) => {
        setLoading(true);
        try {
            const response = await shippingGuideService.getDetail(id);
            setDetail(response);
        } catch (error) {
            console.error("Error al obtener detalle de guía", error);
            setDetail((prev) => ({ ...prev, shippingGuideId: id }));
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (guideId) fetchDetail(guideId);
    }, [guideId]);

    const breadcrumb: BreadcrumbItem[] = [
        { label: "Finanzas", to: "/" },
        { label: "Guías de Embarque", to: "/guias" },
        { label: "Detalle" },
    ];

    return decorate(
        breadcrumb,
        "/guias",
        <div style={styles.container}>
            <div style={styles.header}>
                <div style={styles.title}>Detalle de guía</div>
                <div style={styles.subtitle}>
                    Información relacionada con la guía cancelada.
                </div>
            </div>

            <div style={styles.grid}>
                <div>
                    <div style={styles.label}>Id Guía</div>
                    <div style={styles.value}>
                        {detail.shippingGuideId || guideId}
                    </div>
                </div>
                <div>
                    <div style={styles.label}>Tipo Origen</div>
                    <div style={styles.value}>
                        {detail.originType || "N/D"}
                    </div>
                </div>
                <div>
                    <div style={styles.label}>Motivo (ID)</div>
                    <div style={styles.value}>
                        {detail.reasonId ? detail.reasonId : "N/D"}
                    </div>
                </div>
                <div>
                    <div style={styles.label}>Descripción motivo</div>
                    <div style={styles.value}>
                        {detail.reasonDescription || "N/D"}
                    </div>
                </div>
                <div>
                    <div style={styles.label}>Fecha relación</div>
                    <div style={styles.value}>
                        {formatDateTime(detail.relationDate)}
                    </div>
                </div>
                <div>
                    <div style={styles.label}>Comentario</div>
                    <div style={styles.value}>
                        {detail.comment || "N/D"}
                    </div>
                </div>
                <div>
                    <div style={styles.label}>Id Usuario</div>
                    <div style={styles.value}>
                        {detail.userId || "N/D"}
                    </div>
                </div>
                <div>
                    <div style={styles.label}>Usuario</div>
                    <div style={styles.value}>
                        {detail.userName || "N/D"}
                    </div>
                </div>
            </div>

            <div style={styles.footer}>
                <GenericButton
                    variant="outlineFill"
                    style={{ height: '44px' }}
                    disabled={loading}
                    onClick={() => {
                        if (window.history.length > 1) {
                            nav(-1);
                        } else {
                            nav("/guias");
                        }
                    }}
                >
                    Regresar
                </GenericButton>
            </div>
        </div>,
        loading
    );
}
