// src/features/relatedInformation/AddEditRelatedInformationForm.jsx
import { useState, useMemo, useEffect } from 'react';
import { useLocation, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { Buffer } from 'buffer';

import {
    Breadcrumb,
    GenericSelectFloating,
    GenericButton,
    GenericInput,
    GenericLinearProgress,
} from '@shared/components/ui';
import AttachmentUploader from '@/shared/components/ui/attachmentUploader/AttachmentUploader';

import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';
import { loadCatalog } from '@/features/cases/components/RequestUtils';

import {
    createRelatedInformation,
    updateRelatedInformation,
    getRelatedInformationById,
} from '@/features/relatedInformation/api/relatedInformationService';

import '../styles/AddEditRelatedInformationForm.css';

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

    const businessUnitOptions = useMemo(() => {
        const list = Array.isArray(businessUnits) ? businessUnits : [];
        const reordered = [
            ...list.filter(x => Number(x.id) === 3),
            ...list.filter(x => Number(x.id) !== 3),
        ];
        return reordered.map(({ id, description }) => ({
            value: String(id),
            label: String(description),
        }));
    }, [businessUnits]);

    useEffect(() => {
        if (files.length > 0) {
            setClearImage(false);
        } else if (hadServerImage) {
            setClearImage(true);
        }
    }, [files.length, hadServerImage]);

    useEffect(() => {
        (async () => {
            try {
                setState(STATE.LOADING);

                await loadCatalog(api, CATALOGS_BUSINESSUNITS, (arr) => {
                    setBusinessUnits(arr);
                });

                await loadCatalog(api, CATALOGS_COUNTRIES, (arr) => {
                    setCountries(arr);
                });

                await loadEdit();
            } catch (err) {
                console.error('Failed to load catalogs', err);
            } finally {
                setState(STATE.LOADED);
            }
        })();
    }, []);

    useEffect(() => {
        const handler = () => {
            if (fromMaintainer) {
                nav(withOrigin('/relatedInformation'), { state: stateOrigin });
            } else {
                nav(-1);
            }
        };

        window.addEventListener("country-changed", handler);
        return () => window.removeEventListener("country-changed", handler);
    }, [fromMaintainer, nav, stateOrigin]);

    const loadEdit = async () => {
        if (!idFromRoute) return;
        try {
            const r = await getRelatedInformationById(+idFromRoute);

            setTitle(r.title ?? '');
            setLink(r.link ?? '');
            setBusinessUnitId(r.businessUnitId ? String(r.businessUnitId) : '');
            setCountryId(r.countryId ? String(r.countryId) : '');

            if (r.image) {
                const bytes = Uint8Array.from(atob(r.image), (c) => c.charCodeAt(0));
                const fname = r.imageName ?? 'original.jpg';
                const fileObj = new File([bytes], fname, { type: 'image/jpeg' });
                fileObj.preview = URL.createObjectURL(fileObj);
                fileObj.__fromServer = true;
                setFiles([fileObj]);
                setPreview(fileObj.preview);
                setHadServerImage(true);
                setClearImage(false);
            } else {
                setHadServerImage(false);
                setClearImage(false);
            }
        } catch (err) {
            console.error('Failed to load record', err);
            fromMaintainer
                ? nav(withOrigin('/relatedInformation'), { state: stateOrigin })
                : nav(-1);
        }
    };

    const formIsValid =
        title.trim().length > 2 &&
        link.trim().length > 2 &&
        Number(businessUnitId) > 0 &&
        Number(countryId) > 0;

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
                businessUnitId: businessUnitId ? Number(businessUnitId) : undefined,
                countryId: countryId ? Number(countryId) : undefined,
            };

            if (imageB64) {
                payload.image = imageB64;
                payload.imageName = imageName;
            } else if (idFromRoute && clearImage) {
                payload.image = '';
                payload.imageName = '';
            }

            if (idFromRoute) {
                await updateRelatedInformation(+idFromRoute, payload);
            } else {
                await createRelatedInformation(payload);
            }

            fromMaintainer
                ? nav(withOrigin('/relatedInformation'), { state: stateOrigin })
                : nav(-1);
        } catch (err) {
            console.error('Save failed', err);
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
                <GenericLinearProgress
                    indeterminate
                    value={1}
                    max={3}
                    buffer={1.5}
                    fullWidth
                    className="ri-form-progress"
                />
            )}

            {state === STATE.LOADED && (
                <div className="ri-form-card">

                    <div className="ri-form-card-inner">

                        <div className="ri-form-header">
                            <h3 className="ri-form-title">
                                {idFromRoute
                                    ? 'Editar información relacionada'
                                    : 'Agregar información relacionada'}
                            </h3>
                        </div>

                        <h3 className="ri-form-subsection">Selecciona tu unidad de negocio y país</h3>

                        <div className="ri-field-spacing">
                            <GenericSelectFloating
                                label="Unidad de Negocio"
                                value={businessUnitId}
                                onChange={(e) => setBusinessUnitId(e.target.value)}
                                options={businessUnitOptions}
                            />
                        </div>

                        <div className="ri-field-spacing">
                            <GenericSelectFloating
                                label="País"
                                value={countryId}
                                onChange={(e) => setCountryId(e.target.value)}
                                options={(countries ?? []).map(({ id, description }) => ({
                                    value: String(id),
                                    label: String(description),
                                }))}
                            />
                        </div>

                        <h3 className="ri-form-subsection mt-8">Información relacionada</h3>

                        <GenericInput
                            className="ri-field-spacing"
                            label="Título"
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                            placeholder="Orden de compra"
                            maxLength={64}
                            required
                        />

                        <GenericInput
                            className="ri-field-spacing"
                            label="Enlace"
                            value={link}
                            onChange={(e) => setLink(e.target.value)}
                            placeholder="https://..."
                            maxLength={256}
                            required
                        />

                        <h3 className="ri-form-subsection mt-8">Agregar imagen</h3>
                        <p className="ri-form-image-hint">
                            Formatos soportados: JPG - JPEG - PNG (máx. 2 MB).
                        </p>

                        <AttachmentUploader
                            files={files}
                            setFiles={(arr) => {
                                setFiles(arr);
                                if (arr.length === 0) setPreview(null);
                                else if (arr[0]?.preview) setPreview(arr[0].preview);
                            }}
                            fileExtensions={['jpg', 'jpeg', 'png']}
                            fileSize={2 * 1024 * 1024}
                            multiple={false}
                        />

                        <div className="ri-form-actions">
                            <GenericButton
                                variant="text"
                                className="ri-form-back"
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
