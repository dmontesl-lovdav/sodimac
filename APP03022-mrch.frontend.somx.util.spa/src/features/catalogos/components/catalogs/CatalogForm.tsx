import { useState, useRef, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';
import { catalogService, catalogElementService } from '@features/catalogos/services/catalogosApi';
import type { CatalogElementCreateDto } from '@features/catalogos/services/catalogosApi';
import '@features/catalogos/styles/catalogos-shared.css';
import { useModalNotification } from '@shared/components/ui/modal';
import { extractApiErrorMessage } from '@shared/utils/errorMessage';
import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';
import { withFinanceBreadcrumb } from '@shared/components/ui/navigation/financeBreadcrumb';

interface CatalogFormData {
  code: string;
  prefix: string;
  name: string;
  description: string;
  type: string;
}

interface UploadedFile {
  file: File;
  name: string;
  size: number;
  error?: string;
}

interface ValidationError {
  row: number;
  cell: string;
  column: string;
  description: string;
  code?: string;
}

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
    color: '#003865',
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
    marginBottom: '1rem',
  },
  stepIndicator: {
    display: 'flex',
    flexDirection: 'column' as const,
    alignItems: 'center',
    marginRight: '1.5rem',
  },
  stepNumber: {
    width: '32px',
    height: '32px',
    borderRadius: '50%',
    backgroundColor: '#002d4c',
    color: '#ffffff',
    fontWeight: 700,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '0.875rem',
    flexShrink: 0,
  },
  stepNumberDisabled: {
    backgroundColor: '#cbd5e1',
    color: '#64748b',
  },
  stepLine: {
    width: '2px',
    flexGrow: 1,
    backgroundColor: '#002d4c',
    marginTop: '0.25rem',
    minHeight: '20px',
  },
  stepLineDisabled: {
    backgroundColor: '#cbd5e1',
  },
  stepContent: {
    flex: 1,
    paddingBottom: '1rem',
  },
  stepHeader: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    cursor: 'pointer',
    padding: '0.5rem 0',
    width: '100%',
    textAlign: 'left' as const,
  },
  stepTitle: {
    fontSize: '1rem',
    fontWeight: 600,
    color: '#1e293b',
  },
  stepTitleDisabled: {
    color: '#94a3b8',
  },
  stepSubtitle: {
    fontSize: '0.875rem',
    color: '#64748b',
    marginTop: '2px',
  },
  stepBody: {
    overflow: 'hidden',
    transition: 'max-height 0.3s ease',
  },
  formGroup: {
    marginBottom: '1rem',
  },
  label: {
    display: 'block',
    marginBottom: '0.25rem',
    fontSize: '0.875rem',
    fontWeight: 500,
    color: '#002d4c',
  },
  required: {
    color: '#ef4444',
  },
  input: {
    width: '100%',
    padding: '0.5rem 1rem',
    fontSize: '0.875rem',
    border: 'none',
    borderBottom: '2px solid #002d4c',
    background: 'transparent',
    outline: 'none',
  },
  inputError: {
    borderBottomColor: '#ef4444',
  },
  textarea: {
    width: '100%',
    padding: '0.5rem 1rem',
    fontSize: '0.875rem',
    border: 'none',
    borderBottom: '2px solid #002d4c',
    background: 'transparent',
    outline: 'none',
    resize: 'vertical' as const,
    minHeight: '60px',
    fontFamily: 'inherit',
  },
  select: {
    width: '100%',
    padding: '0.5rem 1rem',
    fontSize: '0.875rem',
    border: 'none',
    borderBottom: '2px solid #002d4c',
    background: 'transparent',
    outline: 'none',
    cursor: 'pointer',
    appearance: 'none' as const,
    backgroundImage: 'url("data:image/svg+xml,%3Csvg xmlns=\'http://www.w3.org/2000/svg\' fill=\'none\' viewBox=\'0 0 24 24\' stroke=\'%23002d4c\'%3E%3Cpath stroke-linecap=\'round\' stroke-linejoin=\'round\' stroke-width=\'2\' d=\'M19 9l-7 7-7-7\'%3E%3C/path%3E%3C/svg%3E")',
    backgroundRepeat: 'no-repeat',
    backgroundPosition: 'right 0.5rem center',
    backgroundSize: '1.5rem',
    paddingRight: '2.5rem',
  },
  charCounter: {
    textAlign: 'right' as const,
    fontSize: '0.75rem',
    color: '#64748b',
    marginTop: '4px',
  },
  fieldError: {
    fontSize: '0.75rem',
    color: '#ef4444',
    marginTop: '4px',
  },
  dropzone: {
    border: '2px dashed #cbd5e1',
    borderRadius: '0.75rem',
    padding: '2rem',
    textAlign: 'center' as const,
    cursor: 'pointer',
    transition: 'border-color 0.2s ease, background 0.2s ease',
    backgroundColor: '#f9fafb',
  },
  dropzoneHover: {
    borderColor: '#002d4c',
    backgroundColor: 'rgba(0, 45, 76, 0.05)',
  },
  dropzoneDisabled: {
    opacity: 0.5,
    cursor: 'not-allowed',
  },
  dropzoneIcon: {
    width: '64px',
    height: '64px',
    margin: '0 auto 1rem',
    color: '#002d4c',
  },
  dropzoneText: {
    fontSize: '1rem',
    color: '#374151',
    marginBottom: '0.25rem',
  },
  dropzoneLink: {
    fontSize: '0.875rem',
    color: '#002d4c',
    textDecoration: 'underline',
    cursor: 'pointer',
  },
  dropzoneFormats: {
    fontSize: '0.75rem',
    color: '#64748b',
    marginTop: '1rem',
  },
  fileItem: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: '0.5rem 1rem',
    backgroundColor: '#dcfce7',
    borderRadius: '0.5rem',
    marginTop: '0.5rem',
  },
  fileInfo: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.5rem',
  },
  fileIcon: {
    color: '#10b981',
  },
  fileName: {
    fontSize: '0.875rem',
    color: '#374151',
  },
  fileSize: {
    fontSize: '0.75rem',
    color: '#64748b',
    marginLeft: '0.5rem',
  },
  fileDelete: {
    background: 'none',
    border: 'none',
    cursor: 'pointer',
    color: '#ef4444',
    padding: '0.25rem',
  },
  footer: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: '2rem',
    paddingTop: '1rem',
    borderTop: '1px solid #e5e7eb',
  },
  ghostBtn: {
    background: 'transparent',
    border: 'none',
    color: '#003865',
    fontSize: '0.875rem',
    cursor: 'pointer',
    padding: '0.5rem 1rem',
    textDecoration: 'underline',
  },
  primaryBtn: {
    backgroundColor: '#002d4c',
    color: '#ffffff',
    border: 'none',
    borderRadius: '0.375rem',
    padding: '0.75rem 1.5rem',
    fontSize: '0.875rem',
    fontWeight: 500,
    cursor: 'pointer',
    transition: 'background-color 0.2s',
  },
  outlineBtn: {
    backgroundColor: 'transparent',
    color: '#002d4c',
    border: '1px solid #002d4c',
    borderRadius: '0.375rem',
    padding: '0.5rem 1rem',
    fontSize: '0.875rem',
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    gap: '0.5rem',
  },
  buttonsRight: {
    display: 'flex',
    gap: '1rem',
    alignItems: 'center',
  },
  chevron: {
    transition: 'transform 0.2s ease',
    color: '#64748b',
  },
};

