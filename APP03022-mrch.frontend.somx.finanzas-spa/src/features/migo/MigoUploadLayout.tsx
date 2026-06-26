import { ReactElement, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Breadcrumb, GenericModal, GenericButton, GenericDropzone } from '@shared/components/ui';
import { withFinanceBreadcrumb } from '@shared/components/ui/navigation/financeBreadcrumb';
import { useFinanceAlertModal } from '@/shared/hooks/useFinanceAlertModal';
import {
    FINANCE_LIST_KEYS,
    useFinanceListReturnFromDetail,
} from '@/shared/hooks';
import BackLinkButton from '@shared/components/ui/button/BackLinkButton';
import { APP_EVENT, PermissionGate } from '@shared/security';

import { migoService } from './api/MigoClient';

interface InvalidRow {
    row: number;
    errors: string[];
}

interface UploadSummary {
    totalRows: number;
    totalValid: number;
    totalInvalid: number;
}

const styles = {
    layout: {
        padding: '8px 24px 16px',
        backgroundColor: '#ffffff',
        minHeight: '100vh',
    } as const,
    box: {
        borderTop: '1px solid #e5e7eb',
        padding: '24px 0',
        marginTop: '16px',
    } as const,
    headerRow: {
        marginBottom: '20px',
    } as const,
    title: {
        fontSize: '20px',
        fontWeight: 500,
        color: '#1f2937',
        margin: '0 0 6px 0',
        lineHeight: 1.3,
    } as const,
    description: {
        fontSize: '14px',
        color: '#6b7280',
        margin: 0,
        lineHeight: 1.5,
    } as const,
    divider: {
        marginTop: '20px',
        marginBottom: '20px',
        borderTop: '1px solid #e5e7eb',
    } as const,
    section: {
        marginTop: '4px',
    } as const,
    hint: {
        fontSize: '13px',
        color: '#374151',
        margin: '12px 0 4px',
        fontWeight: 500,
    } as const,
    columns: {
        fontSize: '12px',
        color: '#6b7280',
        fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Consolas, monospace',
        margin: '0 0 12px',
        lineHeight: 1.5,
    } as const,
    columnsOptional: {
        fontSize: '11px',
        color: '#9ca3af',
        margin: '0 0 16px',
        lineHeight: 1.5,
    } as const,
    actions: {
        display: 'flex',
        gap: '16px',
        marginTop: '24px',
        justifyContent: 'flex-start',
        alignItems: 'center',
        flexWrap: 'wrap' as const,
    },
    summaryResult: {
        margin: '16px 0',
        padding: '16px',
        background: '#f9fafb',
        border: '1px solid #e5e7eb',
        borderRadius: '8px',
    } as const,
    summaryTitle: {
        margin: '0 0 12px',
        fontSize: '15px',
        fontWeight: 600,
        color: '#1f2937',
    } as const,
    successBanner: {
        backgroundColor: '#dcfce7',
        color: '#166534',
        border: '1px solid #bbf7d0',
        padding: '10px 14px',
        borderRadius: '6px',
        fontSize: '14px',
        marginBottom: '12px',
        display: 'flex',
        alignItems: 'center',
        gap: '8px',
    } as const,
    statsRow: {
        display: 'flex',
        gap: '24px',
        margin: '12px 0',
        flexWrap: 'wrap' as const,
    } as const,
    stat: {
        display: 'flex',
        flexDirection: 'column' as const,
        gap: '2px',
        minWidth: '120px',
    } as const,
    statLabel: {
        fontSize: '12px',
        color: '#6b7280',
    } as const,
    statValue: {
        fontSize: '20px',
        fontWeight: 700,
        color: '#1f2937',
    } as const,
    statValid: { color: '#16a34a' },
    statInvalid: { color: '#dc2626' },
    errorsList: {
        maxHeight: '300px',
        overflowY: 'auto' as const,
        border: '1px solid #fca5a5',
        borderRadius: '8px',
        padding: '12px 16px',
        background: '#fef2f2',
        margin: '16px 0',
    } as const,
    errorsHeader: {
        color: '#dc2626',
        margin: '0 0 8px',
        fontSize: '14px',
        fontWeight: 600,
    } as const,
    errorsItem: {
        fontSize: '12px',
        color: '#991b1b',
        padding: '4px 0',
        borderBottom: '1px solid #fecaca',
        listStyle: 'none',
    } as const,
};

