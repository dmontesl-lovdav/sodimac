import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';
import { DiscountRecord, PagedResult, SearchParams } from '../interfaces';

const client = ConfigurationBuilder.client;

/* 🔹 MOCK temporal (simula consumo de API real) */
const mockRows: DiscountRecord[] = [
    {
        id: 1527,
        vendorNumber: '11158-9',
        vendorName: 'PLASTITRIM, S.A. DE C.V.',
        documentNumber: 'Merma01',
        referenceDocument: 'XXXX',
        rebateType: 'Merma',
        sapDocument: '1010101',
        amount: 100.0,
        periodId: 11,
        periodName: 'Enero',
        status: 'Pendiente',
        applyDate: '01/01/2025',
        expirationDate: '01/02/2025',
    },
    {
        id: 1526,
        vendorNumber: '97117NBA',
        vendorName: 'PLASTITRIM, S.A. DE C.V.',
        documentNumber: 'Cross01',
        referenceDocument: 'YYYY',
        rebateType: 'Cross',
        sapDocument: '133433',
        amount: 50.0,
        periodId: 11,
        periodName: 'Enero',
        status: 'Pendiente',
        applyDate: '01/01/2025',
        expirationDate: '01/02/2025',
    },
];

export const rebatesService = {
    async searchDiscounts(params: SearchParams): Promise<PagedResult<DiscountRecord>> {
        await new Promise((r) => setTimeout(r, 400)); // Simula delay
        return {
            items: mockRows,
            page: 1,
            totalItems: mockRows.length,
            totalPages: 1,
        };
    },
};
