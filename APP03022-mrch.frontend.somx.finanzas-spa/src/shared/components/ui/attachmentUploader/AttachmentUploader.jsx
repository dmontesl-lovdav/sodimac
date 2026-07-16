// src/shared/components/ui/attachmentUploader/AttachmentUploader.jsx
import { useRef, useState, useEffect } from 'react';
import './AttachmentUploader.css';

/* íconos */
import trash from './AttachmentUploaderTrash.svg';      // 🗑️ eliminar
import view from './AttachmentUploaderView.svg';       // 👁️ ver
import download from './AttachmentUploaderDownload.svg';   // ⬇️ descargar
import icon from '@assets/AttachmentUploaderIcon.svg'; // ☁️ drag-&-drop

const ERR_INVALID_TYPE = 1;
const ERR_INVALID_SIZE = 2;
const ERR_TOO_LONG_FILENAME = 3;

const ERR_CAPTIONS = {
    [ERR_INVALID_TYPE]: 'Documento no soportado.',
    [ERR_INVALID_SIZE]: 'Documento excede el tamaño máximo permitido.',
    [ERR_TOO_LONG_FILENAME]: 'Nombre del archivo muy largo.',
};

function getFileValidationError(file, validFileExtensions, maxFileSize, maxFilenameLength) {
    const lower = file.name.toLowerCase();
    const hasValidExt = validFileExtensions.some((ext) => lower.endsWith(`.${ext}`));
    if (!hasValidExt) return ERR_INVALID_TYPE;
    if (file.size > maxFileSize) return ERR_INVALID_SIZE;
    if (file.name.length > maxFilenameLength) return ERR_TOO_LONG_FILENAME;
    return 0;
}

function handleActivate(e, action) {
    if (e.key === 'Enter' || e.key === ' ') {
        e.preventDefault();
        action();
    }
}

