import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { conversionService, catalogService, catalogElementService } from '@features/catalogos/services/catalogosApi';
import { useModalNotification } from '@shared/components/ui/modal';
import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';
import { withFinanceBreadcrumb } from '@shared/components/ui/navigation/financeBreadcrumb';

export default function EditConversionForm() {
  const navigate = useNavigate();
  const { elementId, conversionId } = useParams<{ elementId: string; conversionId: string }>();
  const { showSuccess, ModalNode } = useModalNotification();

  const [expandedSteps, setExpandedSteps] = useState<number[]>([1, 2, 3]);
  const [catalogs, setCatalogs] = useState<any[]>([]);
  const [elements, setElements] = useState<any[]>([]);
  const [selectedCatalogId, setSelectedCatalogId] = useState('');
  const [selectedElementId, setSelectedElementId] = useState('');
  const [selectedElement, setSelectedElement] = useState<any>(null);

  const [originalCatalogId, setOriginalCatalogId] = useState('');
  const [originalElementId, setOriginalElementId] = useState('');

  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [showExitModal, setShowExitModal] = useState(false);
  const [message, setMessage] = useState<{ text: string; type: 'success' | 'error' } | null>(null);

  const hasChanges = selectedCatalogId !== originalCatalogId || selectedElementId !== originalElementId;

  useEffect(() => {
    catalogService.search({ page: 1, pageSize: 100, estatus: 1 })
      .then(res => setCatalogs(res.items || []))
      .catch(() => {});
  }, []);

  useEffect(() => {
    if (conversionId) {
      setIsLoading(true);
      conversionService.getById(parseInt(conversionId))
        .then((conv: any) => {
          const targetCatId = conv.idCatalogoOrigen || '';
          const targetElemId = String(conv.idElemento || conv.idElementoDestino || '');

          catalogService.search({ page: 1, pageSize: 100, estatus: 1 })
            .then(catRes => {
              const cats = catRes.items || [];
              setCatalogs(cats);
              const matchedCat = cats.find((c: any) => c.name === conv.catalogoOrigen);
              const catId = matchedCat ? String(matchedCat.id) : String(targetCatId);

              setSelectedCatalogId(catId);
              setOriginalCatalogId(catId);
              setSelectedElementId(targetElemId);
              setOriginalElementId(targetElemId);

              if (catId) {
                catalogElementService.search(parseInt(catId), { page: 1, pageSize: 100, estatus: 1 })
                  .then(elemRes => {
                    setElements(elemRes.items || []);
                    const elem = (elemRes.items || []).find((e: any) => String(e.id) === targetElemId);
                    if (elem) {
                      setSelectedElement({
                        id: elem.id,
                        nombre: elem.element || elem.key || '',
                        catalogoOrigen: matchedCat?.name || conv.catalogoOrigen || '',
                        estatus: elem.status === 1 ? 'Activo' : 'Inactivo',
                        fechaInicioVigencia: elem.validFrom || '-',
                        fechaFinVigencia: elem.validTo || '-',
                        valor: elem.value || '-',
                      });
                    }
                  })
                  .catch(() => {});
              }
            })
            .catch(() => {});
        })
        .catch(() => setMessage({ text: 'Error al cargar la conversión.', type: 'error' }))
        .finally(() => setIsLoading(false));
    }
  }, [conversionId]);

  useEffect(() => {
    if (selectedCatalogId && !isLoading) {
      catalogElementService.search(parseInt(selectedCatalogId), { page: 1, pageSize: 100, estatus: 1 })
        .then(res => setElements(res.items || []))
        .catch(() => setElements([]));
    }
  }, [selectedCatalogId]);

  useEffect(() => {
    if (selectedElementId && elements.length > 0) {
      const elem = elements.find((e: any) => String(e.id) === selectedElementId);
      if (elem) {
        const cat = catalogs.find((c: any) => String(c.id) === selectedCatalogId);
        setSelectedElement({
          id: elem.id,
          nombre: elem.element || elem.key || '',
          catalogoOrigen: cat?.name || '',
          estatus: elem.status === 1 ? 'Activo' : 'Inactivo',
          fechaInicioVigencia: elem.validFrom || '-',
          fechaFinVigencia: elem.validTo || '-',
          valor: elem.value || '-',
        });
      }
    } else if (!selectedElementId) {
      setSelectedElement(null);
    }
  }, [selectedElementId, elements]);

  const handleCatalogChange = (catId: string) => {
    setSelectedCatalogId(catId);
    setSelectedElementId('');
    setSelectedElement(null);
    setErrors({});
    if (catId) {
      setExpandedSteps([1, 2]);
    } else {
      setExpandedSteps([1]);
    }
  };

  const handleElementChange = (elemId: string) => {
    setSelectedElementId(elemId);
    setErrors({});
    if (elemId) {
      setExpandedSteps([1, 2, 3]);
    }
  };

  const handleClear = () => {
    setSelectedCatalogId(originalCatalogId);
    setSelectedElementId(originalElementId);
    setErrors({});
    setExpandedSteps([1, 2, 3]);
    if (originalCatalogId) {
      catalogElementService.search(parseInt(originalCatalogId), { page: 1, pageSize: 100, estatus: 1 })
        .then(res => setElements(res.items || []))
        .catch(() => {});
    }
  };

  const handleSave = async () => {
    const newErrors: Record<string, string> = {};
    if (!selectedCatalogId) newErrors.catalog = 'Seleccione un catálogo de origen.';
    if (!selectedElementId) newErrors.element = 'Seleccione un elemento para la conversión.';
    setErrors(newErrors);
    if (Object.keys(newErrors).length > 0) return;

    setIsSubmitting(true);
    setMessage(null);
    try {
      await conversionService.update(parseInt(conversionId!), {
        targetElementId: parseInt(selectedElementId),
      });
      showSuccess(
        `La conversión ${conversionId} se ha actualizado exitosamente.`,
        'Conversión actualizada',
        () => navigate(`/util/catalogos/elementos/${elementId}/conversiones`),
      );
    } catch (error: any) {
      const status = error?.response?.status;
      const msg = error?.response?.data?.message || error?.response?.data?.details;
      if (status === 403) {
        setMessage({ text: 'No cuenta con permisos para modificar conversiones.', type: 'error' });
      } else if (status === 409) {
        setMessage({ text: msg || 'El elemento ya se encuentra agregado como conversión, seleccione otro elemento.', type: 'error' });
      } else if (status === 401) {
        window.location.reload();
      } else if (status >= 400 && status < 500) {
        setMessage({ text: msg || 'No fue posible guardar la conversión. Intente nuevamente.', type: 'error' });
      } else {
        setMessage({ text: 'Ocurrió un problema al actualizar la conversión. Intente nuevamente.', type: 'error' });
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleBack = () => {
    if (hasChanges) {
      setShowExitModal(true);
    } else {
      navigate(`/util/catalogos/elementos/${elementId}/conversiones`);
    }
  };

  const toggleStep = (step: number) => {
    setExpandedSteps(prev => prev.includes(step) ? prev.filter(s => s !== step) : [...prev, step]);
  };

  const S: any = {
    container: { padding: '1.5rem', fontFamily: 'Inter,system-ui,sans-serif', backgroundColor: '#fff', minHeight: '100vh' },
    title: { fontSize: '1.5rem', fontWeight: 600, color: '#1e293b', marginBottom: '0.25rem' },
    desc: { fontSize: '0.875rem', color: '#64748b', marginBottom: '2rem', lineHeight: 1.6 },
    card: { backgroundColor: '#fff', border: '1px solid #e5e7eb', borderRadius: '0.75rem', padding: '2rem', maxWidth: '700px' },
    stepContainer: { display: 'flex', marginBottom: '1.5rem' },
    stepIndicator: { display: 'flex', flexDirection: 'column', alignItems: 'center', marginRight: '1rem' },
    stepNumber: { width: 32, height: 32, backgroundColor: '#0066CC', color: '#fff', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 600, fontSize: '0.875rem', flexShrink: 0 },
    stepNumberDisabled: { backgroundColor: '#cbd5e1' },
    stepLine: { width: 2, flexGrow: 1, backgroundColor: '#0066CC', marginTop: '0.5rem' },
    stepLineDisabled: { backgroundColor: '#cbd5e1' },
    stepContent: { flex: 1, paddingBottom: '1rem' },
    stepTitle: { fontSize: '1rem', fontWeight: 600, color: '#1e293b', cursor: 'pointer', marginBottom: '0.25rem' },
    stepTitleDisabled: { color: '#94a3b8' },
    stepSubtitle: { fontSize: '0.8rem', color: '#64748b' },
    select: { width: '100%', padding: '0.625rem 0.875rem', fontSize: '0.875rem', border: '1px solid #d1d5db', borderRadius: '0.375rem', backgroundColor: '#fff', boxSizing: 'border-box' as const },
    infoCard: { backgroundColor: '#f8fafc', border: '1px solid #e5e7eb', borderRadius: '0.5rem', padding: '1rem', display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))', gap: '0.75rem' },
    infoLabel: { fontSize: '0.7rem', color: '#64748b' },
    infoValue: { fontSize: '0.8rem', fontWeight: 500, color: '#1e293b' },
    badge: { padding: '0.125rem 0.5rem', borderRadius: '9999px', fontSize: '0.7rem', fontWeight: 500 },
    active: { backgroundColor: '#dcfce7', color: '#166534' },
    inactive: { backgroundColor: '#fee2e2', color: '#991b1b' },
    fieldError: { fontSize: '0.75rem', color: '#dc2626', marginTop: '0.25rem' },
    footer: { display: 'flex', justifyContent: 'space-between', marginTop: '2rem', paddingTop: '1rem', borderTop: '1px solid #e5e7eb' },
    btn: { padding: '0.5rem 1rem', borderRadius: '0.375rem', fontSize: '0.875rem', fontWeight: 500, cursor: 'pointer', border: 'none' },
    primaryBtn: { backgroundColor: '#0066CC', color: '#fff' },
    ghostBtn: { backgroundColor: 'transparent', color: '#64748b', textDecoration: 'underline', border: 'none' },
    modal: { position: 'fixed' as const, top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000 },
    modalBox: { backgroundColor: '#fff', borderRadius: '0.75rem', padding: '1.5rem', maxWidth: '400px', width: '90%' },
    msg: { padding: '0.75rem 1rem', borderRadius: '0.375rem', marginBottom: '1rem', fontSize: '0.875rem' },
  };

  const isStep1Complete = selectedCatalogId !== '';
  const isStep2Complete = selectedElementId !== '';

  if (isLoading) {
    return <div style={S.container}><p style={{ color: '#64748b', textAlign: 'center', padding: '3rem' }}>Cargando conversión...</p></div>;
  }

  return (
    <div style={S.container}>
      <Breadcrumb
        items={withFinanceBreadcrumb([
          { label: 'Gestión de Catálogos', to: '/util/catalogos' },
          { label: 'Catálogos', to: '/util/catalogos/catalogs' },
          { label: 'Conversiones', to: `/util/catalogos/elementos/${elementId}/conversiones` },
          { label: 'Editar Conversión' },
        ])}
      />

      {message && (
        <div style={{ ...S.msg, backgroundColor: message.type === 'success' ? '#dcfce7' : '#fee2e2', color: message.type === 'success' ? '#166534' : '#991b1b' }}>
          {message.text}
          <button style={{ float: 'right', background: 'none', border: 'none', cursor: 'pointer', fontWeight: 600 }} onClick={() => setMessage(null)}>✕</button>
        </div>
      )}

      <h1 style={S.title}>Editar Conversión</h1>
      <p style={S.desc}>Para editar el elemento de conversión, actualiza el formulario asegurándote de proporcionar información precisa y detallada.</p>

      <div style={S.card}>
        <div style={S.stepContainer}>
          <div style={S.stepIndicator}><div style={S.stepNumber}>1</div><div style={S.stepLine} /></div>
          <div style={S.stepContent}>
            <div style={S.stepTitle} onClick={() => toggleStep(1)}>Buscar Catálogo</div>
            <div style={S.stepSubtitle}>Selecciona el catálogo que contiene el elemento destino.</div>
            {expandedSteps.includes(1) && (
              <div style={{ marginTop: '1rem' }}>
                <select style={S.select} value={selectedCatalogId} onChange={e => handleCatalogChange(e.target.value)}>
                  <option value="">Seleccionar catálogo...</option>
                  {catalogs.map(c => <option key={c.id} value={c.id}>{c.name} ({c.code})</option>)}
                </select>
                {errors.catalog && <div style={S.fieldError}>{errors.catalog}</div>}
              </div>
            )}
          </div>
        </div>

        <div style={S.stepContainer}>
          <div style={S.stepIndicator}>
            <div style={{ ...S.stepNumber, ...(isStep1Complete ? {} : S.stepNumberDisabled) }}>2</div>
            <div style={{ ...S.stepLine, ...(isStep1Complete ? {} : S.stepLineDisabled) }} />
          </div>
          <div style={S.stepContent}>
            <div style={{ ...S.stepTitle, ...(isStep1Complete ? {} : S.stepTitleDisabled) }}
              onClick={() => isStep1Complete && toggleStep(2)}>Seleccionar Elemento</div>
            <div style={S.stepSubtitle}>Selecciona el elemento destino para la conversión.</div>
            {expandedSteps.includes(2) && isStep1Complete && (
              <div style={{ marginTop: '1rem' }}>
                <select style={S.select} value={selectedElementId} onChange={e => handleElementChange(e.target.value)}>
                  <option value="">Seleccionar elemento...</option>
                  {elements.map((e: any) => <option key={e.id} value={e.id}>{e.element || e.key} {e.value ? `(${e.value})` : ''}</option>)}
                </select>
                {errors.element && <div style={S.fieldError}>{errors.element}</div>}
              </div>
            )}
          </div>
        </div>

        <div style={S.stepContainer}>
          <div style={S.stepIndicator}>
            <div style={{ ...S.stepNumber, ...(isStep2Complete ? {} : S.stepNumberDisabled) }}>3</div>
          </div>
          <div style={S.stepContent}>
            <div style={{ ...S.stepTitle, ...(isStep2Complete ? {} : S.stepTitleDisabled) }}
              onClick={() => isStep2Complete && toggleStep(3)}>Elemento para Conversión</div>
            <div style={S.stepSubtitle}>Detalle del elemento seleccionado.</div>
            {expandedSteps.includes(3) && isStep2Complete && selectedElement && (
              <div style={{ marginTop: '1rem' }}>
                <div style={S.infoCard}>
                  <div><span style={S.infoLabel}>Nombre del Elemento</span><br/><span style={S.infoValue}>{selectedElement.nombre}</span></div>
                  <div><span style={S.infoLabel}>Catálogo Origen</span><br/><span style={S.infoValue}>{selectedElement.catalogoOrigen}</span></div>
                  <div><span style={S.infoLabel}>Estatus Elemento</span><br/>
                    <span style={{ ...S.badge, ...(selectedElement.estatus === 'Activo' ? S.active : S.inactive) }}>{selectedElement.estatus}</span></div>
                  <div><span style={S.infoLabel}>Fecha Inicio Vigencia</span><br/><span style={S.infoValue}>{selectedElement.fechaInicioVigencia || '-'}</span></div>
                  <div><span style={S.infoLabel}>Fecha Fin Vigencia</span><br/><span style={S.infoValue}>{selectedElement.fechaFinVigencia || '-'}</span></div>
                  <div><span style={S.infoLabel}>Valor del Elemento</span><br/><span style={S.infoValue}>{selectedElement.valor}</span></div>
                </div>
              </div>
            )}
          </div>
        </div>

        <div style={S.footer}>
          <button style={{ ...S.btn, ...S.ghostBtn }} onClick={handleBack}>Volver</button>
          <div style={{ display: 'flex', gap: '0.75rem' }}>
            <button style={{ ...S.btn, ...S.ghostBtn }} onClick={handleClear}>Limpiar</button>
            <button style={{ ...S.btn, ...S.primaryBtn, opacity: isSubmitting ? 0.6 : 1 }} onClick={handleSave} disabled={isSubmitting}>
              {isSubmitting ? 'Guardando...' : 'Guardar'}
            </button>
          </div>
        </div>
      </div>

      {showExitModal && (
        <div style={S.modal}>
          <div style={S.modalBox}>
            <h3 style={{ marginBottom: '1rem', fontSize: '1.1rem' }}>
              Los cambios realizados se perderán. ¿Desea continuar?
            </h3>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
              <button style={{ ...S.btn, border: '1px solid #d1d5db', backgroundColor: '#fff', color: '#374151' }} onClick={() => setShowExitModal(false)}>No</button>
              <button style={{ ...S.btn, ...S.primaryBtn }} onClick={() => navigate(`/util/catalogos/elementos/${elementId}/conversiones`)}>Sí</button>
            </div>
          </div>
        </div>
      )}
      {ModalNode}
    </div>
  );
}
