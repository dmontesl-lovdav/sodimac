import { useState } from 'react';
import { GenericDateRangePicker } from '@shared/components/ui';
import DemoCard from '../components/DemoCard';

type Range = [Date | null, Date | null];

export default function DateRangeDemo() {
    const [range, setRange] = useState<Range>([null, null]);

    return (
        <DemoCard
            title="DateRangePicker"
            desc="Selector de rango de fechas con input custom y portal."
        >
            <div className="grid gap-4 max-w-md">
                <GenericDateRangePicker
                    value={range as any}
                    onChange={(v: any) => setRange(v as Range)}
                    placeholder="Fecha desde – hasta"
                />
                <div className="text-sm text-slate-600">
                    Valor: <b>{range[0]?.toLocaleDateString?.() || '—'} — {range[1]?.toLocaleDateString?.() || '—'}</b>
                </div>
            </div>
        </DemoCard>
    );
}