const formatExcelDateValue = (val: any): string => {
  if (!val) return '';
  if (val instanceof Date) {
    const y = val.getFullYear();
    const m = String(val.getMonth() + 1).padStart(2, '0');
    const d = String(val.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }
  return String(val).trim();
};

const isEmptyExcelRow = (row: any): boolean =>
  !row || row.every((c: any) => c === '' || c === null || c === undefined);

const excelRowToElement = (row: any) => ({
  tipoCatalogo: String(row[0] ?? '').trim(),
  elemento: String(row[1] ?? '').trim(),
  valor: String(row[2] ?? '').trim(),
  fechaInicioVigencia: formatExcelDateValue(row[3]),
  fechaFinVigencia: formatExcelDateValue(row[4]),
  idPadre: String(row[5] ?? '').trim(),
  valorConversion: row[6] != null ? String(row[6]).trim() : '',
});

const readExcelRows = (file: File): Promise<any[][]> =>
  new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      try {
        const data = new Uint8Array(e.target?.result as ArrayBuffer);
        const workbook = XLSX.read(data, { type: 'array', cellDates: true });
        const sheet = workbook.Sheets[workbook.SheetNames[0]];
        const rows: any[] = XLSX.utils.sheet_to_json(sheet, { header: 1, defval: '' });
        resolve(rows);
      } catch (err) {
        reject(err instanceof Error ? err : new Error(String(err)));
      }
    };
    reader.onerror = () => reject(new Error('Error reading file'));
    reader.readAsArrayBuffer(file);
  });

const mapValidationApiErrors = (apiErrors: any[]): ValidationError[] =>
  apiErrors.map((err: any) => ({
    row: err.row,
    cell: err.cell,
    column: err.column,
    description: err.message,
  }));

const parseExcelElements = async (file: File): Promise<Array<{
  tipoCatalogo: string; elemento: string; valor: string;
  fechaInicioVigencia: string; fechaFinVigencia: string;
  idPadre: string; valorConversion: string;
}>> => {
  const rows = await readExcelRows(file);
  if (rows.length < 2) return [];
  return rows.slice(1).filter((r) => !isEmptyExcelRow(r)).map(excelRowToElement);
};

const importElementsAfterCreate = async (createdCatalogId: number, file: File) => {
  const elements = await parseExcelElements(file);
  let createdCount = 0;
  let conversionCount = 0;
  for (const elem of elements) {
    const createDto: CatalogElementCreateDto = {
      element: elem.elemento,
      value: elem.valor || undefined,
      validFrom: elem.fechaInicioVigencia,
      validTo: elem.fechaFinVigencia || undefined,
      parentElementId: elem.idPadre ? Number(elem.idPadre) : undefined,
      externalKey: elem.valorConversion || undefined,
    };
    await catalogElementService.create(createdCatalogId, createDto);
    createdCount++;
    if (elem.valorConversion) conversionCount++;
  }
  return { createdCount, conversionCount };
};

const areCatalogFormsEqual = (a: CatalogFormData, b: CatalogFormData): boolean =>
  a.name === b.name &&
  a.description === b.description &&
  a.type === b.type;

const mapApiCatalogToForm = (catalog: any): CatalogFormData => {
  let typeDisplay = catalog.catalogType;
  if (catalog.catalogType === 'PRIMARIO') typeDisplay = 'Primario';
  else if (catalog.catalogType === 'SECUNDARIO') typeDisplay = 'Secundario';
  return {
    code: catalog.code || '',
    prefix: catalog.prefix || '',
    name: catalog.name,
    description: catalog.description || '',
    type: typeDisplay,
  };
};

const validateCatalogFormValues = (
  formData: CatalogFormData,
  isEditMode: boolean,
): Record<string, string> => {
  const newErrors: Record<string, string> = {};
  if (!isEditMode && !formData.code.trim()) newErrors.code = 'Introduce un código de catálogo.';
  if (!isEditMode && !formData.prefix.trim()) newErrors.prefix = 'Introduce un prefijo.';
  if (!formData.name.trim()) newErrors.name = 'Introduce un nombre de catálogo.';
  if (!formData.type) newErrors.type = 'Seleccione un tipo de catálogo.';
  return newErrors;
};

