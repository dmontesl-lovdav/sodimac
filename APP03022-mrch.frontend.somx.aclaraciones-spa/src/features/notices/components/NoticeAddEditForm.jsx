import { useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';

import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';
import { loadCatalog } from '@features/cases/components/RequestUtils';
import { globalHomeStore } from '@/store/globalStore';

import {
    Breadcrumb,
    GenericButton,
    GenericInput,
    GenericLinearProgress,
    GenericSelectFloating,
} from '@shared/components/ui';

import '../styles/NoticeAddEditForm.css';

/* 🔹 RESOLVER IDs DESDE CATÁLOGOS POR NOMBRE */
function resolveTenantIds(tenant, businessUnits, countries) {
    const buDesc = tenant?.commerce?.description?.toUpperCase();
    const ctDesc = tenant?.country?.description?.toUpperCase();

    const bu = businessUnits.find(
        b => b.description?.toUpperCase() === buDesc
    );

    const ct = countries.find(
        c => c.description?.toUpperCase() === ctDesc
    );

    return {
        businessUnitId: bu?.id ? String(bu.id) : '',
        countryId: ct?.id ? String(ct.id) : '',
    };
}

export default function NoticeAddEditForm() {
    const CATALOGS_BUSINESSUNITS = 1;
    const CATALOGS_COUNTRIES = 2;
    const CATALOGS_POSITIONS = 3;

    const STATE = { LOADED: 1, LOADING: 2 };

    const { id } = useParams();
    const location = useLocation();
    const nav = useNavigate();
    const api = ConfigurationBuilder.client;

    const fromMaintainer =
        location.state?.fromMaintainer ||
        new URLSearchParams(location.search).get('from') === 'mantenedor';

    const withOrigin = (pathname) => ({
        pathname,
        search: fromMaintainer ? '?from=mantenedor' : '',
    });

    const stateOrigin = fromMaintainer ? { fromMaintainer: true } : undefined;

    const [name, setName] = useState('');
    const [description, setDescription] = useState('');
    const [link, setLink] = useState('');
    const [position, setPosition] = useState('');

    const [businessUnit, setBusinessUnit] = useState('');
    const [country, setCountry] = useState('');

    const [businessUnits, setBusinessUnits] = useState([]);
    const [countries, setCountries] = useState([]);
    const [positions, setPositions] = useState([]);

    const [state, setState] = useState(STATE.LOADING);

    async function loadEdit() {
        if (!id) return;
        const notice = await api.getNotice(id);

        setName(notice.name ?? '');
        setDescription(notice.description ?? '');
        setLink(notice.link ?? '');
        setPosition(notice.position ? String(notice.position) : '');

        setBusinessUnit(notice.businessUnit ? String(notice.businessUnit) : '');
        setCountry(notice.country ? String(notice.country) : '');
    }

    /* 🔹 cargar catálogos + edit */
    useEffect(() => {
        (async () => {
            try {
                setState(STATE.LOADING);

                await loadCatalog(api, CATALOGS_BUSINESSUNITS, setBusinessUnits);
                await loadCatalog(api, CATALOGS_COUNTRIES, setCountries);
                await loadCatalog(api, CATALOGS_POSITIONS, setPositions);

                await loadEdit();
            } finally {
                setState(STATE.LOADED);
            }
        })();
    }, []);

    /* 🔹 aplicar tenant (por descripción) */
    useEffect(() => {
        const gs = globalHomeStore?.GetGlobalState?.('aclaraciones');
        const tenant = gs?.configuration?.selectedTenant;
        if (!tenant) return;
        if (!businessUnits.length || !countries.length) return;

        const { businessUnitId, countryId } =
            resolveTenantIds(tenant, businessUnits, countries);

        if (businessUnitId) setBusinessUnit(businessUnitId);
        if (countryId) setCountry(countryId);
    }, [businessUnits, countries]);

    useEffect(() => {
        if (!id) return;

        function handleTenantChange() {
            fromMaintainer
                ? nav(withOrigin('/notices'), { state: stateOrigin })
                : nav(-1);
        }

        window.addEventListener('country-changed', handleTenantChange);
        return () => window.removeEventListener('country-changed', handleTenantChange);
    }, [id, fromMaintainer, nav, stateOrigin]);

    /* 🔹 VALIDACIÓN */
    const formIsValid =
        name.trim().length > 2 &&
        description.trim().length > 2 &&
        link.trim().length > 2 &&
        Number(position) > 0;

    async function handleSave() {
        setState(STATE.LOADING);
        try {
            const notice = {
                businessUnit: Number(businessUnit),
                country: Number(country),
                name: name.trim(),
                description: description.trim(),
                link: link.trim(),
                position: Number(position),
                publised: true,
            };

            id
                ? await api.putNotice(id, notice)
                : await api.postNotice(notice);

            fromMaintainer
                ? nav(withOrigin('/notices'), { state: stateOrigin })
                : nav(-1);
        } catch (e) {
            console.error(e);
            setState(STATE.LOADED);
        }
    }

    const positionOptions = useMemo(
        () => (positions ?? []).map(({ id, description }) => ({
            value: String(id),
            label: String(description),
        })),
        [positions]
    );

    const breadcrumbItems = fromMaintainer
        ? [
            { label: 'Centro de ayuda', to: '/' },
            { label: 'Mantenedor', to: '/mantenedor' },
            { label: 'Sección informativa', to: withOrigin('/notices') },
            { label: id ? 'Editar información' : 'Agregar información' },
        ]
        : [
            { label: 'Centro de ayuda', to: '/' },
            { label: 'Mantenedor de sección informativa', to: '/notices' },
            { label: id ? 'Editar información' : 'Agregar información' },
        ];

    return (
        <div className="naf-container">
            <div className="naf-breadcrumb">
                <Breadcrumb items={breadcrumbItems} />
            </div>

            {state === STATE.LOADING && (
                <GenericLinearProgress indeterminate fullWidth className="naf-progress" />
            )}

            {state === STATE.LOADED && (
                <div className="naf-card">
                    <div className="naf-card-inner">
                        <div className="naf-content">
                            <h3 className="naf-title">
                                {id ? 'Editar información' : 'Agregar información'}
                            </h3>

                            <h3 className="naf-section-title">Detalle de información</h3>

                            <GenericInput
                                className="naf-margin-top"
                                label="Título"
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                                maxLength={32}
                                required
                            />

                            <GenericInput
                                className="naf-margin-top-lg"
                                label="Párrafo"
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                                maxLength={128}
                                required
                            />

                            <GenericInput
                                className="naf-margin-top"
                                label="Enlace"
                                value={link}
                                onChange={(e) => setLink(e.target.value)}
                                maxLength={256}
                                required
                            />

                            <div className="naf-margin-top">
                                <GenericSelectFloating
                                    label="Ubicación"
                                    value={position}
                                    onChange={(e) => setPosition(e.target.value)}
                                    options={positionOptions}
                                />
                            </div>

                            <div className="naf-divider" />

                            <div className="naf-footer">
                                <GenericButton
                                    variant="text"
                                    className="naf-backlink"
                                    onClick={() =>
                                        fromMaintainer
                                            ? nav(withOrigin('/notices'), { state: stateOrigin })
                                            : nav(-1)
                                    }
                                >
                                    Volver
                                </GenericButton>

                                <GenericButton
                                    variant="primary"
                                    disabled={!formIsValid}
                                    onClick={handleSave}
                                >
                                    Guardar
                                </GenericButton>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
