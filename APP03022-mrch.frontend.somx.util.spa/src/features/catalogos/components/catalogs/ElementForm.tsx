import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { catalogService, catalogElementService } from '@features/catalogos/services/catalogosApi';
import type { CatalogSimple, CatalogElement, CatalogElementCreateDto, CatalogElementUpdateDto } from '@features/catalogos/services/catalogosApi';
import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';
import { withFinanceBreadcrumb } from '@shared/components/ui/navigation/financeBreadcrumb';
import { useModalNotification } from '@shared/components/ui/modal';

interface FormData {
  elementName: string;
  startDate: string;
  endDate: string;
  value: string;
  externalKey: string;
  parentCatalogId: string;
  parentElementId: string;
}

interface FormErrors {
  elementName?: string;
  startDate?: string;
  endDate?: string;
  value?: string;
  externalKey?: string;
  parentElementId?: string;
}

const EXTERNAL_KEY_REGEX = /^[a-zA-Z0-9._-]*$/;

const INVALID_CHARS = /[!?¡¿:;.,@#%^&*(){}<>/'"\\]/;

const styles = {
  container: { minHeight: '100vh', backgroundColor: '#ffffff', padding: '1.5rem 2rem' },
  breadcrumb: { fontSize: '0.875rem', color: '#64748b', marginBottom: '1.5rem' },
  breadcrumbLink: { color: '#0066CC', textDecoration: 'none', cursor: 'pointer' },
  card: { backgroundColor: '#ffffff', border: '1px solid #e5e7eb', borderRadius: '0.75rem', padding: '2rem', maxWidth: '800px' },
  title: { fontSize: '1.5rem', fontWeight: 600, color: '#1e293b', marginBottom: '0.5rem' },
  subtitle: { fontSize: '0.875rem', color: '#64748b', marginBottom: '2rem', lineHeight: 1.6 },
  stepContainer: { display: 'flex', marginBottom: '1.5rem' },
  stepIndicator: { display: 'flex', flexDirection: 'column' as const, alignItems: 'center', marginRight: '1rem' },
  stepNumber: { width: '32px', height: '32px', backgroundColor: '#0066CC', color: '#ffffff', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 600, fontSize: '0.875rem', flexShrink: 0 },
  stepNumberDisabled: { backgroundColor: '#94a3b8' },
  stepLine: { width: '2px', flexGrow: 1, backgroundColor: '#0066CC', marginTop: '0.5rem' },
  stepLineDisabled: { backgroundColor: '#cbd5e1' },
  stepContent: { flex: 1, paddingBottom: '1rem' },
  stepHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', cursor: 'pointer', marginBottom: '0.5rem' },
  stepTitle: { fontSize: '1rem', fontWeight: 600, color: '#1e293b', marginBottom: '0.25rem' },
  stepTitleDisabled: { color: '#94a3b8' },
  stepDescription: { fontSize: '0.875rem', color: '#64748b', lineHeight: 1.5 },
  chevron: { color: '#64748b', transition: 'transform 0.2s ease' },
  formGroup: { marginBottom: '1rem' },
  label: { display: 'block', fontSize: '0.75rem', color: '#0066CC', marginBottom: '0.25rem', fontWeight: 500 },
  labelRequired: { color: '#dc2626' },
  input: { width: '100%', padding: '0.75rem', fontSize: '0.875rem', border: 'none', borderBottom: '2px solid #0066CC', backgroundColor: 'transparent', outline: 'none', transition: 'border-color 0.2s ease' },
  inputError: { borderBottomColor: '#dc2626' },
  inputDisabled: { borderBottomColor: '#e5e7eb', backgroundColor: '#f8fafc', cursor: 'not-allowed' },
  select: { width: '100%', padding: '0.75rem', fontSize: '0.875rem', border: 'none', borderBottom: '2px solid #0066CC', backgroundColor: 'transparent', outline: 'none', cursor: 'pointer', appearance: 'none' as const },
  selectDisabled: { borderBottomColor: '#e5e7eb', backgroundColor: '#f8fafc', cursor: 'not-allowed', color: '#94a3b8' },
  selectWrapper: { position: 'relative' as const },
  selectArrow: { position: 'absolute' as const, right: '0.5rem', top: '50%', transform: 'translateY(-50%)', pointerEvents: 'none' as const, color: '#64748b' },
  fieldError: { fontSize: '0.75rem', color: '#dc2626', marginTop: '0.25rem' },
  primaryBtn: { display: 'inline-flex', alignItems: 'center', gap: '0.5rem', padding: '0.625rem 1.25rem', fontSize: '0.875rem', fontWeight: 500, color: '#ffffff', backgroundColor: '#0066CC', border: '1px solid #0066CC', borderRadius: '0.375rem', cursor: 'pointer', transition: 'all 0.2s ease' },
  ghostBtn: { display: 'inline-flex', alignItems: 'center', gap: '0.5rem', padding: '0.625rem 1.25rem', fontSize: '0.875rem', fontWeight: 500, color: '#64748b', backgroundColor: 'transparent', border: 'none', cursor: 'pointer', textDecoration: 'underline' },
  dangerBtn: { display: 'inline-flex', alignItems: 'center', gap: '0.5rem', padding: '0.625rem 1.25rem', fontSize: '0.875rem', fontWeight: 500, color: '#ffffff', backgroundColor: '#dc2626', border: '1px solid #dc2626', borderRadius: '0.375rem', cursor: 'pointer' },
  footer: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '2rem', paddingTop: '1.5rem', borderTop: '1px solid #e5e7eb' },
  buttonsRight: { display: 'flex', gap: '0.75rem' },
  modal: { position: 'fixed' as const, top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0, 0, 0, 0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000 },
  modalContent: { backgroundColor: '#ffffff', borderRadius: '0.75rem', padding: '1.5rem', maxWidth: '480px', width: '90%', boxShadow: '0 10px 25px rgba(0, 0, 0, 0.2)' },
  modalTitle: { fontSize: '1.125rem', fontWeight: 600, marginBottom: '1rem', color: '#1e293b' },
  modalText: { fontSize: '0.875rem', color: '#64748b', marginBottom: '1.5rem', lineHeight: 1.5 },
  modalButtons: { display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' },
  toast: { position: 'fixed' as const, bottom: '24px', right: '24px', padding: '14px 20px', borderRadius: '8px', zIndex: 1001, display: 'flex', alignItems: 'center', gap: '10px', fontSize: '14px', fontWeight: 500, boxShadow: '0 4px 12px rgba(0,0,0,0.15)' },
};

const ChevronIcon = ({ expanded }: { expanded: boolean }) => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"
    style={{ transform: expanded ? 'rotate(180deg)' : 'rotate(0deg)', transition: 'transform 0.2s ease' }}>
    <path d="M6 9l6 6 6-6" />
  </svg>
);

