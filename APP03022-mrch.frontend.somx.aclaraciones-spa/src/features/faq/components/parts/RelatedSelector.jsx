import { GenericButton } from '@shared/components/ui';
import GenericSelectFloating from '@shared/components/ui/select/GenericSelectFloating';
import ChipList from './ChipList';

import './styles/RelatedSelector.css';

const asStr = (v) => (v == null ? '' : String(v).trim());

export default function RelatedSelector({
    categories = [],
    value,
    onChange,
    relatedList = [],
    onAdd,
    onRemove,
}) {
    const effectiveValue = value || relatedList[0] || '';
    const selected = asStr(effectiveValue);

    const isValid = !!categories.find((c) => c.value === selected);
    const canAdd = isValid && selected !== '' && !relatedList.includes(selected);

    const handleAdd = () => {
        if (!canAdd) return;
        onAdd?.(selected);
        onChange?.('');
    };

    return (
        <div className="related-selector">
            <div className="related-row">
                <div className="related-select">
                    <GenericSelectFloating
                        label="Relacionada"
                        value={effectiveValue}
                        onChange={(e) => onChange?.(e.target.value)}
                        options={categories}
                        fullWidth
                        placeholder="Selecciona…"
                    />
                </div>

                <GenericButton
                    variant="link"
                    className={`related-add-btn ${!canAdd ? 'rs-disabled' : ''}`}
                    type="button"
                    onClick={handleAdd}
                >
                    <span className="rs-plus">＋</span>
                    Agregar
                </GenericButton>
            </div>

            <ChipList
                items={relatedList}
                renderLabel={(r) =>
                    categories.find((c) => c.value === r)?.label || r
                }
                onRemove={(r) => onRemove?.(r)}
            />
        </div>
    );
}
