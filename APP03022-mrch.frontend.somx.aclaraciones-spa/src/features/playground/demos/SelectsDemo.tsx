import { useMemo, useState } from 'react';
import { GenericSelectFloating } from '@shared/components/ui';
import DemoCard from '../components/DemoCard';

type Option = { value: string; label: string };

export default function SelectsDemo() {
    const [country, setCountry] = useState<string>('');
    const options: Option[] = useMemo(
        () => [
            { value: 'mx', label: 'México' },
            { value: 'cl', label: 'Chile' },
            { value: 'pe', label: 'Perú' },
            { value: 'co', label: 'Colombia' },
        ],
        []
    );
    const handleSelectChange = (evt: any) => setCountry(evt?.target?.value ?? '');

    return (
        <DemoCard
            title="Selects"
            desc="Select con etiqueta flotante y navegación por teclado."
        >
            <div className="grid gap-5 max-w-md">
                <GenericSelectFloating
                    label="Country"
                    value={country}
                    onChange={handleSelectChange}
                    onValueChange={() => { }}
                    options={options as any}
                    name="country"
                    position={undefined as any}
                    refreshDetails={undefined as any}
                />
                <div className="text-sm text-slate-600">
                    Value: <b>{country || '—'}</b>
                </div>
                <GenericSelectFloating
                    label="Disabled"
                    value=""
                    onChange={() => { }}
                    onValueChange={() => { }}
                    options={options as any}
                    name="disabledSelect"
                    position={undefined as any}
                    refreshDetails={undefined as any}
                    disabled
                />
            </div>
        </DemoCard>
    );
}
