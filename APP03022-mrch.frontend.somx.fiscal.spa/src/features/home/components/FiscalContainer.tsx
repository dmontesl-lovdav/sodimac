import { Breadcrumb } from '@shared/components/ui/navigation';
import FiscalCardsList from './parts/FiscalCardsList';

export default function FiscalContainer(): React.ReactElement {
    return (
        <div className="fiscal-container">
            <Breadcrumb items={[{ label: 'Inicio', to: '/' }, { label: 'Fiscal' }]} />
            <h3 className="fiscal-section-title">Cuéntanos, ¿Qué necesitas?</h3>
            <FiscalCardsList />
        </div>
    );
}