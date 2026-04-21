import { GenericSelectSearchable, GenericButton } from '@shared/components/ui';
import '../styles/RelatedInformationGridToolbar.css';

export default function RelatedInformationGridToolbar({
    relatedInfoId,
    relatedInfoIds,
    onRelatedInfoIdChange,
    onSearchInput,
    onReset,
}) {
    const handleSearch = () => {
        onSearchInput({
            id: relatedInfoId ? Number(relatedInfoId) : undefined,
        });
    };

    return (
        <div className="ri-toolbar">
            <GenericSelectSearchable
                value={relatedInfoId ?? ''}
                onChange={(e) => onRelatedInfoIdChange(e.target.value)}
                options={relatedInfoIds}
                placeholder="Buscar por título…"
                widthClass="ri-select-id"
            />

            <GenericButton variant="outline" onClick={handleSearch}>
                Buscar
            </GenericButton>

            <GenericButton variant="outline" onClick={onReset}>
                Reset
            </GenericButton>
        </div>
    );
}
