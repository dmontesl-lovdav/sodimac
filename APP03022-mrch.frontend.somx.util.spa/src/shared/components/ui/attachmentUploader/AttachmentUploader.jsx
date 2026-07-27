import { useRef, useState, useEffect } from 'react';
import PropTypes from 'prop-types';
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
            <input
                ref={manualInputFile}
                type="file"
                style={{ display: 'none' }}
                accept={acceptAttr}
                onChange={(e) => addFiles(e.target.files)}
                disabled={!multiple && hasFiles}
                multiple={!!multiple}
            />
            <button
                type="button"
                className={!multiple && hasFiles ? 'action grayscale' : 'action'}
                onDragOver={e => e.preventDefault()}
                onDrop={dropFiles}
                onClick={() => manualInputFile.current?.click()}
                style={{ appearance: 'none', font: 'inherit', color: 'inherit', textAlign: 'inherit', cursor: 'pointer' }}
            >
                <div className="caption-icon"><img src={icon} alt="" /></div>
                <div className="caption-text">Arrastra y suelta el archivo</div>
                <div className="caption-text caption-text-alt">o haz clic aquí para explorar</div>
                <div className="caption-footer">
                    <div className="pl-5 pr-5">Formatos soportados: {validFileExtensions.join(', ').toUpperCase()}.</div>
                    <div className="pb-5">Peso máximo por archivo: {formatSize(maxFileSize)}.</div>
                </div>
            </button>

            <div className="files">
                {(files || []).map(f => (
                    <div key={f.name} className={`file-container ${f.err ? 'file-err' : ''}`}>
                        <div className="file-name">{f.name}</div>
                        <div className="file-size">{formatSize(f.size)}</div>

                        {!f.err && (
                            <>
                                <button type="button" className="file-view" onClick={() => setPreviewFile(f)} title="Ver" style={{ appearance: 'none', border: 'none', background: 'transparent', padding: 0, margin: 0, cursor: 'pointer' }}>
                                    <img src={view} alt="Ver" />
                                </button>
                                <button type="button" className="file-download" onClick={() => downloadFile(f)} title="Descargar" style={{ appearance: 'none', border: 'none', background: 'transparent', padding: 0, margin: 0, cursor: 'pointer' }}>
                                    <img src={download} alt="Descargar" />
                                </button>
                            </>
                        )}

                        <button type="button" className="file-delete" onClick={() => removeFile(f.name)} title="Eliminar" style={{ appearance: 'none', border: 'none', background: 'transparent', padding: 0, margin: 0, cursor: 'pointer' }}>
                            <img src={trash} alt="Borrar" />
                        </button>

                        {buildErr(f.err)}
                    </div>
                ))}
            </div>

            {previewFile && (
                <div className="au-modal-overlay">
                    <button
                        type="button"
                        className="au-modal-backdrop"
                        aria-label="Cerrar vista previa"
                        onClick={closePreview}
                        style={{ position: 'absolute', inset: 0, width: '100%', height: '100%', background: 'transparent', border: 'none', padding: 0, margin: 0, cursor: 'default' }}
                    />
                    <div className="au-modal" style={{ position: 'relative', zIndex: 1 }}>
                        <button className="au-close" onClick={closePreview}>×</button>
                        <canvas ref={previewCanvas} className="au-canvas" />
                    </div>
                </div>
            )}
        </div>
    );
}

AttachmentUploader.propTypes = {
    files: PropTypes.array,
    setFiles: PropTypes.func,
    fileExtensions: PropTypes.array,
    fileSize: PropTypes.number,
    filenameLength: PropTypes.number,
    multiple: PropTypes.bool,
};
