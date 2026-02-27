import { InvoiceRecord } from '../../interfaces';

interface InvoicesActionsProps {
    invoices: InvoiceRecord[];
    isAdmin: boolean;
    messages: Record<string, string>;
    selectedInvoices: Set<string>;
    setError: (msg: string) => void;
}

const styles = {
    container: {
        marginTop: '1rem',
        display: 'flex',
        gap: '0.5rem',
        flexWrap: 'wrap' as const,
    },
    button: {
        height: '2.75rem',
        padding: '0 1.5rem',
        borderRadius: '0.375rem',
        fontSize: '0.875rem',
        fontWeight: 500,
        border: '1px solid #9ca3af',
        backgroundColor: '#ffffff',
        cursor: 'pointer',
        transition: 'background-color 150ms',
    },
};

export default function InvoicesActions({
    invoices,
    isAdmin,
    messages,
    selectedInvoices,
    setError,
}: InvoicesActionsProps) {
    const noSelectionMsg = messages['INF6001'] || 'No hay información seleccionada.';

    const validateSelection = () => {
        if (selectedInvoices.size === 0) {
            setError(noSelectionMsg);
            return false;
        }
        return true;
    };

    const handleHover = (e: React.MouseEvent<HTMLButtonElement>, enter: boolean) => {
        e.currentTarget.style.backgroundColor = enter ? '#f9fafb' : '#ffffff';
    };

    return (
        <div style={styles.container}>
            <button
                style={styles.button}
                onClick={() => validateSelection()}
                onMouseEnter={(e) => handleHover(e, true)}
                onMouseLeave={(e) => handleHover(e, false)}
            >
                Descargar reporte XLSX
            </button>
            <button
                style={styles.button}
                onClick={() => validateSelection()}
                onMouseEnter={(e) => handleHover(e, true)}
                onMouseLeave={(e) => handleHover(e, false)}
            >
                Descargar XML (masivo)
            </button>
            <button
                style={styles.button}
                onClick={() => validateSelection()}
                onMouseEnter={(e) => handleHover(e, true)}
                onMouseLeave={(e) => handleHover(e, false)}
            >
                Descargar PDF (masivo)
            </button>
            {isAdmin && (
                <button
                    style={styles.button}
                    onClick={() => validateSelection()}
                    onMouseEnter={(e) => handleHover(e, true)}
                    onMouseLeave={(e) => handleHover(e, false)}
                >
                    Cancelar factura (masivo)
                </button>
            )}
        </div>
    );
}
