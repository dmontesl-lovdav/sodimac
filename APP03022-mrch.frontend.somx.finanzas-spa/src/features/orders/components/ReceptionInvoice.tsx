import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { ReactElement, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { OrderClient } from "../api/OrderClient";
import { EMPTY_RECEPTION, Reception } from "../interfaces";
import { ReceptionInvoiceControl } from "./parts/ReceptionInvoiceControl";
import ReceptionHeader from "./parts/ReceptionHeader";
import "./ReceptionInvoice.css";


export function ReceptionInvoice(): ReactElement {
    const params = useParams();
    const [loading, setLoading] = useState(false);
    const [reception, setReception] = useState<Reception>(EMPTY_RECEPTION);
    const client: any = OrderClient;


    

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
        if(params.uuid){
            fetchData(params.uuid);
        }
        console.log("ReceptionInvoice params:", params);
    }, [params]);

    const breadcrumb: BreadcrumbItem[] = [
        { label: "Finanzas", to: "/finanzas" },
        { label: "Recepciones", to: "/finanzas/recepciones" },
        { label: `${reception.receptionId}` },
    ];

    return decorate(
        breadcrumb,
        "/finanzas/recepciones/" + reception.receptionId,
        <div className="rc-reception-invoice">
            <ReceptionHeader reception={reception} />
            <ReceptionInvoiceControl reception={reception} />
        </div>,
        loading
    );
}

export default ReceptionInvoice;