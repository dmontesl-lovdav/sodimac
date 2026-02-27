import { useState, useEffect } from 'react';
import { Breadcrumb, GenericButton, GenericModal, GenericSelect } from '@shared/components/ui';
import { useNavigate } from 'react-router-dom';

import {
    getAllResolvers,
    getResolversByModule,
    loadModulesCatalog,
    deleteResolver
} from './api/moduleResolverService';

import ModuleResolverGridTable from './components/ModuleResolverGridTable';

import './styles/ModuleResolverContainer.css';

export default function ModuleResolverContainer() {
    const [moduleId, setModuleId] = useState('');
    const [modules, setModules] = useState([]);

    const [items, setItems] = useState([]);
    const [totalPages, setTotalPages] = useState(1);
    const [totalItems, setTotalItems] = useState(0);

    const [loading, setLoading] = useState(false);

    const [confirmVisible, setConfirmVisible] = useState(false);
    const [loadingDelete, setLoadingDelete] = useState(false);
    const [deleteId, setDeleteId] = useState(null);

    const [page, setPage] = useState(1);
    const [perPage, setPerPage] = useState(10);

    const nav = useNavigate();

    /* ========================================================= */
    /*                    CARGAR CATÁLOGO                        */
    /* ========================================================= */
    useEffect(() => {
        (async () => {
            const options = await loadModulesCatalog().catch(() => []);
            setModules(options);
        })();
    }, []);

    /* ========================================================= */
    /*            (PAGINADO BACKEND)                */
    /* ========================================================= */
    const loadAll = async (p = page, size = perPage) => {
        try {
            setLoading(true);

            const result = await getAllResolvers(p, size);

            setItems(result?.content || []);
            setTotalPages(result?.totalPages || 1);
            setTotalItems(result?.totalElements || 0);

        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadAll(page, perPage);
    }, [page, perPage]);

    /* ========================================================= */
    /*      RECARGAR CUANDO CAMBIA PAÍS (EVENTO GLOBAL)          */
    /* ========================================================= */
    useEffect(() => {
        const handler = () => {
            setModuleId('');
            setPage(1);
            setPerPage(10);
            loadAll(1, 10);
        };

        window.addEventListener('country-changed', handler);
        return () => window.removeEventListener('country-changed', handler);
    }, []);

    /* ========================================================= */
    /*                        BUSCAR                             */
    /* ========================================================= */
    const search = async () => {
        if (!moduleId) return;

        try {
            setLoading(true);

            const result = await getResolversByModule(Number(moduleId), page - 1, perPage);

            setItems(result?.content || []);
            setTotalPages(result?.totalPages || 1);
            setPage(1);

        } finally {
            setLoading(false);
        }
    };

    /* ========================================================= */
    /*                        ELIMINAR                           */
    /* ========================================================= */
    const handleDelete = (id) => {
        setDeleteId(id);
        setConfirmVisible(true);
    };

    const confirmDelete = async () => {
        if (!deleteId) return;

        setConfirmVisible(false);
        setLoadingDelete(true);

        try {
            await deleteResolver(deleteId);
            loadAll(); // recarga la página actual
        } finally {
            setLoadingDelete(false);
            setDeleteId(null);
        }
    };

    /* ========================================================= */
    /*                        BREADCRUMB                         */
    /* ========================================================= */
    const breadcrumbItems = [
        { label: 'Centro de ayuda', to: '/' },
        { label: 'Mantenedor', to: '/mantenedor' },
        { label: 'Resolutores por módulo' },
    ];

    /* ========================================================= */
    /*                          UI                               */
    /* ========================================================= */

    return (
        <div className="mrc-layout">

            <GenericModal
                visible={confirmVisible}
                variant="confirm"
                severity="error"
                title="Eliminar resolutor"
                message="¿Estás segura que deseas eliminar este resolutor?"
                confirmText="Eliminar"
                cancelText="Cancelar"
                onConfirm={confirmDelete}
                onCancel={() => setConfirmVisible(false)}
            />

            {loadingDelete && (
                <GenericModal visible variant="loading" message="Eliminando…" />
            )}

            <Breadcrumb items={breadcrumbItems} />

            <div className="mrc-box">
                <div className="mrc-header">
                    <h3>Resolutores por módulo</h3>

                    <GenericButton onClick={() => nav('/moduleResolver/new')}>
                        + Agregar nuevo
                    </GenericButton>
                </div>

                {/* Filtros */}
                <div className="mrc-filters">
                    <GenericSelect
                        value={moduleId}
                        onChange={(e) => setModuleId(e.target.value)}
                        options={modules}
                        placeholder="Seleccionar módulo…"
                        widthClass="gs-width-lg"
                    />

                    <GenericButton variant="outline" onClick={search}>
                        Buscar
                    </GenericButton>

                    <GenericButton
                        variant="outlineFill"
                        onClick={() => {
                            setModuleId('');
                            setPage(1);
                            loadAll(1, perPage);
                        }}
                    >
                        Reset
                    </GenericButton>
                </div>

                {/* TABLA PAGINADA */}
                <ModuleResolverGridTable
                    rows={items}
                    page={page}
                    perPage={perPage}
                    totalPages={totalPages}
                    totalItems={totalItems}
                    onChangePage={(p) => setPage(p)}
                    onChangePerPage={(n) => {
                        setPerPage(n);
                        setPage(1);
                    }}
                    onEdit={(id) => nav(`/moduleResolver/${id}/edit`)}
                    onDelete={handleDelete}
                    loading={loading}
                />

                {loading && (
                    <GenericModal visible variant="loading" message="Cargando…" />
                )}
            </div>
        </div>
    );
}
