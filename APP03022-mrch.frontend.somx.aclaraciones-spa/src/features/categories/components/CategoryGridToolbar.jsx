// src/features/categories/components/CategoryGridToolbar.jsx
import { GenericSelect } from '@shared/components/ui';
import './styles/CategoryGridToolbar.css';

export default function CategoryGridToolbar({
    search,
    onSearchInput,
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
                    defaultValue={search}
                    onChange={onSearchInput}
                    placeholder="Buscar por palabra clave"
                    className="cgt-input"
                />
            </div>
        </div>
    );
}