const buildDownloadFileName = (name: string): string => {
  const now = new Date();
  const day = String(now.getDate()).padStart(2, '0');
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const year = now.getFullYear();
  const dateStr = `${day}${month}${year}`;
  const catalogName = name.replace(/\s+/g, '_').toLowerCase() || 'plantilla';
  return `${catalogName}_${dateStr}.xlsx`;
};

const buildTemplateWorkbook = (isPrimario: boolean) => {
  const headers = ['tipoCatalogo', 'elemento', 'valor', 'fechaInicioVigencia', 'fechaFinVigencia', 'idPadre', 'valorConversion'];
  const exampleRow = isPrimario
    ? ['primario', 'México', 'MEX', '2025-01-01', '2025-12-31', '', 'MX']
    : ['secundario', 'Jalisco', 'JAL', '2025-01-01', '', '151', '21'];
  const wsData = [headers, exampleRow];
  const ws = XLSX.utils.aoa_to_sheet(wsData);
  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, 'Layout');
  return XLSX.write(wb, { bookType: 'xlsx', type: 'array' });
};

const buildLocalErrorReport = (
  fileName: string,
  errors: ValidationError[],
): string => {
  let content = `REPORTE DE ERRORES DE VALIDACIÓN\n`;
  content += `Archivo: ${fileName}\n`;
  content += `Fecha: ${new Date().toLocaleString()}\n`;
  content += `Total de errores: ${errors.length}\n`;
  content += `${'='.repeat(60)}\n\n`;
  errors.forEach((error, index) => {
    content += `Error ${index + 1}:\n`;
    content += `  Fila: ${error.row}\n`;
    content += `  Celda: ${error.cell}\n`;
    content += `  Columna: ${error.column}\n`;
    content += `  Descripción: ${error.description}\n`;
    if (error.code) content += `  Código: ${error.code}\n`;
    content += `\n`;
  });
  return content;
};

const validateUploadedFile = (file: File): string | null => {
  const validExtensions = ['csv', 'xls', 'xlsx'];
  const extension = file.name.split('.').pop()?.toLowerCase();
  const maxSize = 4 * 1024 * 1024;
  const isValidExtension = extension && validExtensions.includes(extension);
  const isValidSize = file.size <= maxSize;
  if (!isValidExtension || !isValidSize) {
    return 'Este documento no es compatible. Por favor, elimínelo y cargue otro archivo.';
  }
  return null;
};

const EMPTY_CATALOG_FORM: CatalogFormData = {
  code: '',
  prefix: '',
  name: '',
  description: '',
  type: '',
};

const getSaveButtonLabel = (state: {
  isValidating: boolean;
  isSaving: boolean;
  isEditMode: boolean;
}): string => {
  if (state.isValidating) return 'Validando archivo...';
  if (state.isSaving) return 'Guardando...';
  return state.isEditMode ? 'Guardar' : 'Guardar Catálogo';
};

const StepChevron = ({ expanded }: { expanded: boolean }) => (
  <svg
    width="20"
    height="20"
    viewBox="0 0 20 20"
    fill="currentColor"
    style={{
      ...styles.chevron,
      transform: expanded ? 'rotate(180deg)' : 'rotate(0deg)',
    }}
  >
    <path
      fillRule="evenodd"
      d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"
      clipRule="evenodd"
    />
  </svg>
);

interface CatalogStep1Props {
  expanded: boolean;
  isEditMode: boolean;
  formData: CatalogFormData;
  errors: Record<string, string>;
  onToggle: () => void;
  onInputChange: (field: keyof CatalogFormData, value: string) => void;
}

