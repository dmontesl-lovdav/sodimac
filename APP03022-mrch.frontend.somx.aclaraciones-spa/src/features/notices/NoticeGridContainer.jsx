import { useState, useEffect, useMemo, useRef } from 'react';
import { createPortal } from 'react-dom';
import { useLocation, useNavigate } from 'react-router-dom';

import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';
import { GenericModal } from '@shared/components/ui';

import NoticeGridToolbar from './components/NoticeGridToolbar';
import NoticeGridTable from './components/NoticeGridTable';

import {
    getNotices as apiGetNotices,
    publishNotice,
    unpublishNotice,
    deleteNotice as apiDeleteNotice,
} from '@/features/notices/api';

import './styles/NoticeGridContainer.css';

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

export default function NoticeGridContainer() {
    const location = useLocation();
    const fromMaintainer =
        location.state?.fromMaintainer ||
        new URLSearchParams(location.search).get('from') === 'mantenedor';

    const withOrigin = (pathname) => ({
        pathname,
        search: fromMaintainer ? '?from=mantenedor' : '',
    });
    const stateOrigin = fromMaintainer ? { fromMaintainer: true } : undefined;

    const [notices, setNotices] = useState([]);
    const [search, setSearch] = useState('');
    const [page, setPage] = useState(1);
    const [perPage, setPerPage] = useState(10);
    const [active, setActive] = useState(null);
    const [loading, setLoading] = useState(false);

    const [opLoading, setOpLoading] = useState(false);
    const [alert, setAlert] = useState({
        visible: false,
        title: '',
        message: '',
        severity: 'info',
    });
    const [confirm, setConfirm] = useState({
        visible: false,
        id: null,
        title: 'Eliminar elemento',
        message: 'Esta acción es permanente. ¿Deseas continuar?',
        confirmText: 'Eliminar',
        cancelText: 'Cancelar',
    });

    const nav = useNavigate();
    const wait = useRef();
    const firstLoadRef = useRef(true);

    const showAlert = (severity, message, title = 'Listo') =>
        setAlert({ visible: true, title, message, severity });

    /* load notices */
    const loadNotices = async () => {
        try {
            setLoading(true);
            const data = await apiGetNotices({ size: 500 });

            const sorted = (Array.isArray(data) ? data : [])
                .slice()
                .sort((a, b) => Number(b?.id ?? 0) - Number(a?.id ?? 0))
                .map((c) => ({
                    id: c.id,
                    name: c.name,
                    description: c.description,
                    published: !!c.published,
                }));

            setNotices(sorted);
        } catch (err) {
            setNotices([]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadNotices();
    }, []);

    useEffect(() => {
        const handler = async () => {
            setOpLoading(true);
            setSearch('');
            setPage(1);

            try {
                await loadNotices();
            } finally {
                setOpLoading(false);
            }
        };

        window.addEventListener('country-changed', handler);
        return () => window.removeEventListener('country-changed', handler);
    }, []);

    const handleSearchInput = (e) => {
        const value = e.target.value;
        setSearch(value);
        clearTimeout(wait.current);
        wait.current = setTimeout(() => {

        }, 250);
    };

    const handleTogglePublished = async (id, current) => {
        try {
            setNotices((prev) =>
                prev.map((c) => (c.id === id ? { ...c, published: !current } : c))
            );
            setOpLoading(true);

            if (current) {
                await unpublishNotice(id);
                showAlert('success', 'El elemento se despublicó correctamente.', 'Despublicado');
            } else {
                await publishNotice(id);
                showAlert('success', 'El elemento se publicó correctamente.', 'Publicado');
            }
        } catch (err) {
            console.error('Error cambiando publicación', err);
            setNotices((prev) =>
                prev.map((c) => (c.id === id ? { ...c, published: current } : c))
            );
            showAlert('error', 'No se pudo actualizar el estado de publicación.', 'Error');
        } finally {
            setOpLoading(false);
        }
    };

    const handleAskDelete = (id) => setConfirm((c) => ({ ...c, visible: true, id }));

    const handleConfirmDelete = async () => {
        const id = confirm.id;
        setConfirm((c) => ({ ...c, visible: false }));
        try {
            setOpLoading(true);
            setNotices((prev) => prev.filter((c) => c.id !== id));
            await Promise.all([apiDeleteNotice(id), sleep(300)]);
            showAlert('success', 'El elemento se eliminó correctamente.', 'Eliminado');
        } catch (err) {
            console.error('Error al eliminar', err);
            showAlert('error', 'No se pudo eliminar el elemento.', 'Error');
        } finally {
            setOpLoading(false);
        }
    };

    const filtered = useMemo(
        () =>
            notices.filter(
                (c) =>
                    !search ||
                    c.name.toLowerCase().includes(search.toLowerCase()) ||
                    (c.description ?? '').toLowerCase().includes(search.toLowerCase())
            ),
        [notices, search]
    );

    const totalPages = Math.max(1, Math.ceil(filtered.length / perPage));
    const start = (page - 1) * perPage;
    const rows = filtered.slice(start, start + perPage);

    const breadcrumbItems = fromMaintainer
        ? [
            { label: 'Inicio', to: '/' },
            { label: 'Centro de ayuda', to: '/' },
            { label: 'Mantenedor', to: '/mantenedor' },
            { label: 'Sección informativa' },
        ]
        : [
            { label: 'Inicio', to: '/' },
            { label: 'Centro de ayuda', to: '/' },
            { label: 'Sección informativa' },
        ];

    return (
        <>
            <div className="ngc-layout">
                <div className="ngc-breadcrumb">
                    <Breadcrumb items={breadcrumbItems} />
                </div>

                <div className="ngc-card">
                    <div className="ngc-card-inner">
                        <div className="ngc-header">
                            <div>
                                <h3 className="ngc-title">Módulo de Sección Informativa</h3>
                                <p className="ngc-subtitle">Búsqueda por nombre o descripción.</p>
                            </div>

                            <div className="ngc-actions">
                                <button
                                    className="ngc-add-btn"
                                    onClick={() => nav(withOrigin('/notices/new'), { state: stateOrigin })}
                                >
                                    + Agregar nueva
                                </button>
                            </div>
                        </div>

                        <NoticeGridToolbar search={search} onSearchInput={handleSearchInput} />

                        <NoticeGridTable
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
                            onShow={(c) => setActive(c)}
                            onEdit={(c) => nav(withOrigin(`/notices/${c.id}`), { state: stateOrigin })}
                            onDelete={handleAskDelete}
                            onTogglePublished={handleTogglePublished}
                        />

                        <div className="ngc-footer">
                            {fromMaintainer ? (
                                <button
                                    onClick={() => nav('/mantenedor')}
                                    className="ngc-backlink"
                                >
                                    ← Volver al Mantenedor
                                </button>
                            ) : (
                                <button
                                    onClick={() => nav(-1)}
                                    className="ngc-backlink"
                                >
                                    Volver
                                </button>
                            )}
                        </div>
                    </div>
                </div>
            </div>

            {active &&
                createPortal(
                    <NoticeGridModal Notice={active} onClose={() => setActive(null)} />,
                    document.body
                )}

            <GenericModal visible={opLoading} variant="loading" message="Procesando…" />

            <GenericModal
                visible={alert.visible}
                variant="alert"
                title={alert.title}
                message={alert.message}
                severity={alert.severity}
                onClose={() => setAlert((a) => ({ ...a, visible: false }))}
            />

            <GenericModal
                visible={confirm.visible}
                variant="confirm"
                title={confirm.title}
                message={confirm.message}
                confirmText={confirm.confirmText}
                cancelText={confirm.cancelText}
                onCancel={() => setConfirm((c) => ({ ...c, visible: false, id: null }))}
                onConfirm={handleConfirmDelete}
            />
        </>
    );
}
