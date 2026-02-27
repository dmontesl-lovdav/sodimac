// src/features/categories/BulkCategoryUpload.jsx
import React, { useRef, useState } from 'react';
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { VerticalStepper, Step } from '@/shared/components/ui/verticalStepper/VerticalStepper';
import { GenericButton, GenericDropzone } from '@shared/components/ui';
import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';
import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';

import '../styles/BulkCategoryUpload.css';

const fileToUrl = (file) => Object.assign(file, { preview: URL.createObjectURL(file) });

export default function BulkCategoryUpload() {
    const [file, setFile] = useState(null);
    const [isUploading, setBusy] = useState(false);

    const containerRef = useRef(null);

    const nav = useNavigate();
    const api = ConfigurationBuilder.client;

    const location = useLocation();
    const [searchParams] = useSearchParams();
    const fromMaintainer =
        location.state?.fromMaintainer || searchParams.get('from') === 'mantenedor';

    const withFrom = (path) => (fromMaintainer ? `${path}?from=mantenedor` : path);
    const withState = fromMaintainer ? { fromMaintainer: true } : undefined;

    const BASE =
        (process.env.APP_URL && process.env.APP_URL.replace(/\/+$/, '')) ||
        (typeof __webpack_public_path__ !== 'undefined'
            ? String(__webpack_public_path__).replace(/\/+$/, '')
            : window.location.origin);

    const handleDownload = () => {
        try {
            const basePath =
                (typeof __webpack_public_path__ !== 'undefined'
                    ? String(__webpack_public_path__).replace(/\/+$/, '')
                    : window.location.origin);

            const filePath = `${basePath}/templates/categorias-template.xlsx`;

            const a = document.createElement('a');
            a.href = filePath;
            a.download = 'categorias-template.xlsx';
            a.rel = 'noopener';
            document.body.appendChild(a);
            a.click();
            a.remove();
        } catch (err) {
            console.error(err);
            alert('Error al descargar la plantilla.');
        }
    };

    const handleContinue = async () => {
        if (!file) {
            alert('Primero selecciona la plantilla .xlsx.');
            return;
        }
        if (!/\.xlsx$/i.test(file.name)) {
            alert('Solo se permite archivo .xlsx');
            return;
        }
        if (isUploading) return;

        try {
            setBusy(true);
            await api.postCategoryBulkUpload(file);
            alert('✅ Categorías cargadas correctamente');
            nav(withFrom('/categories'), { state: withState });
        } catch (err) {
            console.error(err);
            alert('Error al cargar la plantilla.');
        } finally {
            setBusy(false);
        }
    };

    const breadcrumbItems = [
        { label: 'Inicio', to: '/' },
        { label: 'Centro de ayuda', to: '/' },
        ...(fromMaintainer ? [{ label: 'Mantenedor', to: '/mantenedor' }] : []),
        { label: 'Mantenedor de categorías', to: withFrom('/categories') },
        { label: 'Carga masiva de categorías' },
    ];

    return (
        <>
            <Breadcrumb items={breadcrumbItems} />

            <div className="bcu-wrapper" ref={containerRef}>
                <div className="bcu-header">
                    <h3 className="bcu-title">Carga planilla para crear categorías</h3>
                    <p className="bcu-subtitle">
                        Descarga la planilla (.xlsx), complétala y vuelve para subirla.
                    </p>
                </div>

                <VerticalStepper>
                    {/* -------- PASO 1 -------- */}
                    <Step>
                        <div className="bcu-step-row">
                            <div className="bcu-step-info">
                                <h4 className="bcu-step-title">Paso 1</h4>
                                <p className="bcu-step-text">
                                    Descarga la planilla para categorías (.xlsx) y complétala siguiendo el formato.
                                </p>
                            </div>

                            <div className="bcu-step-action">
                                <GenericButton
                                    variant="outlineFill"
                                    onClick={handleDownload}
                                    className="bcu-download-btn"
                                >
                                    <DownloadIcon />
                                    Descargar planilla (.xlsx)
                                </GenericButton>
                            </div>
                        </div>

                        <div className="bcu-separator" />
                    </Step>

                    {/* -------- PASO 2 -------- */}
                    <Step>
                        <div className="bcu-step-row">
                            <div className="bcu-step-info">
                                <h4 className="bcu-step-title">Paso 2</h4>
                                <p className="bcu-step-text">
                                    Sube la planilla completada (.xlsx) arrastrando el archivo o seleccionándolo manualmente.
                                </p>
                            </div>

                            <div className="bcu-step-upload">
                                <GenericDropzone
                                    accept=".xlsx,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                    maxSizeMb={4}
                                    file={file}
                                    onFileSelect={(f) => setFile(f ? fileToUrl(f) : null)}
                                />
                            </div>
                        </div>
                    </Step>
                </VerticalStepper>

                <div className="bcu-footer">
                    <GenericButton variant="text" onClick={() => nav(-1)}>
                        Volver
                    </GenericButton>

                    <GenericButton
                        onClick={handleContinue}
                        disabled={isUploading}
                        className={isUploading ? 'bcu-disabled' : ''}
                    >
                        {isUploading ? 'Cargando…' : 'Continuar'}
                    </GenericButton>
                </div>
            </div>
        </>
    );
}

function DownloadIcon() {
    return (
        <svg viewBox="0 0 24 24" className="bcu-download-icon">
            <path d="M12 16a1 1 0 0 1-1-1V5.414L8.707 7.707a1 1 0 0 1-1.414-1.414l4-4a1 1 0 0 1 1.414 0l4 4a1 1 0 1 1-1.414 1.414L13 5.414V15a1 1 0 0 1-1 1Z" />
            <path d="M20 14a1 1 0 1 1 0 2h-2v3a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2v-3H4a1 1 0 0 1 0-2h16Z" />
        </svg>
    );
}
