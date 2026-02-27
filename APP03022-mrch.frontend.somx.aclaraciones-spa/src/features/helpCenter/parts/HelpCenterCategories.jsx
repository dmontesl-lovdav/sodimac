import '../styles/HelpCenterCategories.css';

export default function HelpCenterCategories({
    categories,
    selectedId,
    loadingCats,
    setSelectedId
}) {
    return (
        <aside className="hcc-aside">
            <h3 className="hcc-title">Categorías</h3>

            {loadingCats ? (
                <div className="hcc-loading">Cargando categorías…</div>
            ) : categories.length === 0 ? (
                <div className="hcc-loading">No se encontraron categorías FAQ.</div>
            ) : (
                <ul className="hcc-list">
                    {categories.map((c) => {
                        const active = c.id === Number(selectedId);

                        return (
                            <li key={c.id}>
                                <button
                                    className={active ? 'hcc-btn hcc-btn-active' : 'hcc-btn'}
                                    onClick={(e) => {
                                        e.currentTarget.blur();
                                        setSelectedId(c.id);
                                    }}
                                >
                                    {c.name}
                                </button>
                            </li>
                        );
                    })}
                </ul>
            )}
        </aside>
    );
}
