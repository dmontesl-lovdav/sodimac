import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';
import { complementService } from '../api/complementService';
import { PaymentInfo, ComplementDocument, LastPublishedComplement } from '../interfaces';

const styles = {
    container: {
        width: '100%',
        padding: '1.5rem 2rem',
        backgroundColor: '#ffffff',
        minHeight: '100vh',
    },
    loadingWrapper: {
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '16rem',
    },
    loadingText: {
        color: '#6b7280',
    },
    errorBox: {
        backgroundColor: '#fef2f2',
        border: '1px solid #fca5a5',
        color: '#991b1b',
        padding: '0.75rem 1rem',
        borderRadius: '0.375rem',
    },
    header: {
        display: 'flex',
        alignItems: 'center',
        gap: '0.75rem',
        marginTop: '1.5rem',
        marginBottom: '1.5rem',
    },
    headerIcon: {
        color: '#4b5563',
    },
    title: {
        fontSize: '1.25rem',
        fontWeight: 600,
        color: '#111827',
    },
    card: {
        border: '1px solid #e5e7eb',
        borderRadius: '0.5rem',
        padding: '1.5rem',
        backgroundColor: '#ffffff',
        marginBottom: '1.5rem',
    },
    cardHeader: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        marginBottom: '1rem',
    },
    cardHeaderLeft: {
        display: 'flex',
        flexDirection: 'column' as const,
        gap: '0.5rem',
    },
    idRow: {
        display: 'flex',
        alignItems: 'center',
        gap: '0.75rem',
    },
    idLabel: {
        fontSize: '0.875rem',
        fontWeight: 500,
        color: '#374151',
    },
    idBadge: {
        backgroundColor: '#002B55',
        color: '#ffffff',
        padding: '0.25rem 0.75rem',
        borderRadius: '9999px',
        fontSize: '0.875rem',
        fontWeight: 500,
    },
    statusRow: {
        display: 'flex',
        alignItems: 'center',
        gap: '0.25rem',
    },
    statusDot: {
        width: '0.5rem',
        height: '0.5rem',
        backgroundColor: '#eab308',
        borderRadius: '9999px',
    },
    statusLabel: {
        fontSize: '0.875rem',
        fontWeight: 500,
    },
    statusValue: {
        fontSize: '0.875rem',
    },
    cardHeaderRight: {
        display: 'flex',
        alignItems: 'center',
        gap: '0.75rem',
    },
    providerName: {
        fontSize: '0.875rem',
        color: '#4b5563',
    },
    providerIcon: {
        width: '2.5rem',
        height: '2.5rem',
        backgroundColor: '#2563eb',
        borderRadius: '0.5rem',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
    },
    detailsGrid: {
        display: 'grid',
        gridTemplateColumns: 'repeat(2, 1fr)',
        gap: '1.5rem',
    },
    detailItem: {},
    detailLabel: {
        fontSize: '0.875rem',
        color: '#4b5563',
    },
    detailValue: {
        fontWeight: 500,
    },
    detailValueLarge: {
        fontWeight: 500,
        fontSize: '1.125rem',
    },
    sectionLabel: {
        fontSize: '0.875rem',
        fontWeight: 500,
        color: '#374151',
        marginBottom: '0.5rem',
        display: 'block',
    },
    requiredMark: {
        color: '#ef4444',
    },
    select: {
        border: '1px solid #d1d5db',
        borderRadius: '0.25rem',
        padding: '0.5rem 0.75rem',
        fontSize: '0.875rem',
        backgroundColor: '#ffffff',
        width: '12rem',
    },
    dropzone: {
        border: '2px dashed #d1d5db',
        borderRadius: '0.5rem',
        padding: '3rem',
        textAlign: 'center' as const,
        backgroundColor: '#f9fafb',
        marginBottom: '1.5rem',
    },
    dropzoneIcon: {
        color: '#3b82f6',
        marginBottom: '1rem',
    },
    dropzoneText: {
        fontSize: '0.875rem',
        color: '#374151',
        marginBottom: '0.5rem',
    },
    dropzoneLink: {
        fontSize: '0.875rem',
        color: '#2563eb',
        marginBottom: '1rem',
        cursor: 'pointer',
    },
    fileCard: {
        border: '1px solid #e5e7eb',
        borderRadius: '0.5rem',
        padding: '1rem',
        backgroundColor: '#ffffff',
        marginBottom: '1.5rem',
    },
    fileCardTitle: {
        fontSize: '0.875rem',
        fontWeight: 500,
        color: '#374151',
        marginBottom: '0.75rem',
    },
    fileItem: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '0.5rem 0.75rem',
        borderRadius: '0.25rem',
        marginBottom: '0.5rem',
    },
    fileItemXml: {
        backgroundColor: '#eff6ff',
    },
    fileItemPdf: {
        backgroundColor: '#f0fdf4',
    },
    fileItemContent: {
        display: 'flex',
        alignItems: 'center',
        gap: '0.5rem',
    },
    fileIcon: {
        color: '#2563eb',
    },
    fileIconPdf: {
        color: '#16a34a',
    },
    fileName: {
        fontSize: '0.875rem',
    },
    removeButton: {
        color: '#dc2626',
        background: 'none',
        border: 'none',
        cursor: 'pointer',
        fontSize: '0.875rem',
    },
    lastComplement: {
        border: '1px solid #bfdbfe',
        borderRadius: '0.5rem',
        padding: '1rem',
        backgroundColor: '#eff6ff',
        marginBottom: '1.5rem',
    },
    lastComplementTitle: {
        fontSize: '0.875rem',
        fontWeight: 500,
        color: '#374151',
        marginBottom: '0.5rem',
    },
    lastComplementText: {
        fontSize: '0.875rem',
        color: '#4b5563',
    },
    successBox: {
        backgroundColor: '#f0fdf4',
        border: '1px solid #86efac',
        color: '#166534',
        padding: '0.75rem 1rem',
        borderRadius: '0.375rem',
        marginBottom: '1rem',
    },
    footer: {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingTop: '1.5rem',
    },
    buttonOutline: {
        padding: '0.5rem 1.5rem',
        backgroundColor: '#ffffff',
        border: '1px solid #111827',
        color: '#111827',
        borderRadius: '0.25rem',
        fontSize: '0.875rem',
        fontWeight: 500,
        cursor: 'pointer',
        transition: 'background-color 150ms',
    },
    buttonPrimary: {
        padding: '0.5rem 1.5rem',
        borderRadius: '0.25rem',
        fontSize: '0.875rem',
        fontWeight: 500,
        border: 'none',
        cursor: 'pointer',
        transition: 'filter 150ms',
    },
    buttonPrimaryEnabled: {
        backgroundColor: '#002B55',
        color: '#ffffff',
    },
    buttonPrimaryDisabled: {
        backgroundColor: '#d1d5db',
        color: '#6b7280',
        cursor: 'not-allowed',
    },
};

