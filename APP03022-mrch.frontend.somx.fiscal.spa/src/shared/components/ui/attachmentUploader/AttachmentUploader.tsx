import { useRef, useState, useEffect } from 'react';
import './AttachmentUploader.css';
import trash from './AttachmentUploaderTrash.svg';
import view from './AttachmentUploaderView.svg';
import download from './AttachmentUploaderDownload.svg';
import icon from '@assets/AttachmentUploaderIcon.svg';

export type AttachmentFile = File & { err?: number };

export interface AttachmentUploaderProps {
  files?: AttachmentFile[];
  setFiles?: (files: AttachmentFile[]) => void;
  fileExtensions?: string[];
  fileSize?: number;
  filenameLength?: number;
  multiple?: boolean;
  className?: string;
}

import { getFileError, ERR_INVALID_TYPE, ERR_INVALID_SIZE, ERR_TOO_LONG_FILENAME } from "./attachmentHelpers";

function buildErr(err?: number): React.ReactNode {
  if (err === ERR_INVALID_TYPE) return <div className="fiscal-attachment-file-err-caption">Documento no soportado.</div>;
  if (err === ERR_INVALID_SIZE) return <div className="fiscal-attachment-file-err-caption">Documento excede el tamaño máximo permitido.</div>;
  if (err === ERR_TOO_LONG_FILENAME) return <div className="fiscal-attachment-file-err-caption">Nombre del archivo muy largo.</div>;
  return null;
}

