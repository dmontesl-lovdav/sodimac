// @ts-nocheck
import { Buffer } from 'buffer';
import { useEffect, useMemo, useState } from 'react';
import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';
import { loadCatalog, translateIdToString } from './RequestUtils';
import AttachmentUploader from '../../../shared/components/ui/attachmentUploader/AttachmentUploader';
import RequestDetail from './RequestDetail';
import CommentSummary from '../utils/CommentSummary';
import AttachmentSummary from '../utils/AttachmentSummary';
import {
    GenericSelectFloating,
    GenericButton,
    GenericInput,
    GenericLinearProgress,
    GenericModal
} from '@shared/components/ui';
import { useNavigate } from 'react-router-dom';
import '../styles/RequestConfigure.css';

export default function RequestConfigure({
    request,
    backCallback,
    businessUnits,
    modules,
    reasons,
    details,
}) {
    const STATE_LOADING = 1;
    const STATE_LOADED = 2;
    const STATE_DETAIL = 3;
    const STATE_POSTING = 4;

    const CATALOGS_CLAZZES = 11;
    const CATALOGS_PROGRESS = 12;
    const CATALOGS_PRIORITIES = 13;
    const CATALOGS_REPEATITIVENESS = 14;

    const navigate = useNavigate();
    const apiClient = ConfigurationBuilder.client;

    const toOptions = (arr) =>
        arr
            .map((it) => ({
                value: String(it?.id ?? ''),
                label: String(it?.description ?? ''),
            }))
            .filter((o) => o.value && o.label);

    const [existingComments, setExistingComments] = useState(request?.comments);
    const [existingAttachments, setExistingAttachments] = useState(request?.attachments);

    const [form, setForm] = useState({
        module: request?.module != null ? String(request.module) : '',
        clazz: request?.clazz != null ? String(request.clazz) : '',
        progress: request?.progress != null ? String(request.progress) : '',
        priority: request?.priority != null ? String(request.priority) : '',
        repeatitiveness: request?.repeatitiveness != null ? String(request.repeatitiveness) : '',
    });

    const [state, setState] = useState(STATE_LOADING);

    const [showModuleModal, setShowModuleModal] = useState(false);
    const [pendingPayload, setPendingPayload] = useState(null);

    const [files, setFiles] = useState([]);
    const [comment, setComment] = useState('');

    const [clazzes, setClazzes] = useState([]);
    const [priorities, setPriorities] = useState([]);
    const [progress, setProgress] = useState([]);
    const [repeatitiveness, setRepeatitiveness] = useState([]);

    useEffect(() => {
        let cancelled = false;

        (async () => {
            try {
                if (!request?.comments?.length) {
                    const list = await apiClient.getRequestComments(request.id);
                    if (!cancelled) setExistingComments(list);
                }
                if (!request?.attachments?.length) {
                    const list = await apiClient.getRequestAttachments(request.id);
                    if (!cancelled) setExistingAttachments(list);
                }
            } catch { }
        })();

        return () => { cancelled = true; };
    }, [request?.id]);

    async function waitForCatalogs() {
        setState(STATE_LOADING);

        let ok = true;
        ok = ok && await loadCatalog(apiClient, CATALOGS_CLAZZES, setClazzes);
        ok = ok && await loadCatalog(apiClient, CATALOGS_PRIORITIES, setPriorities);
        ok = ok && await loadCatalog(apiClient, CATALOGS_PROGRESS, setProgress);
        ok = ok && await loadCatalog(apiClient, CATALOGS_REPEATITIVENESS, setRepeatitiveness);

        setState(ok ? STATE_LOADED : STATE_LOADING);
    }

    useEffect(() => {
        if (state === STATE_LOADING) waitForCatalogs();
    }, [state]);

    function updateField(arg) {
        let name, value;

        if (arg && typeof arg === 'object' && 'name' in arg && 'value' in arg && !('target' in arg)) {
            name = arg.name;
            value = arg.value;
        } else if (arg?.target) {
            name = arg.target.getAttribute('name') ?? arg.target.name;
            value = arg.target.value;
        } else {
            value = arg;
        }

        if (!name) return;
        setForm((prev) => ({ ...prev, [name]: value == null ? '' : String(value) }));
    }

    function updateComment(arg) {
        const val = typeof arg === 'string' ? arg : String(arg?.target?.value ?? '');
        setComment(val);
    }

    async function rejectRequest() {
        try {
            setState(STATE_POSTING);

            const payload = { clazz: '52' };
            await apiClient.configureRequest(request.id, payload);

            backCallback ? backCallback() : navigate('/cases');
        } catch {
            setState(STATE_LOADED);
        }
    }

    function handleSubmit() {
        const originalModule = String(request.module);
        const newModule = String(form.module);

        if (originalModule === newModule) {
            configureRequest();
            return;
        }

        const payload = { ...form };
        setPendingPayload(payload);
        setShowModuleModal(true);
    }

    async function configureRequest(customPayload, moduleChanged = false) {
        const payload = customPayload ?? { ...form };

        if (payload.module) payload.module = Number(payload.module);

        if (Array.isArray(files) && files.length > 0) {
            const valid = files.filter((f) => !f?.err);

            const buffers = await Promise.all(
                valid.map(async (f) => ({
                    name: f.name,
                    content: Buffer.from(await f.arrayBuffer()).toString('base64'),
                }))
            );

            if (buffers.length) payload.attachments = buffers;
        }

        const cleaned = (comment || '').trim();
        if (cleaned) payload.comments = [{ comment: cleaned }];

        try {
            setState(STATE_POSTING);
            await apiClient.configureRequest(request.id, payload);

            if (cleaned) {
                setExistingComments((prev) => [
                    ...prev,
                    {
                        id: Date.now(),
                        comment: cleaned,
                        creationTime: new Date().toISOString(),
                        author: 'yo',
                    }
                ]);
                setComment('');
            }

            if (payload.attachments?.length) {
                const list = await apiClient.getRequestAttachments(request.id);
                setExistingAttachments(list);
                setFiles([]);
            }

            if (moduleChanged) {
                navigate('/mantenedor');
                return;
            }

            backCallback ? backCallback() : setState(STATE_LOADED);

        } catch {
            setState(STATE_LOADED);
        }
    }

    const clazzOptions = toOptions(clazzes);
    const progressOptions = toOptions(progress);
    const priorityOptions = toOptions(priorities);
    const repeatitivenessOptions = toOptions(repeatitiveness);

    const [showAllComments, setShowAllComments] = useState(false);
    const totalComments = existingComments.length;

    const visibleComments = useMemo(
        () => (showAllComments ? existingComments : existingComments.slice(-3)),
        [existingComments, showAllComments]
    );

    const moduleWarningModal = (
        <GenericModal
            visible={showModuleModal}
            variant="confirm"
            severity="warning"
            title="Cambiar módulo"
            message="Si cambias el módulo, este caso será asignado a otro resolutor. ¿Deseas continuar?"
            confirmText="Sí, continuar"
            cancelText="Cancelar"
            onConfirm={() => {
                setShowModuleModal(false);
                configureRequest(pendingPayload, true);
            }}
            onCancel={() => {
                setShowModuleModal(false);
                setPendingPayload(null);
            }}
        />
    );

    function build() {
        return (
            <div className="rc-container">
                {moduleWarningModal}

                <div className="rc-header">
                    <div className="rc-title">Solicitud {request.id}</div>
                </div>

                <div className="rc-block">
                    <div className="rc-row">
                        <span className="rc-label">Unidad de negocio:</span>
                        {translateIdToString(request.businessUnit, businessUnits)}
                    </div>

                    <div className="rc-row">
                        <span className="rc-label">Módulo:</span>
                        {translateIdToString(request.module, modules)}
                    </div>

                    <div className="rc-row">
                        <span className="rc-label">Motivo:</span>
                        {translateIdToString(request.reason, reasons)}
                    </div>

                    <div className="rc-right">
                        <GenericButton
                            className="rc-link"
                            variant="link"
                            onClick={() => setState(STATE_DETAIL)}
                        >
                            Ver la solicitud
                        </GenericButton>
                    </div>
                </div>

                <div className="rc-block">
                    <div className="rc-grid">

                        <GenericSelectFloating
                            key={`module-${modules.length}-${form.module}`}
                            className="rc-select"
                            name="module"
                            label="Módulo"
                            value={form.module}
                            onChange={updateField}
                            options={modules.map((m) => ({
                                value: String(m.id),
                                label: m.description,
                            }))}
                        />

                        <GenericSelectFloating
                            key={`clazz-${clazzOptions.length}-${form.clazz}`}
                            className="rc-select"
                            name="clazz"
                            label="Columna"
                            value={form.clazz}
                            onChange={updateField}
                            options={clazzOptions}
                            fullWidth
                        />

                        <GenericSelectFloating
                            key={`progress-${progressOptions.length}-${form.progress}`}
                            className="rc-select"
                            name="progress"
                            label="Progreso"
                            value={form.progress}
                            onChange={updateField}
                            options={progressOptions}
                            fullWidth
                        />

                        <GenericSelectFloating
                            key={`priority-${priorityOptions.length}-${form.priority}`}
                            className="rc-select"
                            name="priority"
                            label="Prioridad"
                            value={form.priority}
                            onChange={updateField}
                            options={priorityOptions}
                            fullWidth
                        />

                        <GenericSelectFloating
                            key={`repeatitiveness-${repeatitivenessOptions.length}-${form.repeatitiveness}`}
                            className="rc-select"
                            name="repeatitiveness"
                            label="Repetir"
                            value={form.repeatitiveness}
                            onChange={updateField}
                            options={repeatitivenessOptions}
                            fullWidth
                        />
                    </div>

                    <div className="rc-comments-header">
                        <div className="rc-label-bold">Comentarios existentes</div>
                        {totalComments > 3 && (
                            <GenericButton
                                variant="link"
                                className="rc-link"
                                onClick={() => setShowAllComments((v) => !v)}
                            >
                                {showAllComments ? 'Ver menos' : `Ver todos (${totalComments})`}
                            </GenericButton>
                        )}
                    </div>

                    <div className={showAllComments ? 'rc-comments-all' : 'rc-comments'}>
                        <CommentSummary comments={visibleComments} />
                    </div>

                    <div className="rc-label-bold">Agregar comentarios</div>

                    <div className="rc-top">
                        <GenericInput
                            onChange={updateComment}
                            value={comment}
                            name="comments"
                            label="Comentario"
                            placeholder="Comentario para la solicitud"
                            maxLength={300}
                            type="text"
                        />
                    </div>

                    <h3 className="rc-label-bold rc-top">Documentos actuales</h3>

                    <div className="rc-top">
                        <AttachmentSummary requestId={request.id} attachments={existingAttachments} />
                    </div>

                    <h3 className="rc-label-bold rc-top">Adjunta documentos y/o evidencias</h3>

                    <div className="rc-note">
                        Asegúrate de que los documentos sean legibles, estén bien iluminados y contengan solo una imagen por archivo.
                    </div>

                    <AttachmentUploader files={files} setFiles={setFiles} />
                </div>

                <div className="rc-actions">
                    <GenericButton
                        className="rc-link"
                        variant="text"
                        onClick={backCallback}
                    >
                        Volver
                    </GenericButton>

                    <GenericButton
                        className="rc-inline"
                        variant="outline"
                        onClick={rejectRequest}
                    >
                        Rechazar
                    </GenericButton>

                    <GenericButton
                        className="rc-inline rc-ml"
                        variant="primary"
                        onClick={handleSubmit}
                    >
                        Enviar
                    </GenericButton>
                </div>
            </div>
        );
    }

    switch (state) {
        case STATE_LOADING:
        case STATE_POSTING:
            return (
                <div>
                    <GenericLinearProgress indeterminate value={1} max={3} buffer={1.5} fullWidth />
                </div>
            );

        case STATE_LOADED:
            return <div className="rc-padding">{build()}</div>;

        case STATE_DETAIL:
            return (
                <div className="rc-padding">
                    <RequestDetail
                        request={request}
                        backCallback={() => setState(STATE_LOADED)}
                        businessUnits={businessUnits}
                        details={details}
                        modules={modules}
                        reasons={reasons}
                    />
                </div>
            );
    }
}
