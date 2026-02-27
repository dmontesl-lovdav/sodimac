import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { ReactElement, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { OrderClient } from "../api/OrderClient";
import { EMPTY_RECEPTION, Reception } from "../interfaces";
import { ReceptionInvoiceControl } from "./parts/ReceptionInvoiceControl";
import ReceptionHeader from "./parts/ReceptionHeader";


export function ReceptionInvoice(): ReactElement {
    const params = useParams();
    const [loading, setLoading] = useState(false);
    const [reception, setReception] = useState<Reception>(EMPTY_RECEPTION);
    const client: OrderClient = new OrderClient();


    

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
    }, [params]);

    const breadcrumb: BreadcrumbItem[] = [
        { label: "Finanzas", to: "/" },
        { label: "Recepciones", to: "/recepciones" },
        { label: `${reception.receptionId}` },
    ];

    return decorate(
        breadcrumb,
        "/recepciones/" + reception.receptionId,
        <>
            <ReceptionHeader reception={reception} />
            <ReceptionInvoiceControl reception={reception} />
        </>,
        loading
    );
}

export default ReceptionInvoice;