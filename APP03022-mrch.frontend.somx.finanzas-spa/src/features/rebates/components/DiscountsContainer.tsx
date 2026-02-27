import { useState, useEffect } from 'react';
import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';
import FiltersBar from './parts/FiltersBar';
import ResultsTable from './parts/ResultsTable';
import { rebatesService } from '../api/rebatesService';
import { DiscountRecord } from '../interfaces';

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

    // ⬇️ Cargar “todos” al montar
    useEffect(() => {
        handleSearch({}); // sin filtros = traer todo
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return (
        <div className="w-full px-8 py-6">
            <Breadcrumb
                items={[
                    { label: 'Inicio', to: '/' },
                    { label: 'Finanzas', to: '/finanzas' },
                    { label: 'Descuentos comerciales' },
                ]}
            />

            <h1 className="text-2xl font-semibold mt-6 mb-2">Listado de acuerdos comerciales</h1>
            <p className="text-gray-600 mb-6">Buscar descuentos comerciales aplicados</p>

            <FiltersBar onSearch={handleSearch} />

            <div className="mt-6">
                <ResultsTable rows={data} loading={loading} />
            </div>
        </div>
    );
}
