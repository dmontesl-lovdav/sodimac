import { useMemo } from 'react';
import { Breadcrumb } from '@shared/components/ui';
import DemoCard from '../components/DemoCard';

export default function BreadcrumbDemo() {
    const items = useMemo(
        () => [
            { label: 'Inicio', to: '/' },
            { label: 'Centro de ayuda', to: '/' },
            { label: 'Buttons' },
        ],
        []
    );

    return (
        <DemoCard
            title="Breadcrumb"
            desc="Navegación jerárquica simple con elemento activo al final."
        >
            <div className="max-w-xl">
                <Breadcrumb items={items as any} />
            </div>
        </DemoCard>
    );
}
