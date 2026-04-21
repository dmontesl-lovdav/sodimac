// src/features/faq/components/parts/useFaqForm.jsx
import { useEffect, useRef, useState } from 'react';

const asStr = (v) => (v == null ? '' : String(v).trim());
const asNum = (v) => Number(asStr(v));
const uniq = (arr) => Array.from(new Set((arr ?? []).filter(Boolean)));

function normalizeOption(c) {
    return { value: String(c.id), label: c.name, description: c.description };
}

export default function useFaqForm({ api, id, onNotFound }) {
    // master data
    const [categories, setCategories] = useState([]);
    const [relatedInfoOptions, setRelatedInfoOptions] = useState([]);

    // topics
    const [topic, setTopic] = useState('');
    const [topics, setTopics] = useState([]); // string ids

    // questions (máximo 1)
    const [questionText, setQuestionText] = useState('');
    const [questions, setQuestions] = useState([]); // mostradas como chip (0..1)

    // answer
    const [answer, setAnswer] = useState('');

    // related info
    const [related, setRelated] = useState('');
    const [relatedList, setRelatedList] = useState([]); // string ids

    // attachments
    const [files, setFiles] = useState([]);
    const [existingAttachments, setExistingAttachments] = useState([]);
    const [removedAttachmentIds, setRemovedAttachmentIds] = useState([]);

    // flags
    const [isSaving, setIsSaving] = useState(false);
    const [isDownloading, setIsDownloading] = useState(false);

    // --- NUEVO: bootstrapping (catálogos + FAQ en edit) ---
    const [catReady, setCatReady] = useState(false);
    const [riReady, setRiReady] = useState(false);
    const [faqReady, setFaqReady] = useState(!id);
    const [isBootstrapping, setIsBootstrapping] = useState(true);
    useEffect(() => {
        setIsBootstrapping(!(catReady && riReady && faqReady));
    }, [catReady, riReady, faqReady]);

    // --- estado de alert modal (para descargas/errores del hook) ---
    const [alertConfig, setAlertConfig] = useState({
        visible: false,
        severity: 'info',
        title: '',
        message: '',
    });

    const showAlert = (cfg) => setAlertConfig({ visible: true, ...cfg });
    const hideAlert = () => setAlertConfig((p) => ({ ...p, visible: false }));

    // chip helpers (dedupe)
    const addTopic = (v) => {
        const s = asStr(v);
        if (!s) return;
        setTopics((prev) => uniq([...prev, s]));
    };
    const removeTopic = (v) => setTopics((prev) => prev.filter((x) => x !== v));

    // SOLO UNA pregunta
    const addQuestion = (v) => {
        const s = asStr(v);
        if (!s) return;
        setQuestions([s]); // reemplaza siempre
    };
    const removeQuestion = () => setQuestions([]);

    const addRelated = (v) => {
        const s = asStr(v);
        if (!s) return;
        setRelatedList((prev) => uniq([...prev, s]));
    };
    const removeRelated = (v) =>
        setRelatedList((prev) => prev.filter((x) => x !== v));

    // limita a 3 archivos nuevos
    const setFilesLimited = (list) =>
        setFiles((list ?? []).filter(Boolean).slice(0, 3));

    // 1) Cargar categorías
    const categoriesLoadedRef = useRef(false);
    useEffect(() => {
        if (categoriesLoadedRef.current) return;
        categoriesLoadedRef.current = true;

        let aborted = false;
        (async () => {
            try {
                const resp = await api.getFaqCategories({ page: 0, size: 1000 });
                if (aborted) return;

                const list = Array.isArray(resp?.content)
                    ? resp.content
                    : Array.isArray(resp)
                        ? resp
                        : [];

                setCategories(list.map(normalizeOption));
            } catch {
                /* noop */
            } finally {
                if (!aborted) setCatReady(true);
            }
        })();

        return () => {
            aborted = true;
        };
    }, [api]);

    // 1b) Cargar Related Info
    const riLoadedRef = useRef(false);
    useEffect(() => {
        if (riLoadedRef.current) return;
        riLoadedRef.current = true;

        let aborted = false;
        (async () => {
            try {
                const list = await api.getRelatedInformationList({ size: 1000 });
                if (aborted) return;
                setRelatedInfoOptions(
                    (list ?? []).map((ri) => ({
                        value: String(ri.id),
                        label: ri.title ?? ri.name ?? `#${ri.id}`,
                    })),
                );
            } catch {
                /* noop */
            } finally {
                if (!aborted) setRiReady(true);
            }
        })();

        return () => {
            aborted = true;
        };
    }, [api]);

    // 2) Cargar/Reset FAQ (create/edit)
    const loadedForIdRef = useRef(null);
    const alertedRef = useRef(false);
    const createInitRef = useRef(false); // <-- evita reset infinito en create

    useEffect(() => {
        // Si hay id, resetea el guard de create
        if (id) createInitRef.current = false;

        // --- CREATE ---
        if (!id) {
            if (createInitRef.current) return; // ya reseteado una vez
            createInitRef.current = true;

            setTopic('');
            setTopics([]);
            setQuestionText('');
            setQuestions([]);
            setAnswer('');
            setRelated('');
            setRelatedList([]);
            setFiles([]);
            setExistingAttachments([]);
            setRemovedAttachmentIds([]);
            loadedForIdRef.current = null;
            alertedRef.current = false;
            setFaqReady(true); // en create no hay que esperar detalle
            return;
        }

        // --- EDIT ---
        const numericId = Number(id);
        if (loadedForIdRef.current === numericId) return;
        loadedForIdRef.current = numericId;

        let aborted = false;
        (async () => {
            try {
                const data = await api.getFaq(numericId);
                if (aborted) return;

                const principalId = asStr(data.categoryId);
                setTopic(principalId);

                const extraCats = uniq((data.categoryIds ?? []).map(asStr));
                setTopics(uniq([principalId, ...extraCats]));

                const mainQ = asStr(data.question);
                setQuestionText(mainQ);
                setQuestions(mainQ ? [mainQ] : []);

                setAnswer(asStr(data.answer));

                setRelated('');
                const relatedFromApi = (data.relatedInfoIds ?? []).map((id) => String(id));
                setRelatedList(uniq(relatedFromApi));

                setExistingAttachments(
                    (data.attachments ?? []).map((a) => ({
                        id: a.id,
                        fileName: a.fileName,
                        sizeKb: a.sizeKb,
                        contentType: a.contentType,
                    })),
                );
                setFiles([]);
                setRemovedAttachmentIds([]);
            } catch (err) {
                if (!aborted && !alertedRef.current) {
                    alertedRef.current = true;
                    showAlert({
                        severity: 'error',
                        title: 'Error',
                        message: 'No se pudo cargar la pregunta para edición',
                    });
                    onNotFound?.();
                }
            } finally {
                if (!aborted) setFaqReady(true);
            }
        })();

        return () => {
            aborted = true;
        };
        // ⬇️ Importante: NO dependemos de onNotFound (puede cambiar cada render)
    }, [api, id]);

    // descarga de adjuntos
    const downloadAttachment = async (att) => {
        try {
            setIsDownloading(true);
            const blob = await api.downloadFaqAttachment(att.id);
            const objectUrl = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = objectUrl;
            a.download = att.fileName;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            URL.revokeObjectURL(objectUrl);
        } catch {
            showAlert({
                severity: 'error',
                title: 'Error',
                message: 'No se pudo descargar el archivo',
            });
        } finally {
            setIsDownloading(false);
        }
    };

    // recibir ids marcados para eliminar desde AttachmentsBlock
    const onChangeExistingRemoveIds = (ids) => {
        setRemovedAttachmentIds((ids ?? []).map(Number));
    };

    // payload para POST
    const buildPayload = () => {
        const mainQuestion = asStr(questionText) || asStr(questions[0]) || '';

        const principal = asStr(topic);
        const extraCats = uniq((topics ?? []).filter((t) => t !== principal)).map(asNum);
        const relatedInfoIds = uniq(relatedList).map(asNum);

        return {
            categoryId: asNum(principal),
            question: mainQuestion,
            answer: asStr(answer),
            aliases: [],
            relatedInfoIds,
            categoryIds: extraCats,
            files,
        };
    };

    // payload para PUT (agrega keep/remove)
    const buildUpdatePayload = () => {
        const base = buildPayload();

        const allExistingIds = (existingAttachments ?? []).map((a) => Number(a.id));
        const removeIds = [...new Set((removedAttachmentIds ?? []).map(Number))];
        const keepIds = allExistingIds.filter((id) => !removeIds.includes(id));

        return {
            ...base,
            relatedInfoIds: base.relatedInfoIds,
            keepAttachmentIds: keepIds,
            removeAttachmentIds: removeIds,
        };
    };

    // guardado
    const saveFaq = async (body, isEdit, numericId) => {
        try {
            setIsSaving(true);
            if (isEdit) {
                const updateBody = {
                    ...buildUpdatePayload(),
                    relatedInfoIds: body.relatedInfoIds,
                };
                await api.putFaq(Number(numericId), updateBody);
                showAlert({
                    severity: 'success',
                    title: 'Éxito',
                    message: '✅ Pregunta actualizada',
                });
            } else {
                const { id: newId } = await api.postFaq(body);
                showAlert({
                    severity: 'success',
                    title: 'Éxito',
                    message: `✅ Pregunta creada (id: ${newId})`,
                });
            }
        } finally {
            setIsSaving(false);
        }
    };

    return {
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
        setFiles: setFilesLimited,

        existingAttachments,
        onChangeExistingRemoveIds,

        isDownloading,
        downloadAttachment,

        isSaving,
        saveFaq,
        buildPayload,
        buildUpdatePayload,

        alertConfig,
        hideAlert,

        isBootstrapping,
    };
}
