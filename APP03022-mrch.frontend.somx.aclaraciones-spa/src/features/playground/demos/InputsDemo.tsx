import { useState } from 'react';
import { GenericInput } from '@shared/components/ui';
import DemoCard from '../components/DemoCard';

export default function InputsDemo() {
    const [name, setName] = useState('');

    return (
        <DemoCard
            title="Inputs"
            desc="Campo de texto con etiqueta flotante, contador y estados."
        >
            <div className="grid gap-5 max-w-md">
                <GenericInput
                    name="username"
                    label="Your name"
                    value={name}
                    onChange={(e: any) => setName(e.target.value)}
                    placeholder=" "
                    required
                    maxLength={60}
                />
                <div className="text-sm text-slate-600">
                    Value: <b>{name || '—'}</b>
                </div>

                <GenericInput
                    name="email"
                    type="email"
                    label="Email"
                    value={name.includes('@') ? name : ''}
                    onChange={() => { }}
                    placeholder=" "
                    disabled
                    maxLength={254}
                />
            </div>
        </DemoCard>
    );
}
