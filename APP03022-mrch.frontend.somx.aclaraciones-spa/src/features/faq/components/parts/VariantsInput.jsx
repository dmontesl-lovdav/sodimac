import { GenericButton, GenericInput } from '@shared/components/ui';
import ChipList from './ChipList';

import './styles/VariantsInput.css';

export default function VariantsInput({
    value,
    onChange,
    questions = [],
    onAdd,
    onRemove,
}) {
    const val = (value ?? '').trim();
    const existing = (questions[0] ?? '').trim();
    const isSame = val && existing && val.toLowerCase() === existing.toLowerCase();

    const canAdd = Boolean(val) && !isSame && questions.length === 0;

    const handleAdd = () => {
        if (!canAdd) return;
        onAdd?.(val);
        onChange?.('');
    };

    const onKeyDown = (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            handleAdd();
        }
    };

    return (
        <div className="variants-wrapper">
            <div className="variants-row">
                <div className="variants-input">
                    <GenericInput
                        name="question"
                        type="text"
                        label="Pregunta"
                        placeholder="Escribe la pregunta…"
                        value={value}
                        onChange={(e) => onChange?.(e.target.value)}
                        onKeyDown={onKeyDown}
                        required
                        maxLength={512}
                    />
                </div>

                <GenericButton
                    variant="link"
                    className={`variants-add-btn ${!canAdd ? 'vi-disabled' : ''}`}
                    type="button"
                    onClick={handleAdd}
                >
                    <span className="vi-plus">＋</span>
                    Agregar pregunta
                </GenericButton>
            </div>

            <ChipList items={questions} onRemove={onRemove} />
        </div>
    );
}
