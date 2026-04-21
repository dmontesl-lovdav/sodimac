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

const ERR_INVALID_TYPE = 1;
const ERR_INVALID_SIZE = 2;
const ERR_TOO_LONG_FILENAME = 3;

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
    const current = Array.isArray(files) ? files.slice() : [];
    const existingNames = new Set(current.map((c) => c.name));

    if (!eventFiles) return;
    for (const ef of Array.from(eventFiles)) {
      if (!ef) continue;
      if (existingNames.has(ef.name)) continue;

      let err = ERR_INVALID_TYPE;
      const lower = ef.name.toLowerCase();
      for (const ext of validFileExtensions) {
        if (lower.endsWith(`.${ext}`)) {
          err = 0;
          break;
        }
      }
      if (ef.size > maxFileSize) err = ERR_INVALID_SIZE;
      if (ef.name.length > maxFilenameLength) err = ERR_TOO_LONG_FILENAME;

      (ef as AttachmentFile).err = err;
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

  const buildErr = (err?: number): React.ReactNode =>
    err === ERR_INVALID_TYPE ? (
      <div className="fiscal-attachment-file-err-caption">Documento no soportado.</div>
    ) : err === ERR_INVALID_SIZE ? (
      <div className="fiscal-attachment-file-err-caption">Documento excede el tamaño máximo permitido.</div>
    ) : err === ERR_TOO_LONG_FILENAME ? (
      <div className="fiscal-attachment-file-err-caption">Nombre del archivo muy largo.</div>
    ) : null;

  const acceptAttr = validFileExtensions.map((ext) => `.${ext}`).join(',');
  const list = files ?? [];
  const actionDisabled = !multiple && list.length > 0;
  const rootClass = `fiscal-attachment-main ${className}`.trim();
  const actionClass = `fiscal-attachment-action ${actionDisabled ? 'grayscale' : ''}`.trim();

  return (
    <div className={rootClass}>
      <div
        className={actionClass}
        onDragOver={(e) => e.preventDefault()}
        onDrop={dropFiles}
        onClick={() => manualInputFile.current?.click()}
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
      </div>

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
                  onClick={() => setPreviewFile(f)}
                  title="Ver"
                  role="button"
                >
                  <img src={view} alt="Ver" />
                </div>
                <div
                  className="fiscal-attachment-file-download"
                  onClick={() => downloadFile(f)}
                  title="Descargar"
                  role="button"
                >
                  <img src={download} alt="Descargar" />
                </div>
              </>
            ) : null}

            <div
              className="fiscal-attachment-file-delete"
              onClick={() => removeFile(f.name)}
              title="Eliminar"
              role="button"
            >
              <img src={trash} alt="Borrar" />
            </div>

            {buildErr(f.err)}
          </div>
        ))}
      </div>

      {previewFile ? (
        <div className="fiscal-attachment-modal-overlay" onClick={closePreview}>
          <div className="fiscal-attachment-modal" onClick={(e) => e.stopPropagation()}>
            <button type="button" className="fiscal-attachment-modal-close" onClick={closePreview}>
              ×
            </button>
            <canvas ref={previewCanvas} className="fiscal-attachment-modal-canvas" />
          </div>
        </div>
      ) : null}
    </div>
  );
}
