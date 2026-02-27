import { useMemo, useState } from 'react';
import { GenericSelect } from '@shared/components/ui';
import DemoCard from '../components/DemoCard';

type Option = { value: string; label: string };

export default function SelectBasicDemo() {
    const [val, setVal] = useState<string>('');
    const options: Option[] = useMemo(
        () => [
            { value: 'mx', label: 'México' },
            { value: 'cl', label: 'Chile' },
            { value: 'pe', label: 'Perú' },
            { value: 'co', label: 'Colombia' },
        ],
        []
    );

    return (
        <DemoCard
            title="GenericSelect"
            desc="Select nativo estilizado con placeholder y caret."
        >
            <div className="grid gap-4 max-w-md">
                <GenericSelect
                    value={val}
                    onChange={(e: any) => setVal(e.target.value)}
                    options={options as any}
                    placeholder="Selecciona un país…"
                    widthClass="w-64"
                />
                <div className="text-sm text-slate-600">
                    Valor: <b>{val || '—'}</b>
                </div>
            </div>
        </DemoCard>
    );
}
