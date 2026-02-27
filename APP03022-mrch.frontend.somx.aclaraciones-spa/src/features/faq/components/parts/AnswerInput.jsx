// src/features/faq/components/parts/AnswerInput.jsx
import { GenericInput } from '@shared/components/ui';

export default function AnswerInput({ value, onChange }) {
    return (
        <GenericInput
            name="answer"
            type="text"
            label="Detalla la respuesta"
            placeholder="Describe la respuesta…"
            value={value}
            onChange={(e) => onChange?.(e.target.value)}
            required
            maxLength={1024}
        />
    );
}
