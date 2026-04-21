import { useState, useEffect } from 'react';
import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';
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
    const [filters, setFilters] = useState<any>({});
    const [data, setData] = useState<DiscountRecord[]>([]);
    const [loading, setLoading] = useState(false);

    const handleSearch = async (values: any) => {
        setLoading(true);
        try {
            const result = await rebatesService.searchDiscounts(values);
            setData(result.items);
            setFilters(values);
        } catch (err) {
            console.error('Error loading discounts', err);
            setData([]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        handleSearch({});
    }, []);

    return (
        <div style={styles.container}>
            <Breadcrumb
                items={[
                    { label: 'Inicio', to: '/' },
                    { label: 'Finanzas', to: '/finanzas' },
                    { label: 'Descuentos comerciales' },
                ]}
            />

            <h1 style={styles.title}>Listado de acuerdos comerciales</h1>
            <p style={styles.subtitle}>Buscar descuentos comerciales aplicados</p>

            <FiltersBar onSearch={handleSearch} />

            <div style={styles.tableWrapper}>
                <ResultsTable rows={data} loading={loading} />
            </div>
        </div>
    );
}