export default function ComplementPublishContainer() {
    const { paymentId } = useParams<{ paymentId: string }>();
    const navigate = useNavigate();
    
    const [paymentInfo, setPaymentInfo] = useState<PaymentInfo | null>(null);
    const [loading, setLoading] = useState(true);
    const [documentType, setDocumentType] = useState<'xml' | 'pdf'>('xml');
    const [xmlFile, setXmlFile] = useState<File | null>(null);
    const [pdfFile, setPdfFile] = useState<File | null>(null);
    const [lastComplement, setLastComplement] = useState<LastPublishedComplement | null>(null);
    const [error, setError] = useState<string>('');
    const [success, setSuccess] = useState<string>('');
    const [uploading, setUploading] = useState(false);

    useEffect(() => {
        if (paymentId) {
            loadPaymentInfo(paymentId);
            loadLastComplement(paymentId);
        }
    }, [paymentId]);

    const loadPaymentInfo = async (id: string) => {
        setLoading(true);
        try {
            const info = await complementService.getPaymentInfo(id);
            setPaymentInfo(info);
        } catch (err) {
            console.error('Error loading payment info:', err);
            setError('Error al cargar información del pago');
        } finally {
            setLoading(false);
        }
    };

    const loadLastComplement = async (id: string) => {
        try {
            const last = await complementService.getLastPublishedComplement(id);
            setLastComplement(last);
        } catch (err) {
            console.error('Error loading last complement:', err);
        }
    };

    const handleFileSelect = (event: React.ChangeEvent<HTMLInputElement>, type: 'xml' | 'pdf') => {
        const file = event.target.files?.[0];
        if (!file) return;

        if (type === 'xml') {
            if (!file.name.toLowerCase().endsWith('.xml')) {
                setError('El archivo debe ser formato XML');
                return;
            }
            setXmlFile(file);
        } else {
            if (!file.name.toLowerCase().endsWith('.pdf')) {
                setError('El archivo debe ser formato PDF');
                return;
            }
            setPdfFile(file);
        }
        
        setError('');
    };

    const handleDragOver = (e: React.DragEvent) => {
        e.preventDefault();
        e.stopPropagation();
    };

    const handleDrop = (e: React.DragEvent) => {
        e.preventDefault();
        e.stopPropagation();
        
        const file = e.dataTransfer.files?.[0];
        if (!file) return;

        if (documentType === 'xml') {
            if (!file.name.toLowerCase().endsWith('.xml')) {
                setError('El archivo debe ser formato XML');
                return;
            }
            setXmlFile(file);
        } else {
            if (!file.name.toLowerCase().endsWith('.pdf')) {
                setError('El archivo debe ser formato PDF');
                return;
            }
            setPdfFile(file);
        }
        
        setError('');
    };

    const handleSubmit = async () => {
        if (!xmlFile) {
            setError('Se requiere publicar el complemento de pago en formato XML.');
            return;
        }

        if (!paymentInfo) return;

        setUploading(true);
        setError('');
        setSuccess('');

        try {
            const result = await complementService.publishComplement({
                paymentId: paymentInfo.paymentId,
                xmlFile,
                pdfFile: pdfFile || undefined
            });

            if (result.success) {
                setSuccess(result.message);
                await loadLastComplement(paymentInfo.paymentId);
                setXmlFile(null);
                setPdfFile(null);
            } else {
                setError(result.message);
            }
        } catch (err) {
            console.error('Error publishing complement:', err);
            setError('Error al publicar el complemento. Por favor intente nuevamente.');
        } finally {
            setUploading(false);
        }
    };

    if (loading) {
        return (
            <div style={styles.container}>
                <div style={styles.loadingWrapper}>
                    <div style={styles.loadingText}>Cargando...</div>
                </div>
            </div>
        );
    }

    if (!paymentInfo) {
        return (
            <div style={styles.container}>
                <div style={styles.errorBox}>
                    Pago no encontrado
                </div>
            </div>
        );
    }

    return (
        <div style={styles.container}>
            <Breadcrumb
                items={[
                    { label: 'Home', to: '/' },
                    { label: 'Pagos', to: '#' },
                    { label: 'Detalle de pago', to: '#' },
                    { label: 'Publicación de complemento' }
                ]}
            />

            <div style={styles.header}>
                <div style={styles.headerIcon}>
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="1.5"/>
                        <path d="M12 6v6l4 2" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
                    </svg>
                </div>
                <h1 style={styles.title}>
                    Publicación de complemento de pago
                </h1>
            </div>

            <div style={styles.card}>
                <div style={styles.cardHeader}>
                    <div style={styles.cardHeaderLeft}>
                        <div style={styles.idRow}>
                            <span style={styles.idLabel}>Id Pago</span>
                            <span style={styles.idBadge}>
                                #{paymentInfo.paymentNumber}
                            </span>
                        </div>
                        <div style={styles.statusRow}>
                            <span style={styles.statusDot}></span>
                            <span style={styles.statusLabel}>Status:</span>
                            <span style={styles.statusValue}>{paymentInfo.status}</span>
                        </div>
                    </div>
                    <div style={styles.cardHeaderRight}>
                        <span style={styles.providerName}>{paymentInfo.providerName}</span>
                        <div style={styles.providerIcon}>
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                                <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" stroke="white" strokeWidth="2" fill="none"/>
                                <polyline points="9 22 9 12 15 12 15 22" stroke="white" strokeWidth="2" fill="none"/>
                            </svg>
                        </div>
                    </div>
                </div>

                <div style={styles.detailsGrid}>
                    <div style={styles.detailItem}>
                        <p style={styles.detailLabel}>Fecha de pago:</p>
                        <p style={styles.detailValue}>{paymentInfo.paymentDate}</p>
                    </div>
                    <div style={styles.detailItem}>
                        <p style={styles.detailLabel}>Número de facturas:</p>
                        <p style={styles.detailValue}>{paymentInfo.invoiceCount}</p>
                    </div>
                    <div style={styles.detailItem}>
                        <p style={styles.detailLabel}>Total de documento:</p>
                        <p style={styles.detailValue}>{paymentInfo.totalDocuments}</p>
                    </div>
                    <div style={styles.detailItem}>
                        <p style={styles.detailLabel}>Número de notas de crédito:</p>
                        <p style={styles.detailValue}>{paymentInfo.creditNoteCount}</p>
                    </div>
                    <div style={styles.detailItem}>
                        <p style={styles.detailLabel}>Monto:</p>
                        <p style={styles.detailValueLarge}>
                            ${paymentInfo.paymentAmount.toLocaleString('es-MX', {
                                minimumFractionDigits: 2,
                                maximumFractionDigits: 2
                            })}
                        </p>
                    </div>
                </div>
            </div>

            <div style={{ marginBottom: '1rem' }}>
                <label style={styles.sectionLabel}>
                    Adjuntar contrato <span style={styles.requiredMark}>*</span>
                </label>
                <select
                    value={documentType}
                    onChange={(e) => setDocumentType(e.target.value as 'xml' | 'pdf')}
                    style={styles.select}
                >
                    <option value="xml">XML (obligatorio)</option>
                    <option value="pdf">PDF (opcional)</option>
                </select>
            </div>

            <div 
                style={styles.dropzone}
                onDragOver={handleDragOver}
                onDrop={handleDrop}
            >
                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                    <svg width="48" height="48" viewBox="0 0 48 48" style={styles.dropzoneIcon}>
                        <circle cx="24" cy="24" r="20" fill="currentColor" opacity="0.1"/>
                        <path d="M24 16v16m-8-8h16" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                    </svg>
                    <p style={styles.dropzoneText}>
                        Drag and drop files here
                    </p>
                    <p style={styles.dropzoneLink}>
                        <label style={{ cursor: 'pointer' }}>
                            or click here
                            <input
                                type="file"
                                accept={documentType === 'xml' ? '.xml' : '.pdf'}
                                onChange={(e) => handleFileSelect(e, documentType)}
                                style={{ display: 'none' }}
                            />
                        </label>
                    </p>
                </div>
            </div>

            {(xmlFile || pdfFile) && (
                <div style={styles.fileCard}>
                    <h3 style={styles.fileCardTitle}>Archivos seleccionados:</h3>
                    <div>
                        {xmlFile && (
                            <div style={{ ...styles.fileItem, ...styles.fileItemXml }}>
                                <div style={styles.fileItemContent}>
                                    <span style={styles.fileIcon}>📄</span>
                                    <span style={styles.fileName}>XML: {xmlFile.name}</span>
                                </div>
                                <button
                                    onClick={() => setXmlFile(null)}
                                    style={styles.removeButton}
                                >
                                    ✕
                                </button>
                            </div>
                        )}
                        {pdfFile && (
                            <div style={{ ...styles.fileItem, ...styles.fileItemPdf }}>
                                <div style={styles.fileItemContent}>
                                    <span style={styles.fileIconPdf}>📄</span>
                                    <span style={styles.fileName}>PDF: {pdfFile.name}</span>
                                </div>
                                <button
                                    onClick={() => setPdfFile(null)}
                                    style={styles.removeButton}
                                >
                                    ✕
                                </button>
                            </div>
                        )}
                    </div>
                </div>
            )}

            {lastComplement && (
                <div style={styles.lastComplement}>
                    <h3 style={styles.lastComplementTitle}>Último documento publicado:</h3>
                    <div style={styles.lastComplementText}>
                        <p>Archivo: {lastComplement.fileName}</p>
                        <p>Fecha: {lastComplement.uploadDate}</p>
                        <p>Estatus: {lastComplement.status}</p>
                    </div>
                </div>
            )}

            {error && (
                <div style={styles.errorBox}>
                    {error}
                </div>
            )}

            {success && (
                <div style={styles.successBox}>
                    {success}
                </div>
            )}

            <div style={styles.footer}>
                <button
                    onClick={() => navigate(-1)}
                    style={styles.buttonOutline}
                    onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f9fafb'}
                    onMouseLeave={(e) => e.currentTarget.style.backgroundColor = '#ffffff'}
                >
                    Regresar
                </button>
                
                <button
                    onClick={handleSubmit}
                    disabled={uploading || !xmlFile}
                    style={{
                        ...styles.buttonPrimary,
                        ...(uploading || !xmlFile ? styles.buttonPrimaryDisabled : styles.buttonPrimaryEnabled),
                    }}
                    onMouseEnter={(e) => {
                        if (!uploading && xmlFile) {
                            e.currentTarget.style.filter = 'brightness(1.1)';
                        }
                    }}
                    onMouseLeave={(e) => {
                        e.currentTarget.style.filter = 'none';
                    }}
                >
                    {uploading ? 'Guardando...' : 'Guardar'}
                </button>
            </div>
        </div>
    );
}
