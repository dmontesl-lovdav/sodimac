import { useState, useEffect } from 'react';
import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';
import { GenericModal } from '@shared/components/ui';
import { withFinanceBreadcrumb } from '@shared/components/ui/navigation/financeBreadcrumb';
import { useFinanceAlertModal } from '@/shared/hooks/useFinanceAlertModal';
import FiltersBar from './parts/FiltersBar';
import ResultsTable from './parts/ResultsTable';
import { rebatesService } from '../api/rebatesService';
import { DiscountRecord } from '../interfaces';

const styles = {
    container: {
        width: '100%',
        padding: '1.5rem 2rem',
    },
    title: {
        fontSize: '1.5rem',
        fontWeight: 600,
        marginTop: '1.5rem',
        marginBottom: '0.5rem',
    },
    subtitle: {
        color: '#4b5563',
        marginBottom: '1.5rem',
    },
    tableWrapper: {
        marginTop: '1.5rem',
    },
};

export default function DiscountsContainer() {
    const financeAlert = useFinanceAlertModal();
    const [data, setData] = useState<DiscountRecord[]>([]);
    const [loading, setLoading] = useState(false);

    const handleSearch = async (values: any) => {
        setLoading(true);
        try {
            const result = await rebatesService.searchDiscounts(values);
            setData(result.items);

            if (result.items.length === 0) {
                financeAlert.showWarning(
                    'Sin registros',
                    'No se encontraron descuentos con los criterios indicados.'
                );
            }
        } catch (err) {
            financeAlert.showErrorFrom(
                'Error',
                err,
                'No fue posible cargar los descuentos comerciales. Intenta nuevamente.'
            );
            setData([]);
        } finally {
            setLoading(false);
        }
    };

    const handleClear = () => {
        setData([]);
    };

    return (
        <div style={styles.container}>
            <Breadcrumb
                items={withFinanceBreadcrumb([{ label: 'Descuentos comerciales' }])}
            />

            <h1 style={styles.title}>Listado de acuerdos comerciales</h1>
            <p style={styles.subtitle}>Buscar descuentos comerciales aplicados</p>

            <FiltersBar onSearch={handleSearch} onClear={handleClear} />

            <div style={styles.tableWrapper}>
                <ResultsTable rows={data} loading={loading} />
            </div>

            {loading && (
                <GenericModal visible variant="loading" message="Cargando…" />
            )}

            <GenericModal
                visible={financeAlert.alertVisible}
                variant="alert"
                severity={financeAlert.alertSeverity}
                title={financeAlert.alertTitle}
                message={financeAlert.alertMessage}
                buttonText="Aceptar"
                onClose={financeAlert.closeAlert}
            />
        </div>
    );
}
