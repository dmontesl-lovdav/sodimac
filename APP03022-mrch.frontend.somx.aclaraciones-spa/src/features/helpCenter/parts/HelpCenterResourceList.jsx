import '../styles/HelpCenterResourceList.css';

export default function HelpCenterResourceList({
    resources,
    selected,
    loadingRes,
    expandedIds,
    toggleExpand,
    onDownload,
    nav,
}) {
    return (
        <section className="hcrl-section">
            <div className="hcrl-header">
                <h2 className="hcrl-title">{selected?.name || '…'}</h2>

                {selected?.description && (
                    <p className="hcrl-subtitle">{selected.description}</p>
                )}
            </div>

            {loadingRes ? (
                <div className="hcrl-loading">Cargando preguntas frecuentes…</div>
            ) : resources.length === 0 ? (
                <div className="hcrl-loading">No hay preguntas frecuentes en esta categoría.</div>
            ) : (
                <ul className="hcrl-list">
                    {resources.map((r) => {
                        const open = expandedIds.has(r.id);

                        return (
                            <li key={r.id} className="hcrl-item">
                                <button
                                    className="hcrl-item-btn"
                                    onClick={() => toggleExpand(r.id)}
                                    aria-expanded={open}
                                >
                                    <div className="hcrl-item-info">
                                        <svg viewBox="0 0 24 24" fill="currentColor" className="hcrl-file-icon">
                                            <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z" />
                                            <path d="M14 2v6h6" className="hcrl-file-overlay" />
                                        </svg>

                                        <span className="hcrl-item-title">{r.title}</span>
                                    </div>

                                    <svg
                                        viewBox="0 0 20 20"
                                        fill="currentColor"
                                        className={`hcrl-chevron ${open ? 'hcrl-chevron-open' : ''}`}
                                    >
                                        <path
                                            fillRule="evenodd"
                                            d="M5.23 7.21a.75.75 0 011.06.02L10 10.94l3.71-3.71a.75.75 0 111.08 1.04l-4.24 4.24a.75.75 0 01-1.08 0L5.21 8.27a.75.75 0 01.02-1.06z"
                                            clipRule="evenodd"
                                        />
                                    </svg>
                                </button>

                                {open && (
                                    <div className="hcrl-content">
                                        {r.description && (
                                            <p className="hcrl-description">{r.description}</p>
                                        )}

                                        {r.attachments?.length > 0 ? (
                                            <div className="hcrl-attachments">
                                                {r.attachments.map((att) => (
                                                    <div key={att.id} className="hcrl-attachment-item">
                                                        <div className="hcrl-attachment-info">
                                                            <p className="hcrl-attachment-title">
                                                                {att.fileName || 'Archivo PDF'}
                                                            </p>
                                                            <p className="hcrl-attachment-meta">
                                                                PDF {att.sizeKb ? `• ${att.sizeKb} KB` : ''}
                                                            </p>
                                                        </div>

                                                        <button
                                                            className="hcrl-download"
                                                            onClick={() => onDownload(att)}
                                                        >
                                                            Descargar
                                                        </button>
                                                    </div>
                                                ))}
                                            </div>
                                        ) : (
                                            <p className="hcrl-no-files">No hay archivos PDF adjuntos.</p>
                                        )}
                                    </div>
                                )}
                            </li>
                        );
                    })}
                </ul>
            )}

            <div className="hcrl-footer">
                <button onClick={() => nav(-1)} className="hcrl-back-btn">
                    Volver
                </button>
            </div>
        </section>
    );
}
