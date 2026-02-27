import '../styles/HelpCenterHeader.css';

export default function HelpCenterHeader({ showNote, setShowNote }) {
    return (
        <header className="hch-header">
            <div className="hch-box">
                <h1 className="hch-title">Manuales y tutoriales</h1>

                <p className="hch-description">
                    Aquí encontrarás <strong className="hch-strong">manuales detallados</strong> y{' '}
                    <strong className="hch-strong">videos</strong> que te guiarán paso a paso en el
                    uso de nuestras herramientas y servicios.
                </p>

                {showNote && (
                    <div className="hch-note">
                        <div className="hch-note-icon">
                            <svg viewBox="0 0 24 24" fill="none" className="hch-note-svg">
                                <path d="M12 9v4m0 4h.01" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
                                <path
                                    d="M10.29 3.86l-8.08 14.02A1.5 1.5 0 003.5 20h17a1.5 1.5 0 001.29-2.12L13.71 3.86a1.5 1.5 0 00-2.42 0z"
                                    stroke="currentColor"
                                    strokeWidth="2"
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                />
                            </svg>
                        </div>

                        <p className="hch-note-text">
                            <span className="hch-note-strong">Nota:&nbsp;</span>
                            Selecciona una categoría del panel izquierdo para ver el material disponible y descargar archivos.
                        </p>

                        <button
                            aria-label="Cerrar aviso"
                            className="hch-note-close"
                            onClick={() => setShowNote(false)}
                        >
                            ×
                        </button>
                    </div>
                )}
            </div>
        </header>
    );
}