function CatalogStep1({ expanded, isEditMode, formData, errors, onToggle, onInputChange }: Readonly<CatalogStep1Props>) {
  return (
    <div style={styles.stepContainer}>
      <div style={styles.stepIndicator}>
        <div style={styles.stepNumber}>1</div>
        <div style={styles.stepLine} />
      </div>
      <div style={styles.stepContent}>
        <button
          type="button"
          style={{ ...styles.stepHeader, border: 'none', background: 'transparent', appearance: 'none' }}
          onClick={onToggle}
        >
          <div>
            <div style={styles.stepTitle}>Datos de Catálogo</div>
            <div style={styles.stepSubtitle}>
              Captura la información general del nuevo catálogo.
            </div>
          </div>
          <StepChevron expanded={expanded} />
        </button>

        {expanded && (
          <div style={{ marginTop: '1rem' }}>
            <div style={{ display: 'flex', gap: '1rem' }}>
              <div style={{ ...styles.formGroup, flex: 1 }}>
                <label htmlFor="catalog-code" style={styles.label}>
                  <span style={styles.required}>*</span>Código del catálogo
                </label>
                <input
                  id="catalog-code"
                  type="text"
                  style={{
                    ...styles.input,
                    ...(errors.code ? styles.inputError : {}),
                  }}
                  value={formData.code}
                  onChange={(e) => onInputChange('code', e.target.value.replace(/[^A-Za-z]/g, ''))}
                  placeholder="CAT_EJEMPLO"
                  disabled={isEditMode}
                />
                {errors.code && (
                  <div style={styles.fieldError}>{errors.code}</div>
                )}
              </div>
              <div style={{ ...styles.formGroup, flex: 1 }}>
                <label htmlFor="catalog-prefix" style={styles.label}>
                  <span style={styles.required}>*</span>Prefijo
                </label>
                <input
                  id="catalog-prefix"
                  type="text"
                  style={{
                    ...styles.input,
                    ...(errors.prefix ? styles.inputError : {}),
                  }}
                  value={formData.prefix}
                  onChange={(e) => onInputChange('prefix', e.target.value.toUpperCase().replace(/[^A-Z]/g, '').slice(0, 3))}
                  placeholder="CAT"
                  maxLength={3}
                  disabled={isEditMode}
                />
                {errors.prefix && (
                  <div style={styles.fieldError}>{errors.prefix}</div>
                )}
              </div>
            </div>

            <div style={styles.formGroup}>
              <label htmlFor="catalog-name" style={styles.label}>
                <span style={styles.required}>*</span>Nombre del catálogo
              </label>
              <input
                id="catalog-name"
                type="text"
                style={{
                  ...styles.input,
                  ...(errors.name ? styles.inputError : {}),
                }}
                value={formData.name}
                onChange={(e) => onInputChange('name', e.target.value)}
                placeholder="Nombre del catálogo"
              />
              {errors.name && (
                <div style={styles.fieldError}>{errors.name}</div>
              )}
            </div>

            <div style={styles.formGroup}>
              <label htmlFor="catalog-description" style={styles.label}>Descripción del catálogo</label>
              <textarea
                id="catalog-description"
                style={styles.textarea}
                value={formData.description}
                onChange={(e) =>
                  onInputChange(
                    'description',
                    e.target.value.slice(0, 250)
                  )
                }
                placeholder="Descripción del catálogo"
                maxLength={250}
              />
              <div style={styles.charCounter}>
                {formData.description.length} / 250
              </div>
            </div>

            <div style={styles.formGroup}>
              <label htmlFor="catalog-type" style={styles.label}>
                <span style={styles.required}>*</span>Tipo de catálogo
              </label>
              <select
                id="catalog-type"
                style={{
                  ...styles.select,
                  ...(errors.type ? styles.inputError : {}),
                }}
                value={formData.type}
                onChange={(e) => onInputChange('type', e.target.value)}
              >
                <option value="">Seleccionar tipo</option>
                <option value="Primario">Primario</option>
                <option value="Secundario">Secundario</option>
              </select>
              {errors.type && (
                <div style={styles.fieldError}>{errors.type}</div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

interface CatalogStep2Props {
  expanded: boolean;
  isStep1Complete: boolean;
  onToggle: () => void;
  onDownloadTemplate: () => void;
}

function CatalogStep2({ expanded, isStep1Complete, onToggle, onDownloadTemplate }: Readonly<CatalogStep2Props>) {
  return (
    <div style={styles.stepContainer}>
      <div style={styles.stepIndicator}>
        <div
          style={{
            ...styles.stepNumber,
            ...(isStep1Complete ? {} : styles.stepNumberDisabled),
          }}
        >
          2
        </div>
        <div
          style={{
            ...styles.stepLine,
            ...(isStep1Complete ? {} : styles.stepLineDisabled),
          }}
        />
      </div>
      <div style={styles.stepContent}>
        <button
          type="button"
          style={{
            ...styles.stepHeader,
            cursor: isStep1Complete ? 'pointer' : 'not-allowed',
            border: 'none',
            background: 'transparent',
            appearance: 'none',
          }}
          onClick={onToggle}
        >
          <div>
            <div
              style={{
                ...styles.stepTitle,
                ...(isStep1Complete ? {} : styles.stepTitleDisabled),
              }}
            >
              Elementos del Catálogo
            </div>
            <div style={styles.stepSubtitle}>
              Descarga y completa la plantilla con los elementos del
              catálogo, asegurándote de seguir el formato requerido para
              garantizar una carga exitosa.
            </div>
          </div>
          <StepChevron expanded={expanded} />
        </button>

        {expanded && isStep1Complete && (
          <div style={{ marginTop: '1rem' }}>
            <button
              style={styles.outlineBtn}
              onClick={onDownloadTemplate}
            >
              <svg
                width="16"
                height="16"
                viewBox="0 0 16 16"
                fill="currentColor"
              >
                <path d="M8 12l-4-4h2.5V3h3v5H12L8 12z" />
                <path d="M14 14H2v-2h12v2z" />
              </svg>
              Descargar plantilla de elementos de catálogo
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

interface CatalogStep3Props {
  expanded: boolean;
  isStep1Complete: boolean;
  onToggle: () => void;
  onDropzoneClick: () => void;
  onDrop: (e: React.DragEvent<HTMLDivElement>) => void;
  fileInputRef: React.RefObject<HTMLInputElement>;
  onFileSelect: (e: React.ChangeEvent<HTMLInputElement>) => void;
  uploadedFile: UploadedFile | null;
  onRemoveFile: () => void;
  formatFileSize: (bytes: number) => string;
}

function CatalogStep3({
  expanded,
  isStep1Complete,
  onToggle,
  onDropzoneClick,
  onDrop,
  fileInputRef,
  onFileSelect,
  uploadedFile,
  onRemoveFile,
  formatFileSize,
}: Readonly<CatalogStep3Props>) {
  return (
    <div style={styles.stepContainer}>
      <div style={styles.stepIndicator}>
        <div
          style={{
            ...styles.stepNumber,
            ...(isStep1Complete ? {} : styles.stepNumberDisabled),
          }}
        >
          3
        </div>
      </div>
      <div style={styles.stepContent}>
        <button
          type="button"
          style={{
            ...styles.stepHeader,
            cursor: isStep1Complete ? 'pointer' : 'not-allowed',
            border: 'none',
            background: 'transparent',
            appearance: 'none',
          }}
          onClick={onToggle}
        >
          <div>
            <div
              style={{
                ...styles.stepTitle,
                ...(isStep1Complete ? {} : styles.stepTitleDisabled),
              }}
            >
              Cargar Plantilla
            </div>
            <div style={styles.stepSubtitle}>
              Sube la plantilla completada arrastrándola al área de carga o
              seleccionándola manualmente desde el explorador de archivos.
            </div>
          </div>
          <StepChevron expanded={expanded} />
        </button>

        {expanded && isStep1Complete && (
          <div style={{ marginTop: '1rem' }}>
            <button
              type="button"
              style={{
                ...styles.dropzone,
                ...(uploadedFile ? styles.dropzoneDisabled : {}),
                appearance: 'none',
                display: 'block',
                width: '100%',
                boxSizing: 'border-box',
              }}
              onDragOver={(e) => e.preventDefault()}
              onDrop={onDrop}
              onClick={onDropzoneClick}
            >
              <input
                ref={fileInputRef}
                type="file"
                style={{ display: 'none' }}
                accept=".csv,.xls,.xlsx"
                onChange={onFileSelect}
              />
              <svg
                style={styles.dropzoneIcon}
                viewBox="0 0 64 64"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <circle cx="32" cy="32" r="28" strokeDasharray="4 2" />
                <path d="M32 20v24M20 32l12-12 12 12" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
              <div style={styles.dropzoneText}>Arrastra y suelta el archivo</div>
              <div style={styles.dropzoneLink}>o haz clic aquí para explorar</div>
              <div style={styles.dropzoneFormats}>
                Formatos soportados: CSV, XLS, XLSX. Peso máximo: 4mb.
              </div>
            </button>

            {uploadedFile && (
              <div
                style={{
                  ...styles.fileItem,
                  backgroundColor: uploadedFile.error ? '#fee2e2' : '#dcfce7',
                }}
              >
                <div style={styles.fileInfo}>
                  <svg
                    width="16"
                    height="16"
                    viewBox="0 0 16 16"
                    fill={uploadedFile.error ? '#ef4444' : '#10b981'}
                  >
                    {uploadedFile.error ? (
                      <path d="M8 0C3.58 0 0 3.58 0 8s3.58 8 8 8 8-3.58 8-8-3.58-8-8-8zm1 12H7v-2h2v2zm0-3H7V4h2v5z" />
                    ) : (
                      <path d="M8 0C3.58 0 0 3.58 0 8s3.58 8 8 8 8-3.58 8-8-3.58-8-8-8zm3.707 6.707l-4 4a1 1 0 01-1.414 0l-2-2a1 1 0 111.414-1.414L7 8.586l3.293-3.293a1 1 0 111.414 1.414z" />
                    )}
                  </svg>
                  <span style={styles.fileName}>{uploadedFile.name}</span>
                  <span style={styles.fileSize}>
                    {formatFileSize(uploadedFile.size)}
                  </span>
                </div>
                <button style={styles.fileDelete} onClick={onRemoveFile}>
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                    <path d="M5.5 5.5A.5.5 0 016 6v6a.5.5 0 01-1 0V6a.5.5 0 01.5-.5zm2.5 0a.5.5 0 01.5.5v6a.5.5 0 01-1 0V6a.5.5 0 01.5-.5zm3 .5a.5.5 0 00-1 0v6a.5.5 0 001 0V6z" />
                    <path fillRule="evenodd" d="M14.5 3a1 1 0 01-1 1H13v9a2 2 0 01-2 2H5a2 2 0 01-2-2V4h-.5a1 1 0 01-1-1V2a1 1 0 011-1H6a1 1 0 011-1h2a1 1 0 011 1h3.5a1 1 0 011 1v1zM4.118 4L4 4.059V13a1 1 0 001 1h6a1 1 0 001-1V4.059L11.882 4H4.118zM2.5 3V2h11v1h-11z" clipRule="evenodd" />
                  </svg>
                </button>
              </div>
            )}

            {uploadedFile?.error && (
              <div style={styles.fieldError}>{uploadedFile.error}</div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

interface ErrorReportBannerProps {
  count: number;
  onDownload: () => void;
}

function ErrorReportBanner({ count, onDownload }: Readonly<ErrorReportBannerProps>) {
  return (
    <div
      style={{
        marginTop: '1.5rem',
        padding: '1rem',
        backgroundColor: '#fee2e2',
        border: '1px solid #fecaca',
        borderRadius: '0.5rem',
      }}
    >
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          gap: '1rem',
        }}
      >
        <span style={{ color: '#991b1b', fontSize: '0.875rem' }}>
          Se encontraron {count} errores en el archivo. Descarga el reporte para revisarlos todos.
        </span>
        <button
          style={{
            ...styles.outlineBtn,
            borderColor: '#991b1b',
            color: '#991b1b',
          }}
          onClick={onDownload}
        >
          ⬇ Descargar reporte de errores
        </button>
      </div>
    </div>
  );
}

interface ExitModalProps {
  isEditMode: boolean;
  onCancel: () => void;
  onConfirm: () => void;
}

function ExitModal({ isEditMode, onCancel, onConfirm }: Readonly<ExitModalProps>) {
  return (
    <div
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        zIndex: 1000,
      }}
    >
      <div
        style={{
          backgroundColor: '#ffffff',
          borderRadius: '0.75rem',
          padding: '1.5rem',
          maxWidth: '400px',
          width: '90%',
          boxShadow: '0 10px 25px rgba(0, 0, 0, 0.2)',
        }}
      >
        <h3
          style={{
            fontSize: '1.125rem',
            fontWeight: 600,
            marginBottom: '1rem',
            color: '#1e293b',
          }}
        >
          ¿Desea salir?
        </h3>
        <p
          style={{
            fontSize: '0.875rem',
            color: '#64748b',
            marginBottom: '1.5rem',
          }}
        >
          {isEditMode
            ? 'La información editada del catálogo se perderá. ¿Desea continuar?'
            : 'La información ingresada para registrar el catálogo se perderá. ¿Desea continuar?'}
        </p>
        <div
          style={{
            display: 'flex',
            justifyContent: 'flex-end',
            gap: '0.75rem',
          }}
        >
          <button
            style={{
              ...styles.ghostBtn,
              border: '1px solid #e5e7eb',
              borderRadius: '0.5rem',
              textDecoration: 'none',
            }}
            onClick={onCancel}
          >
            No
          </button>
          <button style={styles.primaryBtn} onClick={onConfirm}>
            Sí
          </button>
        </div>
      </div>
    </div>
  );
}

interface TypeChangeModalProps {
  originalType: string;
  pendingType: string | null;
  onCancel: () => void;
  onConfirm: () => void;
}

function TypeChangeModal({ originalType, pendingType, onCancel, onConfirm }: Readonly<TypeChangeModalProps>) {
  return (
    <div
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        zIndex: 1000,
      }}
    >
      <div
        style={{
          backgroundColor: '#ffffff',
          borderRadius: '0.75rem',
          padding: '1.5rem',
          maxWidth: '500px',
          width: '90%',
          boxShadow: '0 10px 25px rgba(0, 0, 0, 0.2)',
        }}
      >
        <h3
          style={{
            fontSize: '1.125rem',
            fontWeight: 600,
            marginBottom: '1rem',
            color: '#1e293b',
          }}
        >
          Cambio de tipo de catálogo
        </h3>
        <p
          style={{
            fontSize: '0.875rem',
            color: '#64748b',
            marginBottom: '1.5rem',
            lineHeight: 1.6,
          }}
        >
          {originalType === 'Primario' && pendingType === 'Secundario'
            ? "El catálogo cambiará a tipo 'Secundario', lo que significa que sus elementos solo podrán ser agrupados o relacionados con elementos de catálogos de tipo 'Primario'. Además, se eliminarán todas las relaciones existentes con elementos de otros catálogos secundarios que actualmente apuntan a este catálogo. ¿Desea continuar?"
            : "El catálogo cambiará a tipo 'Primario', lo que significa que sus elementos solo podrán ser referenciados para agrupar o relacionar elementos de catálogos de tipo 'Secundario'. Además, se eliminarán todas las relaciones existentes con elementos de otros catálogos primarios que actualmente estén vinculados a este catálogo. ¿Desea continuar?"}
        </p>
        <div
          style={{
            display: 'flex',
            justifyContent: 'flex-end',
            gap: '0.75rem',
          }}
        >
          <button
            style={{
              ...styles.ghostBtn,
              border: '1px solid #e5e7eb',
              borderRadius: '0.5rem',
              textDecoration: 'none',
            }}
            onClick={onCancel}
          >
            No
          </button>
          <button style={styles.primaryBtn} onClick={onConfirm}>
            Sí
          </button>
        </div>
      </div>
    </div>
  );
}

export default function CatalogForm() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const { showSuccess, showError, showWarning, showErrorList, ModalNode } = useModalNotification();

  const isEditMode = !!id;

  const [formData, setFormData] = useState<CatalogFormData>({ ...EMPTY_CATALOG_FORM });
  const [originalData, setOriginalData] = useState<CatalogFormData>({ ...EMPTY_CATALOG_FORM });

  const [isLoading, setIsLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [expandedSteps, setExpandedSteps] = useState<number[]>([1]);
  const [uploadedFile, setUploadedFile] = useState<UploadedFile | null>(null);
  const [isSaving, setIsSaving] = useState(false);
  const [isValidating, setIsValidating] = useState(false);
  const [validationErrors, setValidationErrors] = useState<ValidationError[]>([]);
  const [showExitModal, setShowExitModal] = useState(false);
  const [showTypeChangeModal, setShowTypeChangeModal] = useState(false);
  const [pendingTypeChange, setPendingTypeChange] = useState<string | null>(null);

  const ERROR_THRESHOLD = 20;

  useEffect(() => {
    if (!isEditMode || !id) return;
    setIsLoading(true);
    catalogService.getById(parseInt(id))
      .then((catalog) => {
        const data = mapApiCatalogToForm(catalog);
        setFormData(data);
        setOriginalData(data);
      })
      .catch((error) => {
        console.error('Error loading catalog:', error);
        showError('No se pudo cargar el catálogo. Verifique que existe.', 'Error al cargar', () =>
          navigate('/util/catalogos/catalogs'),
        );
      })
      .finally(() => {
        setIsLoading(false);
      });
  }, [isEditMode, id, navigate]);

  const hasChanges = () => !areCatalogFormsEqual(formData, originalData) || uploadedFile !== null;

  const isStep1Complete = formData.name.trim() !== '' && formData.type !== '' && formData.code.trim() !== '' && formData.prefix.trim() !== '';

  useEffect(() => {
    if (isStep1Complete && !isEditMode) {
      setExpandedSteps((prev) => Array.from(new Set([...prev, 2, 3])));
    }
  }, [isStep1Complete, isEditMode]);

  const toggleStep = (step: number) => {
    if (step !== 1 && !isStep1Complete) return;
    setExpandedSteps((prev) =>
      prev.includes(step) ? prev.filter((s) => s !== step) : [...prev, step]
    );
  };

  const handleInputChange = (field: keyof CatalogFormData, value: string) => {
    if (field === 'type' && isEditMode && originalData.type && value !== originalData.type && value !== '') {
      setPendingTypeChange(value);
      setShowTypeChangeModal(true);
      return;
    }

    setFormData((prev) => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: '' }));
    }
  };

  const confirmTypeChange = () => {
    if (pendingTypeChange) {
      setFormData((prev) => ({ ...prev, type: pendingTypeChange }));
      if (errors.type) {
        setErrors((prev) => ({ ...prev, type: '' }));
      }
    }
    setShowTypeChangeModal(false);
    setPendingTypeChange(null);
  };

  const cancelTypeChange = () => {
    setShowTypeChangeModal(false);
    setPendingTypeChange(null);
  };

  const validateForm = (): boolean => {
    const newErrors = validateCatalogFormValues(formData, isEditMode);
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleClear = () => {
    setFormData({ ...EMPTY_CATALOG_FORM });
    setErrors({});
    setUploadedFile(null);
    setValidationErrors([]);

    setExpandedSteps([1]);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleBack = () => {
    const hasUnsavedChanges = isEditMode
      ? hasChanges()
      : formData.name.trim() !== '' ||
        formData.description.trim() !== '' ||
        formData.type !== '' ||
        uploadedFile !== null;

    if (hasUnsavedChanges) {
      setShowExitModal(true);
    } else {
      navigate('/util/catalogos/catalogs');
    }
  };

  const confirmExit = () => {
    setShowExitModal(false);
    navigate('/util/catalogos/catalogs');
  };

  const cancelExit = () => {
    setShowExitModal(false);
  };

  const applyLayoutErrors = (errors: ValidationError[]) => {
    setValidationErrors(errors);
    showErrorList({
      title: 'Errores en la plantilla',
      items: errors.map((e) => e.description),
    });

  };

  const runLayoutValidation = async (): Promise<boolean> => {
    try {
      const errors = await validateFileWithBackend();
      if (errors.length > 0) {
        applyLayoutErrors(errors);
        return false;
      }
      return true;
    } catch (error: any) {
      const apiErrors = error?.response?.data?.errors;
      if (Array.isArray(apiErrors) && apiErrors.length > 0) {
        applyLayoutErrors(mapValidationApiErrors(apiErrors));
        return false;
      }
      console.error('Error validating file:', error);
      const backendMsg =
        error?.response?.data?.error ??
        error?.response?.data?.message ??
        'No fue posible validar el archivo. Verifica tu conexión e inténtalo nuevamente.';
      showError(backendMsg, 'Error al validar');
      return false;
    }
  };

  const showCreateSuccess = (createdCount: number, conversionCount: number) => {
    const base = `El catálogo "${formData.name}" se ha registrado exitosamente.`;
    if (createdCount === 0) {
      showSuccess(base, 'Catálogo creado', () => navigate('/util/catalogos/catalogs'));
      return;
    }
    const extra = conversionCount > 0
      ? ` Se cargaron ${createdCount} elementos, incluyendo ${conversionCount} con valor de conversión.`
      : ` Se cargaron ${createdCount} elementos.`;
    showSuccess(`${base}${extra}`, 'Catálogo creado', () =>
      navigate('/util/catalogos/catalogs'),
    );
  };

  const runUpdateCatalog = async (catalogType: string) => {
    await catalogService.update(parseInt(id ?? ''), {
      name: formData.name,
      description: formData.description || undefined,
      catalogType,
    });
    showSuccess(
      `El catálogo "${formData.name}" se ha editado exitosamente.`,
      'Catálogo actualizado',
      () => navigate('/util/catalogos/catalogs'),
    );
  };

  const runCreateCatalog = async (catalogType: string) => {
    const createdCatalog = await catalogService.create({
      code: formData.code,
      prefix: formData.prefix,
      name: formData.name,
      description: formData.description || undefined,
      catalogType,
    });

    if (!uploadedFile?.file || !createdCatalog?.id) {
      showCreateSuccess(0, 0);
      return;
    }

    try {
      const { createdCount, conversionCount } = await importElementsAfterCreate(
        createdCatalog.id,
        uploadedFile.file,
      );
      showCreateSuccess(createdCount, conversionCount);
    } catch (elemError: any) {
      console.error('Error creating elements from layout:', elemError);
      const msg = elemError?.response?.data?.message || 'Error al crear algunos elementos del layout.';
      showWarning(
        `El catálogo se creó pero hubo un error al cargar elementos: ${msg}`,
        'Atención',
        () => navigate('/util/catalogos/catalogs'),
      );
    }
  };

  const handleSave = async () => {
    if (!validateForm()) return;

    if (uploadedFile && !uploadedFile.error) {
      setIsValidating(true);
      setValidationErrors([]);

      const ok = await runLayoutValidation();
      setIsValidating(false);
      if (!ok) return;
    }

    setIsSaving(true);
    try {
      const catalogType = formData.type === 'Primario' ? 'PRIMARIO' : 'SECUNDARIO';
      if (isEditMode && id) {
        await runUpdateCatalog(catalogType);
      } else {
        await runCreateCatalog(catalogType);
      }
    } catch (error: any) {
      console.error('Error saving catalog:', error);
      showError(
        extractApiErrorMessage(error, {
          fallback: 'No fue posible guardar los cambios del catálogo. Inténtalo nuevamente.',
        }),
        'No se pudo guardar el catálogo',
      );
    } finally {
      setIsSaving(false);
    }
  };

  const handleDownloadTemplate = () => {
    const excelBuffer = buildTemplateWorkbook(formData.type === 'Primario');
    const blob = new Blob([excelBuffer], { type: 'application/octet-stream' });
    saveAs(blob, buildDownloadFileName(formData.name));
  };

  const handleFileDrop = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    if (!isStep1Complete) return;

    const files = e.dataTransfer.files;
    if (files.length > 0) {
      processFile(files[0]);
    }
  };

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (files && files.length > 0) {
      processFile(files[0]);
    }
  };

  const processFile = (file: File) => {
    const error = validateUploadedFile(file);
    if (error) {
      setUploadedFile({ file, name: file.name, size: file.size, error });
      return;
    }
    setUploadedFile({ file, name: file.name, size: file.size });
  };

  const formatFileSize = (bytes: number): string => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  };

  const removeFile = () => {
    setUploadedFile(null);
    setValidationErrors([]);

    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const [backendReportId, setBackendReportId] = useState<string | null>(null);

  const validateFileWithBackend = async (): Promise<ValidationError[]> => {
    if (!uploadedFile?.file) return [];

    const result = await catalogService.validateLayout(
      uploadedFile.file,
      formData.type === 'Primario' ? 'PRIMARIO' : 'SECUNDARIO',
      formData.name,
    );

    if (result.isValid) {
      setBackendReportId(null);
      return [];
    }

    if (result.reportAvailable && result.reportId) {
      setBackendReportId(result.reportId);
    }

    return (result.errors || []).map((err) => ({
      row: err.row,
      cell: err.cell,
      column: err.column,
      description: err.message,
    }));
  };

  const tryDownloadBackendReport = async (): Promise<boolean> => {
    if (!backendReportId) return false;
    try {
      const response = await fetch(`http://localhost:8083/catalogos/validation-reports/${backendReportId}`);
      if (!response.ok) return false;
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `reporte_errores_${backendReportId}.txt`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      return true;
    } catch (e) {
      console.warn('Error descargando reporte del backend, generando localmente:', e);
      return false;
    }
  };

  const downloadErrorReport = async () => {
    if (validationErrors.length === 0) return;
    const downloadedFromBackend = await tryDownloadBackendReport();
    if (downloadedFromBackend) return;

    const reportContent = buildLocalErrorReport(uploadedFile?.name || 'N/A', validationErrors);
    const blob = new Blob([reportContent], { type: 'text/plain;charset=utf-8' });
    const timestamp = new Date().toISOString().replace(/[-:T]/g, '').slice(0, 14);
    saveAs(blob, `reporte_errores_${timestamp}.txt`);
  };

  return (
    <div style={styles.container}>
      <Breadcrumb
        items={withFinanceBreadcrumb([
          { label: 'Gestión de Catálogos', to: '/util/catalogos' },
          { label: 'Catálogos', to: '/util/catalogos/catalogs' },
          { label: isEditMode ? 'Editar Catálogo' : 'Nuevo Catálogo' },
        ])}
      />

      {isLoading ? (
        <div style={{ padding: '2rem', textAlign: 'center' }}>
          <div className="somx-loading-spinner" style={{ margin: '0 auto' }} />
          <p style={{ marginTop: '1rem', color: '#64748b' }}>Cargando catálogo...</p>
        </div>
      ) : (
      <div style={styles.card}>
        <h1 style={styles.title}>{isEditMode ? 'Editar Catálogo' : 'Nuevo Catálogo'}</h1>
        <p style={styles.subtitle}>
          {isEditMode
            ? 'Para editar un catálogo existente, actualiza la información en el formulario, asegurándote de ingresar datos precisos, completos y actualizados.'
            : 'Para crear un nuevo catálogo, sigue las instrucciones y completa el formulario, asegurándote de ingresar información precisa, completa y actualizada.'}
        </p>

        <CatalogStep1
          expanded={expandedSteps.includes(1)}
          isEditMode={isEditMode}
          formData={formData}
          errors={errors}
          onToggle={() => toggleStep(1)}
          onInputChange={handleInputChange}
        />

        {!isEditMode && (
        <>
        <CatalogStep2
          expanded={expandedSteps.includes(2)}
          isStep1Complete={isStep1Complete}
          onToggle={() => toggleStep(2)}
          onDownloadTemplate={handleDownloadTemplate}
        />

        <CatalogStep3
          expanded={expandedSteps.includes(3)}
          isStep1Complete={isStep1Complete}
          onToggle={() => toggleStep(3)}
          onDropzoneClick={() => {
            if (!uploadedFile) fileInputRef.current?.click();
          }}
          onDrop={handleFileDrop}
          fileInputRef={fileInputRef}
          onFileSelect={handleFileSelect}
          uploadedFile={uploadedFile}
          onRemoveFile={removeFile}
          formatFileSize={formatFileSize}
        />
        </>
        )}

        {!isEditMode && validationErrors.length > ERROR_THRESHOLD && backendReportId && (
          <ErrorReportBanner count={validationErrors.length} onDownload={downloadErrorReport} />
        )}

        <div style={styles.footer}>
          <button
            type="button"
            style={{ ...styles.ghostBtn, appearance: 'none' }}
            onClick={handleBack}
          >
            Volver
          </button>
          <div style={styles.buttonsRight}>
            <button
              type="button"
              style={{ ...styles.ghostBtn, appearance: 'none' }}
              onClick={handleClear}
            >
              Limpiar
            </button>
            <button
              style={{
                ...styles.primaryBtn,
                opacity: isSaving || isValidating ? 0.6 : 1,
                cursor: isSaving || isValidating ? 'not-allowed' : 'pointer',
              }}
              onClick={handleSave}
              disabled={isSaving || isValidating}
            >
              {getSaveButtonLabel({ isValidating, isSaving, isEditMode })}
            </button>
          </div>
        </div>
      </div>
      )}

      {showExitModal && (
        <ExitModal isEditMode={isEditMode} onCancel={cancelExit} onConfirm={confirmExit} />
      )}

      {showTypeChangeModal && (
        <TypeChangeModal
          originalType={originalData.type}
          pendingType={pendingTypeChange}
          onCancel={cancelTypeChange}
          onConfirm={confirmTypeChange}
        />
      )}
      {ModalNode}
    </div>
  );
}


