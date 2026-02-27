// src/features/faq/components/parts/AttachmentsBlock.jsx
import { useMemo, useState, useCallback } from 'react';
import AttachmentUploader from '@shared/components/ui/attachmentUploader/AttachmentUploader';

import './styles/AttachmentsBlock.css';
import './styles/AttachmentUploaderFix.css';

const TEN_MB = 10 * 1024 * 1024;

export default function AttachmentsBlock({
    files,
    setFiles,
    existingAttachments = [],
    onDownload,
    onChangeRemoveIds,
    maxFiles = 3,
}) {
    const [toRemove, setToRemove] = useState(new Set());

    const setFilesLimited = useCallback((arr) => {
        const next = (arr ?? []).filter(Boolean).slice(0, maxFiles);
        setFiles(next);
    }, [setFiles, maxFiles]);

    const toggleRemove = (rawId) => {
        const id = Number(rawId);
        const next = new Set(toRemove);
        if (next.has(id)) next.delete(id); else next.add(id);

        setToRemove(next);
        onChangeRemoveIds?.([...next]);
    };

    const totalBytes = useMemo(() => {
        const existingBytes = existingAttachments
            .filter(a => !toRemove.has(Number(a.id)))
            .reduce((acc, a) => acc + (a.sizeKb || 0) * 1024, 0);

        const newBytes = (files || [])
            .reduce((acc, f) => acc + (f?.size || 0), 0);

        return existingBytes + newBytes;
    }, [existingAttachments, toRemove, files]);

    const overQuota = totalBytes > TEN_MB;

    const fmt = (b) => {
        if (!b) return '0 B';
        const e = Math.floor(Math.log(b) / Math.log(1024));
        const d = (b / 1024 ** e).toFixed(e ? 2 : 0);
        return `${d} ${['B', 'KB', 'MB', 'GB', 'TB'][e]}`;
    };

    return (
        <div className="attachments-block">
            <AttachmentUploader
                files={files}
                setFiles={setFilesLimited}
                fileExtensions={['pdf', 'xls', 'xlsx', 'mp4', 'jpg', 'jpeg', 'png', 'gif']}
                fileSize={TEN_MB}
                multiple
            />

            {/* Total */}
            <div className="att-total-row">
                <span className={`att-total ${overQuota ? 'att-total-error' : ''}`}>
                    Total: {fmt(totalBytes)} / 10 MB
                </span>

                {overQuota && (
                    <div className="att-error-msg">
                        Supera el límite de 10 MB. Elimina algunos archivos antes de guardar.
                    </div>
                )}
            </div>

            {/* Lista de archivos existentes */}
            {existingAttachments.length > 0 && (
                <ul className="att-list">
                    {existingAttachments.map((a) => {
                        const id = Number(a.id);
                        const marked = toRemove.has(id);

                        return (
                            <li
                                key={id}
                                className={`att-item ${marked ? 'att-item-warning' : ''}`}
                            >
                                <div className="att-item-left">
                                    <input
                                        type="checkbox"
                                        checked={marked}
                                        onChange={() => toggleRemove(id)}
                                        title="Marcar para eliminar"
                                    />

                                    <button
                                        type="button"
                                        onClick={() => onDownload?.(a)}
                                        className={`att-file ${marked ? 'att-file-marked' : ''}`}
                                        title="Descargar"
                                    >
                                        {a.fileName}
                                    </button>
                                </div>

                                <span className={`att-size ${marked ? 'att-size-marked' : ''}`}>
                                    ({Math.max(1, Math.round(a.sizeKb))} KB)
                                </span>
                            </li>
                        );
                    })}
                </ul>
            )}
        </div>
    );
}
