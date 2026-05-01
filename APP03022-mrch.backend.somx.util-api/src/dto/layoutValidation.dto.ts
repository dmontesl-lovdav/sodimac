export interface LayoutValidationError {
    row: number;
    cell: string;
    column: string;
    message: string;
}

export interface LayoutValidationResponse {
    isValid: boolean;
    errorCount: number;
    errors: LayoutValidationError[];
    reportAvailable: boolean;
    reportId?: string | null;
    rowsProcessed: number;
}

