import { ApiClient } from "@/services/ApiClient";
import { ShippingGuide, ShippingGuideDetail, ShippingGuideFilter, ShippingGuideStatusHistory } from "../interfaces";

export class ShippingGuideClient extends ApiClient {

    private DEFAULT_ROUTE = "api/shipping-guide";

    public constructor() {
        super();
    }

    public async get(filter: ShippingGuideFilter, binary?: boolean): Promise<ShippingGuide[]> {
        const params: URLSearchParams = new URLSearchParams();
        filter.guideNumber && params.set("guideNumber", filter.guideNumber);
        filter.vendorNumber && params.set("vendorNumber", filter.vendorNumber);
        filter.vendorNumber && params.set("supplierNumber", filter.vendorNumber); // alias
        filter.sourceId && params.set("sourceId", filter.sourceId);
        filter.sourceId && params.set("origin", filter.sourceId); // alias
        filter.truckPlate && params.set("truckPlate", filter.truckPlate);
        filter.truckPlate && params.set("plate", filter.truckPlate); // alias
        filter.trailerPlate && params.set("trailerPlate", filter.trailerPlate);
        filter.from && params.set("from", filter.from);
        filter.to && params.set("to", filter.to);
        filter.deliveryType && params.set("deliveryType", filter.deliveryType);

        return binary
            ? this.executeBinary(`${this.DEFAULT_ROUTE}/csv?${params.toString()}`, "get", filter, undefined, "report.csv") as unknown as Promise<ShippingGuide[]>
            : this.execute<ShippingGuide[]>(`${this.DEFAULT_ROUTE}?${params.toString()}`, "get", filter);
    };

    public async getDetail(shippingGuideId: string): Promise<ShippingGuideDetail> {
        return this.execute<ShippingGuideDetail>(`${this.DEFAULT_ROUTE}/${shippingGuideId}`, "get");
    }

    public async cancel(payload: {
        shippingGuideIds: string[];
        reasonId: number;
        comment: string;
    }): Promise<void> {
        return this.execute<void>(`${this.DEFAULT_ROUTE}/cancel`, "post", payload);
    }

    public async updateStatus(payload: {
        shippingGuideId: string;
        targetStatus: number;
        reasonId: number;
        series?: string;
        folio?: string;
        uuid?: string;
        comment: string;
    }): Promise<void> {
        return this.execute<void>(`${this.DEFAULT_ROUTE}/status`, "post", payload);
    }

    public async getStatusHistory(shippingGuideId: string): Promise<ShippingGuideStatusHistory[]> {
        return this.execute<ShippingGuideStatusHistory[]>(`${this.DEFAULT_ROUTE}/${shippingGuideId}/status-history`, "get");
    }
}
