// src/features/categories/components/CategoryGridToolbar.jsx
import { GenericSelect, GenericButton } from '@shared/components/ui';
import './styles/CategoryGridToolbar.css';

export default function CategoryGridToolbar({
    search,
    onSearchInput,
    onSearchSubmit,
    filterActive,
    onFilterChange,
}) {
    const activeOptions = [
        { value: 'true', label: 'Activos' },
        { value: 'false', label: 'Inactivos' },
        { value: 'all', label: 'Todos' },
    ];

    return (
        <div className="cgt-container">

            {/* SELECT Estado */}
            <div className="cgt-select-wrapper">
                <div className="cgt-select-force">
                    <GenericSelect
                        value={filterActive}
                        onChange={(e) => onFilterChange(e.target.value)}
                        options={activeOptions}
                        placeholder="Estado"
                    />
                </div>
            </div>

            {/* Búsqueda */}
            <div className="cgt-search-wrapper cgt-search-spacing">
                <input
                    value={search}
                    onChange={onSearchInput}
                    placeholder="Buscar por palabra clave"
                    className="cgt-input"
                    style={{ marginRight: '8px' }}
                />
                <GenericButton
                    variant="outline"
                    onClick={onSearchSubmit}
                >
                    Buscar
                </GenericButton>
            </div>
        </div>
    );
}
