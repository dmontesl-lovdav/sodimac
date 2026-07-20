import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  supplierBlockService,
  supplierService,
  Supplier,
  SupplierBlockCreateDto,
  SupplierBlockUpdateDto,
} from '@features/catalogos/services/catalogosApi';
import { useModalNotification } from '@shared/components/ui/modal';
import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';
import { withFinanceBreadcrumb } from '@shared/components/ui/navigation/financeBreadcrumb';

const styles = {
  container: {
    padding: '1.5rem 2rem',
    backgroundColor: '#ffffff',
    minHeight: '100vh',
    fontFamily: 'inherit',
    color: '#1f2937',
  } as const,
  breadcrumb: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.5rem',
    fontSize: '0.875rem',
    color: '#6b7280',
    marginBottom: '1rem',
  } as const,
  breadcrumbLink: {
    color: '#003865',
    textDecoration: 'none',
    cursor: 'pointer',
  } as const,
  pageHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '1.25rem',
    flexWrap: 'wrap' as const,
    gap: '1rem',
  },
  pageTitle: {
    fontSize: '1.5rem',
    fontWeight: 600,
    color: '#1f2937',
    margin: 0,
  },
  card: {
    backgroundColor: '#ffffff',
    border: '1px solid #e5e7eb',
    borderRadius: '0.5rem',
    padding: '1.5rem',
    boxShadow: '0 1px 2px rgba(0,0,0,0.04)',
  },
  grid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
    gap: '1rem',
  } as const,
  formGroup: {
    display: 'flex',
    flexDirection: 'column' as const,
    gap: '0.4rem',
  },
  formLabel: {
    fontSize: '0.8125rem',
    fontWeight: 500,
    color: '#374151',
  },
  formInput: {
    padding: '0.5rem 0.75rem',
    border: '1px solid #d1d5db',
    borderRadius: '0.375rem',
    fontSize: '0.875rem',
    color: '#1f2937',
    backgroundColor: '#ffffff',
    outline: 'none',
    fontFamily: 'inherit',
  },
  actions: {
    display: 'flex',
    justifyContent: 'flex-end',
    gap: '0.75rem',
    marginTop: '1.5rem',
    paddingTop: '1rem',
    borderTop: '1px solid #e5e7eb',
  },
  btnPrimary: {
    padding: '0.55rem 1.5rem',
    backgroundColor: '#002d4c',
    color: '#ffffff',
    border: '1px solid #002d4c',
    borderRadius: '0.375rem',
    fontSize: '0.875rem',
    fontWeight: 500,
    cursor: 'pointer',
  },
  btnSecondary: {
    padding: '0.55rem 1.5rem',
    backgroundColor: '#ffffff',
    color: '#1f2937',
    border: '1px solid #d1d5db',
    borderRadius: '0.375rem',
    fontSize: '0.875rem',
    fontWeight: 500,
    cursor: 'pointer',
  },
  error: {
    backgroundColor: '#fee2e2',
    color: '#991b1b',
    border: '1px solid #fecaca',
    borderRadius: '0.375rem',
    padding: '0.75rem 1rem',
    marginBottom: '1rem',
    fontSize: '0.875rem',
  },
  loading: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    padding: '3rem 1rem',
  },
  spinner: {
    width: '2.25rem',
    height: '2.25rem',
    border: '3px solid #e5e7eb',
    borderTopColor: '#002d4c',
    borderRadius: '50%',
    animation: 'somx-spin 0.8s linear infinite',
  },
  helperText: {
    fontSize: '0.75rem',
    color: '#6b7280',
  },
};

const getSubmitLabel = (saving: boolean, isEditing: boolean): string => {
  if (saving) return 'Guardando...';
  return isEditing ? 'Actualizar' : 'Crear Bloqueo';
};

