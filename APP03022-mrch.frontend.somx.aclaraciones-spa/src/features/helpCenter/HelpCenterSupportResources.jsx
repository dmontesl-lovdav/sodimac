import { useState, useRef, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';
import { GenericModal } from '@shared/components/ui';
import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';

import HelpCenterHeader from './parts/HelpCenterHeader';
import HelpCenterCategories from './parts/HelpCenterCategories';
import HelpCenterResourceList from './parts/HelpCenterResourceList';

import './styles/HelpCenterSupportResources.css';

export default function HelpCenterSupportResources() {
    const [categories, setCategories] = useState([]);
    const [selectedId, setSelectedId] = useState(null);
    const [resources, setResources] = useState([]);
    const [expandedIds, setExpandedIds] = useState(new Set());

    const [loadingCats, setLoadingCats] = useState(false);
    const [loadingRes, setLoadingRes] = useState(false);
    const [opLoading, setOpLoading] = useState(false);

    const [alert, setAlert] = useState({
        visible: false,
        title: '',
        message: '',
        severity: 'info'
    });

    const [showNote, setShowNote] = useState(true);

    const api = ConfigurationBuilder.client;
    const nav = useNavigate();
    const firstLoadRef = useRef(true);

    const breadcrumbItems = [
        { label: 'Inicio', to: '/' },
        { label: 'Centro de ayuda', to: '/help-center' },
        { label: 'Manuales y tutoriales' },
    ];

    const selected = useMemo(
        () => categories.find((c) => c.id === Number(selectedId)),
        [categories, selectedId]
    );

    const showAlert = (severity, message, title = 'Listo') =>
        setAlert({ visible: true, title, message, severity });

    // === Cargar categorías ===
    useEffect(() => {
        (async () => {
            try {
                setLoadingCats(true);
                setOpLoading(true);
                const data = await api.getFaqCategories({ includeIcons: true, active: 'true' });

                const rows = (Array.isArray(data?.content) ? data.content : [])
                    .filter((c) => c.isActive)
                    .map((c) => ({
                        id: Number(c.id),
                        name: c.name,
                        description: c.description,
                    }))
                    .sort((a, b) => a.name.localeCompare(b.name));

                setCategories(rows);
                setSelectedId(rows[0]?.id ?? null);
            } catch (err) {
                console.error('Error al cargar categorías FAQ', err);
                showAlert('error', 'No se pudieron cargar las categorías FAQ.', 'Error');
            } finally {
                setLoadingCats(false);
                setOpLoading(false);
            }
        })();
    }, [api]);

    // === Cargar recursos FAQ ===
    useEffect(() => {
        if (!selectedId) {
            setResources([]);
            return;
        }

        (async () => {
            try {
                setLoadingRes(true);
                if (firstLoadRef.current) setOpLoading(true);

                const faqs = await api.getFaqs({ categoryId: Number(selectedId), size: 1000 });
                const publishedFaqs = (faqs ?? []).filter(f => f.published === true);
                const detailedFaqs = await Promise.all(
                    publishedFaqs.map(async (f) => {
                        try {
                            const detail = await api.getFaq(f.id);
                            return {
                                id: Number(detail.id),
                                title: detail.question || detail.title || 'Pregunta frecuente',
                                description: detail.answer || detail.description || '',
                                attachments: (detail.attachments || []).map((a) => ({
                                    id: a.id,
                                    fileName: a.fileName,
                                    sizeKb: a.sizeKb ?? a.sizetx ?? null,
                                    contentType: a.contentType,
                                })),
                            };
                        } catch {
                            return {
                                id: Number(f.id),
                                title: f.question || f.title || 'Pregunta frecuente',
                                description: f.answer || f.description || '',
                                attachments: [],
                            };
                        }
                    })
                );

                setResources(detailedFaqs);
                setExpandedIds(new Set());
            } catch (err) {
                console.error('Error al cargar FAQs', err);
                showAlert('error', 'No se pudieron cargar las preguntas frecuentes.', 'Error');
            } finally {
                setLoadingRes(false);
                if (firstLoadRef.current) {
                    setOpLoading(false);
                    firstLoadRef.current = false;
                }
            }
        })();
    }, [api, selectedId]);

    const toggleExpand = (id) => {
        setExpandedIds((prev) => {
            const next = new Set(prev);
            next.has(id) ? next.delete(id) : next.add(id);
            return next;
        });
    };

    // === Descargar archivo ===
    const onDownload = async (att) => {
        try {
            setOpLoading(true);

            const blob = await api.downloadFaqAttachment(att.id);
            if (!(blob instanceof Blob)) throw new Error('Respuesta inválida');

            const objectUrl = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = objectUrl;
            a.download = att.fileName || 'archivo.pdf';
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            URL.revokeObjectURL(objectUrl);
        } catch (err) {
            console.error('Error al descargar archivo', err);
            showAlert('error', 'No se pudo descargar el archivo.', 'Error');
        } finally {
            setOpLoading(false);
        }
    };

    return (
        <main className="hcsr-main">
            <div className="hcsr-inner">
                <div className="hcsr-breadcrumb-wrapper">
                    <Breadcrumb items={breadcrumbItems} />
                </div>

                <HelpCenterHeader showNote={showNote} setShowNote={setShowNote} />

                <div className="hcsr-panel-wrapper">
                    <div className="hcsr-panel">
                        <div className="hcsr-panel-padding">
                            <div className="hcsr-grid">
                                <HelpCenterCategories
                                    categories={categories}
                                    selectedId={selectedId}
                                    loadingCats={loadingCats}
                                    setSelectedId={setSelectedId}
                                />

                                <HelpCenterResourceList
                                    resources={resources}
                                    selected={selected}
                                    loadingRes={loadingRes}
                                    expandedIds={expandedIds}
                                    toggleExpand={toggleExpand}
                                    onDownload={onDownload}
                                    nav={nav}
                                />
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <GenericModal visible={opLoading} variant="loading" message="Procesando…" />

            <GenericModal
                visible={alert.visible}
                variant="alert"
                title={alert.title}
                message={alert.message}
                severity={alert.severity}
                onClose={() => setAlert((a) => ({ ...a, visible: false }))}
            />
        </main>
    );
}
