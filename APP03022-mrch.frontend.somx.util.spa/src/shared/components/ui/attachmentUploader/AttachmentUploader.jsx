import { useRef, useState, useEffect } from 'react';
import './AttachmentUploader.css';

import trash from './AttachmentUploaderTrash.svg';     
import view from './AttachmentUploaderView.svg';       
import download from './AttachmentUploaderDownload.svg';   
import icon from '@assets/AttachmentUploaderIcon.svg'; 

export default function AttachmentUploader({
    files,
    setFiles,
    fileExtensions,
    fileSize,
    filenameLength,
    multiple,
}) {
    const manualInputFile = useRef(null);

    const validFileExtensions =
        fileExtensions ??
        ['zip', 'gif', 'xls', 'xlsx', 'pdf', 'jpg', 'jpeg', 'docx', 'doc', 'png', 'xml'];
    const maxFileSize = fileSize ?? 4_194_304; 
    const maxFilenameLength = filenameLength ?? 64;

    const ERR_INVALID_TYPE = 1;
    const ERR_INVALID_SIZE = 2;
    const ERR_TOO_LONG_FILENAME = 3;

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

    const formatSize = b => {
        if (!b && b !== 0) return '';
        const e = Math.floor(Math.log(Math.max(b, 1)) / Math.log(1024));
        const d = (b / 1024 ** e).toFixed(e ? 2 : 0);
        return `${d} ${['B', 'KB', 'MB', 'GB', 'TB'][e]}`;
    };

    function hasValidExtension(name) {
        const lower = name.toLowerCase();
        return validFileExtensions.some(ext => lower.endsWith(`.${ext}`));
    }

    function computeFileError(ef) {
        if (!hasValidExtension(ef.name)) return ERR_INVALID_TYPE;
        if (ef.size > maxFileSize) return ERR_INVALID_SIZE;
        if (ef.name.length > maxFilenameLength) return ERR_TOO_LONG_FILENAME;
        return 0;
    }

    function addFiles(eventFiles) {
        const current = Array.isArray(files) ? files.slice() : [];
        const existingNames = new Set(current.map(c => c.name));

        for (const ef of Array.from(eventFiles || [])) {
            if (!ef) continue;
            if (existingNames.has(ef.name)) continue;
            ef.err = computeFileError(ef);
            existingNames.add(ef.name);
            current.push(ef);
        }

        setFiles(current);
        if (manualInputFile.current) manualInputFile.current.value = '';
    }

    const dropFiles = e => {
        e.preventDefault();
        if (e?.dataTransfer?.files) addFiles(e.dataTransfer.files);
    };
    const removeFile = name => setFiles((Array.isArray(files) ? files : []).filter(f => f.name !== name));

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
        if (err === ERR_INVALID_TYPE) return <div className="file-err-caption">Documento no soportado.</div>;
        if (err === ERR_INVALID_SIZE) return <div className="file-err-caption">Documento excede el tamaño máximo permitido.</div>;
        if (err === ERR_TOO_LONG_FILENAME) return <div className="file-err-caption">Nombre del archivo muy largo.</div>;
        return null;
    };

    const acceptAttr = validFileExtensions.map(ext => `.${ext}`).join(',');
    const hasFiles = Boolean(files?.length);

    return (
        <div className="main">
            <div
                className={!multiple && hasFiles ? 'action grayscale' : 'action'}
                role="button"
                tabIndex={0}
                onDragOver={e => e.preventDefault()}
                onDrop={dropFiles}
                onClick={() => manualInputFile.current?.click()}
                onKeyDown={e => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); manualInputFile.current?.click(); } }}
            >
                <input
                    ref={manualInputFile}
                    type="file"
                    style={{ display: 'none' }}
                    accept={acceptAttr}
                    onChange={(e) => addFiles(e.target.files)}
                    disabled={!multiple && (files?.length ? true : false)}
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

            <div className="files">
                {(files || []).map(f => (
                    <div key={f.name} className={`file-container ${f.err ? 'file-err' : ''}`}>
                        <div className="file-name">{f.name}</div>
                        <div className="file-size">{formatSize(f.size)}</div>

                        {!f.err && (
                            <>
                                <div className="file-view" role="button" tabIndex={0} onClick={() => setPreviewFile(f)} onKeyDown={e => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); setPreviewFile(f); } }} title="Ver">
                                    <img src={view} alt="Ver" />
                                </div>
                                <div className="file-download" role="button" tabIndex={0} onClick={() => downloadFile(f)} onKeyDown={e => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); downloadFile(f); } }} title="Descargar">
                                    <img src={download} alt="Descargar" />
                                </div>
                            </>
                        )}

                        <div className="file-delete" role="button" tabIndex={0} onClick={() => removeFile(f.name)} onKeyDown={e => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); removeFile(f.name); } }} title="Eliminar">
                            <img src={trash} alt="Borrar" />
                        </div>

                        {buildErr(f.err)}
                    </div>
                ))}
            </div>

            {previewFile && (
                <div className="au-modal-overlay" role="button" tabIndex={0} aria-label="Cerrar vista previa" onClick={closePreview} onKeyDown={e => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); closePreview(); } }}>
                    <div className="au-modal" onClick={e => e.stopPropagation()}>
                        <button className="au-close" onClick={closePreview}>×</button>
                        <canvas ref={previewCanvas} className="au-canvas" />
                    </div>
                </div>
            )}
        </div>
    );
}
