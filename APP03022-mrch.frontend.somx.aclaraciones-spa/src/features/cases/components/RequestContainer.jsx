// src/features/cases/components/RequestContainer.jsx
import { useEffect, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';

import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';
import { loadCatalog } from './RequestUtils';

import RequestForm from './RequestForm';
import RequestGrid from './RequestGrid';
import RequestSummary from './RequestSummary';

import { useAppSelector } from '../../../store/hooks/useAppSelector';
import { Breadcrumb, GenericLinearProgress } from '@/shared/components/ui';

export default function RequestContainer({ initialState, requestId }) {
    const STATE_LOADING = 1;
    const STATE_FORM_LOADED = 2;
    const STATE_GRID_LOADED = 3;
    const STATE_SUMMARY_LOADED = 4;

    const CATALOGS_BUSINESSUNITS = 1;
    const CATALOGS_COUNTRIES = 2;
    const CATALOGS_MODULES = 3;
    const CATALOGS_REASONS = 4;
    const CATALOGS_DETAILS = 5;

    const apiClient = ConfigurationBuilder.client;
    const navigate = useNavigate();
    const location = useLocation();
    const params = useParams();

    const fromMaintainer =
        location.state?.fromMaintainer ||
        new URLSearchParams(location.search).get('from') === 'mantenedor';

    const linkWithOrigin = (path) => (fromMaintainer ? `${path}?from=mantenedor` : path);

    const isCreateRoute = location.pathname.endsWith('/cases/new');

    const [businessUnits, setBusinessUnits] = useState(false);
    const [countries, setCountries] = useState(false);
    const [modules, setModules] = useState(false);
    const [reasons, setReasons] = useState(false);
    const [details, setDetails] = useState(false);

    const _initialState = initialState ?? STATE_SUMMARY_LOADED;
    const [state, setState] = useState(STATE_LOADING);
    const [_requestId, setRequestId] = useState(requestId);

    const tokenDecoded = useAppSelector(({ authentication }) => authentication.tokenDecoded);

    useEffect(() => {
        if (params?.id) setRequestId(params.id);
    }, [params?.id]);

    async function waitForCatalogs(targetState) {
        let ok = true;
        setState(STATE_LOADING);

        ok = (ok && (await loadCatalog(apiClient, CATALOGS_BUSINESSUNITS, setBusinessUnits))) ?? ok;
        ok = (ok && (await loadCatalog(apiClient, CATALOGS_COUNTRIES, setCountries))) ?? ok;
        ok = (ok && (await loadCatalog(apiClient, CATALOGS_MODULES, setModules))) ?? ok;
        ok = (ok && (await loadCatalog(apiClient, CATALOGS_REASONS, setReasons))) ?? ok;
        ok = (ok && (await loadCatalog(apiClient, CATALOGS_DETAILS, setDetails))) ?? ok;

        const next = isCreateRoute ? STATE_FORM_LOADED : targetState;
        setState(ok ? next : STATE_LOADING);
    }

    useEffect(() => {
        if (state === STATE_LOADING) waitForCatalogs(_initialState);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [state, isCreateRoute]);

    const goBack = () => {
        if (fromMaintainer && state === STATE_GRID_LOADED) {
            navigate('/mantenedor');
            return;
        }
        navigate(-1);
    };

    const handleGoToGrid = (e) => {
        e.preventDefault();
        setState(STATE_GRID_LOADED);
        navigate('/cases');
    };

    function buildBreadcrumb() {
        const items = [{ label: 'Centro de ayuda', to: '/' }];

        if (fromMaintainer) items.push({ label: 'Mantenedor', to: '/mantenedor' });

        switch (state) {
            case STATE_FORM_LOADED:
                items.push({
                    label: 'Mis solicitudes',
                    to: linkWithOrigin('/cases'),
                });
                items.push({ label: 'Crear nuevo caso' });
                break;

            case STATE_GRID_LOADED:
                items.push({ label: 'Mis solicitudes' });
                break;

            case STATE_SUMMARY_LOADED:
                items.push({
                    label: 'Mis solicitudes',
                    to: linkWithOrigin('/cases'),
                });
                items.push({ label: `Solicitud ${_requestId ?? ''}` });
                break;

            default:
                break;
        }

        return <Breadcrumb items={items} />;
    }

    // 🔥 Este efecto fuerza la funcionalidad del click en "Mis solicitudes"
    useEffect(() => {
        const breadcrumbLink = document.querySelector('a[href="#/cases"], a[href="/cases"]');
        if (breadcrumbLink) {
            breadcrumbLink.addEventListener('click', (e) => {
                e.preventDefault();
                setState(STATE_GRID_LOADED);
                navigate('/cases');
            });
        }
        return () => {
            if (breadcrumbLink)
                breadcrumbLink.removeEventListener('click', () => { });
        };
    }, [state, navigate]);

    // ────────────────────────────────────────────────
    switch (state) {
        case STATE_LOADING:
        default:
            return (
                <div>
                    <GenericLinearProgress
                        indeterminate
                        value={1}
                        max={3}
                        buffer={1.5}
                        fullWidth
                    />
                </div>
            );

        case STATE_FORM_LOADED:
            return (
                <>
                    {buildBreadcrumb()}
                    <RequestForm
                        backCallback={goBack}
                        businessUnits={businessUnits}
                        countries={countries}
                        modules={modules}
                        reasons={reasons}
                    />
                </>
            );

        case STATE_SUMMARY_LOADED:
            return (
                <>
                    {buildBreadcrumb()}
                    <RequestSummary
                        requestId={_requestId}
                        backCallback={() => setState(STATE_GRID_LOADED)}
                        businessUnits={businessUnits}
                        countries={countries}
                        modules={modules}
                        reasons={reasons}
                        details={details}
                    />
                </>
            );

        case STATE_GRID_LOADED:
            return (
                <>
                    {buildBreadcrumb()}
                    <RequestGrid
                        reasons={reasons}
                        onShowHelper={(rid) => {
                            setRequestId(rid);
                            setState(STATE_SUMMARY_LOADED);
                        }}
                    />
                </>
            );
    }
}