export default function AttachmentUploader({
  files,
  setFiles,
  fileExtensions,
  fileSize,
  filenameLength,
  multiple,
  className = '',
}: AttachmentUploaderProps): React.ReactElement {
  const manualInputFile = useRef<HTMLInputElement>(null);

  const validFileExtensions =
    fileExtensions ?? ['zip', 'gif', 'xls', 'xlsx', 'pdf', 'jpg', 'jpeg', 'docx', 'doc', 'png', 'xml'];
  const maxFileSize = fileSize ?? 4_194_304;
  const maxFilenameLength = filenameLength ?? 64;

  const [previewFile, setPreviewFile] = useState<AttachmentFile | null>(null);
  const previewCanvas = useRef<HTMLCanvasElement>(null);
  const closePreview = (): void => setPreviewFile(null);

  useEffect(() => {
    if (!previewFile || !previewCanvas.current) return;
    (async () => {
      try {
        const bmp = await createImageBitmap(previewFile);
        const maxW = window.innerWidth * 0.8;
        const maxH = window.innerHeight * 0.8;
        let { width: w, height: h } = bmp;
        const ratio = Math.min(maxW / w, maxH / h, 1);
        w *= ratio;
        h *= ratio;
        const cv = previewCanvas.current;
        if (!cv) return;
        cv.width = w;
        cv.height = h;
        const ctx = cv.getContext('2d');
        if (ctx) {
          ctx.clearRect(0, 0, w, h);
          ctx.drawImage(bmp, 0, 0, w, h);
        }
      } catch (err) {
        console.error('Preview error', err);
      }
    })();
  }, [previewFile]);

  const formatSize = (b: number): string => {
    if (!b && b !== 0) return '';
    const e = Math.floor(Math.log(Math.max(b, 1)) / Math.log(1024));
    const d = (b / 1024 ** e).toFixed(e ? 2 : 0);
    return `${d} ${['B', 'KB', 'MB', 'GB', 'TB'][e]}`;
  };

  function addFiles(eventFiles: FileList | null): void {
    if (!eventFiles) return;
    const current = Array.isArray(files) ? files.slice() : [];
    const existingNames = new Set(current.map((c) => c.name));

    for (const ef of Array.from(eventFiles)) {
      if (!ef || existingNames.has(ef.name)) continue;
      (ef as AttachmentFile).err = getFileError(ef, validFileExtensions, maxFileSize, maxFilenameLength);
      existingNames.add(ef.name);
      current.push(ef as AttachmentFile);
    }

    setFiles?.(current);
    if (manualInputFile.current) manualInputFile.current.value = '';
  }

  const dropFiles = (e: React.DragEvent): void => {
    e.preventDefault();
    if (e?.dataTransfer?.files) addFiles(e.dataTransfer.files);
  };

  const removeFile = (name: string): void =>
    setFiles?.((Array.isArray(files) ? files : []).filter((f) => f.name !== name));

  const downloadFile = (file: AttachmentFile): void => {
    const url = URL.createObjectURL(file);
    const a = document.createElement('a');
    a.href = url;
    a.download = file.name;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
  };

  const acceptAttr = validFileExtensions.map((ext) => `.${ext}`).join(',');
  const list = files ?? [];
  const actionDisabled = !multiple && list.length > 0;
  const rootClass = `fiscal-attachment-main ${className}`.trim();
  const actionClass = `fiscal-attachment-action ${actionDisabled ? 'grayscale' : ''}`.trim();

  return (
    <div className={rootClass}>
      <button
        type="button"
        className={actionClass}
        onDragOver={(e) => e.preventDefault()}
        onDrop={dropFiles}
        onClick={() => manualInputFile.current?.click()}
        onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') manualInputFile.current?.click(); }}
      >
        <input
          ref={manualInputFile}
          type="file"
          className="fiscal-attachment-hidden"
          accept={acceptAttr}
          onChange={(e) => addFiles(e.target.files)}
          disabled={actionDisabled}
          multiple={!!multiple}
        />
        <div className="fiscal-attachment-caption-icon">
          <img src={icon} alt="" />
        </div>
        <div className="fiscal-attachment-caption-text">Arrastra y suelta el archivo</div>
        <div className="fiscal-attachment-caption-text fiscal-attachment-caption-text-alt">
          o haz clic aquí para explorar
        </div>
        <div className="fiscal-attachment-caption-footer">
          <div>Formatos soportados: {validFileExtensions.join(', ').toUpperCase()}.</div>
          <div>Peso máximo por archivo: {formatSize(maxFileSize)}.</div>
        </div>
      </button>

      <div className="fiscal-attachment-files">
        {list.map((f) => (
          <div
            key={f.name}
            className={`fiscal-attachment-file-container ${f.err ? 'fiscal-attachment-file-err' : ''}`}
          >
            <div className="fiscal-attachment-file-name">{f.name}</div>
            <div className="fiscal-attachment-file-size">{formatSize(f.size)}</div>

            {!f.err ? (
              <>
                <div
                  className="fiscal-attachment-file-view"
                  role="button"
                  tabIndex={0}
                  onClick={() => setPreviewFile(f)}
                  onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') setPreviewFile(f); }}
                  title="Ver"
                >
                  <img src={view} alt="Ver" />
                </div>
                <div
                  className="fiscal-attachment-file-download"
                  role="button"
                  tabIndex={0}
                  onClick={() => downloadFile(f)}
                  onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') downloadFile(f); }}
                  title="Descargar"
                >
                  <img src={download} alt="Descargar" />
                </div>
              </>
            ) : null}

            <div
              className="fiscal-attachment-file-delete"
              role="button"
              tabIndex={0}
              onClick={() => removeFile(f.name)}
              onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') removeFile(f.name); }}
              title="Eliminar"
            >
              <img src={trash} alt="Borrar" />
            </div>

            {buildErr(f.err)}
          </div>
        ))}
      </div>

      {previewFile ? (
        <div
          className="fiscal-attachment-modal-overlay"
          onClick={closePreview}
          onKeyDown={(e) => { if (e.key === 'Escape') closePreview(); }}
        >
          <dialog open className="fiscal-attachment-modal" aria-modal="true" onClick={(e) => e.stopPropagation()}>
            <button type="button" className="fiscal-attachment-modal-close" onClick={closePreview}>
              ×
            </button>
            <canvas ref={previewCanvas} className="fiscal-attachment-modal-canvas" />
          </dialog>
        </div>
      ) : null}
    </div>
  );
}
