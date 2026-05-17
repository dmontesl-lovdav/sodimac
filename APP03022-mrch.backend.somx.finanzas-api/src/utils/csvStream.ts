import { Readable } from 'stream';
import type { Rebate } from '@/entities/Rebate.entity.js';

/**
 * Creates a readable stream that generates CSV data from rebates
 * This is more memory-efficient than loading all data at once
 */
export class RebateCsvStream extends Readable {
    private rebates: Rebate[];
    private currentIndex: number;
    private headerSent: boolean;

    constructor(rebates: Rebate[]) {
        super();
        this.rebates = rebates;
        this.currentIndex = 0;
        this.headerSent = false;
    }

    _read() {
        // Send header first
        if (!this.headerSent) {
            const headers = [
                'Rebate ID',
                'Document Number',
                'Document Reference',
                'SAP Document',
                'Supplier Number',
                'Amount',
                'Origin ID',
                'Period ID',
                'Due Date',
                'Posting Date',
                'Status',
                'Created By',
                'Created At',
                'Updated By',
                'Updated At',
                'Stamped Rebate UUID',
                'Stamped Document Number',
                'Stamped Reference Number',
                'Stamped Status'
            ];
            this.push(headers.join(',') + '\n');
            this.headerSent = true;
            return;
        }

        // Send rows one by one
        if (this.currentIndex < this.rebates.length) {
            const rebate = this.rebates[this.currentIndex];
            if (!rebate) {
                this.push(null);
                return;
            }
            const row = [
                rebate.rebateId,
                rebate.documentNumber,
                rebate.referenceNumber || '',
                rebate.sapDocument || '',
                rebate.vendorNumber,
                rebate.amount,
                rebate.source,
                rebate.periodId,
                rebate.dueDate?.toISOString() || '',
                rebate.postingDate?.toISOString() || '',
                rebate.status,
                rebate.createdBy || '',
                rebate.createdAt?.toISOString() || '',
                rebate.updatedBy || '',
                rebate.updatedAt?.toISOString() || '',
                rebate.stampedRebate?.stampedRebateUuid || '',
                rebate.stampedRebate?.documentNumber || '',
                rebate.stampedRebate?.referenceNumber || '',
                rebate.stampedRebate?.status || ''
            ];

            const csvLine = row.map(cell => `"${cell}"`).join(',') + '\n';
            this.push(csvLine);
            this.currentIndex++;
        } else {
            // No more data, signal end of stream
            this.push(null);
        }
    }
}

/**
 * Creates a readable stream from an async generator/iterator
 * Useful for database cursor-based pagination
 */
export async function* streamRebatesFromQuery(
    fetchFn: () => Promise<Rebate[]>
): AsyncGenerator<string, void, unknown> {
    const headers = [
        'Rebate ID',
        'Document Number',
        'Document Reference',
        'SAP Document',
        'Supplier Number',
        'Amount',
        'Origin ID',
        'Period ID',
        'Due Date',
        'Posting Date',
        'Status',
        'Created By',
        'Created At',
        'Updated By',
        'Updated At',
        'Stamped Rebate UUID',
        'Stamped Document Number',
        'Stamped Reference Number',
        'Stamped Status'
    ];

    // Yield header
    yield headers.join(',') + '\n';

    // Fetch and yield data
    const rebates = await fetchFn();

    for (const r of rebates) {
        const row = [
            r.rebateId,
            r.documentNumber,
            r.referenceNumber || '',
            r.sapDocument || '',
            r.vendorNumber,
            r.amount,
            r.source,
            r.periodId,
            r.dueDate?.toISOString() || '',
            r.postingDate?.toISOString() || '',
            r.status,
            r.createdBy || '',
            r.createdAt?.toISOString() || '',
            r.updatedBy || '',
            r.updatedAt?.toISOString() || '',
            r.stampedRebate?.stampedRebateUuid || '',
            r.stampedRebate?.documentNumber || '',
            r.stampedRebate?.referenceNumber || '',
            r.stampedRebate?.status || ''
        ];

        yield row.map(cell => `"${cell}"`).join(',') + '\n';
    }
}

/**
 * Converts async generator to Node.js Readable stream
 */
export function asyncGeneratorToStream(
    generator: AsyncGenerator<string, void, unknown>
): Readable {
    return Readable.from(generator);
}
