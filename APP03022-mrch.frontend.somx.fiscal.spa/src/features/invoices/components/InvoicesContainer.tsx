import { GenericModal } from '@shared/components/ui';
import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';
import FiltersBar from './parts/FiltersBar';
import ResultsTable from './parts/ResultsTable';
import InvoicesActions from './parts/InvoicesActions';
import { useInvoices } from './hooks/useInvoices';

const styles = {
    container: {
        width: '100%',
        padding: '1.5rem 2rem',
    },
    header: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        marginTop: '1.5rem',
        marginBottom: '1.5rem',
    },
    title: {
        fontSize: '1.125rem',
        fontWeight: 600,
        color: '#111827',
    },
    errorAlert: {
        marginTop: '1rem',
        backgroundColor: '#fefce8',
        border: '1px solid #fde047',
        color: '#854d0e',
        padding: '0.75rem 1rem',
        borderRadius: '0.375rem',
    },
    successAlert: {
        marginTop: '1rem',
        backgroundColor: '#f0fdf4',
        border: '1px solid #86efac',
        color: '#166534',
        padding: '0.75rem 1rem',
        borderRadius: '0.375rem',
    },
    tableWrapper: {
        marginTop: '1.5rem',
    },
    emptyState: {
        marginTop: '1.5rem',
        border: '1px solid #e5e7eb',
        borderRadius: '0.5rem',
        padding: '2rem',
        textAlign: 'center' as const,
        color: '#6b7280',
    },
    emptyText: {
        fontSize: '0.875rem',
    },
};

export default function InvoicesContainer() {
    const isAdmin = true;
    const {
        invoices,
        loading,
        searchApplied,
        error,
        success,
        messages,
        page,
        perPage,
        totalPages,
        totalItems,
        selectedInvoices,
        handleSearch,
        handlePageChange,
        handlePerPageChange,
        handleSelectInvoice,
        handleSelectAll,
        handleDownloadXML,
        handleDownloadPDF,
        handleViewCreditNotes,
        handleCancelInvoice,
        setError,
    } = useInvoices(isAdmin);

    return (
        <div style={styles.container}>
            <Breadcrumb items={[{ label: 'Home', to: '/' }, { label: 'Fiscal', to: '/' }, { label: 'Facturas' }]} />

            <div style={styles.header}>
                <h1 style={styles.title}>Listado de facturas</h1>
            </div>

            <FiltersBar onSearch={handleSearch} isAdmin={isAdmin} messages={messages} />

            {error && <div style={styles.errorAlert}>{error}</div>}
            {success && <div style={styles.successAlert}>{success}</div>}

            {searchApplied && invoices.length > 0 && (
                <InvoicesActions
                    invoices={invoices}
                    isAdmin={isAdmin}
                    messages={messages}
                    selectedInvoices={selectedInvoices}
                    setError={setError}
                />
            )}

            {searchApplied && (
                <div style={styles.tableWrapper}>
                    <ResultsTable
                        rows={invoices}
                        loading={loading}
                        isAdmin={isAdmin}
                        page={page}
                        perPage={perPage}
                        totalPages={totalPages}
                        totalItems={totalItems}
                        onPageChange={handlePageChange}
                        onPerPageChange={handlePerPageChange}
                        onDownloadXML={handleDownloadXML}
                        onDownloadPDF={handleDownloadPDF}
                        onViewCreditNotes={handleViewCreditNotes}
                        onCancelInvoice={isAdmin ? handleCancelInvoice : undefined}
                        selectedInvoices={selectedInvoices}
                        onSelectInvoice={handleSelectInvoice}
                        onSelectAll={handleSelectAll}
                    />
                </div>
            )}

            {!searchApplied && !loading && (
                <div style={styles.emptyState}>
                    <p style={styles.emptyText}>No hay resultados para mostrar.</p>
                    <p style={styles.emptyText}>Por favor, ingrese criterios de búsqueda.</p>
                </div>
            )}

            <GenericModal visible={loading} variant="loading" message="Cargando información, por favor espera..." />
        </div>
    );
}
