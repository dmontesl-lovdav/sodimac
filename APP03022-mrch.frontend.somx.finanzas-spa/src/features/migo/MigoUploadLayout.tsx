import { ReactElement, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Breadcrumb, GenericModal, GenericButton, GenericDropzone } from '@shared/components/ui';
import { Title } from '@/shared/components/ui/misc';
import SuccessMessage from '@/shared/components/ui/alerts/SuccessMessage';
import ErrorMessage from '@/shared/components/ui/alerts/ErrorMessage';

import { migoService } from './api/MigoClient';

import './styles/MigoContainer.css';

interface InvalidRow {
    row: number;
    errors: string[];
}

interface UploadSummary {
    totalRows: number;
    totalValid: number;
    totalInvalid: number;
}

export default function MigoUploadLayout(): ReactElement {
    const navigate = useNavigate();

    const [file, setFile] = useState<File | null>(null);
    const [uploading, setUploading] = useState(false);
    const [globalError, setGlobalError] = useState('');
    const [summary, setSummary] = useState<UploadSummary | null>(null);
    const [invalidRows, setInvalidRows] = useState<InvalidRow[]>([]);
    const [successMsg, setSuccessMsg] = useState('');

    const resetState = () => {
        setGlobalError('');
        setSummary(null);
        setInvalidRows([]);
        setSuccessMsg('');
    };

    const handleFileSelect = useCallback((f: File | null) => {
        resetState();
        if (f) {
            const ext = f.name.split('.').pop()?.toLowerCase();
            if (ext !== 'csv') {
                setGlobalError('Solo se permiten archivos en formato CSV.');
                return;
            }
        }
        setFile(f);
    }, []);

    const handleUpload = async () => {
        if (!file) return;

        setUploading(true);
        resetState();

        try {
            const res: any = await migoService.uploadCsv(file);

            if (res.success) {
                const data = res.data;
                setSummary(data?.summary ?? null);
                setInvalidRows(data?.invalidRows ?? []);
                setSuccessMsg(res.message || 'Archivo procesado exitosamente.');
                setFile(null);
            } else {
                setGlobalError(res.message || 'Error al procesar el archivo.');
            }
        } catch (err: any) {
            const response = err?.response?.data;
            setGlobalError(response?.message || 'Error al subir el archivo.');
        } finally {
            setUploading(false);
        }
    };

    return (
        <div className="migo-layout">
            <Breadcrumb
                items={[
                    { label: 'Finanzas', to: '/' },
                    { label: 'Publicación de recepción MIGO', to: '/finanzas/migo' },
                    { label: 'Publicar OC' },
                ]}
            />

            <div className="migo-box">
                <Title
                    title="Publicar Archivo de Recepciones MIGO"
                    description="Seleccione un archivo CSV con el layout de recepciones MIGO para publicar."
                />

                <div className="migo-upload-section">
                    <p className="migo-upload-hint">Cabecera obligatoria del CSV (12 columnas):</p>
                    <p className="migo-upload-columns">
                        Nro_OC, Nro_Recepcion, Sucursal, Nro_Guia, Origen, Fecha_Recepcion,
                        Importe_sin_impuesto, SKU, Descripcion_Sku, Cantidad, Importe_Unitario,
                        Importe_SinImpuesto
                    </p>
                    <p className="migo-upload-hint" style={{ fontSize: 11, color: '#9ca3af' }}>
                        Columna opcional: MontoOC (si se incluye, se valida cuadratura OC vs suma de importes)
                    </p>

                    {!summary && (
                        <GenericDropzone
                            file={file}
                            onFileSelect={handleFileSelect}
                            accept=".csv"
                            maxSizeMb={10}
                        />
                    )}

                    <ErrorMessage message={globalError} />

                    {summary && (
                        <div className="migo-summary-result">
                            <h4>Resultado de la validación</h4>
                            <SuccessMessage message={successMsg} />
                            <div className="migo-summary-stats">
                                <div className="migo-stat">
                                    <span className="migo-stat-label">Total de registros:</span>
                                    <span className="migo-stat-value">{summary.totalRows}</span>
                                </div>
                                <div className="migo-stat">
                                    <span className="migo-stat-label">Total válidos:</span>
                                    <span className="migo-stat-value migo-stat-valid">{summary.totalValid}</span>
                                </div>
                                <div className="migo-stat">
                                    <span className="migo-stat-label">Total incorrectos:</span>
                                    <span className="migo-stat-value migo-stat-invalid">{summary.totalInvalid}</span>
                                </div>
                            </div>

                            {invalidRows.length > 0 && (
                                <div className="migo-errors-list">
                                    <h4>Detalle de registros incorrectos ({invalidRows.length})</h4>
                                    <ul>
                                        {invalidRows.map((r, i) => (
                                            <li key={i}>
                                                <strong>Fila {r.row}:</strong> {r.errors.join(' | ')}
                                            </li>
                                        ))}
                                    </ul>
                                </div>
                            )}
                        </div>
                    )}

                    <div className="migo-upload-actions">
                        <GenericButton variant="outline" onClick={() => navigate('/finanzas/migo')}>
                            Regresar
                        </GenericButton>
                        {!summary && (
                            <GenericButton
                                variant="primary"
                                onClick={handleUpload}
                                disabled={!file || uploading}
                            >
                                {uploading ? 'Publicando...' : 'Publicar'}
                            </GenericButton>
                        )}
                    </div>
                </div>

                {uploading && <GenericModal visible variant="loading" message="Validando y publicando archivo..." />}
            </div>
        </div>
    );
}
