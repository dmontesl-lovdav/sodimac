// @ts-nocheck
import { useMemo, useState, useEffect } from 'react';
import { translateDate, getStatusDescription, getStatusClass } from './RequestUtils.jsx';
import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';
import '../styles/RequestBoard.css';

/** Columnas visibles en el tablero (orden y títulos) */
const BOARD_COLUMNS = [
    { id: 'todo', title: 'Sin atender' },
    { id: 'doing', title: 'En atención' },
    { id: 'done', title: 'Resuelto' },
    { id: 'rejected', title: 'Rechazado' },
];

/** Mapeo de clazz (ID de estado) -> columna */
const CLAZZ_TO_COLUMN = {
    23: 'todo',      // Sin atender
    24: 'doing',     // En atención
    25: 'done',      // Resuelto
    52: 'rejected',  // Rechazado
};

export default function RequestBoard({ requests = [], reasons = [], onShow }) {
    const reasonJSON = JSON.stringify(reasons);

    /* ───────── ROL ───────── */
    const [isAdmin, setIsAdmin] = useState(false);
    const [isProveedor, setIsProveedor] = useState(false);

    useEffect(() => {
        (async () => {
            setIsAdmin(await ConfigurationBuilder.authenticator.isAdmin());
            setIsProveedor(await ConfigurationBuilder.authenticator.isProveedor());
        })();
    }, []);

    /* ───────── GROUP RECORDS ───────── */
    const grouped = useMemo(() => {
        const buckets = { todo: [], doing: [], done: [], rejected: [] };

        (Array.isArray(requests) ? requests : []).forEach((r) => {
            const clazzNum = Number(r?.clazz ?? 23);
            const col = CLAZZ_TO_COLUMN[clazzNum] || 'todo';
            buckets[col].push(r);
        });

        return buckets;
    }, [requests]);

    /* ───────── CARD ───────── */
    const Card = (r) => {
        const creation = r?.creationTime ? new Date(r.creationTime) : new Date();
        const extraDays = Number(r?.elapsedTime) || 0;
        const venc = new Date(creation);
        venc.setDate(venc.getDate() + extraDays);

        let roleBadge = null;
        if (isAdmin) {
            roleBadge = <span className="admin-dot" title="Visible solo para administradores" />;
        } else if (isProveedor) {
            roleBadge = <span className="prov-dot" title="Visible solo para proveedores" />;
        }

        const requesterInitials =
            (r?.requester || '')
                .split('@')[0]
                .slice(0, 2)
                .toUpperCase() || 'QA';

        const handleClick = () => {
            onShow?.(r.id);
        };

        return (
            <div
                key={r.id}
                className="case-card"
                onClick={() => onShow?.(r.id)}
                style={{ cursor: 'pointer' }}
            >
                <span className="warn-circle">!</span>

                <div className="card-body">
                    <span className={`badge badge-${getStatusClass(r.clazz)}`}>
                        {getStatusDescription(r.clazz)}
                    </span>

                    <h5 className="case-title">
                        {`Caso CDA-${r.orderId}`}
                        {roleBadge}
                    </h5>

                    <p className="case-desc">{r.description || 'Sin descripción'}</p>

                    <div className="footer">
                        <span>
                            Vencimiento:&nbsp;{translateDate(venc.toISOString())}
                        </span>
                        <span className="avatar">{requesterInitials}</span>
                    </div>
                </div>
            </div>
        );
    };

    /* ───────── RENDER ───────── */
    return (
        <div className="board-area">
            <div className="board-wrapper">
                {BOARD_COLUMNS.map(({ id, title }) => (
                    <section key={id} className="column">
                        <h4 className="column-title">{title}</h4>
                        {grouped[id].map(Card)}
                    </section>
                ))}
            </div>
        </div>
    );
}
