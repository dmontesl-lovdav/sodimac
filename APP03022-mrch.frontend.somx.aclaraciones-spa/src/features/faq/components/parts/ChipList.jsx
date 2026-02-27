import './styles/ChipList.css';

export default function ChipList({ items, renderLabel, onRemove }) {
    if (!items?.length) return null;

    const uniqItems = Array.from(new Set(items));

    return (
        <div className="chiplist">
            {uniqItems.map((v) => (
                <span key={v} className="chip">
                    {renderLabel ? renderLabel(v) : v}
                    <button
                        onClick={() => onRemove?.(v)}
                        className="chip-remove"
                    >
                        ×
                    </button>
                </span>
            ))}
        </div>
    );
}
