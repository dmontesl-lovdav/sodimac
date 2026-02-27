// src/features/faq/components/AddEditFaqForm.jsx
import { useCallback, useMemo, useState, useEffect } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';
import { Breadcrumb, GenericModal } from '@shared/components/ui';

// Hook personalizado que gestiona el estado, carga y guardado de una FAQ
import useFaqForm from './parts/useFaqForm';

// Selector de categorías o temas principales de ayuda
import TopicSelector from './parts/TopicSelector';

// Campo para agregar y listar las diferentes formas de preguntar (alias de la FAQ)
import VariantsInput from './parts/VariantsInput';

// Campo de texto para escribir la respuesta de la pregunta frecuente
import AnswerInput from './parts/AnswerInput';

// Selector de información relacionada (otras FAQs o enlaces de referencia)
import RelatedSelector from './parts/RelatedSelector';

// Bloque que gestiona los archivos adjuntos (ver, subir, eliminar, descargar)
import AttachmentsBlock from './parts/AttachmentsBlock';

// Pie de formulario con los botones de guardar o cancelar
import FormFooter from './parts/FormFooter';

import '../styles/AddEditFaqForm.css';

export default function AddEditFaqForm() {
    const location = useLocation();
    const fromMaintainer =
        location.state?.fromMaintainer ||
        new URLSearchParams(location.search).get('from') === 'mantenedor';

    const nav = useNavigate();
    const { id } = useParams();
    const isEdit = Boolean(id);

    const withOrigin = useCallback(
        (pathname) => ({
            pathname,
            search: fromMaintainer ? '?from=mantenedor' : '',
        }),
        [fromMaintainer]
    );
    const stateOrigin = fromMaintainer ? { fromMaintainer: true } : undefined;

    const goBack = useCallback(() => {
        if (fromMaintainer) {
            nav(withOrigin('/faqs'), { state: stateOrigin });
        } else {
            nav(-1);
        }
    }, [fromMaintainer, nav, stateOrigin, withOrigin]);

    const api = useMemo(() => ConfigurationBuilder.client, []);

    const {
        categories,
        relatedInfoOptions,

        topic,
        setTopic,
        topics,
        addTopic,
        removeTopic,

        questionText,
        setQuestionText,
        questions,
        addQuestion,
        removeQuestion,

        answer,
        setAnswer,

        related,
        setRelated,
        relatedList,
        addRelated,
        removeRelated,

        files,
        setFiles,
        existingAttachments,
        onChangeExistingRemoveIds,
        isDownloading,
        downloadAttachment,

        isSaving,
        saveFaq,

        buildPayload,
        isBootstrapping,
    } = useFaqForm({ api, id, onNotFound: goBack });

    useEffect(() => {
        const handler = () => goBack();
        window.addEventListener("country-changed", handler);
        return () => window.removeEventListener("country-changed", handler);
    }, [goBack]);

    const [alertModal, setAlertModal] = useState({
        visible: false,
        title: '',
        message: '',
        severity: 'info',
        buttonText: 'Aceptar',
    });

    const showAlert = ({ title = '', message = '', severity = 'info', buttonText = 'Aceptar' }) =>
        setAlertModal({ visible: true, title, message, severity, buttonText });

    const closeAlert = () =>
        setAlertModal((m) => ({ ...m, visible: false }));

    const handleSubmit = async () => {
        if (isSaving) return;

        const topicsCount =
            (Array.isArray(topics) ? topics.length : 0) || (topic ? 1 : 0);

        const hasTopics =
            topicsCount > 0 ||
            Boolean(topic?.id) ||
            (typeof topic === 'string' && topic.trim().length > 0);

        const hasAnswer =
            typeof answer === 'string' && answer.trim().length > 0;

        const hasQuestionText =
            typeof questionText === 'string' && questionText.trim().length > 0;

        const hasQuestions =
            Array.isArray(questions) && questions.length > 0;

        if (!hasTopics || !hasAnswer) {
            showAlert({
                severity: 'warning',
                title: 'Campos incompletos',
                message: 'Completa la categoría (al menos un tema) y la respuesta.',
            });
            return;
        }
        if (!hasQuestionText && !hasQuestions) {
            showAlert({
                severity: 'warning',
                title: 'Falta la pregunta',
                message: 'Agrega al menos una forma de preguntar.',
            });
            return;
        }

        try {
            const raw = buildPayload();

            const uniqNums = (arr) =>
                Array.from(new Set((arr ?? []).map((n) => Number(n)))).filter(
                    (n) => Number.isFinite(n) && n > 0
                );

            const categoryIds = uniqNums(raw.categoryIds);
            const relatedInfoIds = uniqNums(raw.relatedInfoIds);

            const primaryCategoryId =
                (raw.categoryId && Number(raw.categoryId)) || categoryIds[0];

            const firstQuestion =
                (hasQuestionText && questionText.trim()) ||
                (Array.isArray(raw.aliases) &&
                    String(raw.aliases[0] ?? '').trim()) ||
                '';

            const body = {
                ...raw,
                question: firstQuestion || raw.question,
                aliases: firstQuestion ? [firstQuestion] : [],
                categoryIds,
                relatedInfoIds,
                ...(primaryCategoryId ? { categoryId: primaryCategoryId } : {}),
            };

            await saveFaq(body, Boolean(id), Number(id));
            goBack();
        } catch {
            showAlert({
                severity: 'error',
                title: 'Error',
                message: 'Error al guardar la pregunta.',
            });
        }
    };

    const breadcrumbItems = fromMaintainer
        ? [
            { label: 'Centro de ayuda', to: '/' },
            { label: 'Mantenedor', to: '/mantenedor' },
            { label: 'Preguntas frecuentes', to: withOrigin('/faqs') },
            { label: isEdit ? 'Editar pregunta' : 'Agregar pregunta' },
        ]
        : [
            { label: 'Centro de ayuda', to: '/' },
            { label: 'Preguntas frecuentes', to: '/faqs' },
            { label: isEdit ? 'Editar pregunta' : 'Agregar pregunta' },
        ];

    return (
        <>
            <GenericModal
                visible={isBootstrapping}
                variant="loading"
                message={isEdit ? 'Cargando pregunta…' : 'Cargando catálogos…'}
            />

            <GenericModal
                visible={isDownloading}
                variant="loading"
                message="Descargando archivo…"
            />

            <GenericModal
                visible={alertModal.visible}
                variant="alert"
                title={alertModal.title}
                message={alertModal.message}
                severity={alertModal.severity}
                buttonText={alertModal.buttonText}
                onClose={closeAlert}
            />

            <div className="faq-form-wrapper">
                <div className="faq-breadcrumb">
                    <Breadcrumb items={breadcrumbItems} />
                </div>

                <div className="faq-container">
                    <div className="faq-inner">

                        <section className="faq-section">
                            <h3>Asignar temas de ayuda</h3>
                            <TopicSelector
                                categories={categories}
                                value={topic}
                                onChange={setTopic}
                                topics={topics}
                                onAdd={() => addTopic(topic)}
                                onRemove={removeTopic}
                            />
                        </section>

                        <section className="faq-section">
                            <h3>Formas de preguntar</h3>
                            <VariantsInput
                                value={questionText}
                                onChange={setQuestionText}
                                questions={questions}
                                onAdd={() => addQuestion(questionText)}
                                onRemove={removeQuestion}
                            />
                        </section>

                        <section className="faq-section">
                            <h3>Respuesta</h3>
                            <AnswerInput
                                value={answer}
                                onChange={setAnswer}
                            />
                        </section>

                        <section className="faq-section">
                            <h3>Información relacionada</h3>
                            <RelatedSelector
                                categories={relatedInfoOptions}
                                value={related}
                                onChange={setRelated}
                                relatedList={relatedList}
                                onAdd={() => addRelated(related)}
                                onRemove={removeRelated}
                            />
                        </section>

                        <section className="faq-section">
                            <h4>Agregar documento, imagen o video</h4>
                            <p className="faq-hint">
                                Asegúrate de que los archivos sean claros y legibles.
                            </p>

                            <AttachmentsBlock
                                files={files}
                                setFiles={setFiles}
                                existingAttachments={existingAttachments}
                                onDownload={downloadAttachment}
                                onChangeRemoveIds={onChangeExistingRemoveIds}
                            />
                        </section>

                        <FormFooter
                            isEdit={isEdit}
                            isSaving={isSaving}
                            onCancel={goBack}
                            onSubmit={handleSubmit}
                        />
                    </div>
                </div>
            </div>
        </>
    );
}
