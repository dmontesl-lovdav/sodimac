import { formatDateOnly } from '../utils/auditLogsTrain.utils';

type Header = {
    idTren: string;
    idUsuario: string;
    modulo: string;
    aplicativo: string;
    accion: string;
    proceso: string;
    fechaRegistro: string | null;
    estadoFinal: { label: string; kind: 'success' | 'error' | 'info' };
};

const pillClassByKind = (kind: 'success' | 'error' | 'info'): string => {
    if (kind === 'success') return 'alt-pill alt-pill-success';
    if (kind === 'error') return 'alt-pill alt-pill-error';
    return 'alt-pill alt-pill-info';
};

export default function TrainHeaderCard({ header }: Readonly<{ header: Header | null }>) {
    return (
        <div className="alt-section">
            <div className="alt-sectionTitle">Detalle de Actividades</div>

            <div className="alt-headerCard">
                {header ? (
                    <div className="alt-headerGrid alt-headerGrid-fbc">
                        <div className="alt-headerItem alt-headerItem-fbc">
                            <div className="alt-label">ID Tren Actividades</div>
                            <div className="alt-value">{header.idTren}</div>
                        </div>

                        <div className="alt-headerItem alt-headerItem-fbc">
                            <div className="alt-label">ID Usuario</div>
                            <div className="alt-value">{header.idUsuario}</div>
                        </div>

                        <div className="alt-headerItem alt-headerItem-fbc">
                            <div className="alt-label">Módulo</div>
                            <div className="alt-value">{header.modulo}</div>
                        </div>

                        <div className="alt-headerItem alt-headerItem-fbc">
                            <div className="alt-label">Aplicativo</div>
                            <div className="alt-value">{header.aplicativo}</div>
                        </div>

                        <div className="alt-headerItem alt-headerItem-fbc">
                            <div className="alt-label">Acción</div>
                            <div className="alt-value">{header.accion}</div>
                        </div>

                        <div className="alt-headerItem alt-headerItem-fbc">
                            <div className="alt-label">Proceso</div>
                            <div className="alt-value">{header.proceso}</div>
                        </div>

                        <div className="alt-headerItem alt-headerItem-fbc">
                            <div className="alt-label">Fecha Registro</div>
                            <div className="alt-value">
                                {formatDateOnly(header.fechaRegistro)}
                            </div>
                        </div>

                        <div className="alt-headerItem alt-headerItem-fbc alt-headerItemLast">
                            <div className="alt-label">Estado Final</div>
                            <div className="alt-value">
                                <span className={pillClassByKind(header.estadoFinal.kind)}>
                                    {header.estadoFinal.label}
                                </span>
                            </div>
                        </div>
                    </div>
                ) : (
                    <div className="alt-muted">Sin datos para este trace_id.</div>
                )}
            </div>
        </div>
    );
}