export default function AttachmentUploader({
    files,
    setFiles,
    fileExtensions,
    fileSize,
    filenameLength,
    multiple,
}) {
    /* ---------- refs & consts ---------- */
    const manualInputFile = useRef(null);

    const validFileExtensions =
        fileExtensions ??
        ['zip', 'gif', 'xls', 'xlsx', 'pdf', 'jpg', 'jpeg', 'docx', 'doc', 'png', 'xml'];
    const maxFileSize = fileSize ?? 4_194_304; // 4 MB por archivo
    const maxFilenameLength = filenameLength ?? 64;

    /* ---------- modal preview (canvas) ---------- */
    const [previewFile, setPreviewFile] = useState(null);
    const previewCanvas = useRef(null);
    const closePreview = () => setPreviewFile(null);

    useEffect(() => {
        if (!previewFile || !previewCanvas.current) return;

        (async () => {
            try {
                const bmp = await createImageBitmap(previewFile);
                const maxW = window.innerWidth * 0.8;
                const maxH = window.innerHeight * 0.8;
                let { width: w, height: h } = bmp;
                const ratio = Math.min(maxW / w, maxH / h, 1);
                w *= ratio; h *= ratio;

                const cv = previewCanvas.current;
                cv.width = w;
                cv.height = h;
                const ctx = cv.getContext('2d');
                ctx.clearRect(0, 0, w, h);
                ctx.drawImage(bmp, 0, 0, w, h);
            } catch (err) { console.error('Preview error', err); }
        })();
    }, [previewFile]);

    /* ---------- helpers ---------- */
    const formatSize = b => {
        if (!b && b !== 0) return '';
        const e = Math.floor(Math.log(Math.max(b, 1)) / Math.log(1024));
        const d = (b / 1024 ** e).toFixed(e ? 2 : 0);
        return `${d} ${['B', 'KB', 'MB', 'GB', 'TB'][e]}`;
    };

    function addFiles(eventFiles) {
        const current = Array.isArray(files) ? files.slice() : [];
        const existingNames = new Set(current.map(c => c.name));

        for (const ef of Array.from(eventFiles || [])) {
            if (!ef || existingNames.has(ef.name)) continue;

            ef.err = getFileValidationError(
                ef,
                validFileExtensions,
                maxFileSize,
                maxFilenameLength
            );

            existingNames.add(ef.name);
            current.push(ef);
        }

        setFiles(current);
        // limpiar el input para que pueda volver a escoger el mismo nombre si lo borró
        if (manualInputFile.current) manualInputFile.current.value = '';
    }

    const dropFiles = e => {
        e.preventDefault();
        if (e?.dataTransfer?.files) addFiles(e.dataTransfer.files);
    };
    const removeFile = name => setFiles((Array.isArray(files) ? files : []).filter(f => f.name !== name));

    /* descargar (local) */
    const downloadFile = file => {
        const url = URL.createObjectURL(file);
        const a = document.createElement('a');
        a.href = url; a.download = file.name;
        document.body.appendChild(a);
        a.click();
        a.remove();
        URL.revokeObjectURL(url);
    };

    const buildErr = err => {
        const caption = ERR_CAPTIONS[err];
        return caption ? <div className="file-err-caption">{caption}</div> : null;
    };

    const acceptAttr = validFileExtensions.map(ext => `.${ext}`).join(',');
    const isUploadDisabled = !multiple && Boolean(files?.length);
    const openFilePicker = () => manualInputFile.current?.click();

    /* ---------- UI ---------- */
    return (
        <div className="main">
            {/* drag & drop */}
            <div
                className={isUploadDisabled ? 'action grayscale' : 'action'}
                role="button"
                tabIndex={isUploadDisabled ? -1 : 0}
                onDragOver={e => e.preventDefault()}
                onDrop={dropFiles}
                onClick={openFilePicker}
                onKeyDown={e => handleActivate(e, openFilePicker)}
            >
                <input
                    ref={manualInputFile}
                    type="file"
                    style={{ display: 'none' }}
                    accept={acceptAttr}
                    onChange={(e) => addFiles(e.target.files)}
                    disabled={isUploadDisabled}
                    multiple={!!multiple}
                />
                <div className="caption-icon"><img src={icon} alt="" /></div>
                <div className="caption-text">Arrastra y suelta el archivo</div>
                <div className="caption-text caption-text-alt">o haz clic aquí para explorar</div>
                <div className="caption-footer">
                    <div className="pl-5 pr-5">Formatos soportados: {validFileExtensions.join(', ').toUpperCase()}.</div>
                    <div className="pb-5">Peso máximo por archivo: {formatSize(maxFileSize)}.</div>
                </div>
            </div>

            {/* lista */}
            <div className="files">
                {(files || []).map(f => (
                    <div key={f.name} className={`file-container ${f.err ? 'file-err' : ''}`}>
                        <div className="file-name">{f.name}</div>
                        <div className="file-size">{formatSize(f.size)}</div>

                        {!f.err && (
                            <>
                                <button
                                    type="button"
                                    className="file-view"
                                    onClick={() => setPreviewFile(f)}
                                    title="Ver"
                                    aria-label="Ver"
                                >
                                    <img src={view} alt="" />
                                </button>
                                <button
                                    type="button"
                                    className="file-download"
                                    onClick={() => downloadFile(f)}
                                    title="Descargar"
                                    aria-label="Descargar"
                                >
                                    <img src={download} alt="" />
                                </button>
                            </>
                        )}

                        <button
                            type="button"
                            className="file-delete"
                            onClick={() => removeFile(f.name)}
                            title="Eliminar"
                            aria-label="Eliminar"
                        >
                            <img src={trash} alt="" />
                        </button>

                        {buildErr(f.err)}
                    </div>
                ))}
            </div>

            {/* modal */}
            {previewFile && (
                <div
                    className="au-modal-overlay"
                    role="button"
                    tabIndex={0}
                    onClick={closePreview}
                    onKeyDown={e => handleActivate(e, closePreview)}
                >
                    <div
                        className="au-modal"
                        role="dialog"
                        aria-modal="true"
                        onClick={e => e.stopPropagation()}
                        onKeyDown={e => e.stopPropagation()}
                    >
                        <button className="au-close" onClick={closePreview}>×</button>
                        <canvas ref={previewCanvas} className="au-canvas" />
                    </div>
                </div>
            )}
        </div>
    );
}
