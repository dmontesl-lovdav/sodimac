import { GenericSelect } from '@shared/components/ui';
import '../styles/FaqGridContainer.css';

export default function FaqGridToolbar({
    category,
    categories,
    onCategoryChange,
    search,
    onSearchInput,
}) {
    return (
        <div className="faq-toolbar">
            <GenericSelect
                value={category}
                onChange={(e) => onCategoryChange(e.target.value)}
                options={categories}
                placeholder="Categoría"
                widthClass="faq-toolbar-select"
            />

            {/* Si después quieres activar búsqueda, dejamos el placeholder */}
            {/* 
            <div className="faq-toolbar-search-wrapper">
                <input
                    defaultValue={search}
                    onChange={onSearchInput}
                    placeholder="Buscar por palabra clave"
                    className="faq-toolbar-search"
                />
            </div>
            */}
        </div>
    );
}
