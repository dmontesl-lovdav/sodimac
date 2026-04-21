import { useRef, useEffect, useState } from 'react';
import uploadIcon from '@assets/AttachmentUploaderIcon.svg';
import './Dropzone.css';

export interface GenericDropzoneProps {
  file?: File | null;
  onFileSelect?: (file: File | null) => void;
  accept?: string;
  maxSizeMb?: number;
  className?: string;
  children?: React.ReactNode;
  fileInfoPosition?: 'above' | 'below';
}

const fileInfoBlock = (file: File, onClear: () => void) => (
  <div className="fiscal-dropzone-file-info">
    <span className="fiscal-dropzone-file-name">{file.name}</span>
    <button
      type="button"
      className="fiscal-dropzone-clear-btn"
      onClick={onClear}
      aria-label="Eliminar archivo"
      title="Eliminar archivo"
    >
      🗑
    </button>
  </div>
);

export default function GenericDropzone({
  file,
  onFileSelect,
  accept = '',
  maxSizeMb = 10,
  className = '',
  children,
  fileInfoPosition = 'below',
}: GenericDropzoneProps): React.ReactElement {
  const boxRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);
  const [dragOver, setDragOver] = useState(false);

  useEffect(() => {
    const el = boxRef.current;
    if (!el) return;

    const stop = (e: DragEvent): void => {
      e.preventDefault();
      e.stopPropagation();
    };
    const onDragOver = (e: DragEvent): void => {
      stop(e);
      setDragOver(true);
    };
    const onDragLeave = (e: DragEvent): void => {
      stop(e);
      if (!e.relatedTarget || !el.contains(e.relatedTarget as Node)) setDragOver(false);
    };
    const onDrop = (e: DragEvent): void => {
      stop(e);
      setDragOver(false);
      const f = e.dataTransfer?.files?.[0] ?? null;
      if (f) onFileSelect?.(f);
      if (inputRef.current) inputRef.current.value = '';
    };

    el.addEventListener('dragover', onDragOver);
    el.addEventListener('dragleave', onDragLeave);
    el.addEventListener('drop', onDrop);
    document.addEventListener('dragover', stop);
    document.addEventListener('drop', stop);

    return () => {
      el.removeEventListener('dragover', onDragOver);
      el.removeEventListener('dragleave', onDragLeave);
      el.removeEventListener('drop', onDrop);
      document.removeEventListener('dragover', stop);
      document.removeEventListener('drop', stop);
    };
  }, [onFileSelect]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    const f = e.target.files?.[0] ?? null;
    onFileSelect?.(f);
    e.target.value = '';
  };

  const handleClear = (): void => {
    onFileSelect?.(null);
    if (inputRef.current) inputRef.current.value = '';
  };

  const openDialog = (): void => inputRef.current?.click();

  const acceptText =
    accept ||
    '.xlsx, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel';

  const boxClass = `fiscal-dropzone ${dragOver ? 'fiscal-dropzone-dragOver' : ''} ${className}`.trim();

  const fileInfo = file ? fileInfoBlock(file, handleClear) : null;

  return (
    <>
      {fileInfoPosition === 'above' && fileInfo}
      <div
        ref={boxRef}
        role="button"
        tabIndex={0}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            openDialog();
          }
        }}
        className={boxClass}
      >
        <input
          ref={inputRef}
          type="file"
          accept={accept}
          className="fiscal-dropzone-hidden-input"
          onChange={handleInputChange}
          onClick={(e) => e.stopPropagation()}
          aria-label="Seleccionar archivo"
        />

        {children ?? (
          <div className="fiscal-dropzone-content">
            <img src={uploadIcon} alt="" className="fiscal-dropzone-icon" />
            <p className="fiscal-dropzone-text">
              Arrastra y suelta el archivo <br />
              <span className="fiscal-dropzone-link">o haz clic aquí para explorar</span>
            </p>
            <p className="fiscal-dropzone-support">
              Formatos soportados: {acceptText}. Peso máximo: {maxSizeMb}&nbsp;mb.
            </p>
          </div>
        )}
      </div>
      {fileInfoPosition === 'below' && fileInfo}
    </>
  );
}