export default function MigoUploadLayout(): ReactElement {
    const navigate = useNavigate();

    const financeAlert = useFinanceAlertModal();

    useFinanceListReturnFromDetail(
        FINANCE_LIST_KEYS.migo.moduleKey,
        FINANCE_LIST_KEYS.migo.listPath
    );

    const [file, setFile] = useState<File | null>(null);
    const [uploading, setUploading] = useState(false);
    const [summary, setSummary] = useState<UploadSummary | null>(null);
    const [invalidRows, setInvalidRows] = useState<InvalidRow[]>([]);
    const [successMsg, setSuccessMsg] = useState('');

    const resetState = () => {
        setSummary(null);
        setInvalidRows([]);
        setSuccessMsg('');
    };

    const handleFileSelect = useCallback((f: File | null) => {
        resetState();
        if (f) {
            const ext = f.name.split('.').pop()?.toLowerCase();
            if (ext !== 'csv') {
                financeAlert.showWarning(
                    'Archivo no válido',
                    'Solo se permiten archivos en formato CSV.',
                );
                return;
            }
        }
        setFile(f);
    }, [financeAlert.showWarning]);

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
                financeAlert.showError(
                    'Error al procesar',
                    res.message || 'Error al procesar el archivo.',
                );
            }
        } catch (err: unknown) {
            financeAlert.showErrorFrom(
                'Error al subir',
                err,
                'No fue posible procesar el archivo. Verifica el layout e inténtalo nuevamente.',
            );
        } finally {
            setUploading(false);
        }
    };

    return (
        <div style={styles.layout}>
            <Breadcrumb
                items={withFinanceBreadcrumb([
                    { label: 'Publicación de recepción MIGO', to: '/finanzas/migo' },
                    { label: 'Publicar OC' },
                ])}
            />

            <div style={styles.box}>
                <div style={styles.headerRow}>
                    <h3 style={styles.title}>Publicar Archivo de Recepciones MIGO</h3>
                    <p style={styles.description}>
                        Seleccione un archivo CSV con el layout de recepciones MIGO para publicar.
                    </p>
                </div>
                <div style={styles.divider} />

                <div style={styles.section}>
                    <p style={styles.hint}>Cabecera obligatoria del CSV (13 columnas):</p>
                    <p style={styles.columns}>
                        Nro_OC, Nro_Recepcion, Numero_Proveedor, Sucursal, Nro_Guia, Origen,
                        Fecha_Recepcion, Importe_sin_impuesto, SKU, Descripcion_Sku, Cantidad,
                        Importe_Unitario, Importe_SinImpuesto
                    </p>
                    <p style={styles.columnsOptional}>
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

                    {summary && (
                        <div style={styles.summaryResult}>
                            <h4 style={styles.summaryTitle}>Resultado de la validación</h4>
                            {successMsg && (
                                <div style={styles.successBanner}>
                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                                        <polyline points="22 4 12 14.01 9 11.01" />
                                    </svg>
                                    <span>{successMsg}</span>
                                </div>
                            )}
                            <div style={styles.statsRow}>
                                <div style={styles.stat}>
                                    <span style={styles.statLabel}>Total de registros:</span>
                                    <span style={styles.statValue}>{summary.totalRows}</span>
                                </div>
                                <div style={styles.stat}>
                                    <span style={styles.statLabel}>Total válidos:</span>
                                    <span style={{ ...styles.statValue, ...styles.statValid }}>{summary.totalValid}</span>
                                </div>
                                <div style={styles.stat}>
                                    <span style={styles.statLabel}>Total incorrectos:</span>
                                    <span style={{ ...styles.statValue, ...styles.statInvalid }}>{summary.totalInvalid}</span>
                                </div>
                            </div>

                            {invalidRows.length > 0 && (
                                <div style={styles.errorsList}>
                                    <h4 style={styles.errorsHeader}>Detalle de registros incorrectos ({invalidRows.length})</h4>
                                    <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
                                        {invalidRows.map((r, i) => (
                                            <li key={i} style={styles.errorsItem}>
                                                <strong>Fila {r.row}:</strong> {r.errors.join(' | ')}
                                            </li>
                                        ))}
                                    </ul>
                                </div>
                            )}
                        </div>
                    )}

                    <div style={styles.actions}>
                        <BackLinkButton
                            onClick={() =>
                                navigate('/finanzas/migo', {
                                    state: summary ? { autoSearchAfterUpload: true } : undefined,
                                })
                            }
                        >
                            Volver
                        </BackLinkButton>
                        {!summary && (
                            <PermissionGate appEvent={APP_EVENT.MIGO.PUBLISH}>
                                <GenericButton
                                    variant="primary"
                                    onClick={handleUpload}
                                    disabled={!file || uploading}
                                >
                                    {uploading ? 'Publicando...' : 'Publicar'}
                                </GenericButton>
                            </PermissionGate>
                        )}
                    </div>
                </div>

                {uploading && <GenericModal visible variant="loading" message="Validando y publicando archivo..." />}

                <GenericModal
                    visible={financeAlert.alertVisible}
                    variant="alert"
                    severity={financeAlert.alertSeverity}
                    title={financeAlert.alertTitle}
                    message={financeAlert.alertMessage}
                    buttonText="Aceptar"
                    onClose={financeAlert.closeAlert}
                />
            </div>
        </div>
    );
}
