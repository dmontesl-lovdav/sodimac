import { useState, useEffect, useMemo, useRef } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

import { Breadcrumb, GenericButton, GenericModal } from '@shared/components/ui';
import FeedbackGridTable from './components/FeedbackGridTable';

import {
    getFeedback,
    publishFeedback,
    unpublishFeedback,
    deleteFeedback,
} from '@/features/feedback/api';

import './styles/FeedbackContainer.css';

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

export default function FeedbackContainer() {
    const location = useLocation();
    const fromMaintainer =
        location.state?.fromMaintainer ||
        new URLSearchParams(location.search).get('from') === 'mantenedor';

    const withOrigin = (pathname) => ({
        pathname,
        search: fromMaintainer ? '?from=mantenedor' : '',
    });

    const stateOrigin = fromMaintainer ? { fromMaintainer: true } : undefined;

    /* state */
    const [items, setItems] = useState([]);
    const [page, setPage] = useState(1);
    const [perPage, setPerPage] = useState(10);
    const [loading, setLoading] = useState(false);

    const [opLoading, setOpLoading] = useState(true);
    const [alert, setAlert] = useState({
        visible: false,
        title: '',
        message: '',
        severity: 'info',
    });

    const [confirm, setConfirm] = useState({
        visible: false,
        id: null,
        title: 'Eliminar feedback',
        message: 'Esta acción es permanente. ¿Deseas continuar?',
        confirmText: 'Eliminar',
        cancelText: 'Cancelar',
    });

    const nav = useNavigate();

    /* initial load */
    useEffect(() => {
        (async () => {
            try {
                setLoading(true);
                const data = await getFeedback({ size: 500 });

                const normalized = Array.isArray(data)
                    ? data.map(({ id, question, isActive }) => ({
                        id,
                        title: question,
                        isActive,
                    }))
                    : [];

                setItems(normalized);
            } catch {
                setItems([]);
            } finally {
                setLoading(false);
                setOpLoading(false);
            }
        })();
    }, []);

    /* recargar por cambio de país */
    useEffect(() => {
        const handler = () => {
            setOpLoading(true);

            setTimeout(async () => {
                try {
                    setLoading(true);
                    const data = await getFeedback({ size: 500 });

                    const normalized = Array.isArray(data)
                        ? data.map(({ id, question, isActive }) => ({
                            id,
                            title: question,
                            isActive,
                        }))
                        : [];

                    setItems(normalized);
                } catch {
                    setItems([]);
                } finally {
                    setLoading(false);
                    setOpLoading(false);
                }
            }, 150);
        };

        window.addEventListener('country-changed', handler);
        return () => window.removeEventListener('country-changed', handler);
    }, []);

    /* helpers */
    const showAlert = (severity, message, title = 'Listo') =>
        setAlert({ visible: true, title, message, severity });

    /* handlers */
    const handleTogglePublished = async (id, current) => {
        try {
            setItems((prev) =>
                prev.map((r) =>
                    r.id === id ? { ...r, isActive: !current } : r
                )
            );

            setOpLoading(true);

            if (current) {
                await unpublishFeedback(id);
                showAlert('success', 'El registro se despublicó correctamente.', 'Despublicado');
            } else {
                await publishFeedback(id);
                showAlert('success', 'El registro se publicó correctamente.', 'Publicado');
            }
        } catch {
            setItems((prev) =>
                prev.map((r) =>
                    r.id === id ? { ...r, isActive: current } : r
                )
            );
            showAlert('error', 'No se pudo actualizar el estado de publicación.', 'Error');
        } finally {
            setOpLoading(false);
        }
    };

    const handleAskDelete = (id) =>
        setConfirm((c) => ({ ...c, visible: true, id }));

    const handleConfirmDelete = async () => {
        const id = confirm.id;
        setConfirm((c) => ({ ...c, visible: false }));

        try {
            setOpLoading(true);
            setItems((prev) => prev.filter((r) => r.id !== id));

            await Promise.all([deleteFeedback(id), sleep(300)]);

            showAlert('success', 'El registro se eliminó correctamente.', 'Eliminado');
        } catch {
            showAlert('error', 'No se pudo eliminar el registro.', 'Error');
        } finally {
            setOpLoading(false);
        }
    };

    /* derived pagination */
    const filtered = useMemo(() => items, [items]);
    const totalPages = Math.max(1, Math.ceil(filtered.length / perPage));
    const start = (page - 1) * perPage;
    const rows = filtered.slice(start, start + perPage);

    const breadcrumbItems = fromMaintainer
        ? [
            { label: 'Inicio', to: '/' },
            { label: 'Centro de ayuda', to: '/' },
            { label: 'Mantenedor', to: '/mantenedor' },
            { label: 'Feedback preguntas frecuentes' },
        ]
        : [
            { label: 'Inicio', to: '/' },
            { label: 'Centro de ayuda', to: '/' },
            { label: 'Feedback preguntas frecuentes' },
        ];

    /* render */
    return (
        <>
            <div className="fc-layout">
                <div className="fc-breadcrumb">
                    <Breadcrumb items={breadcrumbItems} />
                </div>

                <div className="fc-box">
                    <div className="fc-inner">
                        <div className="fc-header">
                            <div>
                                <h3 className="fc-title">Módulo de feedback preguntas frecuentes</h3>
                                <p className="fc-subtitle">
                                    Búsqueda por folio, orden de compra, proveedor o usuario.
                                </p>
                                <p className="fc-count">{filtered.length} ítems encontrados</p>
                            </div>

                            <div className="fc-actions">
                                <GenericButton
                                    onClick={() =>
                                        nav(withOrigin('/feedback/new'), { state: stateOrigin })
                                    }
                                >
                                    + Agregar nueva
                                </GenericButton>
                            </div>
                        </div>

                        <FeedbackGridTable
                            loading={loading}
                            rows={rows}
                            emptyLabel="Sin resultados"
                            perPage={perPage}
                            page={page}
                            totalPages={totalPages}
                            onChangePerPage={(n) => {
                                setPerPage(n);
                                setPage(1);
                            }}
                            onChangePage={(n) => setPage(n)}
                            onEdit={(id) =>
                                nav(withOrigin(`/feedback/${id}`), {
                                    state: stateOrigin,
                                })
                            }
                            onDelete={handleAskDelete}
                            onTogglePublished={handleTogglePublished}
                        />

                        <div className="fc-footer">
                            {fromMaintainer ? (
                                <button className="fc-link" onClick={() => nav('/mantenedor')}>
                                    ← Volver al Mantenedor
                                </button>
                            ) : (
                                <button className="fc-link" onClick={() => nav(-1)}>
                                    Volver
                                </button>
                            )}
                        </div>
                    </div>
                </div>
            </div>

            {/* Loading */}
            <GenericModal visible={opLoading} variant="loading" message="Procesando…" />

            {/* Alert */}
            <GenericModal
                visible={alert.visible}
                variant="alert"
                title={alert.title}
                message={alert.message}
                severity={alert.severity}
                onClose={() => setAlert((a) => ({ ...a, visible: false }))}
            />

            {/* Confirmación de borrar (usando GenericModal) */}
            <GenericModal
                visible={confirm.visible}
                variant="confirm"
                title={confirm.title}
                message={confirm.message}
                confirmText={confirm.confirmText}
                cancelText={confirm.cancelText}
                onConfirm={handleConfirmDelete}
                onCancel={() => setConfirm((c) => ({ ...c, visible: false, id: null }))}
            />
        </>
    );
}
