import { useMemo } from 'react';
import { GenericButton } from '@shared/components/ui';
import GenericSelectFloating from '@shared/components/ui/select/GenericSelectFloating';
import ChipList from './ChipList';

import './styles/TopicSelector.css';

const asStr = (v) => (v == null ? '' : String(v).trim());

export default function TopicSelector({
    categories = [],
    value,
    onChange,
    topics = [],
    onAdd,
    onRemove,
}) {
    const handleSelectChange = (e) => onChange?.(asStr(e?.target?.value));

    const isValid = useMemo(
        () => !!categories.find((c) => c.value === value),
        [categories, value]
    );

    const canAdd =
        isValid &&
        value !== '' &&
        !topics.includes(asStr(value));

    const handleAdd = () => {
        if (!canAdd) return;
        onAdd?.(value);
        onChange?.('');
    };

    return (
        <div className="topic-selector">
            <div className="topic-row">
                <div className="topic-select">
                    <GenericSelectFloating
                        label="Categorías (Pregunta frecuente)"
                        value={value ?? ''}
                        onChange={handleSelectChange}
                        options={categories}
                        placeholder="Selecciona…"
                        fullWidth
                    />
                </div>

                <GenericButton
                    variant="link"
                    className={`topic-add-btn ${!canAdd ? 'ts-disabled' : ''}`}
                    type="button"
                    onClick={handleAdd}
                >
                    <span className="ts-plus">＋</span>
                    Agregar tema
                </GenericButton>
            </div>

            <ChipList
                items={topics}
                renderLabel={(t) =>
                    categories.find((c) => c.value === t)?.label || t
                }
                onRemove={onRemove}
            />
        </div>
    );
}
