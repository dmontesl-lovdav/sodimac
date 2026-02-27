import { GenericSelect, GenericSelectSearchable, GenericButton } from '@shared/components/ui';
import '../styles/RelatedInformationGridToolbar.css';

export default function RelatedInformationGridToolbar({
    relatedInfoId,
    relatedInfoIds,
    onRelatedInfoIdChange,
    bizUnit,
    bizUnits,
    onBizUnitChange,
    country,
    countries,
    onCountryChange,
    onSearchInput,
    onReset,
}) {
    const handleSearch = () => {
        onSearchInput({
            id: relatedInfoId ? Number(relatedInfoId) : undefined,
            businessUnit: !relatedInfoId && bizUnit ? Number(bizUnit) : undefined,
            country: !relatedInfoId && country ? Number(country) : undefined,
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

            <GenericSelect
                value={bizUnit ?? ''}
                onChange={(e) => onBizUnitChange(e.target.value)}
                options={bizUnits}
                placeholder="Unidad de negocio"
                widthClass="ri-select-bu"
            />

            <GenericSelect
                value={country ?? ''}
                onChange={(e) => onCountryChange(e.target.value)}
                options={countries}
                placeholder="País"
                widthClass="ri-select-country"
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
