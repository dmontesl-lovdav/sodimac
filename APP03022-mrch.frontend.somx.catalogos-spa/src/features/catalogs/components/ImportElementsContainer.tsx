import { useState, useEffect, useRef } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';
import { catalogService, catalogElementService } from '../../../services/api';
import type { CatalogElementCreateDto } from '../../../services/api';

interface CatalogData {
  id: string;
  name: string;
  description: string;
  type: 'Primario' | 'Secundario';
  status: 'Activo' | 'Inactivo';
}

interface UploadedFile {
  file: File;
  name: string;
  size: number;
  error?: string;
}

const MOCK_CATALOGS: CatalogData[] = [
  { id: '0001', name: 'Catálogo de Productos', description: 'Lista de artículos con detalles como nombre, descripción, precio y fotos.', type: 'Primario', status: 'Activo' },
  { id: '0002', name: 'Catálogo de Proveedores', description: 'Lista de proveedores con información sobre sus productos, servicios y contacto.', type: 'Primario', status: 'Activo' },
  { id: '0003', name: 'Catálogo de Motivos', description: 'Lista de motivos para aclaraciones y ajustes en transacciones.', type: 'Secundario', status: 'Activo' },
];

const styles = {
  container: {
    minHeight: '100vh',
    backgroundColor: '#ffffff',
    padding: '1.5rem 2rem',
  },
  breadcrumb: {
    fontSize: '0.875rem',
    color: '#64748b',
    marginBottom: '1.5rem',
  },
  breadcrumbLink: {
    color: '#0066CC',
    textDecoration: 'none',
    cursor: 'pointer',
  },
  card: {
    backgroundColor: '#ffffff',
    border: '1px solid #e5e7eb',
    borderRadius: '0.75rem',
    padding: '2rem',
    maxWidth: '800px',
  },
  title: {
    fontSize: '1.5rem',
    fontWeight: 600,
    color: '#1e293b',
    marginBottom: '0.5rem',
  },
  subtitle: {
    fontSize: '0.875rem',
    color: '#64748b',
    marginBottom: '2rem',
    lineHeight: 1.6,
  },
  stepContainer: {
    display: 'flex',
    marginBottom: '1.5rem',
  },
  stepIndicator: {
    display: 'flex',
    flexDirection: 'column' as const,
    alignItems: 'center',
    marginRight: '1rem',
  },
  stepNumber: {
    width: '32px',
    height: '32px',
    backgroundColor: '#0066CC',
    color: '#ffffff',
    borderRadius: '50%',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontWeight: 600,
    fontSize: '0.875rem',
    flexShrink: 0,
  },
  stepLine: {
    width: '2px',
    flexGrow: 1,
    backgroundColor: '#0066CC',
    marginTop: '0.5rem',
  },
  stepContent: {
    flex: 1,
    paddingBottom: '1rem',
  },
  stepHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    cursor: 'pointer',
    marginBottom: '0.5rem',
  },
  stepTitle: {
    fontSize: '1rem',
    fontWeight: 600,
    color: '#1e293b',
    marginBottom: '0.25rem',
  },
  stepDescription: {
    fontSize: '0.875rem',
    color: '#64748b',
    lineHeight: 1.5,
  },
  chevron: {
    color: '#64748b',
    transition: 'transform 0.2s ease',
    cursor: 'pointer',
  },
  outlineBtn: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '0.5rem',
    padding: '0.625rem 1.25rem',
    fontSize: '0.875rem',
    fontWeight: 500,
    color: '#0066CC',
    backgroundColor: 'transparent',
    border: '1px solid #0066CC',
    borderRadius: '0.375rem',
    cursor: 'pointer',
    transition: 'all 0.2s ease',
  },
  primaryBtn: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '0.5rem',
    padding: '0.625rem 1.25rem',
    fontSize: '0.875rem',
    fontWeight: 500,
    color: '#ffffff',
    backgroundColor: '#0066CC',
    border: '1px solid #0066CC',
    borderRadius: '0.375rem',
    cursor: 'pointer',
    transition: 'all 0.2s ease',
  },
  ghostBtn: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '0.5rem',
    padding: '0.625rem 1.25rem',
    fontSize: '0.875rem',
    fontWeight: 500,
    color: '#64748b',
    backgroundColor: 'transparent',
    border: 'none',
    cursor: 'pointer',
    textDecoration: 'underline',
  },
  dropzone: {
    border: '2px dashed #d1d5db',
    borderRadius: '0.5rem',
    padding: '2rem',
    textAlign: 'center' as const,
    cursor: 'pointer',
    transition: 'border-color 0.2s ease, background-color 0.2s ease',
    marginTop: '1rem',
  },
  dropzoneActive: {
    borderColor: '#0066CC',
    backgroundColor: '#f0f9ff',
  },
  dropzoneDisabled: {
    opacity: 0.6,
    cursor: 'not-allowed',
    backgroundColor: '#f8fafc',
  },
  dropzoneIcon: {
    width: '48px',
    height: '48px',
    margin: '0 auto 1rem',
    color: '#0066CC',
  },
  dropzoneText: {
    fontSize: '0.875rem',
    color: '#1e293b',
    marginBottom: '0.25rem',
  },
  dropzoneLink: {
    color: '#0066CC',
    textDecoration: 'underline',
    cursor: 'pointer',
  },
  dropzoneHint: {
    fontSize: '0.75rem',
    color: '#64748b',
    marginTop: '0.5rem',
  },
  fileInfo: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: '0.75rem 1rem',
    backgroundColor: '#f0fdf4',
    border: '1px solid #bbf7d0',
    borderRadius: '0.375rem',
    marginTop: '1rem',
  },
  fileInfoContent: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.75rem',
  },
  fileInfoIcon: {
    color: '#16a34a',
  },
  fileInfoDetails: {
    display: 'flex',
    flexDirection: 'column' as const,
  },
  fileInfoName: {
    fontSize: '0.875rem',
    fontWeight: 500,
    color: '#1e293b',
  },
  fileInfoSize: {
    fontSize: '0.75rem',
    color: '#64748b',
  },
  fileDeleteBtn: {
    background: 'none',
    border: 'none',
    color: '#dc2626',
    cursor: 'pointer',
    padding: '0.25rem',
  },
  fileError: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.75rem',
    padding: '0.75rem 1rem',
    backgroundColor: '#fef2f2',
    border: '1px solid #fecaca',
    borderRadius: '0.375rem',
    marginTop: '1rem',
    fontSize: '0.875rem',
    color: '#991b1b',
  },
  footer: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: '2rem',
    paddingTop: '1.5rem',
    borderTop: '1px solid #e5e7eb',
  },
  buttonsRight: {
    display: 'flex',
    gap: '0.75rem',
  },
  modal: {
    position: 'fixed' as const,
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: 1000,
  },
  modalContent: {
    backgroundColor: '#ffffff',
    borderRadius: '0.75rem',
    padding: '1.5rem',
    maxWidth: '400px',
    width: '90%',
    boxShadow: '0 10px 25px rgba(0, 0, 0, 0.2)',
  },
  modalTitle: {
    fontSize: '1.125rem',
    fontWeight: 600,
    marginBottom: '1rem',
    color: '#1e293b',
  },
  modalText: {
    fontSize: '0.875rem',
    color: '#64748b',
    marginBottom: '1.5rem',
    lineHeight: 1.5,
  },
  modalButtons: {
    display: 'flex',
    justifyContent: 'flex-end',
    gap: '0.75rem',
  },
};

