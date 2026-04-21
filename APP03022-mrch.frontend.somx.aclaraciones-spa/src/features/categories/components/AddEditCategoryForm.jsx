import { useEffect, useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { Buffer } from 'buffer';
import { getCategoryById } from '@/features/categories/api/categoryService';
import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';
import {
    Breadcrumb,
    GenericMarqueeBar,
    GenericButton,
    GenericInput,
    GenericAttachmentUploader,
    GenericModal
} from '@shared/components/ui';

import './styles/AddEditCategoryForm.css';

export default function AddEditCategoryForm({ id }) {
    const STATE = { LOADED: 1, LOADING: 2 };

    const params = useParams();
    const [searchParams] = useSearchParams();
    const nav = useNavigate();
    const api = ConfigurationBuilder.client;

    // id limpio
    const _idRaw = id ?? params.id;
    const _id = _idRaw ? String(_idRaw).split('?')[0] : undefined;

    // form
    const [_name, setName] = useState('');
    const [_description, setDescription] = useState('');
    const [state, setState] = useState(STATE.LOADED);

    // files / preview
    const [files, setFiles] = useState([]);
    const [preview, setPreview] = useState(null);
    const [existingIconName, setExistingIconName] = useState(null);

    // limpiar imagen del server
    const [hadServerIcon, setHadServerIcon] = useState(false);
    const [clearIcon, setClearIcon] = useState(false);

    const [submitBlocked, setSubmitBlocked] = useState(false);
    const [lastFailedName, setLastFailedName] = useState('');


    const [alert, setAlert] = useState({
        visible: false,
        title: '',
        message: '',
        severity: 'error',
    });


    useEffect(() => {
        if (files.length > 0) {
            setClearIcon(false);
        } else if (hadServerIcon) {
            setClearIcon(true);
        }
    }, [files.length, hadServerIcon]);

    // navegación estilo "relatedInformation"
    const fromMaintainer = searchParams.get('from') === 'mantenedor';
    const withOrigin = (pathname) => ({
        pathname,
        search: fromMaintainer ? '?from=mantenedor' : '',
    });
    const stateOrigin = fromMaintainer ? { fromMaintainer: true } : undefined;

    const formIsValid = _name.trim().length > 2 && _description.trim().length > 2;

    // base64 helpers
    const cleanB64 = (s = '') => s.replace(/\s+/g, '').replace(/^data:.*;base64,/, '');
    const guessMimeFromB64 = (b64 = '') => {
        const h = b64.slice(0, 10);
        if (h.startsWith('iVBORw0KG')) return 'image/png';
        if (h.startsWith('/9j/')) return 'image/jpeg';
        if (h.startsWith('R0lGOD')) return 'image/gif';
        return 'application/octet-stream';
    };

    // cargar datos en edición
    useEffect(() => {
        let active = true;

        async function load() {
            if (!_id) return;
            try {
                setState(STATE.LOADING);
                const found = await getCategoryById(Number(_id));
                if (!active) return;

                setName(found?.name ?? '');
                setDescription(found?.description ?? '');
                setExistingIconName(found?.iconName ?? null);

                if (found?.icon) {
                    const clean = cleanB64(found.icon);
                    const bytes = Uint8Array.from(atob(clean), (c) => c.charCodeAt(0));
                    const mime = guessMimeFromB64(clean);
                    const fname = found.iconName || 'icon.bin';

                    const fileObj = new File([bytes], fname, { type: mime });
                    fileObj.preview = URL.createObjectURL(fileObj);
                    fileObj.__fromServer = true;

                    setFiles([fileObj]);
                    setPreview(fileObj.preview);
                    setHadServerIcon(true);
                    setClearIcon(false);
                } else {
                    setFiles([]);
                    setPreview(null);
                    setHadServerIcon(false);
                    setClearIcon(false);
                }
            } catch (err) {
                console.error('Error fetching category', err);
            } finally {
                if (active) setState(STATE.LOADED);
            }
        }

        load();
        return () => { active = false; };
    }, [_id]);

    useEffect(() => {
        const handler = () => {
            fromMaintainer
                ? nav(withOrigin('/categories'), { state: stateOrigin })
                : nav('/categories');
        };

        window.addEventListener("country-changed", handler);
        return () => window.removeEventListener("country-changed", handler);
    }, [fromMaintainer, nav, stateOrigin]);

    async function handleSave() {
        let icon;
        let iconName;

        const first = files[0];
        const isUserFile = first && first instanceof File && !first.__fromServer;

        if (isUserFile) {
            icon = Buffer.from(await first.arrayBuffer()).toString('base64');
            iconName = first.name ?? 'icon.bin';
        } else if (_id && clearIcon) {
            icon = '';
            iconName = '';
        }

        setState(STATE.LOADING);
        try {
            const base = {
                name: _name.trim(),
                description: _description.trim(),
                ...(icon !== undefined ? { icon } : {}),
                ...(iconName !== undefined ? { iconName } : {}),
            };

            if (_id) {
                await api.putFaqCategory(_id, base);
            } else {
                await api.postFaqCategory({
                    id: null,
                    isActive: true,
                    ...base,
                });
            }

            fromMaintainer
                ? nav(withOrigin('/categories'), { state: stateOrigin })
                : nav(-1);
        } catch (e) {
            console.error(e);

            const apiMessage =
                e?.response?.data?.message ||
                'Ocurrió un error al guardar la categoría.';

            setAlert({
                visible: true,
                title: 'No se pudo guardar',
                message: apiMessage,
                severity: 'error',
            });

            setSubmitBlocked(true);
            setLastFailedName(_name.trim());
            setState(STATE.LOADED);
        }
    }

    const breadcrumbItems = fromMaintainer
        ? [
            { label: 'Centro de ayuda', to: '/' },
            { label: 'Mantenedor', to: '/mantenedor' },
            { label: 'Categorías', to: withOrigin('/categories') },
            { label: _id ? 'Editar categoría' : 'Agregar categoría' },
        ]
        : [
            { label: 'Centro de ayuda', to: '/' },
            { label: 'Categorías', to: '/categories' },
            { label: _id ? 'Editar categoría' : 'Agregar categoría' },
        ];

    return (
        <div className="addedit-wrapper">
            <div className="addedit-neg-margin-left" style={{ marginTop: -4 }}>
                <Breadcrumb items={breadcrumbItems} />
            </div>

            {state === STATE.LOADING && <GenericMarqueeBar height={6} />}

            <div className={`addedit-box ${state === STATE.LOADING ? 'is-loading' : ''}`}>
                <div className="addedit-box-padding">
                    <div className="addedit-inner">

                        <div>
                            <h3 className="addedit-title">
                                {_id
                                    ? 'Editar categoría'
                                    : 'Agregar nuevo tema de categoría'}
                            </h3>

                            <p className="addedit-subtitle">
                            </p>
                        </div>

                        <div>
                            <h3 className="addedit-mt4" style={{ fontWeight: 'bold' }}>Ingresa la categoría</h3>

                            <GenericInput
                                className="addedit-mt2"
                                onChange={(e) => {
                                    const value = e?.target?.value ?? '';
                                    setName(value);

                                    if (submitBlocked && value.trim() !== lastFailedName) {
                                        setSubmitBlocked(false);
                                    }
                                }}
                                value={_name}
                                name="name"
                                label="Nombre de la categoría"
                                placeholder="Financieros"
                                required
                                type="text"
                                maxLength={32}
                                minLength={2}
                                autoComplete="off"
                            />

                            <GenericInput
                                className="addedit-mt4"
                                onChange={(e) => setDescription(e?.target?.value ?? '')}
                                value={_description}
                                name="description"
                                label="Descripción"
                                placeholder="Temas relacionados con facturas, pagos y reembolsos"
                                required
                                type="text"
                                maxLength={128}
                                minLength={2}
                                autoComplete="off"
                            />
                        </div>

                        {/* Agregar imagen */}
                        <div>
                            <h3 className="addedit-mt4" style={{ fontWeight: 'bold' }}>Agregar imagen</h3>
                            <div className="addedit-mt6 addedit-mb3">
                                Asegúrate de que los documentos sean legibles, estén bien iluminados y contengan solo una imagen por archivo.
                            </div>

                            {preview && files.length > 0 && (
                                <div className="addedit-preview-box">
                                    <img
                                        src={preview}
                                        alt={existingIconName || 'Icono actual'}
                                        style={{
                                            width: 72,
                                            height: 72,
                                            objectFit: 'contain',
                                            borderRadius: 6,
                                            border: '1px solid #e5e7eb',
                                        }}
                                        onError={(e) => { e.currentTarget.style.display = 'none'; }}
                                    />

                                    <GenericButton
                                        variant="text"
                                        color="secondary"
                                        onClick={() => {
                                            setFiles([]);
                                            setPreview(null);
                                        }}
                                    >
                                        Quitar imagen
                                    </GenericButton>
                                </div>
                            )}

                            <GenericAttachmentUploader
                                files={files}
                                setFiles={(arr) => {
                                    setFiles(arr);
                                    if (arr.length === 0) setPreview(null);
                                    else if (arr[0]?.preview) setPreview(arr[0].preview);
                                    else if (arr[0] instanceof File) {
                                        try {
                                            const url = URL.createObjectURL(arr[0]);
                                            arr[0].preview = url;
                                            setPreview(url);
                                        } catch { }
                                    }
                                }}
                                fileExtensions={['jpg', 'jpeg', 'png', 'gif']}
                                fileSize={1024 * 512}
                                multiple={false}
                            />
                        </div>

                        {/* Botones */}
                        <div className="addedit-actions">
                            <GenericButton
                                variant="text"
                                className="addedit-back-btn"
                                onClick={() =>
                                    fromMaintainer
                                        ? nav(withOrigin('/categories'), { state: stateOrigin })
                                        : nav(-1)
                                }
                            >
                                Volver
                            </GenericButton>

                            <GenericButton
                                variant="primary"
                                disabled={!formIsValid || submitBlocked || state === STATE.LOADING}
                                onClick={handleSave}
                            >
                                Guardar
                            </GenericButton>

                        </div>

                    </div>
                </div>
            </div>
            <GenericModal
                visible={alert.visible}
                variant="alert"
                severity={alert.severity}
                title={alert.title}
                message={alert.message}
                buttonText="Aceptar"
                onClose={() =>
                    setAlert((a) => ({ ...a, visible: false }))
                }
            />
            <GenericModal
                visible={state === STATE.LOADING}
                variant="loading"
                message="Guardando categoría…"
            />

        </div>
    );
}
