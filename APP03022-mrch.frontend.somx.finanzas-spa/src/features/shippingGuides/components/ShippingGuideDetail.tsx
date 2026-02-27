import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { GenericButton } from "@shared/components/ui/button";
import { ReactElement, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { ShippingGuideClient } from "../api/ShippingGuideClient";
import { ShippingGuideDetail } from "../interfaces";

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
  const client = new ShippingGuideClient();

  const [detail, setDetail] = useState<ShippingGuideDetail>(DEFAULT_DETAIL);
  const [loading, setLoading] = useState<boolean>(false);

  const fetchDetail = async (id: string) => {
    setLoading(true);
    try {
      const response = await client.getDetail(id);
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
    <div className="space-y-6">
      <div className="flex flex-col gap-2">
        <div className="text-xl font-bold">Detalle de guía</div>
        <div className="text-sm text-gray-600">
          Información relacionada con la guía cancelada.
        </div>
      </div>

      <div className="grid grid-cols-2 gap-6 bg-white border border-gray-200 rounded-lg p-4">
        <div>
          <div className="text-gray-500 text-xs uppercase">Id Guía</div>
          <div className="text-sm font-semibold">{detail.shippingGuideId || guideId}</div>
        </div>
        <div>
          <div className="text-gray-500 text-xs uppercase">Tipo Origen</div>
          <div className="text-sm font-semibold">{detail.originType || "N/D"}</div>
        </div>
        <div>
          <div className="text-gray-500 text-xs uppercase">Motivo (ID)</div>
          <div className="text-sm font-semibold">
            {detail.reasonId ? detail.reasonId : "N/D"}
          </div>
        </div>
        <div>
          <div className="text-gray-500 text-xs uppercase">Descripción motivo</div>
          <div className="text-sm font-semibold">
            {detail.reasonDescription || "N/D"}
          </div>
        </div>
        <div>
          <div className="text-gray-500 text-xs uppercase">Fecha relación</div>
          <div className="text-sm font-semibold">
            {formatDateTime(detail.relationDate)}
          </div>
        </div>
        <div>
          <div className="text-gray-500 text-xs uppercase">Comentario</div>
          <div className="text-sm font-semibold">
            {detail.comment || "N/D"}
          </div>
        </div>
        <div>
          <div className="text-gray-500 text-xs uppercase">Id Usuario</div>
          <div className="text-sm font-semibold">
            {detail.userId || "N/D"}
          </div>
        </div>
        <div>
          <div className="text-gray-500 text-xs uppercase">Usuario</div>
          <div className="text-sm font-semibold">
            {detail.userName || "N/D"}
          </div>
        </div>
      </div>

      <div className="flex justify-end">
        <GenericButton
          variant="outlineFill"
          className="h-11"
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
