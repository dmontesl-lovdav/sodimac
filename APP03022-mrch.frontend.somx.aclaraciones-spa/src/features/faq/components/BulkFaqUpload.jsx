// src/features/faq/components/BulkFaqUpload.jsx
import { useLayoutEffect, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';

import { Step, VerticalStepper } from '@/shared/components/ui/verticalStepper/VerticalStepper';
import {
    Breadcrumb,
    GenericButton,
    GenericDropzone,
    GenericModal,
} from '@shared/components/ui';

import '../styles/BulkFaqUpload.css';

const fileToUrl = (file) => Object.assign(file, { preview: URL.createObjectURL(file) });

const FILE_BADGE_RESERVED_H = 28;

export default function BulkFaqUpload() {
    const location = useLocation();
    const fromMaintainer =
        location.state?.fromMaintainer ||
        new URLSearchParams(location.search).get('from') === 'mantenedor';

    const withOrigin = (pathname) => ({
        pathname,
        search: fromMaintainer ? '?from=mantenedor' : '',
    });
    const stateOrigin = fromMaintainer ? { fromMaintainer: true } : undefined;

    const [file, setFile] = useState(null);
    const [isUploading, setUploading] = useState(false);
    const [modal, setModal] = useState({ visible: false });

    const [lineStart, setLineStart] = useState(0);
    const [lineEnd, setLineEnd] = useState(0);
    const containerRef = useRef(null);
    const badge1Ref = useRef(null);
    const badge2Ref = useRef(null);

    const nav = useNavigate();
    const api = ConfigurationBuilder.client;

    const recomputeLine = () => {
        const c = containerRef.current;
        const b1 = badge1Ref.current;
        const b2 = badge2Ref.current;

        if (!c || !b1 || !b2) return;

        const EXTRA = 12;

        setLineStart(Math.max(b1.offsetTop + b1.offsetHeight / 2 - EXTRA, 0));
        setLineEnd(
            Math.max(c.offsetHeight - (b2.offsetTop + b2.offsetHeight / 2) - EXTRA, 0)
        );
    };

    useLayoutEffect(() => {
        recomputeLine();
        window.addEventListener('resize', recomputeLine);
        return () => window.removeEventListener('resize', recomputeLine);
    }, [file]);

    const showLoading = (msg = 'Procesando…') =>
        setModal({ visible: true, variant: 'loading', message: msg });

    const showAlert = (opts) =>
        setModal({ visible: true, variant: 'alert', ...opts });

    const closeModal = () => setModal({ visible: false });

    const BASE =
        (process.env.APP_URL && process.env.APP_URL.replace(/\/+$/, '')) ||
        (typeof __webpack_public_path__ !== 'undefined'
            ? String(__webpack_public_path__).replace(/\/+$/, '')
            : window.location.origin);

    const handleDownload = () => {
        try {
            const a = document.createElement('a');
            a.href = `${BASE}/templates/faq-template.xlsx`;
            a.download = 'faq-template.xlsx';
            a.rel = 'noopener';
            document.body.appendChild(a);
            a.click();
            a.remove();
        } catch (err) {
            console.error(err);
            showAlert({
                severity: 'error',
                title: 'Error',
                message: 'No se pudo descargar la planilla.',
                onClose: closeModal,
            });
        }
    };

    const handleFileSelect = (f) => {
        const isXlsx = !!f && /\.xlsx$/i.test(f.name);
        if (f && !isXlsx) {
            showAlert({
                severity: 'warning',
                title: 'Formato incorrecto',
                message: 'Solo se permiten archivos .xlsx',
                onClose: closeModal,
            });
            setFile(null);
            return;
        }
        setFile(f ? fileToUrl(f) : null);
    };

    const handleContinue = async () => {
        if (!file || isUploading) return;

        try {
            setUploading(true);
            showLoading('Cargando preguntas…');

            await api.postFaqBulkUpload(file);

            showAlert({
                severity: 'success',
                title: '¡Completado!',
                message: 'Preguntas cargadas correctamente.',
                onClose: () => {
                    closeModal();
                    nav(withOrigin('/faqs'), { state: stateOrigin });
                },
            });
        } catch (err) {
            console.error(err);
            showAlert({
                severity: 'error',
                title: 'Error',
                message: 'Ocurrió un problema al cargar la planilla.',
                onClose: closeModal,
            });
        } finally {
            setUploading(false);
        }
    };

    const isXlsx = file && /\.xlsx$/i.test(file.name);

    const breadcrumbItems = fromMaintainer
        ? [
            { label: 'Centro de ayuda', to: '/' },
            { label: 'Mantenedor', to: '/mantenedor' },
            { label: 'Preguntas frecuentes', to: withOrigin('/faqs') },
            { label: 'Carga masiva preguntas frecuentes' },
        ]
        : [
            { label: 'Centro de ayuda', to: '/' },
            { label: 'Preguntas frecuentes', to: '/faqs' },
            { label: 'Carga masiva preguntas frecuentes' },
        ];

    return (
        <>
            <Breadcrumb items={breadcrumbItems} />

            <div className="bulk-container">

                <div className="bulk-header">
                    <h3 className="bulk-title">
                        Carga planilla para crear preguntas frecuentes
                    </h3>
                    <p className="bulk-subtitle">
                        Descarga la planilla, complétala con las preguntas y vuelve para subirla.
                    </p>
                </div>

                <VerticalStepper innerRef={containerRef}>
                    <Step innerRef={badge1Ref}>
                        <div className="bulk-step-row">
                            <div>
                                <h4 className="bulk-step-title">Paso 1</h4>
                                <p className="bulk-step-text">
                                    Descarga la planilla para preguntas frecuentes y complétala siguiendo el formato.
                                </p>
                            </div>

                            <div className="bulk-step-button">
                                <GenericButton
                                    variant="outlineFill"
                                    onClick={handleDownload}
                                >
                                    Descargar planilla preguntas frecuentes
                                </GenericButton>
                            </div>
                        </div>

                        <div className="bulk-separator" />
                    </Step>

                    <Step innerRef={badge2Ref}>
                        <div className="bulk-step-row">
                            <div>
                                <h4 className="bulk-step-title">Paso 2</h4>
                                <p className="bulk-step-text">
                                    Sube la planilla completada arrastrando el archivo o seleccionándolo manualmente.
                                </p>
                            </div>

                            <div className="bulk-dropzone-col">
                                <div
                                    style={{ height: FILE_BADGE_RESERVED_H }}
                                    aria-hidden="true"
                                />
                                <GenericDropzone
                                    accept=".xlsx, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel"
                                    maxSizeMb={4}
                                    file={file}
                                    onFileSelect={handleFileSelect}
                                />
                            </div>
                        </div>
                    </Step>
                </VerticalStepper>

                <div className="bulk-separator" />

                <div className="bulk-actions">
                    <GenericButton
                        variant="text"
                        onClick={() =>
                            fromMaintainer
                                ? nav(withOrigin('/faqs'), { state: stateOrigin })
                                : nav(-1)
                        }
                    >
                        Volver
                    </GenericButton>

                    <GenericButton
                        onClick={handleContinue}
                        disabled={!isXlsx || isUploading}
                        className={(!isXlsx || isUploading) ? 'disabled-btn' : ''}
                    >
                        {isUploading ? 'Cargando…' : 'Continuar'}
                    </GenericButton>
                </div>

            </div>

            <GenericModal {...modal} />
        </>
    );
}
