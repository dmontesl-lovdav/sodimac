// src/features/helpCenter/HelpCenterFaqByCategory.jsx
import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';
import { GenericModal } from '@shared/components/ui';
import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';
import { getCategories as getCategoriesApi } from '@/features/categories/api';
import './styles/HelpCenterFaqByCategory.css';

export default function HelpCenterFaqByCategory() {
    const [categories, setCategories] = useState([]);
    const [selectedId, setSelectedId] = useState(null);
    const [faqs, setFaqs] = useState([]);
    const [loadingCats, setLoadingCats] = useState(false);
    const [loadingFaqs, setLoadingFaqs] = useState(false);
    const [opLoading, setOpLoading] = useState(false);
    const [alert, setAlert] = useState({
        visible: false,
        title: '',
        message: '',
        severity: 'info'
    });

    const api = ConfigurationBuilder.client;
    const nav = useNavigate();
    const [params] = useSearchParams();
    const firstLoadRef = useRef(true);

    const showAlert = (severity, message, title = 'Listo') =>
        setAlert({ visible: true, title, message, severity });

    function getSelectedCategory() {
        return categories.find((c) => c.id === Number(selectedId));
    }

    const breadcrumbItems = [
        { label: 'Inicio', to: '/' },
        { label: 'Centro de ayuda', to: '/help-center' },
        { label: 'Preguntas frecuentes', to: `/help-center/faqs?categoryId=${selectedId ?? ''}` },
        { label: getSelectedCategory()?.name || 'Categoría' },
    ];

    useEffect(() => {
        (async () => {
            try {
                setLoadingCats(true);
                setOpLoading(true);

                const data = await getCategoriesApi({ size: 500, active: 'true' });
                const rows = (Array.isArray(data?.content) ? data.content : [])
                    .filter((c) => !!c.isActive)
                    .map((c) => ({
                        id: Number(c.id),
                        name: c.name,
                        description: c.description,
                    }))
                    .sort((a, b) => a.name.localeCompare(b.name));

                setCategories(rows);
                const qp = Number(params.get('categoryId') || 0);
                const exists = rows.some((c) => c.id === qp);
                setSelectedId(exists ? qp : rows[0]?.id ?? null);
            } catch (err) {
                console.error('No pude cargar categorías activas', err);
                showAlert('error', 'No se pudieron cargar las categorías.', 'Error');
            } finally {
                setLoadingCats(false);
                setOpLoading(false);
            }
        })();
    }, []);

    useEffect(() => {
        if (!selectedId) {
            setFaqs([]);
            return;
        }

        (async () => {
            const showInitial = firstLoadRef.current;
            try {
                setLoadingFaqs(true);
                if (showInitial) setOpLoading(true);

                let collected = [];
                let isPaginated = false;
                const size = 500;
                let page = 0;

                const firstResp = await api.getFaqs({
                    categoryId: Number(selectedId),
                    page,
                    size
                });

                if (firstResp && typeof firstResp === 'object' && !Array.isArray(firstResp)) {
                    const content = firstResp.content ?? [];
                    const totalPages = Number(firstResp.totalPages ?? 1);
                    isPaginated = Array.isArray(content);

                    if (isPaginated) {
                        collected = collected.concat(content);

                        for (let p = 1; p < Math.min(totalPages, 50); p++) {
                            const resp = await api.getFaqs({
                                categoryId: Number(selectedId),
                                page: p,
                                size
                            });
                            const more = resp?.content ?? [];
                            if (!Array.isArray(more) || more.length === 0) break;
                            collected = collected.concat(more);
                            if (collected.length >= 5000) break;
                        }
                    }
                }

                if (!isPaginated) {
                    const arr = await api.getFaqs({
                        categoryId: Number(selectedId),
                        size: 1000
                    });
                    collected = Array.isArray(arr) ? arr : [];
                }

                const onlyPublished = collected.filter((f) => f.published !== false);
                setFaqs(onlyPublished);
            } catch (err) {
                console.error('No se pudieron cargar las FAQs de la categoría', err);
                setFaqs([]);
                showAlert('error', 'No se pudieron cargar las preguntas frecuentes.', 'Error');
            } finally {
                setLoadingFaqs(false);
                if (firstLoadRef.current) {
                    setOpLoading(false);
                    firstLoadRef.current = false;
                }
            }
        })();
    }, [selectedId]);

    const selected = useMemo(() => getSelectedCategory(), [categories, selectedId]);

    return (
        <main className="hcf-main">
            <div className="hcf-container">
                <div className="hcf-breadcrumb-wrapper">
                    <Breadcrumb items={breadcrumbItems} />
                </div>

                <div className="hcf-content-wrapper">
                    <div className="hcf-content-card">
                        <div className="hcf-content-inner">

                            <div className="hcf-grid">
                                {/* ---------- SIDEBAR ---------- */}
                                <aside className="hcf-sidebar">
                                    <h3 className="hcf-sidebar-title">Categorías</h3>

                                    {loadingCats ? (
                                        <div className="hcf-text-loading">Cargando categorías…</div>
                                    ) : (
                                        <ul className="hcf-category-list">
                                            {categories.map((c) => {
                                                const active = c.id === Number(selectedId);
                                                return (
                                                    <li key={c.id}>
                                                        <button
                                                            className={
                                                                active
                                                                    ? 'hcf-cat-btn hcf-cat-btn-active'
                                                                    : 'hcf-cat-btn hcf-cat-btn-inactive'
                                                            }
                                                            onClick={() => setSelectedId(c.id)}
                                                        >
                                                            {c.name}
                                                        </button>
                                                    </li>
                                                );
                                            })}
                                        </ul>
                                    )}
                                </aside>

                                {/* ---------- FAQS LIST ---------- */}
                                <section className="hcf-faq-section">
                                    <div className="hcf-faq-header">
                                        <h2 className="hcf-title">{selected?.name || '...'}</h2>
                                        {selected?.description && (
                                            <p className="hcf-description">{selected.description}</p>
                                        )}
                                    </div>

                                    <div className="hcf-faq-list">
                                        {loadingFaqs ? (
                                            <div className="hcf-text-loading">Cargando preguntas…</div>
                                        ) : faqs.length === 0 ? (
                                            <div className="hcf-text-loading">No hay preguntas en esta categoría.</div>
                                        ) : (
                                            <div className="hcf-faq-items">
                                                {faqs.map((f) => (
                                                    <FaqItem
                                                        key={f.id}
                                                        faq={f}
                                                        categoryId={selectedId}
                                                        categoryName={selected?.name}
                                                    />
                                                ))}
                                            </div>
                                        )}
                                    </div>

                                    <div className="hcf-footer-bar">
                                        <button onClick={() => nav(-1)} className="hcf-back-btn">
                                            Volver
                                        </button>
                                    </div>
                                </section>
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

/* ---------- Single FAQ Item ---------- */
function FaqItem({ faq, categoryId, categoryName }) {
    const nav = useNavigate();

    return (
        <div
            className="hcf-faq-item"
            onClick={() =>
                nav(
                    `/help-center/faqs/detail?faqId=${faq.id}` +
                    `&categoryId=${categoryId}` +
                    `&categoryName=${encodeURIComponent(categoryName || '')}`
                )
            }
        >
            <div className="hcf-faq-item-inner">
                <span className="hcf-faq-question">{faq.question}</span>

                <svg className="hcf-faq-arrow" viewBox="0 0 20 20" fill="currentColor">
                    <path
                        fillRule="evenodd"
                        d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 111.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z"
                        clipRule="evenodd"
                    />
                </svg>
            </div>
        </div>
    );
}
