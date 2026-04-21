import { useState, useMemo, useEffect } from 'react';
import { useLocation, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { Buffer } from 'buffer';

import {
    Breadcrumb,
    GenericButton,
    GenericInput,
    GenericLinearProgress,
} from '@shared/components/ui';
import AttachmentUploader from '@/shared/components/ui/attachmentUploader/AttachmentUploader';

import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';
import { loadCatalog } from '@/features/cases/components/RequestUtils';
import { globalHomeStore } from '@/store/globalStore';

import {
    createRelatedInformation,
    updateRelatedInformation,
    getRelatedInformationById,
} from '@/features/relatedInformation/api/relatedInformationService';

import '../styles/AddEditRelatedInformationForm.css';

/* 🔹 misma lógica que RequestForm */
function applyTenant({ commerce, country }, businessUnits, countries, setBusinessUnitId, setCountryId) {
    if (!commerce || !country) return;
    if (!businessUnits.length || !countries.length) return;

    const bu = businessUnits.find(b =>
        b.description?.toUpperCase().includes(
            commerce === 'TOT' ? 'TOTTUS'
                : commerce === 'SOD' ? 'SODIMAC'
                    : commerce === 'FAL' ? 'FALABELLA'
                        : commerce
        )
    );

    const COUNTRY_CODE_MAP = {
        CL: 'CHILE',
        MX: 'MEXICO',
        CO: 'COLOMBIA',
        PE: 'PERU',
        AR: 'ARGENTINA',
        BR: 'BRASIL',
        UY: 'URUGUAY',
    };

    const normalizedCountry =
        COUNTRY_CODE_MAP[country] || country?.toUpperCase();

    const ct = countries.find(c =>
        c.description?.toUpperCase() === normalizedCountry
    );

    if (bu?.id) setBusinessUnitId(String(bu.id));
    if (ct?.id) setCountryId(String(ct.id));
}

export default function AddEditRelatedInformationForm() {
    const { id: idFromRoute } = useParams();
    const [searchParams] = useSearchParams();
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

    const STATE = { LOADED: 1, LOADING: 2 };
    const [state, setState] = useState(STATE.LOADED);

    const CATALOGS_BUSINESSUNITS = 1;
    const CATALOGS_COUNTRIES = 2;

    const [title, setTitle] = useState(searchParams.get('title') ?? '');
    const [link, setLink] = useState(searchParams.get('link') ?? '');
    const [businessUnitId, setBusinessUnitId] = useState('');
    const [countryId, setCountryId] = useState('');

    const [businessUnits, setBusinessUnits] = useState([]);
    const [countries, setCountries] = useState([]);

    const [files, setFiles] = useState([]);
    const [preview, setPreview] = useState(null);
    const [hadServerImage, setHadServerImage] = useState(false);
    const [clearImage, setClearImage] = useState(false);

    useEffect(() => {
        if (files.length > 0) {
            setClearImage(false);
        } else if (hadServerImage) {
            setClearImage(true);
        }
    }, [files.length, hadServerImage]);

    /* 🔹 cargar catálogos + edit */
    useEffect(() => {
        (async () => {
            try {
                setState(STATE.LOADING);

                await loadCatalog(api, CATALOGS_BUSINESSUNITS, setBusinessUnits);
                await loadCatalog(api, CATALOGS_COUNTRIES, setCountries);

                await loadEdit();
            } finally {
                setState(STATE.LOADED);
            }
        })();
    }, []);

    /* 🔹 aplicar tenant inicial */
    useEffect(() => {
        if (!globalHomeStore?.GetGlobalState) return;

        const state = globalHomeStore.GetGlobalState('aclaraciones');
        const tenant = state?.configuration?.selectedTenant;
        if (!tenant) return;

        applyTenant(
            { commerce: tenant?.commerce?.name, country: tenant?.country?.name },
            businessUnits,
            countries,
            setBusinessUnitId,
            setCountryId
        );
    }, [businessUnits, countries]);

    /* 🔹 escuchar country-changed */
    useEffect(() => {
        function handler(e) {
            const { commerce, country } = e.detail || {};
            applyTenant(
                { commerce, country },
                businessUnits,
                countries,
                setBusinessUnitId,
                setCountryId
            );
        }

        window.addEventListener('country-changed', handler);
        return () => window.removeEventListener('country-changed', handler);
    }, [businessUnits, countries]);

    useEffect(() => {
        if (!idFromRoute) return;

        function handleTenantChange() {
            fromMaintainer
                ? nav(withOrigin('/relatedInformation'), { state: stateOrigin })
                : nav(-1);
        }

        window.addEventListener('country-changed', handleTenantChange);
        return () =>
            window.removeEventListener('country-changed', handleTenantChange);
    }, [idFromRoute, fromMaintainer, nav, stateOrigin]);

    const loadEdit = async () => {
        if (!idFromRoute) return;
        try {
            const r = await getRelatedInformationById(+idFromRoute);

            setTitle(r.title ?? '');
            setLink(r.link ?? '');
            setBusinessUnitId(r.businessUnitId ? String(r.businessUnitId) : '');
            setCountryId(r.countryId ? String(r.countryId) : '');

            if (r.image) {
                const bytes = Uint8Array.from(atob(r.image), c => c.charCodeAt(0));
                const fname = r.imageName ?? 'original.jpg';
                const fileObj = new File([bytes], fname, { type: 'image/jpeg' });
                fileObj.preview = URL.createObjectURL(fileObj);
                fileObj.__fromServer = true;
                setFiles([fileObj]);
                setPreview(fileObj.preview);
                setHadServerImage(true);
            }
        } catch {
            fromMaintainer
                ? nav(withOrigin('/relatedInformation'), { state: stateOrigin })
                : nav(-1);
        }
    };

    const formIsValid =
        title.trim().length > 2 &&
        link.trim().length > 2;

    async function handleSave() {
        try {
            setState(STATE.LOADING);

            let imageB64;
            let imageName;
            const first = files[0];
            const isUserFile = first && first instanceof File && !first.__fromServer;

            if (isUserFile) {
                imageB64 = Buffer.from(await first.arrayBuffer()).toString('base64');
                imageName = first.name;
            }

            const payload = {
                title: title.trim(),
                link: link.trim(),
                businessUnitId: Number(businessUnitId),
                countryId: Number(countryId),
            };

            if (imageB64) {
                payload.image = imageB64;
                payload.imageName = imageName;
            } else if (idFromRoute && clearImage) {
                payload.image = '';
                payload.imageName = '';
            }

            idFromRoute
                ? await updateRelatedInformation(+idFromRoute, payload)
                : await createRelatedInformation(payload);

            fromMaintainer
                ? nav(withOrigin('/relatedInformation'), { state: stateOrigin })
                : nav(-1);
        } finally {
            setState(STATE.LOADED);
        }
    }

    const breadcrumbItems = fromMaintainer
        ? [
            { label: 'Centro de ayuda', to: '/' },
            { label: 'Mantenedor', to: '/mantenedor' },
            { label: 'Información relacionada', to: withOrigin('/relatedInformation') },
            { label: idFromRoute ? 'Editar información' : 'Agregar información' },
        ]
        : [
            { label: 'Centro de ayuda', to: '/' },
            { label: 'Información relacionada', to: '/relatedInformation' },
            { label: idFromRoute ? 'Editar información' : 'Agregar información' },
        ];

    return (
        <div className="ri-form-container">
            <div className="ri-form-breadcrumb">
                <Breadcrumb items={breadcrumbItems} />
            </div>

            {state === STATE.LOADING && (
                <GenericLinearProgress indeterminate fullWidth />
            )}

            {state === STATE.LOADED && (
                <div className="ri-form-card">
                    <div className="ri-form-card-inner">
                        <h3 className="ri-form-title">
                            {idFromRoute ? 'Editar información relacionada' : 'Agregar información relacionada'}
                        </h3>

                        <h3 className="ri-form-subsection">Información relacionada</h3>

                        <GenericInput
                            className="ri-field-spacing"
                            label="Título"
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                            maxLength={64}
                            required
                        />

                        <GenericInput
                            className="ri-field-spacing"
                            label="Enlace"
                            value={link}
                            onChange={(e) => setLink(e.target.value)}
                            maxLength={256}
                            required
                        />

                        <h3 className="ri-form-subsection mt-8">Agregar imagen</h3>

                        <AttachmentUploader
                            files={files}
                            setFiles={(arr) => {
                                setFiles(arr);
                                setPreview(arr[0]?.preview ?? null);
                            }}
                            fileExtensions={['jpg', 'jpeg', 'png']}
                            fileSize={2 * 1024 * 1024}
                            multiple={false}
                        />

                        <div className="ri-form-actions">
                            <GenericButton
                                variant="text"
                                onClick={() =>
                                    fromMaintainer
                                        ? nav(withOrigin('/relatedInformation'), { state: stateOrigin })
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
            )}
        </div>
    );
}
