import { useState, useEffect, useRef } from 'react';
import { createPortal } from 'react-dom';
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom';

import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';
import GenericModal from '@shared/components/ui/modal/GenericModal';
import GenericTable, { Switch } from '@/shared/components/ui/table/GenericTable';

import CategoryGridToolbar from './components/CategoryGridToolbar';

import editIcon from '@assets/edit.svg';
import deleteIcon from '@assets/delete.svg';

import './styles/CategoryGridContainer.css';

/* servicios API */
import {
    getCategories,
    publishCategory,
    unpublishCategory,
    deleteCategory as apiDeleteCategory,
    hasFaqsInCategory,
} from '@/features/categories/api';

/* helper */
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

export default function CategorieGridContainer() {
    const [categories, setCategories] = useState([]);
    const [search, setSearch] = useState('');
    const [page, setPage] = useState(1);
    const [perPage, setPerPage] = useState(10);
    const [totalPages, setTotalPages] = useState(1);
    const [active, setActive] = useState(null);
    const [loading, setLoading] = useState(false);
    const [reloadKey, setReloadKey] = useState(0);

    /* modales */
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
        title: 'Eliminar categoría',
        messageConfirm: 'Esta acción es permanente. ¿Deseas continuar?',
    });

    const nav = useNavigate();
    const wait = useRef();
    const firstLoadRef = useRef(true);

    /* from mantenedor */
    const location = useLocation();
    const [searchParams] = useSearchParams();
    const fromMaintainer =
        location.state?.fromMaintainer || searchParams.get('from') === 'mantenedor';

    const [filterActive, setFilterActive] = useState('all');

    const showAlert = (severity, message, title = 'Listo') =>
        setAlert({ visible: true, title, message, severity });

    /* ============================= */
    /*          DATA LOAD             */
    /* ============================= */
    useEffect(() => {
        (async () => {
            const showInitialModal = firstLoadRef.current;

            try {
                setLoading(true);
                setOpLoading(true);

                const res = await getCategories({
                    page: page - 1,
                    size: perPage,
                    active: filterActive,
                });

                setCategories(
                    res.content.map((c) => ({
                        id: c.id,
                        name: c.name,
                        description: c.description,
                        published: !!c.isActive,
                    }))
                );

                setTotalPages(res.totalPages ?? 1);
            } catch (err) {
                console.error('No pude cargar categorías', err);
                showAlert('error', 'No se pudieron cargar las categorías.', 'Error');
            } finally {
                setLoading(false);
                setOpLoading(false);
                if (showInitialModal) firstLoadRef.current = false;
            }
        })();
    }, [filterActive, page, perPage, reloadKey]);

    useEffect(() => {
        const handler = () => {
            setOpLoading(true);

            setTimeout(() => {
                setSearch('');
                setFilterActive('all');
                setPage(1);
                setReloadKey((k) => k + 1);
                firstLoadRef.current = false;
                setOpLoading(false);
            }, 150);
        };

        window.addEventListener('country-changed', handler);
        return () => window.removeEventListener('country-changed', handler);
    }, []);

    /* ============================= */
    /*            HANDLERS            */
    /* ============================= */
    const handleSearchInput = (e) => {
        clearTimeout(wait.current);
        wait.current = setTimeout(() => setSearch(e.target.value), 250);
        setPage(1);
    };

    const handleTogglePublished = async (id, current) => {
        try {
            setCategories((prev) =>
                prev.map((c) => (c.id === id ? { ...c, published: !current } : c))
            );

            setOpLoading(true);

            if (current) {
                await unpublishCategory(id);
                showAlert('success', 'La categoría se despublicó correctamente.', 'Despublicada');
            } else {
                await publishCategory(id);
                showAlert('success', 'La categoría se publicó correctamente.', 'Publicada');
            }
        } catch (err) {
            console.error(err);
            setCategories((prev) =>
                prev.map((c) => (c.id === id ? { ...c, published: current } : c))
            );
            showAlert('error', 'No se pudo actualizar el estado.', 'Error');
        } finally {
            setOpLoading(false);
        }
    };

    const handleAskDelete = (id) => {
        setConfirm((c) => ({ ...c, visible: true, id }));
    };

    const handleConfirmDelete = async () => {
        const id = confirm.id;
        setConfirm((c) => ({ ...c, visible: false, id: null }));

        try {
            setOpLoading(true);

            const hasFaqs = await hasFaqsInCategory(id);
            if (hasFaqs) {
                showAlert(
                    'warning',
                    'No puedes eliminar la categoría porque existen preguntas frecuentes asociadas.',
                    'No se puede eliminar'
                );
                return;
            }

            setCategories((prev) => prev.filter((c) => c.id !== id));
            await Promise.all([apiDeleteCategory(id), sleep(300)]);
            showAlert('success', 'La categoría se eliminó correctamente.', 'Eliminada');
        } catch (err) {
            console.error(err);
            showAlert('error', 'No se pudo eliminar la categoría.', 'Error');
        } finally {
            setOpLoading(false);
        }
    };

    /* ============================= */
    /*          TABLE CONFIG          */
    /* ============================= */
    const columns = [
        {
            header: 'Título de la categoría',
            render: (row) => row.name,
        },
        {
            header: 'Descripción',
            render: (row) => row.description || '—',
        },
        {
            header: 'Publicado',
            align: 'center',
            render: (row) => (
                <Switch
                    on={row.published}
                    onClick={() => handleTogglePublished(row.id, row.published)}
                />
            ),
        },
    ];

    const actions = [
        {
            title: 'Editar',
            icon: editIcon,
            onClick: (row, nav) =>
                nav(
                    withFrom(
                        `/categories/${row.id}?name=${encodeURI(row.name)}&description=${encodeURI(
                            row.description ?? ''
                        )}`
                    ),
                    { state: withState }
                ),
        },
        {
            title: 'Eliminar',
            icon: deleteIcon,
            onClick: (row) => handleAskDelete(row.id),
        },
    ];

    /* ============================= */
    /*             UI                */
    /* ============================= */
    const breadcrumbItems = [
        { label: 'Inicio', to: '/' },
        { label: 'Centro de ayuda', to: '/' },
        ...(fromMaintainer ? [{ label: 'Mantenedor', to: '/mantenedor' }] : []),
        { label: 'Categorías' },
    ];

    const withFrom = (path) => {
        if (!fromMaintainer) return path;
        const sep = path.includes('?') ? '&' : '?';
        return `${path}${sep}from=mantenedor`;
    };
    const withState = fromMaintainer ? { fromMaintainer: true } : undefined;

    return (
        <>
            <div className="px-4">
                <Breadcrumb items={breadcrumbItems} />

                <div className="border border-gray-300 rounded-md bg-transparent mt-2 p-4">
                    <div className="category-header">
                        <div>
                            <h3 className="category-title">Categorías</h3>
                            <p className="category-description">
                                Búsqueda por nombre o descripción.
                            </p>
                        </div>

                        <div className="category-actions">
                            <button
                                className="btn-outline category-btn"
                                onClick={() =>
                                    nav(withFrom('/categories/bulk-upload'), {
                                        state: withState,
                                    })
                                }
                            >
                                Carga masiva
                            </button>

                            <button
                                className="btn-primary category-btn"
                                onClick={() =>
                                    nav(withFrom('/categories/new'), { state: withState })
                                }
                            >
                                + Agregar nueva categoría
                            </button>
                        </div>
                    </div>

                    <CategoryGridToolbar
                        search={search}
                        onSearchInput={handleSearchInput}
                        filterActive={filterActive}
                        onFilterChange={setFilterActive}
                    />

                    <GenericTable
                        rows={categories}
                        columns={columns}
                        actions={actions}
                        emptyLabel="Sin resultados"
                        perPage={perPage}
                        page={page}
                        totalPages={totalPages}
                        onChangePerPage={(n) => {
                            setPerPage(n);
                            setPage(1);
                        }}
                        onChangePage={setPage}
                    />
                </div>
            </div>

            {/* LOADING */}
            <GenericModal visible={opLoading} variant="loading" />

            {/* ALERT */}
            <GenericModal
                visible={alert.visible}
                variant="alert"
                title={alert.title}
                message={alert.message}
                severity={alert.severity}
                onClose={() => setAlert((a) => ({ ...a, visible: false }))}
            />

            {/* CONFIRM */}
            <GenericModal
                visible={confirm.visible}
                variant="confirm"
                severity="warning"
                title={confirm.title}
                messageConfirm={confirm.messageConfirm}
                confirmText="Eliminar"
                cancelText="Cancelar"
                onCancel={() => setConfirm((c) => ({ ...c, visible: false, id: null }))}
                onConfirm={handleConfirmDelete}
            />
        </>
    );
}