const SupplierBlockForm = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = Boolean(id);
  const { showSuccess, showError, ModalNode } = useModalNotification();

  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [supplierInfo, setSupplierInfo] = useState<Supplier | null>(null);
  const [supplierSearching, setSupplierSearching] = useState(false);

  const [formData, setFormData] = useState({
    supplierNumber: '',
    validFrom: '',
    validTo: '',
    blockReason: '',
    status: '1',
  });

  useEffect(() => {
    if (isEditing && id) {
      loadBlock(parseInt(id));
    }
  }, [id, isEditing]);

  const loadBlock = async (blockId: number) => {
    setLoading(true);
    try {
      const block = await supplierBlockService.getById(blockId);
      setFormData({
        supplierNumber: block.supplierNumber,
        validFrom: block.validFrom,
        validTo: block.validTo,
        blockReason: block.blockReason || '',
        status: block.status.toString(),
      });
      await searchSupplier(block.supplierNumber);
    } catch (err) {
      setError('Error al cargar el bloqueo');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const searchSupplier = async (supplierNumber: string) => {
    if (!supplierNumber) {
      setSupplierInfo(null);
      return;
    }
    setSupplierSearching(true);
    try {
      const supplier = await supplierService.getByNumber(supplierNumber);
      setSupplierInfo(supplier);
    } catch (err) {
      setSupplierInfo(null);
    } finally {
      setSupplierSearching(false);
    }
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>,
  ) => {
    const { name, value } = e.target;
    if (name === 'supplierNumber') {
      const onlyDigits = value.replace(/\D+/g, '');
      setFormData({ ...formData, [name]: onlyDigits });
      return;
    }
    setFormData({ ...formData, [name]: value });
  };

  const handleSupplierBlur = () => {
    if (formData.supplierNumber && !isEditing) {
      searchSupplier(formData.supplierNumber);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setError(null);

    try {
      if (isEditing && id) {
        const updateData: SupplierBlockUpdateDto = {
          validFrom: formData.validFrom,
          validTo: formData.validTo,
          blockReason: formData.blockReason || undefined,
          status: parseInt(formData.status),
        };
        await supplierBlockService.update(parseInt(id), updateData);
        showSuccess('Bloqueo actualizado correctamente.', 'Actualización exitosa', () =>
          navigate('/util/catalogos/bloqueos'),
        );
      } else {
        const createData: SupplierBlockCreateDto = {
          supplierNumber: formData.supplierNumber,
          validFrom: formData.validFrom,
          validTo: formData.validTo,
          blockReason: formData.blockReason || undefined,
        };
        await supplierBlockService.create(createData);
        showSuccess('Bloqueo registrado correctamente.', 'Creación exitosa', () =>
          navigate('/util/catalogos/bloqueos'),
        );
      }
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Error al guardar el bloqueo';
      setError(msg);
      showError(msg);
      console.error(err);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div style={styles.container}>
        <div style={styles.loading}>
          <div style={styles.spinner}></div>
        </div>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <Breadcrumb
        items={withFinanceBreadcrumb([
          { label: 'Gestión de Catálogos', to: '/util/catalogos' },
          { label: 'Bloqueos', to: '/util/catalogos/bloqueos' },
          { label: isEditing ? 'Editar' : 'Crear' },
        ])}
      />

      <div style={styles.pageHeader}>
        <h1 style={styles.pageTitle}>
          {isEditing ? 'Editar Bloqueo' : 'Nuevo Bloqueo de Proveedor'}
        </h1>
      </div>

      {error && <div style={styles.error}>{error}</div>}

      <div style={styles.card}>
        <form onSubmit={handleSubmit}>
          <div style={styles.grid}>
            <div style={styles.formGroup}>
              <label htmlFor="block-supplierNumber" style={styles.formLabel}>Número de Proveedor *</label>
              <input
                id="block-supplierNumber"
                type="text"
                inputMode="numeric"
                pattern="[0-9]*"
                name="supplierNumber"
                style={styles.formInput}
                value={formData.supplierNumber}
                onChange={handleChange}
                onBlur={handleSupplierBlur}
                required
                disabled={isEditing}
                placeholder="Ej: 100123"
              />
              {supplierSearching && <span style={styles.helperText}>Buscando...</span>}
            </div>

            <div style={styles.formGroup}>
              <label htmlFor="block-supplier-name" style={styles.formLabel}>Proveedor</label>
              <input
                id="block-supplier-name"
                type="text"
                style={{
                  ...styles.formInput,
                  background: supplierInfo ? '#dcfce7' : '#fee2e2',
                  color: supplierInfo ? '#166534' : '#991b1b',
                }}
                value={supplierInfo ? supplierInfo.businessName : 'No encontrado'}
                disabled
              />
            </div>

            {supplierInfo && (
              <>
                <div style={styles.formGroup}>
                  <label htmlFor="block-supplier-rfc" style={styles.formLabel}>RFC</label>
                  <input id="block-supplier-rfc" type="text" style={styles.formInput} value={supplierInfo.rfc} disabled />
                </div>
                <div style={styles.formGroup}>
                  <label htmlFor="block-supplier-type" style={styles.formLabel}>Tipo de Proveedor</label>
                  <input
                    id="block-supplier-type"
                    type="text"
                    style={styles.formInput}
                    value={supplierInfo.supplierType?.description || '-'}
                    disabled
                  />
                </div>
              </>
            )}

            <div style={styles.formGroup}>
              <label htmlFor="block-validFrom" style={styles.formLabel}>Fecha Inicio Vigencia *</label>
              <input
                id="block-validFrom"
                type="date"
                name="validFrom"
                style={styles.formInput}
                value={formData.validFrom}
                onChange={handleChange}
                required
              />
            </div>

            <div style={styles.formGroup}>
              <label htmlFor="block-validTo" style={styles.formLabel}>Fecha Fin Vigencia *</label>
              <input
                id="block-validTo"
                type="date"
                name="validTo"
                style={styles.formInput}
                value={formData.validTo}
                onChange={handleChange}
                required
              />
            </div>

            <div style={{ ...styles.formGroup, gridColumn: 'span 2' }}>
              <label htmlFor="block-blockReason" style={styles.formLabel}>Razón del Bloqueo</label>
              <textarea
                id="block-blockReason"
                name="blockReason"
                style={{ ...styles.formInput, resize: 'vertical' }}
                value={formData.blockReason}
                onChange={handleChange}
                rows={3}
                placeholder="Describa el motivo del bloqueo..."
              />
            </div>

            {isEditing && (
              <div style={styles.formGroup}>
                <label htmlFor="block-status" style={styles.formLabel}>Estatus</label>
                <select
                  id="block-status"
                  name="status"
                  style={styles.formInput}
                  value={formData.status}
                  onChange={handleChange}
                >
                  <option value="1">Activo</option>
                  <option value="0">Inactivo</option>
                </select>
              </div>
            )}
          </div>

          <div style={styles.actions}>
            <button
              type="button"
              style={styles.btnSecondary}
              onClick={() => navigate('/util/catalogos/bloqueos')}
            >
              Cancelar
            </button>
            <button
              type="submit"
              style={{
                ...styles.btnPrimary,
                opacity: saving || !supplierInfo ? 0.65 : 1,
                cursor: saving || !supplierInfo ? 'not-allowed' : 'pointer',
              }}
              disabled={saving || !supplierInfo}
            >
              {getSubmitLabel(saving, isEditing)}
            </button>
          </div>
        </form>
      </div>
      {ModalNode}
    </div>
  );
};

export default SupplierBlockForm;
