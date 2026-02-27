// src/features/helpCenter/HelpCenterFaqDetail.jsx
import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { getFaq } from '@/features/faq/api/faqService';
import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';
import { GenericModal } from '@shared/components/ui';
import { getRelatedInformation } from '@/features/relatedInformation/api/relatedInformationService';
import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';
import { createFeedback } from '@/features/feedback/api';

import thumbUpIcon from '@assets/icons/thumb-up.png';
import thumbDownIcon from '@assets/icons/thumb-down.png';
import documentIcon from '@assets/icons/document.png';
import imgIcon from '@assets/icons/img.png';
import mp4Icon from '@assets/icons/mp4.png';

import "./HelpCenterFaqDetail.css";

export default function HelpCenterFaqDetail() {

    const [faq, setFaq] = useState(null);
    const [related, setRelated] = useState([]);
    const [loading, setLoading] = useState(true);
    const [opLoading, setOpLoading] = useState(false);
    const [isDownloading, setIsDownloading] = useState(false);

    const [thanksModal, setThanksModal] = useState({
        visible: false, title: "", message: "", severity: "success"
    });

    const [isCommenting, setIsCommenting] = useState(false);
    const [comment, setComment] = useState("");
    const [sending, setSending] = useState(false);

    const [voteModal, setVoteModal] = useState({ visible: false, type: null });
    const [voted, setVoted] = useState(null);

    const nav = useNavigate();
    const [params] = useSearchParams();

    const faqId = Number(params.get("faqId"));
    const categoryId = params.get("categoryId") || "";
    const categoryName = decodeURIComponent(params.get("categoryName") || "");

    const client = useMemo(() => ConfigurationBuilder.client, []);

    // Load FAQ
    useEffect(() => {
        (async () => {
            try {
                setOpLoading(true);
                const data = await getFaq(faqId);
                setFaq(data);

                if (data?.relatedInfoIds?.length) {
                    const info = await getRelatedInformation({ size: 1000 });
                    setRelated(info.filter(r => data.relatedInfoIds.includes(r.id)));
                }

            } catch (err) {
                console.error("FAQ error:", err);
            } finally {
                setOpLoading(false);
                setLoading(false);
            }
        })();
    }, [faqId]);

    const breadcrumbItems = [
        { label: "Inicio", to: "/" },
        { label: "Centro de ayuda", to: "/help-center" },
        { label: "Preguntas frecuentes", to: `/help-center/faqs/category?categoryId=${categoryId}` },
        { label: categoryName || "Categoría", to: `/help-center/faqs/category?categoryId=${categoryId}` },
        { label: faq?.question || "Detalle" }
    ];

    const resolveDownloadPath = (att) =>
        att?.downloadUrl || att?.url || `/faqs/${faqId}/attachments/${att.id}`;

    const parseFilenameFromDisposition = (dispo) => {
        if (!dispo) return null;
        const m = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/i.exec(dispo);
        if (!m) return null;
        return decodeURIComponent(String(m[1]).replace(/['"]/g, ""));
    };

    const getApiBaseUrl = () =>
        client?.baseURL ||
        client?.baseUrl ||
        client?.options?.baseURL ||
        "";

    const getBearerToken = async () => {
        try { if (typeof client?.getAccessToken === "function") return await client.getAccessToken(); }
        catch { }
        return (
            localStorage.getItem("access_token") ||
            sessionStorage.getItem("access_token") ||
            ""
        );
    };

    const handleDownload = async (att) => {
        try {
            const path = resolveDownloadPath(att);
            if (!path) return;

            setIsDownloading(true);

            const base = getApiBaseUrl();
            const absoluteUrl = path.startsWith("http") ? path : `${base}${path}`;

            const token = await getBearerToken();
            const headers = new Headers();
            if (token) headers.set("Authorization", `Bearer ${token}`);

            const res = await fetch(absoluteUrl, { method: "GET", headers });

            if (!res.ok) throw new Error("Download error");

            const contentType = res.headers.get("content-type") || "application/octet-stream";
            const dispo = res.headers.get("content-disposition") || "";
            const filename =
                parseFilenameFromDisposition(dispo) ||
                att.fileName ||
                att.filename ||
                "archivo";

            const blob = new Blob([await res.arrayBuffer()], { type: contentType });

            const url = URL.createObjectURL(blob);
            const a = document.createElement("a");

            a.href = url;
            a.download = filename;
            a.click();
            a.remove();

            setTimeout(() => URL.revokeObjectURL(url), 1500);

        } catch (err) {
            console.error("Download error:", err);
        } finally {
            setIsDownloading(false);
        }
    };

    const getSizeKb = (a) => {
        const v = a?.sizeKb ?? (a.size ? a.size / 1024 : 0);
        return Math.max(1, Math.round(v || 0));
    };

    const handleVoteClick = (type) => {
        if (voted) return;
        setVoteModal({ visible: true, type });
    };

    const handleCommentClick = () => setIsCommenting(true);

    const handleSendComment = async () => {
        if (!comment.trim()) return;

        try {
            setSending(true);
            await createFeedback({
                question: faq?.question || "Sin pregunta",
                answers: [{ text: comment.trim(), position: 0 }]
            });

            setComment("");
            setIsCommenting(false);

            setThanksModal({
                visible: true,
                title: "Gracias",
                message: "Tu comentario ha sido enviado.",
                severity: "success"
            });

        } catch {
            setThanksModal({
                visible: true,
                title: "Error",
                message: "No se pudo enviar tu comentario.",
                severity: "error"
            });
        } finally {
            setSending(false);
        }
    };

    if (loading) return <GenericModal visible variant="loading" message="Cargando…" />;

    return (
        <main className="hcd-main">

            <div className="hcd-container">

                <div className="hcd-breadcrumb">
                    <Breadcrumb items={breadcrumbItems} />
                </div>

                <div className="hcd-card">
                    <h2 className="hcd-title">{faq?.question}</h2>
                    <p className="hcd-answer">{faq?.answer}</p>

                    {/* ATTACHMENTS */}
                    {faq?.attachments?.length > 0 && (
                        <>
                            <h3 className="hcd-subtitle">Documentos</h3>

                            <ul className="hcd-attachments-list">
                                {faq.attachments.map((a) => {
                                    const name = a.fileName || a.filename || "";
                                    const ext = name.toLowerCase();

                                    let icon = documentIcon;
                                    if (/\.(png|jpg|jpeg|gif)$/.test(ext)) icon = imgIcon;
                                    if (ext.endsWith(".mp4")) icon = mp4Icon;

                                    return (
                                        <li key={a.id ?? name} className="hcd-attachment-row">
                                            <div className="hcd-attachment-left">
                                                <img src={icon} className="hcd-attachment-icon" />

                                                <button
                                                    className="hcd-file-btn"
                                                    onClick={() => handleDownload(a)}
                                                >
                                                    {name}
                                                </button>
                                            </div>

                                            <span className="hcd-attachment-size">
                                                ({getSizeKb(a)} KB)
                                            </span>
                                        </li>
                                    );
                                })}
                            </ul>
                        </>
                    )}

                    {/* RELATED INFO */}
                    {related.length > 0 && (
                        <>
                            <h3 className="hcd-subtitle hcd-related-title">Información relacionada</h3>

                            <ul className="hcd-related-list">
                                {related.map((r) => (
                                    <li className="hcd-related-item" key={r.id}>
                                        {r.title || r.name}
                                    </li>
                                ))}
                            </ul>
                        </>
                    )}

                    {/* VOTE */}
                    <div className="hcd-vote-block">
                        <p className="hcd-vote-label">¿Te ayudó esta información?</p>

                        <div className="hcd-vote-row">
                            <button
                                disabled={!!voted}
                                className={`hcd-vote-btn ${voted === "yes" ? "hcd-vote-active" : ""}`}
                                onClick={() => handleVoteClick("yes")}
                            >
                                Sí <img src={thumbUpIcon} className="hcd-vote-icon" />
                            </button>

                            <button
                                disabled={!!voted}
                                className={`hcd-vote-btn ${voted === "no" ? "hcd-vote-active" : ""}`}
                                onClick={() => handleVoteClick("no")}
                            >
                                No <img src={thumbDownIcon} className="hcd-vote-icon" />
                            </button>
                        </div>
                    </div>

                    {/* COMMENTS */}
                    <div className="hcd-comment-block">

                        <h3 className="hcd-comment-title">Ayúdanos a mejorar</h3>
                        <p className="hcd-comment-desc">
                            Tu opinión es importante para nosotros.
                        </p>

                        {!isCommenting ? (
                            <button
                                onClick={handleCommentClick}
                                className="hcd-comment-start-btn"
                            >
                                Ingresa tu comentario
                            </button>
                        ) : (
                            <div className="hcd-comment-row">
                                <input
                                    className="hcd-comment-input"
                                    value={comment}
                                    onChange={(e) => setComment(e.target.value)}
                                    placeholder="Escribe tu comentario…"
                                />

                                <button
                                    onClick={handleSendComment}
                                    disabled={sending || !comment.trim()}
                                    className={`hcd-comment-send ${sending || !comment.trim()
                                        ? "hcd-disabled"
                                        : "hcd-comment-send-active"
                                        }`}
                                >
                                    {sending ? "Enviando…" : "Enviar"}
                                </button>
                            </div>
                        )}
                    </div>

                    {/* BACK BUTTON */}
                    <div className="hcd-back-wrapper">
                        <button
                            className="hcd-back-btn"
                            onClick={() => nav(`/help-center/faqs/category?categoryId=${categoryId}`)}
                        >
                            Volver
                        </button>
                    </div>
                </div>
            </div>

            {/* LOADING */}
            <GenericModal visible={opLoading} variant="loading" message="Procesando…" />
            <GenericModal visible={isDownloading} variant="loading" message="Descargando archivo…" />

            {/* VOTE MODAL */}
            {voteModal.visible && (
                <GenericModal
                    visible
                    variant="confirm"
                    title="Confirmar voto"
                    message={
                        voteModal.type === "yes"
                            ? "¿Confirmas tu voto positivo?"
                            : "¿Confirmas tu voto negativo?"
                    }
                    confirmText="Aceptar"
                    cancelText="Cancelar"
                    onConfirm={() => {
                        setVoted(voteModal.type);
                        setVoteModal({ visible: false, type: null });
                    }}
                    onCancel={() => setVoteModal({ visible: false, type: null })}
                />
            )}

            {/* THANKS MODAL */}
            {thanksModal.visible && (
                <GenericModal
                    visible
                    variant="alert"
                    title={thanksModal.title}
                    message={thanksModal.message}
                    severity={thanksModal.severity}
                    onClose={() =>
                        setThanksModal({
                            visible: false,
                            title: "",
                            message: "",
                            severity: "success"
                        })
                    }
                />
            )}
        </main>
    );
}
