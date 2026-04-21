import { GenericInput, GenericSelect } from "@/shared/components/ui";
import { GenericButton } from "@/shared/components/ui/button";
import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";

import { ChangeEvent, ReactElement, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { OrderClient } from "../api/OrderClient";
import { EMPTY_RECEPTION, ReceptionStatusOptions, Reception } from "../interfaces";
import ReceptionHeader from "./parts/ReceptionHeader";
import ReceptionSkusTable from "./parts/ReceptionSkusTable";
import ErrorMessage from "@/shared/components/ui/alerts/ErrorMessage";
import "./ReceptionDetail.css";

const buildDetail = (reception: Reception, updateOrderStatus: Function) => {
    const [status, setStatus] = useState(reception.status);
    const [reason, setReason] = useState("");
    const [uuid, setUuid] = useState("");
    const [error, setError] = useState("");

    useEffect(() => {
        if (status !== 2) {
            setUuid("");
        }
    }, [status])

    const checkInformation = () => {
        setError("");
        if (reason.trim() === "") {
            setError("Razón o motivo es un campo requerido")
            return;
        }
        if (status === 2 && uuid.trim() === "") {
            setError("UUID es un campo requerido")
            return;
        }
        updateOrderStatus(reason, status, uuid);
    }

    return (
        <div className="rc-detail-container">
            <div className="rc-detail-card">
                <div className="rc-row">
                    <div className="rc-label">Editar Estado:</div>
                </div>
                <div className="rc-row">
                    <GenericSelect
                        value={status}
                        onChange={(event: ChangeEvent<HTMLInputElement>) => setStatus(parseInt(event.target.value))}
                        placeholder="Estado"
                        disablePlaceholder={true}
                        options={ReceptionStatusOptions}
                    />
                </div>
                <div className="rc-row">
                    <GenericInput
                        label="Motivo de cambio de estado"
                        placeholder="Escribe la razón por el cambio de estado"
                        value={reason}
                        onChange={(event: ChangeEvent<HTMLInputElement>) => setReason(event.target.value)}
                    />
                </div>

                {status === 2 &&
                    (<div className="rc-row">
                        <GenericInput
                            label="UUID"
                            placeholder="Proporciona el UUID para complementar"
                            value={uuid}
                            onChange={(event: ChangeEvent<HTMLInputElement>) => setUuid(event.target.value)}
                        />
                    </div>)}
                <ErrorMessage message={error} />
                <div className="rc-button-row">
                    <GenericButton variant="outline" onClick={checkInformation} > Guardar Estado </GenericButton>
                </div>
            </div>
        </div>
    );
};


interface ReceptionDetailProps {
    editable?: boolean
}

export function ReceptionDetail({ editable = false }: ReceptionDetailProps): ReactElement {
    const params = useParams();
    const [loading, setLoading] = useState(false);
    const [reception, setReception] = useState<Reception>(EMPTY_RECEPTION);
    const client = OrderClient;

    useEffect(() => {
        const fetchData = async (uuid: string) => {
            setLoading(true);
            try {
                const response = await client.getReceptionByUuid(uuid);
                setReception(response.data);
            } finally {
                setLoading(false);
            }
        };
        fetchData(params.uuid || "");
    }, [params]);

    const updateOrderStatus = async (reason: string, status: number, uuid: string) => {

        try {
            if (reception && reception.receptionId) {
                setLoading(true);
                const updated = await client.updateReceptionStatus(reception.receptionId, {
                    status: status,
                    comment: reason
                });
            }

        } finally {
            setLoading(false);
        }
    };



    const breadcrumb: BreadcrumbItem[] = [
        { label: "Finanzas", to: "/finanzas" },
        { label: "Recepciones", to: "/finanzas/recepciones" },
        { label: `${reception.receptionNumber}` },
    ];

    return decorate(
        breadcrumb,
        "/finanzas/recepciones",
        <>
            <ReceptionHeader reception={reception} />
            {editable && buildDetail(reception, updateOrderStatus)}
            {!editable && <ReceptionSkusTable reception={reception} />}
        </>,
        loading
    );
}

export default ReceptionDetail;