const buildUpdateSuccessMessage = (
  elementName: string,
  relationChangeType: 'add' | 'change' | 'delete' | 'none',
  externalKeyAdded: boolean,
  externalKeyModified: boolean,
  externalKeyRemoved: boolean,
): string => {
  if (relationChangeType === 'add') return `El elemento '${elementName}' se ha actualizado y relacionado exitosamente.`;
  if (relationChangeType === 'change') return `La relación del elemento '${elementName}' se ha actualizado exitosamente.`;
  if (relationChangeType === 'delete') return `La relación del elemento '${elementName}' se ha eliminado exitosamente.`;
  if (externalKeyAdded) return `El elemento '${elementName}' se ha actualizado y ahora tiene valor de conversión.`;
  if (externalKeyModified) return `El elemento '${elementName}' y su valor de conversión se han actualizado exitosamente.`;
  if (externalKeyRemoved) return `El elemento '${elementName}' se ha actualizado. El valor de conversión ha sido eliminado.`;
  return `El elemento '${elementName}' se ha actualizado exitosamente.`;
};

const buildCreateSuccessMessage = (
  elementName: string,
  hasRelation: boolean,
  hasExternalKey: boolean,
): string => {
  if (hasExternalKey) return `El elemento '${elementName}' se ha creado exitosamente con su valor de conversión.`;
  if (hasRelation) return `El elemento '${elementName}' se ha creado y relacionado exitosamente.`;
  return `El elemento '${elementName}' se ha creado exitosamente.`;
};

const areFormsEqual = (a: FormData, b: FormData): boolean =>
  a.elementName === b.elementName &&
  a.startDate === b.startDate &&
  a.endDate === b.endDate &&
  a.value === b.value &&
  a.externalKey === b.externalKey &&
  a.parentCatalogId === b.parentCatalogId &&
  a.parentElementId === b.parentElementId;

const EMPTY_ELEMENT_FORM: FormData = {
  elementName: '',
  startDate: '',
  endDate: '',
  value: '',
  externalKey: '',
  parentCatalogId: '',
  parentElementId: '',
};

const mapElementToFormData = (element: any): FormData => ({
  elementName: element.element || '',
  startDate: element.validFrom || '',
  endDate: element.validTo || '',
  value: element.value || '',
  externalKey: element.externalKey || '',
  parentCatalogId: element.parentCatalogId ? String(element.parentCatalogId) : '',
  parentElementId: element.parentElementId ? String(element.parentElementId) : '',
});

