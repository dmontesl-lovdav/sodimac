import { useState } from 'react';
import { GenericButton } from '@shared/components/ui';
import DemoCard from '../components/DemoCard';

export default function ButtonsDemo() {
    const [disabled, setDisabled] = useState(false);

    return (
        <DemoCard
            title="Buttons"
            desc="Variantes y estados del botón genérico."
            right={
                <label className="inline-flex items-center gap-2 text-sm">
                    <input
                        type="checkbox"
                        checked={disabled}
                        onChange={(e) => setDisabled(e.target.checked)}
                    />
                    Disabled
                </label>
            }
        >
            <div className="flex flex-wrap items-center gap-3">
                <GenericButton variant="primary" disabled={disabled}>Primary</GenericButton>
                <GenericButton variant="outline" disabled={disabled}>Outline</GenericButton>
                <GenericButton variant="outlineFill" disabled={disabled}>OutlineFill</GenericButton>
                <GenericButton variant="link" disabled={disabled}>Link</GenericButton>
            </div>
        </DemoCard>
    );
}
