import { ApiClient } from "@/services/ApiClient";
import { OrdersFilters } from "../components/parts/FiltersBar";
import { Order, Reception, ReceptionAxios, ReceptionAxiosSingle } from "../interfaces";

export class OrderClient extends ApiClient {

    private DEFAULT_ROUTE = "/purchase-orders";

    public constructor() {
        super();
    }

    public async get(criteria: OrdersFilters): Promise<ReceptionAxios> {
        const params: URLSearchParams = new URLSearchParams();
        params.set("purchaseOrderDateAtInitial", criteria.purchaseOrderDateAtInitial);
        params.set("purchaseOrderDateAtEnd", criteria.purchaseOrderDateAtEnd);
        params.set("pageNumber", criteria.pageNumber+"");
        params.set("pageSize", criteria.pageSize+"");
        if (criteria.supplierNumber) {
//          params.set("supplierNumber", criteria.supplierNumber + "");
        }
        if (criteria.status) {
//          params.set("status", criteria.status + "");
        }

        return this.execute<ReceptionAxios>(`${this.DEFAULT_ROUTE}?${params.toString()}`, "get", criteria);
    };
    

    public async getByUuid(uuid: string): Promise<Order> {
        return this.execute<Order>(`${this.DEFAULT_ROUTE}/${uuid}`, "get");
    };

    public async getReceptionByUuid(uuid: string): Promise<ReceptionAxiosSingle> {
        const params: URLSearchParams = new URLSearchParams();
        params.set("uuid", uuid);
        return this.execute<ReceptionAxiosSingle>(`${this.DEFAULT_ROUTE}/reception/${uuid}`, "get", params);
    };

    public async updateReceptionStatus(uuid: string, order: Partial<Reception>): Promise<Reception> {
        return this.execute<Reception>(`${this.DEFAULT_ROUTE}/reception/${uuid}`, "patch", order);
    }

    public async updateByUuid(uuid: string, order: Order): Promise<Order> {
        return this.execute<Order>(`${this.DEFAULT_ROUTE}/${uuid}`, "patch", order);
    }

}