interface ElementStep1Props {
  expanded: boolean;
  isEditMode: boolean;
  formData: FormData;
  errors: FormErrors;
  elementStatus: number;
  onToggle: () => void;
  onInputChange: (field: keyof FormData, value: string) => void;
  onStatusChange: (value: number) => void;
}

function ElementStep1({ expanded, isEditMode, formData, errors, elementStatus, onToggle, onInputChange, onStatusChange }: Readonly<ElementStep1Props>) {
  return (
    <div style={styles.stepContainer}>
      <div style={styles.stepIndicator}>
        <div style={styles.stepNumber}>1</div>
        <div style={styles.stepLine} />
      </div>
      <div style={styles.stepContent}>
        <button type="button" style={{ ...styles.stepHeader, border: 'none', background: 'transparent', appearance: 'none', padding: 0 }} onClick={onToggle}>
          <div>
            <div style={styles.stepTitle}>Datos de Elemento</div>
            <div style={styles.stepDescription}>{isEditMode ? 'Modifica la información general del elemento.' : 'Captura la información general del nuevo elemento.'}</div>
          </div>
          <span style={styles.chevron}><ChevronIcon expanded={expanded} /></span>
        </button>
        {expanded && (
          <div style={{ marginTop: '1rem' }}>
            <div style={styles.formGroup}>
              <label htmlFor="element-name" style={styles.label}><span style={styles.labelRequired}>*</span>Nombre del elemento</label>
              <input id="element-name" type="text" style={{ ...styles.input, ...(errors.elementName ? styles.inputError : {}) }}
                value={formData.elementName} onChange={e => onInputChange('elementName', e.target.value)} placeholder="Nombre del elemento" maxLength={100} />
              {errors.elementName && <div style={styles.fieldError}>{errors.elementName}</div>}
            </div>
            <div style={styles.formGroup}>
              <label htmlFor="element-start-date" style={styles.label}><span style={styles.labelRequired}>*</span>Fecha de inicio vigencia</label>
              <input id="element-start-date" type="date" style={{ ...styles.input, ...(errors.startDate ? styles.inputError : {}) }}
                value={formData.startDate} onChange={e => onInputChange('startDate', e.target.value)} />
              {errors.startDate && <div style={styles.fieldError}>{errors.startDate}</div>}
            </div>
            <div style={styles.formGroup}>
              <label htmlFor="element-end-date" style={styles.label}>Fecha de fin vigencia</label>
              <input id="element-end-date" type="date" style={{ ...styles.input, ...(errors.endDate ? styles.inputError : {}) }}
                value={formData.endDate} onChange={e => onInputChange('endDate', e.target.value)} />
              {errors.endDate && <div style={styles.fieldError}>{errors.endDate}</div>}
            </div>
            {isEditMode && (
              <div style={styles.formGroup}>
                <label htmlFor="element-status" style={styles.label}><span style={styles.labelRequired}>*</span>Estatus de elemento</label>
                <div style={styles.selectWrapper}>
                  <select id="element-status" style={styles.select} value={elementStatus} onChange={e => onStatusChange(Number(e.target.value))}>
                    <option value={1}>Activado</option>
                    <option value={0}>Desactivado</option>
                  </select>
                  <span style={styles.selectArrow}>▼</span>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

interface ElementStep2Props {
  expanded: boolean;
  isStep1Complete: boolean;
  formData: FormData;
  errors: FormErrors;
  onToggle: () => void;
  onInputChange: (field: keyof FormData, value: string) => void;
}

function ElementStep2({ expanded, isStep1Complete, formData, errors, onToggle, onInputChange }: Readonly<ElementStep2Props>) {
  return (
    <div style={styles.stepContainer}>
      <div style={styles.stepIndicator}>
        <div style={{ ...styles.stepNumber, ...(isStep1Complete ? {} : styles.stepNumberDisabled) }}>2</div>
        <div style={{ ...styles.stepLine, ...(isStep1Complete ? {} : styles.stepLineDisabled) }} />
      </div>
      <div style={styles.stepContent}>
        <button type="button" style={{ ...styles.stepHeader, cursor: isStep1Complete ? 'pointer' : 'not-allowed', border: 'none', background: 'transparent', appearance: 'none', padding: 0 }} onClick={onToggle}>
          <div>
            <div style={{ ...styles.stepTitle, ...(isStep1Complete ? {} : styles.stepTitleDisabled) }}>Valores del Elemento (Opcional)</div>
            <div style={styles.stepDescription}>Captura el valor por defecto del nuevo elemento.</div>
          </div>
          <span style={{ ...styles.chevron, opacity: isStep1Complete ? 1 : 0.5 }}><ChevronIcon expanded={expanded} /></span>
        </button>
        {expanded && isStep1Complete && (
          <div style={{ marginTop: '1rem' }}>
            <div style={styles.formGroup}>
              <label htmlFor="element-value" style={styles.label}>Valor del elemento</label>
              <input id="element-value" type="text" style={{ ...styles.input, ...(errors.value ? styles.inputError : {}) }}
                value={formData.value} onChange={e => onInputChange('value', e.target.value)} placeholder="Valor del elemento" maxLength={100} />
              {errors.value && <div style={styles.fieldError}>{errors.value}</div>}
            </div>
            <div style={styles.formGroup}>
              <label htmlFor="element-external-key" style={styles.label}>
                Valor de Conversión (Opcional)
                {' '}
                <span
                  title="Valor utilizado por sistemas externos para identificar este elemento. Ejemplo: Si el elemento es 'México', el valor de conversión podría ser 'MX' (código ISO)."
                  style={{ marginLeft: '6px', cursor: 'help', color: '#64748b', fontSize: '0.8rem' }}
                >ⓘ</span>
              </label>
              <input id="element-external-key" type="text" style={{ ...styles.input, ...(errors.externalKey ? styles.inputError : {}) }}
                value={formData.externalKey} onChange={e => onInputChange('externalKey', e.target.value)}
                placeholder="Ingrese el valor de conversión para otros sistemas" maxLength={50} />
              {errors.externalKey && <div style={styles.fieldError}>{errors.externalKey}</div>}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

interface ElementStep3Props {
  expanded: boolean;
  isStep1Complete: boolean;
  isPrimaryCatalog: boolean;
  formData: FormData;
  errors: FormErrors;
  parentCatalogs: CatalogSimple[];
  parentElements: CatalogElement[];
  onToggle: () => void;
  onInputChange: (field: keyof FormData, value: string) => void;
}

function ElementStep3({ expanded, isStep1Complete, isPrimaryCatalog, formData, errors, parentCatalogs, parentElements, onToggle, onInputChange }: Readonly<ElementStep3Props>) {
  return (
    <div style={styles.stepContainer}>
      <div style={styles.stepIndicator}>
        <div style={{ ...styles.stepNumber, ...(isStep1Complete ? {} : styles.stepNumberDisabled) }}>3</div>
      </div>
      <div style={styles.stepContent}>
        <button type="button" style={{ ...styles.stepHeader, cursor: isStep1Complete ? 'pointer' : 'not-allowed', border: 'none', background: 'transparent', appearance: 'none', padding: 0 }} onClick={onToggle}>
          <div>
            <div style={{ ...styles.stepTitle, ...(isStep1Complete ? {} : styles.stepTitleDisabled) }}>Catálogo y Elemento Padre (Opcional)</div>
            <div style={styles.stepDescription}>Selecciona y relaciona el elemento con un catálogo y elemento padre para crear una agrupación.</div>
          </div>
          <span style={{ ...styles.chevron, opacity: isStep1Complete ? 1 : 0.5 }}><ChevronIcon expanded={expanded} /></span>
        </button>
        {expanded && isStep1Complete && (
          <div style={{ marginTop: '1rem' }}>
            <div style={styles.formGroup}>
              <label htmlFor="element-parent-catalog" style={styles.label}>Seleccionar catálogo padre</label>
              <div style={styles.selectWrapper}>
                <select id="element-parent-catalog" style={{ ...styles.select, ...(isPrimaryCatalog ? styles.selectDisabled : {}) }}
                  value={formData.parentCatalogId} onChange={e => onInputChange('parentCatalogId', e.target.value)} disabled={isPrimaryCatalog}>
                  <option value="">Seleccionar catálogo padre</option>
                  {parentCatalogs.map(pc => <option key={pc.id} value={pc.id}>{pc.name}</option>)}
                </select>
                <span style={styles.selectArrow}>▼</span>
              </div>
              {isPrimaryCatalog && <div style={{ fontSize: '0.75rem', color: '#64748b', marginTop: '0.25rem' }}>No disponible para catálogos de tipo Primario.</div>}
            </div>
            <div style={styles.formGroup}>
              <label htmlFor="element-parent-element" style={styles.label}>Seleccionar elemento padre</label>
              <div style={styles.selectWrapper}>
                <select id="element-parent-element" style={{ ...styles.select, ...(isPrimaryCatalog || !formData.parentCatalogId ? styles.selectDisabled : {}), ...(errors.parentElementId ? styles.inputError : {}) }}
                  value={formData.parentElementId} onChange={e => onInputChange('parentElementId', e.target.value)} disabled={isPrimaryCatalog || !formData.parentCatalogId}>
                  <option value="">Seleccionar elemento padre</option>
                  {parentElements.map(pe => <option key={pe.id} value={pe.id}>{pe.element || pe.value}</option>)}
                </select>
                <span style={styles.selectArrow}>▼</span>
              </div>
              {errors.parentElementId && <div style={styles.fieldError}>{errors.parentElementId}</div>}
              {!isPrimaryCatalog && !formData.parentCatalogId && <div style={{ fontSize: '0.75rem', color: '#64748b', marginTop: '0.25rem' }}>Primero seleccione un catálogo padre.</div>}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

interface ElementExitModalProps {
  isEditMode: boolean;
  onNo: () => void;
  onYes: () => void;
}

function ElementExitModal({ isEditMode, onNo, onYes }: Readonly<ElementExitModalProps>) {
  return (
    <div style={styles.modal}>
      <div style={styles.modalContent}>
        <h3 style={styles.modalTitle}>¿Desea salir?</h3>
        <p style={styles.modalText}>{isEditMode ? 'La información editada del elemento se perderá. ¿Desea continuar?' : 'La información ingresada para registrar el elemento se perderá. ¿Desea continuar?'}</p>
        <div style={styles.modalButtons}>
          <button style={{ ...styles.ghostBtn, border: '1px solid #e5e7eb', borderRadius: '0.5rem', textDecoration: 'none' }} onClick={onNo}>No</button>
          <button style={styles.primaryBtn} onClick={onYes}>Sí</button>
        </div>
      </div>
    </div>
  );
}

interface ChangeRelationModalProps {
  originalParentCatalogName: string;
  originalParentElementName: string;
  newParentCatalogName: string;
  newParentElementName: string;
  onCancel: () => void;
  onConfirm: () => void;
}

function ChangeRelationModal({ originalParentCatalogName, originalParentElementName, newParentCatalogName, newParentElementName, onCancel, onConfirm }: Readonly<ChangeRelationModalProps>) {
  return (
    <div style={styles.modal}>
      <div style={styles.modalContent}>
        <h3 style={styles.modalTitle}>Cambiar relación del elemento</h3>
        <p style={styles.modalText}>
          Vas a cambiar la relación de este elemento:<br /><br />
          <strong>De:</strong> {originalParentCatalogName} → {originalParentElementName}<br />
          <strong>A:</strong> {newParentCatalogName} → {newParentElementName}<br /><br />
          ¿Deseas continuar?
        </p>
        <div style={styles.modalButtons}>
          <button style={{ ...styles.ghostBtn, border: '1px solid #e5e7eb', borderRadius: '0.5rem', textDecoration: 'none' }} onClick={onCancel}>Cancelar</button>
          <button style={styles.primaryBtn} onClick={onConfirm}>Confirmar Cambio</button>
        </div>
      </div>
    </div>
  );
}

interface DeleteRelationModalProps {
  elementName: string;
  originalParentCatalogName: string;
  originalParentElementName: string;
  onCancel: () => void;
  onConfirm: () => void;
}

function DeleteRelationModal({ elementName, originalParentCatalogName, originalParentElementName, onCancel, onConfirm }: Readonly<DeleteRelationModalProps>) {
  return (
    <div style={styles.modal}>
      <div style={styles.modalContent}>
        <h3 style={styles.modalTitle}>Eliminar relación del elemento</h3>
        <p style={styles.modalText}>
          Vas a eliminar la relación del elemento '{elementName}' con:<br /><br />
          <strong>Catálogo Padre:</strong> {originalParentCatalogName}<br />
          <strong>Elemento Padre:</strong> {originalParentElementName}<br /><br />
          ¿Deseas continuar?
        </p>
        <div style={styles.modalButtons}>
          <button style={{ ...styles.ghostBtn, border: '1px solid #e5e7eb', borderRadius: '0.5rem', textDecoration: 'none' }} onClick={onCancel}>Cancelar</button>
          <button style={styles.dangerBtn} onClick={onConfirm}>Confirmar Eliminación</button>
        </div>
      </div>
    </div>
  );
}

export default function ElementForm() {
  const navigate = useNavigate();
  const { id, elementId } = useParams<{ id: string; elementId?: string }>();
  const catalogId = Number(id);
  const isEditMode = !!elementId;

  const [catalogType, setCatalogType] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [expandedSteps, setExpandedSteps] = useState<number[]>([1]);

  const [formData, setFormData] = useState<FormData>({ ...EMPTY_ELEMENT_FORM });
  const [originalData, setOriginalData] = useState<FormData>({ ...EMPTY_ELEMENT_FORM });
  const [originalParentCatalogName, setOriginalParentCatalogName] = useState('');
  const [originalParentElementName, setOriginalParentElementName] = useState('');

  const [originalStatus, setOriginalStatus] = useState(1);
  const [errors, setErrors] = useState<FormErrors>({});
  const [elementStatus, setElementStatus] = useState(1);

  const [parentCatalogs, setParentCatalogs] = useState<CatalogSimple[]>([]);
  const [parentElements, setParentElements] = useState<CatalogElement[]>([]);

  const [showExitModal, setShowExitModal] = useState(false);
  const [showChangeRelationModal, setShowChangeRelationModal] = useState(false);
  const [showDeleteRelationModal, setShowDeleteRelationModal] = useState(false);
  const [pendingSaveAction, setPendingSaveAction] = useState<(() => void) | null>(null);

  const { showSuccess, showError, ModalNode } = useModalNotification();

  const isStep1Complete = formData.elementName.trim() !== '' && formData.startDate !== '';
  const isPrimaryCatalog = catalogType === 'PRIMARIO' || catalogType === 'HIERARCHICAL';

  const hasChanges = () => !areFormsEqual(formData, originalData);

  const showToast = useCallback(
    (type: 'success' | 'error', text: string, onAccept?: () => void) => {
      if (type === 'success') {
        showSuccess(text, 'Operación exitosa', onAccept);
      } else {
        showError(text, 'Error', onAccept);
      }
    },
    [showSuccess, showError],
  );

  useEffect(() => {
    const loadEditingElement = async () => {
      const element = await catalogElementService.getById(catalogId, Number(elementId));
      const data = mapElementToFormData(element);
      setFormData(data);
      setOriginalData(data);
      setElementStatus(element.status);
      setOriginalStatus(element.status);
      setOriginalParentCatalogName(element.parentCatalogName ?? '');
      setOriginalParentElementName(element.parentElementName ?? '');
      if (element.parentCatalogId) {
        const elems = await catalogElementService.getActiveElements(element.parentCatalogId);
        setParentElements(elems);
      }
      setExpandedSteps([1, 2, 3]);
    };

    const loadData = async () => {
      setIsLoading(true);
      try {
        const catalogDetail = await catalogElementService.getCatalogDetail(catalogId);
        setCatalogType(catalogDetail.catalogType ?? '');
        const primaries = await catalogService.getPrimaryCatalogs();
        setParentCatalogs(primaries);
        if (isEditMode && elementId) {
          await loadEditingElement();
        }
      } catch (err) {
        console.error('Error loading data:', err);
      } finally {
        setIsLoading(false);
      }
    };
    loadData();
  }, [catalogId, isEditMode, elementId]);

  useEffect(() => {
    if (formData.parentCatalogId) {
      catalogElementService.getActiveElements(Number(formData.parentCatalogId))
        .then(setParentElements)
        .catch(() => setParentElements([]));
      if (formData.parentCatalogId !== originalData.parentCatalogId) {
        setFormData(prev => ({ ...prev, parentElementId: '' }));
      }
    } else {
      setParentElements([]);
      if (formData.parentElementId) {
        setFormData(prev => ({ ...prev, parentElementId: '' }));
      }
    }
  }, [formData.parentCatalogId]);

  const toggleStep = (step: number) => {
    if (step === 1 || isStep1Complete) {
      setExpandedSteps(prev => prev.includes(step) ? prev.filter(s => s !== step) : [...prev, step]);
    }
  };

  const handleInputChange = (field: keyof FormData, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    if (errors[field as keyof FormErrors]) {
      setErrors(prev => ({ ...prev, [field]: undefined }));
    }
  };

  const validateForm = (): boolean => {
    const newErrors: FormErrors = {};
    if (!formData.elementName.trim()) newErrors.elementName = 'Debe ingresar el nombre del elemento.';
    else if (INVALID_CHARS.test(formData.elementName)) newErrors.elementName = 'El nombre contiene caracteres no permitidos.';
    if (!formData.startDate) newErrors.startDate = 'Debe ingresar la fecha de inicio de vigencia.';
    if (formData.endDate) {
      const startDate = new Date(formData.startDate);
      const endDate = new Date(formData.endDate);
      const today = new Date(); today.setHours(0, 0, 0, 0);
      if (endDate <= today) newErrors.endDate = 'La fecha de fin debe ser mayor a la fecha actual.';
      else if (endDate <= startDate) newErrors.endDate = 'La fecha de fin debe ser mayor a la fecha de inicio.';
    }
    if (formData.value && INVALID_CHARS.test(formData.value)) newErrors.value = 'El valor contiene caracteres no permitidos.';
    if (formData.externalKey) {
      if (formData.externalKey.length > 50) newErrors.externalKey = 'El valor de conversión no puede exceder 50 caracteres.';
      else if (!EXTERNAL_KEY_REGEX.test(formData.externalKey)) newErrors.externalKey = 'El valor de conversión solo puede contener letras, números, guiones, guiones bajos y puntos';
    }
    if (!isPrimaryCatalog && formData.parentCatalogId && !formData.parentElementId) {
      newErrors.parentElementId = 'Debe seleccionar un elemento padre cuando se ha elegido un catálogo padre.';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const getNewParentCatalogName = (): string => {
    const found = parentCatalogs.find(c => String(c.id) === formData.parentCatalogId);
    return found?.name || '';
  };

  const getNewParentElementName = (): string => {
    const found = parentElements.find(e => String(e.id) === formData.parentElementId);
    return found?.element || found?.value || '';
  };

  const detectRelationChange = (): 'none' | 'change' | 'delete' | 'add' => {
    const hadRelation = originalData.parentCatalogId !== '' && originalData.parentElementId !== '';
    const hasRelation = formData.parentCatalogId !== '' && formData.parentElementId !== '';
    const relationChanged = formData.parentCatalogId !== originalData.parentCatalogId ||
      formData.parentElementId !== originalData.parentElementId;

    if (!relationChanged) return 'none';
    if (hadRelation && hasRelation) return 'change';
    if (hadRelation && !hasRelation) return 'delete';
    if (!hadRelation && hasRelation) return 'add';
    return 'none';
  };

  const runUpdate = async () => {
    const updateData: CatalogElementUpdateDto = {
      element: formData.elementName,
      value: formData.value || undefined,
      externalKey: formData.externalKey || '',
      validFrom: formData.startDate,
      validTo: formData.endDate || undefined,
      parentCatalogId: formData.parentCatalogId ? Number(formData.parentCatalogId) : undefined,
      parentElementId: formData.parentElementId ? Number(formData.parentElementId) : undefined,
      status: elementStatus,
    };
    if (!formData.parentCatalogId) {
      updateData.parentCatalogId = null as any;
      updateData.parentElementId = null as any;
    }
    await catalogElementService.update(catalogId, Number(elementId), updateData);

    const relationChangeType = detectRelationChange();
    const externalKeyAdded = !originalData.externalKey && !!formData.externalKey;
    const externalKeyModified =
      !!originalData.externalKey && !!formData.externalKey &&
      originalData.externalKey !== formData.externalKey;
    const externalKeyRemoved = !!originalData.externalKey && !formData.externalKey;
    const msg = buildUpdateSuccessMessage(
      formData.elementName,
      relationChangeType,
      externalKeyAdded,
      externalKeyModified,
      externalKeyRemoved,
    );
    showToast('success', msg, () => navigate(`/util/catalogos/catalogs/${id}/elementos`));
  };

  const runCreate = async () => {
    const createData: CatalogElementCreateDto = {
      element: formData.elementName,
      value: formData.value || undefined,
      externalKey: formData.externalKey || undefined,
      validFrom: formData.startDate,
      validTo: formData.endDate || undefined,
      parentCatalogId: formData.parentCatalogId ? Number(formData.parentCatalogId) : undefined,
      parentElementId: formData.parentElementId ? Number(formData.parentElementId) : undefined,
    };
    await catalogElementService.create(catalogId, createData);

    const hasRelation = !!(formData.parentCatalogId && formData.parentElementId);
    const hasExternalKey = !!formData.externalKey;
    const msg = buildCreateSuccessMessage(formData.elementName, hasRelation, hasExternalKey);
    showToast('success', msg, () => navigate(`/util/catalogos/catalogs/${id}/elementos`));
  };

  const executeSave = async () => {
    try {
      if (isEditMode && elementId) {
        await runUpdate();
      } else {
        await runCreate();
      }
    } catch (error: any) {
      const backendMsg = error?.response?.data?.message || error?.response?.data?.error || 'Ocurrió un error al guardar. Intente nuevamente.';
      showToast('error', backendMsg);
    } finally {
      setIsSaving(false);
    }
  };

  const handleSave = async () => {
    if (!validateForm()) return;
    setIsSaving(true);

    if (isEditMode) {
      const changeType = detectRelationChange();
      if (changeType === 'change') {
        setPendingSaveAction(() => executeSave);
        setShowChangeRelationModal(true);
        setIsSaving(false);
        return;
      }
      if (changeType === 'delete') {
        setPendingSaveAction(() => executeSave);
        setShowDeleteRelationModal(true);
        setIsSaving(false);
        return;
      }
    }

    await executeSave();
  };

  const confirmRelationChange = async () => {
    setShowChangeRelationModal(false);
    setShowDeleteRelationModal(false);
    setIsSaving(true);
    if (pendingSaveAction) await pendingSaveAction();
    setPendingSaveAction(null);
  };

  const cancelRelationChange = () => {
    setShowChangeRelationModal(false);
    setShowDeleteRelationModal(false);
    setPendingSaveAction(null);
    setIsSaving(false);
  };

  const handleClear = () => {
    if (isEditMode) {
      setFormData({ ...originalData });
      setElementStatus(originalStatus);
      if (originalData.parentCatalogId) {
        catalogElementService.getActiveElements(Number(originalData.parentCatalogId))
          .then(setParentElements)
          .catch(() => setParentElements([]));
      } else {
        setParentElements([]);
      }
    } else {
      setFormData({ elementName: '', startDate: '', endDate: '', value: '', externalKey: '', parentCatalogId: '', parentElementId: '' });
      setExpandedSteps([1]);
      setParentElements([]);
    }
    setErrors({});
  };

  const handleBack = () => {
    const hasUnsavedData = isEditMode ? hasChanges() :
      formData.elementName || formData.startDate || formData.endDate || formData.value || formData.externalKey || formData.parentCatalogId || formData.parentElementId;
    if (hasUnsavedData) setShowExitModal(true);
    else navigate(`/util/catalogos/catalogs/${id}/elementos`);
  };

  if (isLoading) return <div style={styles.container}><div style={{ padding: '3rem', textAlign: 'center' }}><p style={{ color: '#64748b' }}>Cargando...</p></div></div>;

  return (
    <div style={styles.container}>
      <Breadcrumb
        items={withFinanceBreadcrumb([
          { label: 'Gestión de Catálogos', to: '/util/catalogos' },
          { label: 'Catálogos', to: '/util/catalogos/catalogs' },
          { label: 'Elementos', to: `/util/catalogos/catalogs/${id}/elementos` },
          { label: isEditMode ? 'Editar Elemento' : 'Nuevo Elemento' },
        ])}
      />

      <div style={styles.card}>
        <h1 style={styles.title}>{isEditMode ? 'Editar Elemento' : 'Nuevo Elemento'}</h1>
        <p style={styles.subtitle}>
          {isEditMode
            ? 'Para editar el elemento actualiza el formulario, asegurándote de proporcionar información precisa y detallada.'
            : 'Para crear un nuevo elemento, sigue las instrucciones y completa el formulario, asegurándote de proporcionar información precisa y detallada.'}
        </p>

        <ElementStep1
          expanded={expandedSteps.includes(1)}
          isEditMode={isEditMode}
          formData={formData}
          errors={errors}
          elementStatus={elementStatus}
          onToggle={() => toggleStep(1)}
          onInputChange={handleInputChange}
          onStatusChange={setElementStatus}
        />

        <ElementStep2
          expanded={expandedSteps.includes(2)}
          isStep1Complete={isStep1Complete}
          formData={formData}
          errors={errors}
          onToggle={() => toggleStep(2)}
          onInputChange={handleInputChange}
        />

        <ElementStep3
          expanded={expandedSteps.includes(3)}
          isStep1Complete={isStep1Complete}
          isPrimaryCatalog={isPrimaryCatalog}
          formData={formData}
          errors={errors}
          parentCatalogs={parentCatalogs}
          parentElements={parentElements}
          onToggle={() => toggleStep(3)}
          onInputChange={handleInputChange}
        />

        <div style={styles.footer}>
          <button type="button" style={{ ...styles.ghostBtn, appearance: 'none' }} onClick={handleBack}>Volver</button>
          <div style={styles.buttonsRight}>
            <button type="button" style={{ ...styles.ghostBtn, appearance: 'none' }} onClick={handleClear}>Limpiar</button>
            <button style={{ ...styles.primaryBtn, opacity: isSaving ? 0.6 : 1, cursor: isSaving ? 'not-allowed' : 'pointer' }}
              onClick={handleSave} disabled={isSaving}>
              {isSaving ? 'Guardando...' : 'Guardar Elemento'}
            </button>
          </div>
        </div>
      </div>

      {showExitModal && (
        <ElementExitModal
          isEditMode={isEditMode}
          onNo={() => setShowExitModal(false)}
          onYes={() => { setShowExitModal(false); navigate(`/util/catalogos/catalogs/${id}/elementos`); }}
        />
      )}

      {showChangeRelationModal && (
        <ChangeRelationModal
          originalParentCatalogName={originalParentCatalogName}
          originalParentElementName={originalParentElementName}
          newParentCatalogName={getNewParentCatalogName()}
          newParentElementName={getNewParentElementName()}
          onCancel={cancelRelationChange}
          onConfirm={confirmRelationChange}
        />
      )}

      {showDeleteRelationModal && (
        <DeleteRelationModal
          elementName={formData.elementName}
          originalParentCatalogName={originalParentCatalogName}
          originalParentElementName={originalParentElementName}
          onCancel={cancelRelationChange}
          onConfirm={confirmRelationChange}
        />
      )}

      {/* STM-15xx: el toast inferior derecha fue reemplazado por el modal
          compartido (mismo estilo del resto del módulo). */}
      {ModalNode}
    </div>
  );
}
