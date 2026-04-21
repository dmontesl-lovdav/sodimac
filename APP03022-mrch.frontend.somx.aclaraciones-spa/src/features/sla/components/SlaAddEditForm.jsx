import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { globalHomeStore } from '@/store/globalStore';

import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';

import {
    Breadcrumb,
    GenericButton,
    GenericModal,
    GenericInput,
    GenericSelectFloating,
} from '@shared/components/ui';

import '../styles/SlaAddEditForm.css';

function applyTenantToSla(tenant, businessUnits, countries, setData) {
    const buDesc = tenant?.commerce?.description?.toUpperCase();
    const ctDesc = tenant?.country?.description?.toUpperCase();

    if (!buDesc || !ctDesc) return;
    if (!businessUnits.length || !countries.length) return;

    const bu = businessUnits.find(
        b => b.description?.toUpperCase() === buDesc
    );

    const ct = countries.find(
        c => c.description?.toUpperCase() === ctDesc
    );

    if (bu?.id && ct?.id) {
        setData(p => ({
            ...p,
            businessUnit: bu.id,
            country: ct.id,
        }));
    }
}

export default function SlaAddEditForm() {
    const CATALOG_BUSINESSUNITS = 1;
    const CATALOG_COUNTRIES = 2;
    const CATALOG_MODULES = 3;
    const CATALOG_REASONS = 4;
    const CATALOG_PRIORITIES = 13;
    const CATALOG_FIRSTRESPONSELEVEL = 21;
    const CATALOG_RESOLUTIONLEVEL = 22;

    const BUSINESS_UNIT_COUNTRY_MAP = {
        1: [6, 5, 48],
        2: [46, 47, 6, 5, 4, 48, 50],
        3: [6, 48],
    };

    const [businessUnits, setBusinessUnits] = useState([]);
    const [countries, setCountries] = useState([]);
    const [modules, setModules] = useState([]);
    const [reasons, setReasons] = useState([]);
    const [priorities, setPriorities] = useState([]);
    const [firstResponseLevels, setFirstResponseLevels] = useState([]);
    const [resolutionLevels, setResolutionLevels] = useState([]);

    const [data, setData] = useState({
        businessUnit: 0,
        country: 0,
        module: 0,
        reason: 0,
        priority: 0,
        firstResponseLevel: 0,
        resolutionLevel: 0,
        manager: '',
    });

    const filteredCountries = data.businessUnit
        ? countries.filter(c =>
            BUSINESS_UNIT_COUNTRY_MAP[data.businessUnit]?.includes(c.id)
        )
        : [];

    const [isSaving, setIsSaving] = useState(false);
    const [isDownloading] = useState(false);
    const [isLoading, setIsLoading] = useState(false);

    const [alertOpen, setAlertOpen] = useState(false);
    const [alertCfg, setAlertCfg] = useState({
        title: '',
        message: '',
        severity: 'info',
        onCloseExtra: null,
    });

    const showAlert = ({ title, message, severity, onClose }) => {
        setAlertCfg({
            title,
            message,
            severity,
            onCloseExtra: onClose || null,
        });
        setAlertOpen(true);
    };

    const closeAlert = () => {
        setAlertOpen(false);
        alertCfg.onCloseExtra?.();
    };

    const nav = useNavigate();
    const { id } = useParams();
    const isEdit = Boolean(id);
    const api = ConfigurationBuilder.client;

    const getErrorMessage = (err) => {
        const msg =
            err?.response?.data?.message ||
            err?.response?.data?.error ||
            err?.data?.message;

        if (msg === 'SLA_ALREADY_EXISTS_FOR_MODULE') {
            return 'Ya existe un SLA configurado para este módulo.';
        }

        return msg || err?.message || 'Ocurrió un error inesperado.';
    };

    const normalizeCatalog = (value) => {
        if (Array.isArray(value)) return value;
        try {
            const parsed = JSON.parse(value);
            return Array.isArray(parsed) ? parsed : [];
        } catch {
            return [];
        }
    };

    useEffect(() => {
        (async () => {
            try {
                setIsLoading(true);

                const [bu, co, mo, re, pr, fr, rl] = await Promise.all([
                    api.getCatalog(CATALOG_BUSINESSUNITS),
                    api.getCatalog(CATALOG_COUNTRIES),
                    api.getCatalog(CATALOG_MODULES),
                    api.getCatalog(CATALOG_REASONS),
                    api.getCatalog(CATALOG_PRIORITIES),
                    api.getCatalog(CATALOG_FIRSTRESPONSELEVEL),
                    api.getCatalog(CATALOG_RESOLUTIONLEVEL),
                ]);

                setBusinessUnits(normalizeCatalog(bu));
                setCountries(normalizeCatalog(co));
                setModules(normalizeCatalog(mo));
                setReasons(normalizeCatalog(re));
                setPriorities(normalizeCatalog(pr));
                setFirstResponseLevels(normalizeCatalog(fr));
                setResolutionLevels(normalizeCatalog(rl));

                if (isEdit) {
                    const loaded = await api.getSla(id);
                    setData({
                        businessUnit: loaded.businessUnit ?? 0,
                        country: loaded.country ?? 0,
                        module: loaded.module ?? 0,
                        reason: loaded.reason ?? 0,
                        priority: loaded.priority ?? 0,
                        firstResponseLevel: loaded.firstResponseLevel ?? 0,
                        resolutionLevel: loaded.resolutionLevel ?? 0,
                        manager: loaded.manager ?? '',
                    });
                }
            } catch (err) {
                showAlert({
                    title: 'No se pudo cargar',
                    message: getErrorMessage(err),
                    severity: 'error',
                    onClose: () => nav(-1),
                });
            } finally {
                setIsLoading(false);
            }
        })();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    useEffect(() => {
        if (!globalHomeStore?.GetGlobalState) return;

        const gs = globalHomeStore.GetGlobalState('aclaraciones');
        const tenant = gs?.configuration?.selectedTenant;

        if (!tenant) return;

        applyTenantToSla(
            tenant,
            businessUnits,
            countries,
            setData
        );
    }, [businessUnits, countries]);

    useEffect(() => {
        function handleCountryChanged() {
            const gs = globalHomeStore?.GetGlobalState?.('aclaraciones');
            const tenant = gs?.configuration?.selectedTenant;
            if (!tenant) return;

            applyTenantToSla(
                tenant,
                businessUnits,
                countries,
                setData
            );
        }

        window.addEventListener('country-changed', handleCountryChanged);
        return () => window.removeEventListener('country-changed', handleCountryChanged);
    }, [businessUnits, countries]);


    useEffect(() => {
        const handler = () => {
            nav('/slas');
        };
        window.addEventListener('country-changed', handler);
        return () => window.removeEventListener('country-changed', handler);
    }, [nav]);

    useEffect(() => {
        if (
            data.businessUnit &&
            !BUSINESS_UNIT_COUNTRY_MAP[data.businessUnit]?.includes(data.country)
        ) {
            setData(p => ({ ...p, country: 0 }));
        }
    }, [data.businessUnit]);

    const updateNumberField = (name) => (e) => {
        const v = e.target.value;
        setData((p) => ({ ...p, [name]: v ? Number(v) : 0 }));
    };

    const updateTextField = (name) => (e) => {
        setData((p) => ({ ...p, [name]: e.target.value }));
    };

    const handleSubmit = async () => {
        if (isSaving) return;
        try {
            setIsSaving(true);

            if (isEdit) {
                await api.putSla(Number(id), data);
                showAlert({
                    title: 'SLA actualizado',
                    message: 'Cambios guardados correctamente.',
                    severity: 'success',
                    onClose: () => nav(-1),
                });
            } else {
                const newId = await api.postSla(data);
                showAlert({
                    title: 'SLA creado',
                    message: `Se creó el elemento (id: ${newId}).`,
                    severity: 'success',
                    onClose: () => nav(-1),
                });
            }
        } catch (err) {
            showAlert({
                title: 'Error al guardar',
                message: getErrorMessage(err),
                severity: 'error',
            });
        } finally {
            setIsSaving(false);
        }
    };

    const toOptions = (arr) =>
        (arr ?? [])
            .sort((a, b) => Number(a.description) - Number(b.description))
            .map(({ id, description }) => ({
                value: String(id),
                label: description,
            }));

    const emailValid =
        /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.manager);

    const formValid =
        data.businessUnit > 0 &&
        data.country > 0 &&
        data.module > 0 &&
        data.reason > 0 &&
        data.priority > 0 &&
        data.firstResponseLevel > 0 &&
        data.resolutionLevel > 0 &&
        data.manager.trim().length > 0 &&
        emailValid;

    const loadingMessage = isSaving
        ? 'Guardando…'
        : isDownloading
            ? 'Descargando archivo…'
            : 'Cargando…';

    const showProcessing = isSaving || isLoading || isDownloading;

    return (
        <>
            <GenericModal
                visible={showProcessing}
                variant="loading"
                message={loadingMessage}
            />

            <GenericModal
                visible={alertOpen}
                variant="alert"
                title={alertCfg.title}
                message={alertCfg.message}
                severity={alertCfg.severity}
                onClose={closeAlert}
            />

            <div className="sla-form-layout">
                <div className="sla-form-breadcrumb">
                    <Breadcrumb
                        items={[
                            { label: 'Centro de ayuda', to: '/' },
                            { label: 'Mantenedores', to: '/mantenedor' },
                            { label: 'SLAs', to: '/slas' },
                            { label: isEdit ? 'Editar SLA' : 'Agregar SLA' },
                        ]}
                    />
                </div>

                <div className="sla-form-box">
                    <div className="sla-form-inner">
                        <div className="sla-form-container">
                            <h3 className="sla-form-title">
                                Configuración de SLA
                            </h3>

                            <section className="sla-form-fields">
                                {/* <GenericSelectFloating
                                    label="Unidad de Negocio"
                                    value={String(data.businessUnit || '')}
                                    onChange={updateNumberField('businessUnit')}
                                    options={toOptions(businessUnits)}
                                    required
                                />

                                <GenericSelectFloating
                                    label="País"
                                    value={String(data.country || '')}
                                    onChange={updateNumberField('country')}
                                    options={toOptions(filteredCountries)}
                                    required
                                    disabled={!data.businessUnit}
                                /> */}

                                <GenericSelectFloating
                                    label="Módulo"
                                    value={String(data.module || '')}
                                    onChange={updateNumberField('module')}
                                    options={toOptions(modules)}
                                    required
                                />

                                <GenericSelectFloating
                                    label="Tipo de motivo"
                                    value={String(data.reason || '')}
                                    onChange={updateNumberField('reason')}
                                    options={toOptions(reasons)}
                                    required
                                />

                                <GenericSelectFloating
                                    label="Prioridad"
                                    value={String(data.priority || '')}
                                    onChange={updateNumberField('priority')}
                                    options={toOptions(priorities)}
                                    required
                                />

                                <GenericSelectFloating
                                    label="Responder en"
                                    value={String(data.firstResponseLevel || '')}
                                    onChange={updateNumberField('firstResponseLevel')}
                                    options={toOptions(firstResponseLevels)}
                                    required
                                />

                                <GenericSelectFloating
                                    label="Solucionar en"
                                    value={String(data.resolutionLevel || '')}
                                    onChange={updateNumberField('resolutionLevel')}
                                    options={toOptions(resolutionLevels)}
                                    required
                                />

                                <GenericInput
                                    label="Mail de escalamiento"
                                    placeholder="mail@falabella.cl"
                                    value={data.manager}
                                    onChange={updateTextField('manager')}
                                    maxLength={128}
                                    required
                                    type="email"
                                    validateEmail
                                    helperText="Ingresa un correo válido, por ejemplo: usuario@falabella.com"
                                />
                            </section>

                            <div className="sla-form-footer">
                                <GenericButton
                                    variant="text"
                                    type="button"
                                    onClick={() => nav(-1)}
                                >
                                    Volver
                                </GenericButton>

                                <GenericButton
                                    type="button"
                                    onClick={handleSubmit}
                                    disabled={!formValid || isSaving}
                                >
                                    {isSaving
                                        ? isEdit
                                            ? 'Actualizando…'
                                            : 'Guardando…'
                                        : isEdit
                                            ? 'Actualizar'
                                            : 'Guardar'}
                                </GenericButton>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
}
