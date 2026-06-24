/** Body POST /requests — el requester se resuelve desde el Bearer token (sin email en body). */
export interface CreateClarificationRequestBody {
    company: string;
    rut: string;
    businessUnit: number;
    country: number;
    module: number;
    reason: number;
    detail: number;
    clazz: number;
    description: string;
    nombreProveedor: string;
    orderId: string;
}
