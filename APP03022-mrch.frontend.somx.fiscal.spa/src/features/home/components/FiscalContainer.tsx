import { useEffect } from 'react';
import { Breadcrumb } from '@shared/components/ui/navigation';
import { syncFiscalUser } from '@/services/fiscalUserSync';
import FiscalCardsList from './parts/FiscalCardsList';

export default function FiscalContainer(): React.ReactElement {
    useEffect(() => {
        syncFiscalUser();
    }, []);

    return (
        <div className="fiscal-container">
            <Breadcrumb items={[{ label: 'Fiscal' }]} />
            <h3 className="fiscal-section-title">Gestión de documentación fiscal</h3>
            <FiscalCardsList />
        </div>
    );
}