const CloudUploadIcon = () => (
  <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
    <path d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
  </svg>
);

const DownloadIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4M7 10l5 5 5-5M12 15V3" />
  </svg>
);

const CheckIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M20 6L9 17l-5-5" />
  </svg>
);

const TrashIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M3 6h18M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2" />
  </svg>
);

const ChevronIcon = ({ expanded }: { expanded: boolean }) => (
  <svg
    width="20"
    height="20"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    style={{ transform: expanded ? 'rotate(180deg)' : 'rotate(0deg)', transition: 'transform 0.2s ease' }}
  >
    <path d="M6 9l6 6 6-6" />
  </svg>
);

export default function ImportElementsContainer() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [catalog, setCatalog] = useState<CatalogData | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [expandedSteps, setExpandedSteps] = useState<number[]>([1, 2]);
  const [uploadedFile, setUploadedFile] = useState<UploadedFile | null>(null);
  const [isDownloading, setIsDownloading] = useState(false);
  const [isImporting, setIsImporting] = useState(false);
  const [isValidating, setIsValidating] = useState(false);
  const [validationErrors, setValidationErrors] = useState<Array<{ cell: string; message: string }>>([]);
  const [showValidationErrors, setShowValidationErrors] = useState(false);
  const [showExitModal, setShowExitModal] = useState(false);
  const [isDragging, setIsDragging] = useState(false);

  useEffect(() => {
    if (id) {
      setIsLoading(true);
      catalogService.getById(parseInt(id))
        .then((response) => {
          setCatalog({
            id: String(response.id),
            name: response.name,
            description: response.description || '',
            type: response.catalogType === 'PRIMARIO' ? 'Primario' : 'Secundario',
            status: response.status === 1 ? 'Activo' : 'Inactivo',
          });
        })
        .catch((error) => {
          console.error('Error fetching catalog:', error);
          setCatalog(null);
        })
        .finally(() => setIsLoading(false));
    }
  }, [id]);

  const toggleStep = (step: number) => {
    setExpandedSteps((prev) =>
      prev.includes(step) ? prev.filter((s) => s !== step) : [...prev, step]
    );
  };

  const handleDownloadTemplate = async () => {
    if (isDownloading || !catalog) return;
    setIsDownloading(true);

    try {
      const templateFile = catalog.type === 'Primario' ? 'c_Paises.xlsx' : 'c_Estados.xlsx';
      
      const date = new Date();
      const formattedDate = `${String(date.getDate()).padStart(2, '0')}${String(date.getMonth() + 1).padStart(2, '0')}${date.getFullYear()}`;
      
      const catalogName = catalog.name.replace(/\s+/g, '_').toLowerCase();
      const downloadFileName = `${catalogName}_${formattedDate}.xlsx`;

      const response = await fetch(`/templates/${templateFile}`);
      if (!response.ok) {
        throw new Error('No se pudo descargar la plantilla');
      }
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = downloadFileName;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Error downloading template:', error);
      alert('Ocurrió un problema al descargar la plantilla. Intente nuevamente.');
    } finally {
      setIsDownloading(false);
    }
  };

  const handleFileSelect = (file: File) => {
    const allowedTypes = [
      'text/csv',
      'application/vnd.ms-excel',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    ];
    const maxSize = 4 * 1024 * 1024;

    if (!allowedTypes.includes(file.type) && !file.name.match(/\.(csv|xls|xlsx)$/i)) {
      setUploadedFile({
        file,
        name: file.name,
        size: file.size,
        error: 'Este documento no es compatible. Por favor, elimínelo y cargue otro archivo.',
      });
      return;
    }

    if (file.size > maxSize) {
      setUploadedFile({
        file,
        name: file.name,
        size: file.size,
        error: 'El archivo excede el tamaño máximo permitido (4MB).',
      });
      return;
    }

    setUploadedFile({
      file,
      name: file.name,
      size: file.size,
    });
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    if (uploadedFile && !uploadedFile.error) return;

    const files = e.dataTransfer.files;
    if (files.length > 0) {
      handleFileSelect(files[0]);
    }
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    if (!uploadedFile || uploadedFile.error) {
      setIsDragging(true);
    }
  };

  const handleDragLeave = () => {
    setIsDragging(false);
  };

  const handleFileInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (files && files.length > 0) {
      handleFileSelect(files[0]);
    }
  };

  const removeFile = () => {
    setUploadedFile(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleClear = () => {
    removeFile();
  };

  const handleBack = () => {
    if (uploadedFile && !uploadedFile.error) {
      setShowExitModal(true);
    } else {
      navigate(`/catalogos/catalogs/${id}/elementos`);
    }
  };

  const confirmExit = () => {
    setShowExitModal(false);
    navigate(`/catalogos/catalogs/${id}/elementos`);
  };

  const cancelExit = () => {
    setShowExitModal(false);
  };

  const parseExcelElements = async (file: File): Promise<Array<{
    tipoCatalogo: string; elemento: string; valor: string;
    fechaInicioVigencia: string; fechaFinVigencia: string;
    idPadre: string; valorConversion: string;
  }>> => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = (e) => {
        try {
          const data = new Uint8Array(e.target?.result as ArrayBuffer);
          const workbook = XLSX.read(data, { type: 'array', cellDates: true });
          const sheet = workbook.Sheets[workbook.SheetNames[0]];
          const rows: any[] = XLSX.utils.sheet_to_json(sheet, { header: 1, defval: '' });
          if (rows.length < 2) { resolve([]); return; }
          const elements: Array<{
            tipoCatalogo: string; elemento: string; valor: string;
            fechaInicioVigencia: string; fechaFinVigencia: string;
            idPadre: string; valorConversion: string;
          }> = [];
          for (let i = 1; i < rows.length; i++) {
            const row = rows[i];
            if (!row || row.every((c: any) => c === '' || c === null || c === undefined)) continue;
            const formatDate = (val: any): string => {
              if (!val) return '';
              if (val instanceof Date) {
                const y = val.getFullYear(); const m = String(val.getMonth() + 1).padStart(2, '0'); const d = String(val.getDate()).padStart(2, '0');
                return `${y}-${m}-${d}`;
              }
              return String(val).trim();
            };
            elements.push({
              tipoCatalogo: String(row[0] ?? '').trim(),
              elemento: String(row[1] ?? '').trim(),
              valor: String(row[2] ?? '').trim(),
              fechaInicioVigencia: formatDate(row[3]),
              fechaFinVigencia: formatDate(row[4]),
              idPadre: String(row[5] ?? '').trim(),
              valorConversion: row[6] != null ? String(row[6]).trim() : '',
            });
          }
          resolve(elements);
        } catch (err) { reject(err); }
      };
      reader.onerror = () => reject(new Error('Error reading file'));
      reader.readAsArrayBuffer(file);
    });
  };

  const handleImport = async () => {
    if (!uploadedFile || uploadedFile.error || isImporting || !id || !catalog) return;

    setIsValidating(true);
    setValidationErrors([]);
    setShowValidationErrors(false);

    try {
      const payload = new FormData();
      payload.append('file', uploadedFile.file);
      payload.append('tipoCatalogoSeleccionado', catalog.type === 'Primario' ? 'PRIMARIO' : 'SECUNDARIO');
      payload.append('nombreCatalogo', catalog.name);

      const response = await fetch('http://localhost:8083/catalogos/validate-layout', {
        method: 'POST',
        body: payload,
      });
      const result = await response.json();

      if (!result.isValid || (result.errors && result.errors.length > 0)) {
        setValidationErrors((result.errors || []).map((e: any) => ({ cell: e.cell || '', message: e.message || 'Error desconocido' })));
        setShowValidationErrors(true);
        setIsValidating(false);
        return;
      }
    } catch (error) {
      setValidationErrors([{ cell: 'N/A', message: 'Ocurrió un problema al validar el archivo.' }]);
      setShowValidationErrors(true);
      setIsValidating(false);
      return;
    }
    setIsValidating(false);

    setIsImporting(true);
    try {
      const elements = await parseExcelElements(uploadedFile.file);
      let createdCount = 0;
      let conversionCount = 0;
      const catalogId = parseInt(id);

      for (const elem of elements) {
        const createDto: CatalogElementCreateDto = {
          element: elem.elemento,
          value: elem.valor || undefined,
          validFrom: elem.fechaInicioVigencia,
          validTo: elem.fechaFinVigencia || undefined,
          parentElementId: elem.idPadre ? Number(elem.idPadre) : undefined,
          externalKey: elem.valorConversion || undefined,
        };

        await catalogElementService.create(catalogId, createDto);
        createdCount++;
        if (elem.valorConversion) conversionCount++;
      }

      if (conversionCount > 0) {
        alert(`Se importaron ${createdCount} elementos exitosamente, incluyendo ${conversionCount} con valor de conversión.`);
      } else {
        alert(`Se importaron ${createdCount} elementos exitosamente.`);
      }
      navigate(`/catalogos/catalogs/${id}/elementos`);
    } catch (error: any) {
      console.error('Error importing elements:', error);
      const msg = error?.response?.data?.message || 'Ocurrió un problema al importar los elementos.';
      alert(msg);
    } finally {
      setIsImporting(false);
    }
  };

  const formatFileSize = (bytes: number): string => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(0)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
  };

  if (isLoading) {
    return (
      <div style={styles.container}>
        <div style={{ padding: '3rem', textAlign: 'center' }}>
          <p style={{ color: '#64748b' }}>Cargando...</p>
        </div>
      </div>
    );
  }

  if (!catalog) {
    return (
      <div style={styles.container}>
        <div style={{ padding: '3rem', textAlign: 'center' }}>
          <p style={{ color: '#991b1b' }}>Catálogo no encontrado.</p>
          <button style={styles.primaryBtn} onClick={() => navigate('/catalogos/catalogs')}>
            Volver a Catálogos
          </button>
        </div>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <div style={styles.breadcrumb}>
        <span style={styles.breadcrumbLink} onClick={() => navigate('/')}>
          Inicio
        </span>
        {' / '}
        <span style={styles.breadcrumbLink} onClick={() => navigate('/catalogos/catalogs')}>
          Gestión de Catálogos
        </span>
        {' / '}
        <span style={styles.breadcrumbLink} onClick={() => navigate('/catalogos/catalogs')}>
          Catálogos
        </span>
        {' / '}
        <span
          style={styles.breadcrumbLink}
          onClick={() => navigate(`/catalogos/catalogs/${id}/elementos`)}
        >
          Elementos
        </span>
        {' / '}
        <span>Importar Elementos</span>
      </div>

      <div style={styles.card}>
        <h1 style={styles.title}>Importar Elementos</h1>
        <p style={styles.subtitle}>
          Para crear varios elementos a la vez, sigue las instrucciones y completa el formulario,
          asegurándote de proporcionar información precisa y detallada.
        </p>

        <div style={styles.stepContainer}>
          <div style={styles.stepIndicator}>
            <div style={styles.stepNumber}>1</div>
            <div style={styles.stepLine} />
          </div>
          <div style={styles.stepContent}>
            <div style={styles.stepHeader} onClick={() => toggleStep(1)}>
              <div>
                <div style={styles.stepTitle}>Elementos del Catálogo</div>
                <div style={styles.stepDescription}>
                  Descarga y llena la plantilla con los elementos del catálogo, cuidando de cumplir
                  con el formato requerido.
                </div>
              </div>
              <span style={styles.chevron}>
                <ChevronIcon expanded={expandedSteps.includes(1)} />
              </span>
            </div>

            {expandedSteps.includes(1) && (
              <div style={{ marginTop: '1rem' }}>
                <button
                  style={{
                    ...styles.outlineBtn,
                    opacity: isDownloading ? 0.6 : 1,
                    cursor: isDownloading ? 'not-allowed' : 'pointer',
                  }}
                  onClick={handleDownloadTemplate}
                  disabled={isDownloading}
                >
                  <DownloadIcon />
                  {isDownloading ? 'Descargando...' : 'Descargar plantilla de elementos de catálogo'}
                </button>
              </div>
            )}
          </div>
        </div>

        <div style={styles.stepContainer}>
          <div style={styles.stepIndicator}>
            <div style={styles.stepNumber}>2</div>
          </div>
          <div style={styles.stepContent}>
            <div style={styles.stepHeader} onClick={() => toggleStep(2)}>
              <div>
                <div style={styles.stepTitle}>Cargar Plantilla</div>
                <div style={styles.stepDescription}>
                  Sube la plantilla ya completada, arrastrándola o buscándola y seleccionándola con
                  el explorador de archivos.
                </div>
              </div>
              <span style={styles.chevron}>
                <ChevronIcon expanded={expandedSteps.includes(2)} />
              </span>
            </div>

            {expandedSteps.includes(2) && (
              <div style={{ marginTop: '1rem' }}>
                <input
                  type="file"
                  ref={fileInputRef}
                  style={{ display: 'none' }}
                  accept=".csv,.xls,.xlsx"
                  onChange={handleFileInputChange}
                />

                <div
                  style={{
                    ...styles.dropzone,
                    ...(isDragging ? styles.dropzoneActive : {}),
                    ...(uploadedFile && !uploadedFile.error ? styles.dropzoneDisabled : {}),
                  }}
                  onDrop={handleDrop}
                  onDragOver={handleDragOver}
                  onDragLeave={handleDragLeave}
                  onClick={() => {
                    if (!uploadedFile || uploadedFile.error) {
                      fileInputRef.current?.click();
                    }
                  }}
                >
                  <div style={styles.dropzoneIcon}>
                    <CloudUploadIcon />
                  </div>
                  <div style={styles.dropzoneText}>Arrastra y suelta el archivo</div>
                  <div>
                    <span style={styles.dropzoneLink}>o haz clic aquí para explorar</span>
                  </div>
                  <div style={styles.dropzoneHint}>
                    Formatos soportados: CSV, XLS, XLSX. Peso máximo: 4mb.
                  </div>
                </div>

                {uploadedFile && !uploadedFile.error && (
                  <div style={styles.fileInfo}>
                    <div style={styles.fileInfoContent}>
                      <span style={styles.fileInfoIcon}>
                        <CheckIcon />
                      </span>
                      <div style={styles.fileInfoDetails}>
                        <span style={styles.fileInfoName}>{uploadedFile.name}</span>
                        <span style={styles.fileInfoSize}>{formatFileSize(uploadedFile.size)}</span>
                      </div>
                    </div>
                    <button style={styles.fileDeleteBtn} onClick={removeFile} title="Eliminar archivo">
                      <TrashIcon />
                    </button>
                  </div>
                )}

                {uploadedFile?.error && (
                  <div style={styles.fileError}>
                    <span>⚠️</span>
                    <span>{uploadedFile.error}</span>
                    <button
                      style={{ ...styles.fileDeleteBtn, marginLeft: 'auto' }}
                      onClick={removeFile}
                    >
                      <TrashIcon />
                    </button>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>

        {showValidationErrors && validationErrors.length > 0 && (
          <div style={{
            marginTop: '1rem',
            padding: '1rem',
            backgroundColor: '#FEF2F2',
            border: '1px solid #FECACA',
            borderRadius: '0.5rem',
            maxHeight: '300px',
            overflowY: 'auto',
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
              <h4 style={{ color: '#991B1B', margin: 0, fontSize: '0.875rem', fontWeight: 600 }}>
                Se encontraron {validationErrors.length} error{validationErrors.length > 1 ? 'es' : ''} en el archivo
              </h4>
              <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                {validationErrors.length > 20 && (
                  <button
                    type="button"
                    onClick={() => {
                      const content = '=== REPORTE DE ERRORES ===\nTotal: ' + validationErrors.length + '\n\n' +
                        validationErrors.map((e, i) => `${i + 1}. ${e.message}`).join('\n');
                      const blob = new Blob([content], { type: 'text/plain;charset=utf-8' });
                      const url = URL.createObjectURL(blob);
                      const a = document.createElement('a');
                      a.href = url;
                      a.download = 'reporte_errores_importacion.txt';
                      document.body.appendChild(a);
                      a.click();
                      document.body.removeChild(a);
                      URL.revokeObjectURL(url);
                    }}
                    style={{ background: '#991B1B', color: '#fff', border: 'none', borderRadius: '4px', padding: '4px 10px', fontSize: '0.75rem', cursor: 'pointer' }}
                  >Descargar reporte .txt</button>
                )}
                <button
                  type="button"
                  onClick={() => setShowValidationErrors(false)}
                  style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#991B1B', fontSize: '1.25rem', padding: 0 }}
                >✕</button>
              </div>
            </div>
            <ul style={{ margin: 0, paddingLeft: '1.25rem', listStyleType: 'disc' }}>
              {validationErrors.map((err, idx) => (
                <li key={idx} style={{ color: '#DC2626', fontSize: '0.8125rem', marginBottom: '0.25rem', lineHeight: 1.4 }}>
                  {err.message}
                </li>
              ))}
            </ul>
          </div>
        )}

        <div style={styles.footer}>
          <span style={styles.ghostBtn} onClick={handleBack}>
            Volver
          </span>
          <div style={styles.buttonsRight}>
            <span style={styles.ghostBtn} onClick={handleClear}>
              Limpiar
            </span>
            <button
              style={{
                ...styles.primaryBtn,
                opacity: !uploadedFile || uploadedFile.error || isImporting || isValidating ? 0.6 : 1,
                cursor: !uploadedFile || uploadedFile.error || isImporting || isValidating ? 'not-allowed' : 'pointer',
              }}
              onClick={handleImport}
              disabled={!uploadedFile || !!uploadedFile.error || isImporting || isValidating}
            >
              {isValidating ? 'Validando...' : isImporting ? 'Importando...' : 'Importar Elementos'}
            </button>
          </div>
        </div>
      </div>

      {showExitModal && (
        <div style={styles.modal}>
          <div style={styles.modalContent}>
            <h3 style={styles.modalTitle}>¿Desea salir?</h3>
            <p style={styles.modalText}>
              El archivo cargado se perderá. ¿Desea continuar?
            </p>
            <div style={styles.modalButtons}>
              <button
                style={{
                  ...styles.ghostBtn,
                  border: '1px solid #e5e7eb',
                  borderRadius: '0.5rem',
                  textDecoration: 'none',
                }}
                onClick={cancelExit}
              >
                No
              </button>
              <button style={styles.primaryBtn} onClick={confirmExit}>
                Sí
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}


