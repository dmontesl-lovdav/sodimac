// src/features/faq/FaqGridContainer.jsx
import { useState, useEffect, useMemo, useRef } from 'react';
import { createPortal } from 'react-dom';
import { useLocation, useNavigate } from 'react-router-dom';

import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';
import { Breadcrumb, GenericButton, GenericModal } from '@shared/components/ui';

import FaqGridToolbar from './components/FaqGridToolbar';
import FaqGridTable from './components/FaqGridTable';
import FaqGridModal from './components/FaqGridModal';

import './styles/FaqGridContainer.css';

export default function FaqGridContainer() {
    const location = useLocation();
    const fromMaintainer =
        location.state?.fromMaintainer ||
        new URLSearchParams(location.search).get('from') === 'mantenedor';

    const FAQ_CATEGORY_KEY = 'faq:lastCategory';
    const [search, setSearch] = useState('');
    const [category, setCategory] = useState(
        () => sessionStorage.getItem(FAQ_CATEGORY_KEY) || ''
    );
    const [categories, setCategories] = useState([]);
    const [faqs, setFaqs] = useState([]);
    const [page, setPage] = useState(1);
    const [perPage, setPerPage] = useState(10);
    const [active, setActive] = useState(null);
    const [loading, setLoading] = useState(false);
    const [selectedIds, setSelectedIds] = useState([]);

    const [opLoading, setOpLoading] = useState(false);
    const [alert, setAlert] = useState({
        visible: false,
        title: '',
        message: '',
        severity: 'info',
    });
    const [confirm, setConfirm] = useState({
        visible: false,
        title: 'Confirmar eliminación',
        message: '',
        confirmText: 'Eliminar',
        cancelText: 'Cancelar',
    });

    const api = ConfigurationBuilder.client;
    const nav = useNavigate();
    const wait = useRef();
    const firstLoadRef = useRef(true);

    const showAlert = (severity, message, title = 'Listo') =>
        setAlert({ visible: true, title, message, severity });

    const withOrigin = (pathname) => ({
        pathname,
        search: fromMaintainer ? '?from=mantenedor' : '',
    });
    const stateOrigin = fromMaintainer ? { fromMaintainer: true } : undefined;

    // cargar categorías
    useEffect(() => {
        let aborted = false;

        (async () => {
            try {
                const resp = await api.getFaqCategories({
                    page: 0,
                    size: 1000,
                    includeIcons: true,
                });

                if (aborted) return;

                const list = Array.isArray(resp?.content)
                    ? resp.content
                    : Array.isArray(resp)
                        ? resp
                        : [];

                setCategories(
                    list.map((c) => ({
                        id: c.id,
                        value: String(c.id),
                        label: c.name,
                        description: c.description,
                        icon: c.icon,
                    }))
                );
            } catch (err) {
                console.error('No pude cargar categorías', err);
                setCategories([]);
            }
        })();

        return () => {
            aborted = true;
        };
    }, [api]);

    // cargar FAQs (con soporte a backend paginado o no paginado)
    useEffect(() => {
        (async () => {
            const showInitialModal = firstLoadRef.current;
            try {
                setLoading(true);
                if (showInitialModal) setOpLoading(true);

                const baseParams = {};
                if (search && search.trim()) baseParams.searchTerm = search.trim();
                if (category) baseParams.categoryId = Number(category);

                let collected = [];
                let pageIdx = 0;
                const pageSize = 500;
                let isPaginated = false;

                let firstResp = await api.getFaqs({
                    ...baseParams,
                    page: pageIdx,
                    size: pageSize,
                });

                if (
                    firstResp &&
                    typeof firstResp === 'object' &&
                    !Array.isArray(firstResp)
                ) {
                    const content = firstResp.content ?? [];
                    const totalPages = Number(firstResp.totalPages ?? 1);
                    const current = Number(firstResp.number ?? 0);

                    if (Array.isArray(content)) {
                        isPaginated = true;
                        collected = collected.concat(content);

                        for (let p = current + 1; p < Math.min(totalPages, 100); p++) {
                            const resp = await api.getFaqs({
                                ...baseParams,
                                page: p,
                                size: pageSize,
                            });
                            const more = resp?.content ?? [];
                            if (!Array.isArray(more) || more.length === 0) break;
                            collected = collected.concat(more);
                            if (collected.length >= 5000) break;
                        }
                    }
                }

                if (isPaginated) {
                    setFaqs(collected);
                } else {
                    const data = await api.getFaqs({ ...baseParams, size: 1000 });
                    setFaqs(Array.isArray(data) ? data : []);
                }
            } catch (err) {
                console.error('No se pudieron cargar las FAQ', err);
                setFaqs([]);
            } finally {
                setLoading(false);
                if (showInitialModal) {
                    setOpLoading(false);
                    firstLoadRef.current = false;
                }
            }
        })();
    }, [search, category, api]);

    const handleTogglePublished = async (id, current) => {
        try {
            setFaqs((prev) =>
                prev.map((f) =>
                    f.id === id ? { ...f, published: !current } : f
                )
            );
            setOpLoading(true);

            if (current) {
                await api.postFaqUnpublish(id);
                showAlert('success', 'La FAQ se despublicó correctamente.', 'Despublicado');
            } else {
                await api.postFaqPublish(id);
                showAlert('success', 'La FAQ se publicó correctamente.', 'Publicado');
            }
        } catch (err) {
            console.error('Error cambiando publicación', err);
            // rollback
            setFaqs((prev) =>
                prev.map((f) =>
                    f.id === id ? { ...f, published: current } : f
                )
            );
            showAlert('error', 'No se pudo actualizar el estado de publicación.', 'Error');
        } finally {
            setOpLoading(false);
        }
    };

    const handleSelectRow = (id, checked) => {
        setSelectedIds((prev) =>
            checked ? [...prev, id] : prev.filter((x) => x !== id)
        );
    };

    const handleSelectAll = (checked, visibleRows) => {
        setSelectedIds(checked ? visibleRows.map((r) => r.id) : []);
    };

    const askBulkDelete = () => {
        if (selectedIds.length === 0) return;
        setConfirm({
            visible: true,
            title: 'Eliminar preguntas seleccionadas',
            message: `Se eliminarán ${selectedIds.length} preguntas frecuentes de forma permanente. ¿Deseas continuar?`,
            confirmText: 'Eliminar',
            cancelText: 'Cancelar',
        });
    };

    const handleBulkDelete = async () => {
        setConfirm((c) => ({ ...c, visible: false }));
        if (selectedIds.length === 0) return;

        setOpLoading(true);
        try {
            await Promise.all(selectedIds.map((id) => api.deleteFaq(id)));
            setFaqs((prev) => prev.filter((f) => !selectedIds.includes(f.id)));
            setSelectedIds([]);
            showAlert(
                'success',
                `Se eliminaron ${selectedIds.length} FAQs correctamente.`,
                'Eliminado'
            );
        } catch (err) {
            console.error(err);
            showAlert('error', 'Ocurrió un error al eliminar FAQs.', 'Error');
        } finally {
            setOpLoading(false);
        }
    };

    const filtered = useMemo(
        () =>
            faqs.filter(
                (f) =>
                    (!search ||
                        f.question
                            ?.toLowerCase()
                            .includes(search.toLowerCase())) &&
                    (!category || String(f.categoryId) === category)
            ),
        [faqs, search, category]
    );

    const totalPages = Math.max(1, Math.ceil(filtered.length / perPage));
    const start = (page - 1) * perPage;
    const rows = filtered.slice(start, start + perPage);

    const handleSearch = (e) => {
        clearTimeout(wait.current);
        const value = e.target.value;
        wait.current = setTimeout(() => setSearch(value), 250);
        setPage(1);
    };

    // cerrar modal detalle con ESC
    useEffect(() => {
        const esc = (e) => e.key === 'Escape' && setActive(null);
        if (active) window.addEventListener('keydown', esc);
        return () => window.removeEventListener('keydown', esc);
    }, [active]);

    // cambio de país
    useEffect(() => {
        const handler = () => {
            setOpLoading(true);

            sessionStorage.removeItem(FAQ_CATEGORY_KEY);

            setTimeout(() => {
                setSearch('');
                setCategory('');
                setSelectedIds([]);
                setPage(1);
                firstLoadRef.current = false;
                setOpLoading(false);
            }, 150);
        };

        window.addEventListener('country-changed', handler);
        return () => window.removeEventListener('country-changed', handler);
    }, []);

    const breadcrumbItems = fromMaintainer
        ? [
            { label: 'Inicio', to: '/' },
            { label: 'Centro de ayuda', to: '/' },
            { label: 'Mantenedor', to: '/mantenedor' },
            { label: 'Preguntas frecuentes' },
        ]
        : [
            { label: 'Inicio', to: '/' },
            { label: 'Centro de ayuda', to: '/' },
            { label: 'Preguntas frecuentes' },
        ];

    return (
        <>
            <div className="faq-page">
                <div className="faq-breadcrumb-wrapper">
                    <Breadcrumb items={breadcrumbItems} />
                </div>

                <div className="faq-card">
                    <div className="faq-card-body">
                        {/* encabezado */}
                        <div className="faq-header">
                            <div>
                                <h3 className="faq-title">
                                    Módulo preguntas frecuentes
                                </h3>
                                <p className="faq-subtitle">
                                    Búsqueda por folio, orden de compra, proveedor
                                    o usuario.
                                </p>
                            </div>

                            <div className="faq-header-actions">
                                <GenericButton
                                    variant="outline"
                                    onClick={() =>
                                        nav(
                                            withOrigin('/faq/bulk-upload'),
                                            { state: stateOrigin }
                                        )
                                    }
                                >
                                    Carga masiva
                                </GenericButton>

                                <GenericButton
                                    onClick={() =>
                                        nav(withOrigin('/faq/new'), {
                                            state: stateOrigin,
                                        })
                                    }
                                >
                                    + Agregar nueva pregunta frecuente
                                </GenericButton>
                            </div>
                        </div>

                        {/* filtros */}
                        <FaqGridToolbar
                            category={category}
                            categories={categories}
                            onCategoryChange={(id) => {
                                setCategory(id);
                                sessionStorage.setItem(FAQ_CATEGORY_KEY, id);
                                setPage(1);
                            }}
                            search={search}
                            onSearchInput={handleSearch}
                        />

                        {/* acciones de selección + tabla */}
                        <div className="faq-selection-wrapper">
                            <div className="faq-selection-bar">
                                {selectedIds.length > 0 && (
                                    <div className="faq-bulk-delete-wrapper">
                                        <button
                                            type="button"
                                            onClick={askBulkDelete}
                                            className="faq-bulk-delete-btn"
                                        >
                                            <svg
                                                viewBox="0 0 20 20"
                                                aria-hidden="true"
                                                className="faq-bulk-delete-icon"
                                            >
                                                <path
                                                    fillRule="evenodd"
                                                    d="M6 8a1 1 0 011 1v6a1 1 0 11-2 0V9a1 1 0 011-1zm4 0a1 1 0 011 1v6a1 1 0 11-2 0V9a1 1 0 011-1zm5-3a1 1 0 00-1-1h-3.382l-.724-1.447A1 1 0 009.382 2H6a1 1 0 00-1 1v1H3a1 1 0 000 2h14a1 1 0 000-2h-2zm-2 3v9a2 2 0 01-2 2H8a2 2 0 01-2-2V8h8z"
                                                    clipRule="evenodd"
                                                />
                                            </svg>
                                            Eliminar seleccionadas (
                                            {selectedIds.length})
                                        </button>
                                    </div>
                                )}
                            </div>

                            <div className="faq-table-wrapper">
                                <FaqGridTable
                                    loading={loading}
                                    rows={rows}
                                    categories={categories}
                                    emptyLabel="Sin resultados"
                                    perPage={perPage}
                                    page={page}
                                    totalPages={totalPages}
                                    enableSelection
                                    selectedIds={selectedIds}
                                    onSelectRow={handleSelectRow}
                                    onSelectAll={handleSelectAll}
                                    onChangePerPage={(n) => {
                                        setPerPage(n);
                                        setPage(1);
                                    }}
                                    onChangePage={(n) => setPage(n)}
                                    onShow={(f) => setActive(f)}
                                    onEdit={(id) =>
                                        nav(
                                            withOrigin(`/faq/${id}/edit`),
                                            { state: stateOrigin }
                                        )
                                    }
                                    onTogglePublished={handleTogglePublished}
                                />
                            </div>
                        </div>

                        {active &&
                            createPortal(
                                <FaqGridModal
                                    faq={active}
                                    onClose={() => setActive(null)}
                                />,
                                document.body
                            )}

                        {/* footer back link */}
                        <div className="faq-footer">
                            {fromMaintainer ? (
                                <button
                                    type="button"
                                    onClick={() => nav('/mantenedor')}
                                    className="faq-back-link"
                                >
                                    ← Volver al Mantenedor
                                </button>
                            ) : (
                                <button
                                    type="button"
                                    onClick={() => nav(-1)}
                                    className="faq-back-link"
                                >
                                    Volver
                                </button>
                            )}
                        </div>
                    </div>
                </div>
            </div>

            {/* modales globales */}
            <GenericModal
                visible={opLoading}
                variant="loading"
                message="Procesando…"
            />

            <GenericModal
                visible={alert.visible}
                variant="alert"
                title={alert.title}
                message={alert.message}
                severity={alert.severity}
                onClose={() =>
                    setAlert((a) => ({ ...a, visible: false }))
                }
            />

            {confirm.visible && (
                <ConfirmModal
                    visible={confirm.visible}
                    title={confirm.title}
                    message={confirm.message}
                    confirmText={confirm.confirmText}
                    cancelText={confirm.cancelText}
                    onCancel={() =>
                        setConfirm((c) => ({ ...c, visible: false }))
                    }
                    onConfirm={handleBulkDelete}
                />
            )}
        </>
    );
}

/* ========================================================= */
/*                     Confirm dialog                         */
/* ========================================================= */

function ConfirmModal({
    visible,
    title,
    message,
    confirmText,
    cancelText,
    onCancel,
    onConfirm,
}) {
    if (!visible) return null;

    return (
        <div className="faq-confirm-overlay">
            <div className="faq-confirm-dialog">
                <div className="faq-confirm-icon-circle">
                    <svg
                        className="faq-confirm-icon"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        strokeWidth="2"
                    >
                        <path
                            d="M12 7v4m0 4h.01"
                            strokeLinecap="round"
                        />
                        <polygon points="12 2 22 20 2 20" />
                    </svg>
                </div>

                <h3 className="faq-confirm-title">{title}</h3>
                <p className="faq-confirm-message">{message}</p>

                <div className="faq-confirm-actions">
                    <button
                        type="button"
                        onClick={onCancel}
                        className="faq-confirm-btn faq-confirm-btn-cancel"
                    >
                        {cancelText}
                    </button>
                    <button
                        type="button"
                        onClick={onConfirm}
                        className="faq-confirm-btn faq-confirm-btn-danger"
                    >
                        {confirmText}
                    </button>
                </div>
            </div>
        </div>
    );
}
