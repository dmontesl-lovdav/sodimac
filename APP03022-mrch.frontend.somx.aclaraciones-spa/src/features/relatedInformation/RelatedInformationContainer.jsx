import { useState, useEffect, useMemo } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';
import {
    getRelatedInformation,
    publishRelatedInformation,
    unpublishRelatedInformation,
    deleteRelatedInformation,
} from '@/features/relatedInformation/api/relatedInformationService';

import { loadCatalog } from '@/features/cases/components/RequestUtils';
import { Breadcrumb, GenericButton, GenericModal } from '@shared/components/ui';
import RelatedInformationGridToolbar from './components/RelatedInformationGridToolbar';
import RelatedInformationTable from './components/RelatedInformationGridTable';

import './styles/RelatedInformationContainer.css';

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

export default function RelatedInformationContainer() {
    const location = useLocation();
    const fromMaintainer =
        location.state?.fromMaintainer ||
        new URLSearchParams(location.search).get('from') === 'mantenedor';

    const withOrigin = (pathname) => ({
        pathname,
        search: fromMaintainer ? '?from=mantenedor' : '',
    });

    const stateOrigin = fromMaintainer ? { fromMaintainer: true } : undefined;

    const [bizUnit, setBizUnit] = useState('');
    const [country, setCountry] = useState('');
    const [bizUnits, setBizUnits] = useState([]);
    const [countries, setCountries] = useState([]);
    const [items, setItems] = useState([]);
    const [relatedInfoId, setRelatedInfoId] = useState('');
    const [relatedInfoIds, setRelatedInfoIds] = useState([]);

    const [page, setPage] = useState(1);
    const [perPage, setPerPage] = useState(10);

    const [loading, setLoading] = useState(false);
    const [criteria, setCriteria] = useState({ id: '', bizUnit: '', country: '' });
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
        title: 'Eliminar información relacionada',
        message: 'Esta acción es permanente. ¿Deseas continuar?',
        confirmText: 'Eliminar',
        cancelText: 'Cancelar',
    });

    const api = ConfigurationBuilder.client;
    const nav = useNavigate();

    const CATALOGS_BUSINESSUNITS = 1;
    const CATALOGS_COUNTRIES = 2;

    // cargar combos
    useEffect(() => {
        (async () => {
            try {
                await loadCatalog(api, CATALOGS_BUSINESSUNITS, (arr) => {
                    setBizUnits(arr.map(({ id, description }) => ({ value: String(id), label: String(description) })));
                });

                await loadCatalog(api, CATALOGS_COUNTRIES, (arr) => {
                    setCountries(arr.map(({ id, description }) => ({ value: String(id), label: String(description) })));
                });
            } catch {
                setBizUnits([]);
                setCountries([]);
            }
        })();
    }, [api]);

    useEffect(() => {
        (async () => {
            try {
                const data = await getRelatedInformation({ size: 1000 });
                setRelatedInfoIds(
                    (Array.isArray(data) ? data : []).map((r) => ({
                        value: String(r.id),
                        label: `${r.title}${r.countryName ? ` (${r.countryName})` : ''}`,
                    }))
                );
            } catch {
                setRelatedInfoIds([]);
            }
        })();
    }, []);

    // load data
    const loadRelatedInfo = async () => {
        try {
            setLoading(true);

            const data = await getRelatedInformation({
                id: criteria.id ? Number(criteria.id) : undefined,
                businessUnit: criteria.id ? undefined : criteria.bizUnit || undefined,
                country: criteria.id ? undefined : criteria.country || undefined,
                size: 500,
            });

            const sorted = (Array.isArray(data) ? data : []).sort((a, b) => b.id - a.id);
            setItems(sorted);
        } catch {
            setItems([]);
        } finally {
            setLoading(false);
            setOpLoading(false);
        }
    };

    useEffect(() => {
        loadRelatedInfo();
    }, [criteria]);


    useEffect(() => {
        const handler = () => {
            setOpLoading(true);

            // reset filtros
            setBizUnit('');
            setCountry('');
            setRelatedInfoId('');
            setCriteria({ id: '', bizUnit: '', country: '' });
            setPage(1);

            loadRelatedInfo();
        };

        window.addEventListener('country-changed', handler);
        return () => window.removeEventListener('country-changed', handler);
    }, []);



    const showAlert = (severity, message, title = 'Listo') =>
        setAlert({ visible: true, title, message, severity });


    const handleTogglePublished = async (id, current) => {
        try {
            setItems((prev) => prev.map((f) => (f.id === id ? { ...f, isActive: !current } : f)));
            setOpLoading(true);

            if (current) {
                await unpublishRelatedInformation(id);
                showAlert('success', 'El registro se despublicó correctamente.', 'Despublicado');
            } else {
                await publishRelatedInformation(id);
                showAlert('success', 'El registro se publicó correctamente.', 'Publicado');
            }
        } catch {
            setItems((prev) => prev.map((f) => (f.id === id ? { ...f, isActive: current } : f)));
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
            setItems((prev) => prev.filter((f) => f.id !== id));
            await Promise.all([deleteRelatedInformation(id), sleep(300)]);
            showAlert('success', 'El registro se eliminó correctamente.', 'Eliminado');
        } catch {
            showAlert('error', 'No se pudo eliminar el registro.', 'Error');
            setCriteria((c) => ({ ...c }));
        } finally {
            setOpLoading(false);
        }
    };

    const handleResetFilters = () => {
        setRelatedInfoId('');
        setBizUnit('');
        setCountry('');
        setCriteria({ id: '', bizUnit: '', country: '' });
        setPage(1);
        setOpLoading(true);
    };

    const handleSearchFilters = ({ id, businessUnit, country }) => {
        setOpLoading(true);
        setCriteria({
            id: id ?? '',
            bizUnit: id ? '' : businessUnit ?? '',
            country: id ? '' : country ?? '',
        });
        setPage(1);
    };


    const filtered = useMemo(() => items, [items]);
    const totalPages = Math.max(1, Math.ceil(filtered.length / perPage));
    const start = (page - 1) * perPage;
    const rows = filtered.slice(start, start + perPage);


    const breadcrumbItems = fromMaintainer
        ? [
            { label: 'Inicio', to: '/' },
            { label: 'Centro de ayuda', to: '/' },
            { label: 'Mantenedor', to: '/mantenedor' },
            { label: 'Información relacionada' },
        ]
        : [
            { label: 'Inicio', to: '/' },
            { label: 'Centro de ayuda', to: '/' },
            { label: 'Información relacionada' },
        ];

    return (
        <div className="ri-container">

            <div className="ri-breadcrumb">
                <Breadcrumb items={breadcrumbItems} />
            </div>

            <div className="ri-card">
                <div className="ri-card-inner">

                    <div className="ri-header">
                        <div>
                            <h3 className="ri-title">Módulo de información relacionada</h3>
                            <p className="ri-subtitle">Búsqueda por unidad de negocio o país.</p>
                            <p className="ri-count">{filtered.length} ítems encontrados</p>
                        </div>

                        <div className="ri-add-btn">
                            <GenericButton
                                onClick={() =>
                                    nav(withOrigin('/relatedInformation/new'), { state: stateOrigin })
                                }
                            >
                                + Agregar nueva
                            </GenericButton>
                        </div>
                    </div>

                    <RelatedInformationGridToolbar
                        relatedInfoId={relatedInfoId}
                        relatedInfoIds={relatedInfoIds}
                        onRelatedInfoIdChange={setRelatedInfoId}
                        bizUnit={bizUnit}
                        bizUnits={bizUnits}
                        onBizUnitChange={setBizUnit}
                        country={country}
                        countries={countries}
                        onCountryChange={setCountry}
                        onSearchInput={handleSearchFilters}
                        onReset={handleResetFilters}
                    />

                    <RelatedInformationTable
                        loading={loading}
                        rows={rows}
                        buOptions={bizUnits}
                        countryOptions={countries}
                        emptyLabel="Sin resultados"
                        perPage={perPage}
                        page={page}
                        totalPages={totalPages}
                        onChangePerPage={(n) => {
                            setPerPage(n);
                            setPage(1);
                        }}
                        onChangePage={(n) => setPage(n)}
                        onShow={(r) => nav(withOrigin(`/relatedInformation/${r.id}`), { state: stateOrigin })}
                        onEdit={(id) => nav(withOrigin(`/relatedInformation/${id}/edit`), { state: stateOrigin })}
                        onDelete={handleAskDelete}
                        onTogglePublished={handleTogglePublished}
                    />

                    <div className="ri-footer">
                        {fromMaintainer ? (
                            <GenericButton
                                variant="text"
                                onClick={() => nav('/mantenedor')}
                                className="ri-backlink"
                            >
                                ← Volver al Mantenedor
                            </GenericButton>
                        ) : (
                            <GenericButton
                                variant="text"
                                onClick={() => nav(-1)}
                                className="ri-backlink"
                            >
                                Volver
                            </GenericButton>
                        )}
                    </div>


                </div>
            </div>

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
        </div>
    );
}
