import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';
import { loadCatalog } from '@features/cases/components/RequestUtils';
import {
    Breadcrumb,
    GenericButton,
    GenericInput,
    GenericLinearProgress,
    GenericSelectFloating,
} from '@shared/components/ui';
import { useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';

import '../styles/NoticeAddEditForm.css';

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
    const [businessUnit, setBusinessUnit] = useState('');
    const [country, setCountry] = useState('');
    const [link, setLink] = useState('');
    const [position, setPosition] = useState('');

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
        setBusinessUnit(notice.businessUnit ? String(notice.businessUnit) : '');
        setCountry(notice.country ? String(notice.country) : '');
        setPosition(notice.position ? String(notice.position) : '');
    }

    useEffect(() => {
        (async () => {
            try {
                setState(STATE.LOADING);

                await loadCatalog(api, CATALOGS_BUSINESSUNITS, (data) => setBusinessUnits(data));
                await loadCatalog(api, CATALOGS_COUNTRIES, (data) => setCountries(data));
                await loadCatalog(api, CATALOGS_POSITIONS, (data) => setPositions(data));

                await loadEdit();
            } catch (e) {
                console.error('Failed to load catalogs/notices', e);
            } finally {
                setState(STATE.LOADED);
            }
        })();
    }, []);

    useEffect(() => {
        const handler = () => {
            if (fromMaintainer) {
                nav(withOrigin('/notices'), { state: stateOrigin });
            } else {
                nav(-1);
            }
        };

        window.addEventListener("country-changed", handler);
        return () => window.removeEventListener("country-changed", handler);
    }, [fromMaintainer, nav, stateOrigin]);

    const formIsValid =
        name.trim().length > 2 &&
        description.trim().length > 2 &&
        link.trim().length > 2 &&
        Number(businessUnit) > 0 &&
        Number(country) > 0 &&
        Number(position) > 0;

    async function handleSave() {
        setState(STATE.LOADING);
        try {
            const notice = {
                businessUnit: businessUnit ? Number(businessUnit) : undefined,
                country: country ? Number(country) : undefined,
                name: name.trim(),
                description: description.trim(),
                link: link.trim(),
                position: position ? Number(position) : undefined,
                publised: true,
            };

            if (id) {
                await api.putNotice(id, notice);
            } else {
                await api.postNotice(notice);
            }

            if (fromMaintainer) {
                nav(withOrigin('/notices'), { state: stateOrigin });
            } else {
                nav(-1);
            }
        } catch (e) {
            console.log(e);
            setState(STATE.LOADED);
        }
    }

    const businessUnitOptions = useMemo(() => {
        const list = Array.isArray(businessUnits) ? businessUnits : [];
        const reordered = [
            ...list.filter((x) => Number(x.id) === 3),
            ...list.filter((x) => Number(x.id) !== 3),
        ];
        return reordered.map(({ id, description }) => ({
            value: String(id),
            label: String(description),
        }));
    }, [businessUnits]);

    const countryOptions = useMemo(
        () => (countries ?? []).map(({ id, description }) => ({
            value: String(id),
            label: String(description),
        })),
        [countries]
    );

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
                <GenericLinearProgress
                    indeterminate
                    value={1}
                    max={3}
                    buffer={1.5}
                    fullWidth
                    className="naf-progress"
                />
            )}

            {state === STATE.LOADED && (
                <div className="naf-card">
                    <div className="naf-card-inner">
                        <div className="naf-content">
                            <h3 className="naf-title">
                                {id ? 'Editar información' : 'Agregar información'}
                            </h3>

                            <h3 className="naf-section-title">Selecciona tu unidad de negocio y país</h3>

                            <div className="naf-margin-top">
                                <GenericSelectFloating
                                    label="Unidad de Negocio"
                                    value={businessUnit}
                                    onChange={(e) => setBusinessUnit(e.target.value)}
                                    options={businessUnitOptions}
                                />
                            </div>

                            <div className="naf-margin-top">
                                <GenericSelectFloating
                                    label="País"
                                    value={country}
                                    onChange={(e) => setCountry(e.target.value)}
                                    options={countryOptions}
                                />
                            </div>

                            <h3 className="naf-section-title naf-section-margin">
                                Detalle de información
                            </h3>

                            <GenericInput
                                className="naf-margin-top"
                                label="Título"
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                                placeholder="Financieros"
                                maxLength={32}
                                required
                            />

                            <GenericInput
                                className="naf-margin-top-lg"
                                label="Párrafo"
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                                placeholder="Temas relacionados con facturas, pagos y reembolsos"
                                maxLength={128}
                                required
                            />

                            <GenericInput
                                className="naf-margin-top"
                                label="Enlace"
                                value={link}
                                onChange={(e) => setLink(e.target.value)}
                                placeholder="http://fbusinesscenter.com"
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
