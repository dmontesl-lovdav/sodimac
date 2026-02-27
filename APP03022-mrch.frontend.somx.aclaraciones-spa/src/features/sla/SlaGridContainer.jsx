import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';
import { Breadcrumb, GenericButton, GenericModal } from '@shared/components/ui';
import SlaGridTable from './components/SlaGridTable';

import './styles/SlaGridContainer.css';

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

export default function SlaGridContainer() {
    const [slas, setSlas] = useState([]);
    const [page, setPage] = useState(1);
    const [perPage, setPerPage] = useState(10);
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
        title: 'Eliminar',
        message: 'Esta acción es permanente. ¿Deseas continuar?',
        confirmText: 'Eliminar',
        cancelText: 'Cancelar',
    });

    const api = ConfigurationBuilder.client;
    const nav = useNavigate();
    const firstLoadRef = useRef(true);

    const showAlert = (severity, message, title = 'Listo') =>
        setAlert({ visible: true, title, message, severity });

    useEffect(() => {
        (async () => {
            const showInitial = firstLoadRef.current;

            try {
                setLoading(true);
                if (showInitial) setOpLoading(true);

                const data = await api.getSlas();
                setSlas(Array.isArray(data) ? data : []);
            } catch (err) {
                console.error('No se pudieron cargar los SLAs', err);
                setSlas([]);
            } finally {
                setLoading(false);
                if (showInitial) {
                    setOpLoading(false);
                    firstLoadRef.current = false;
                }
            }
        })();
    }, [api]);

    useEffect(() => {
        const handler = () => {
            setOpLoading(true);

            setTimeout(async () => {
                try {
                    setLoading(true);

                    const data = await api.getSlas();
                    setSlas(Array.isArray(data) ? data : []);
                } catch (err) {
                    console.error("No pude recargar SLAs", err);
                    setSlas([]);
                } finally {
                    setLoading(false);
                    setOpLoading(false);
                }
            }, 150);
        };

        window.addEventListener("country-changed", handler);
        return () => window.removeEventListener("country-changed", handler);
    }, [api]);

    const handleTogglePublished = async (id, current) => {
        try {
            setSlas((prev) =>
                prev.map((f) => (f.id === id ? { ...f, published: !current } : f))
            );

            setOpLoading(true);

            if (current) {
                await api.publishSla(id, false);
                showAlert('success', 'Despublicado correctamente.', 'Despublicado');
            } else {
                await api.publishSla(id, true);
                showAlert('success', 'Publicado correctamente.', 'Publicado');
            }
        } catch (err) {
            console.error('Error cambiando publicación', err);
            setSlas((prev) =>
                prev.map((f) => (f.id === id ? { ...f, published: current } : f))
            );
            showAlert('error', 'No se pudo actualizar el estado.', 'Error');
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
            setSlas((prev) => prev.filter((f) => f.id !== id));
            await Promise.all([api.deleteSla(id), sleep(300)]);
            showAlert('success', 'Eliminado correctamente.', 'Eliminado');
        } catch (err) {
            console.error('Error eliminando SLA', err);
            showAlert('error', 'No se pudo eliminar.', 'Error');
        } finally {
            setOpLoading(false);
        }
    };

    const totalPages = Math.max(1, Math.ceil(slas.length / perPage));
    const start = (page - 1) * perPage;
    const rows = slas.slice(start, start + perPage);

    return (
        <>
            <div className="sla-layout">
                <div className="sla-breadcrumb">
                    <Breadcrumb
                        items={[
                            { label: 'Inicio', to: '/' },
                            { label: 'Centro de ayuda', to: '/' },
                            { label: 'Mantenedores', to: '/mantenedor' },
                            { label: 'SLAs' },
                        ]}
                    />
                </div>

                <div className="sla-box">
                    <div className="sla-inner">
                        {/* encabezado */}
                        <div className="sla-header">
                            <div>
                                <h3 className="sla-title">Módulo SLAs</h3>
                                <p className="sla-subtitle">
                                    Búsqueda por folio, orden de compra, proveedor o usuario.
                                </p>
                            </div>

                            <div className="sla-header-actions">
                                <GenericButton onClick={() => nav('/slas/new')}>
                                    + Agregar nuevo SLA
                                </GenericButton>
                            </div>
                        </div>

                        {/* tabla */}
                        <SlaGridTable
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
                            onEdit={(id) => nav(`/slas/${id}`)}
                            onDelete={handleAskDelete}
                            onTogglePublished={handleTogglePublished}
                        />

                        {/* footer */}
                        <div className="sla-footer">
                            <button
                                onClick={() => nav(-1)}
                                className="sla-back"
                            >
                                Volver
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {/* operation loading */}
            <GenericModal visible={opLoading} variant="loading" message="Procesando…" />

            {/* alert */}
            <GenericModal
                visible={alert.visible}
                variant="alert"
                title={alert.title}
                message={alert.message}
                severity={alert.severity}
                onClose={() => setAlert((a) => ({ ...a, visible: false }))}
            />

            {/* confirm delete */}
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
