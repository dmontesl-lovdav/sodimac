
import { useRef, useEffect, useState } from 'react';
import uploadIcon from '@assets/AttachmentUploaderIcon.svg';

export default function GenericDropzone({
  file,
  onFileSelect,
  accept = '',
  maxSizeMb = 10,
  className = '',
  children,
}) {
  const boxRef = useRef(null);
  const inputRef = useRef(null);
  const [dragOver, setDragOver] = useState(false);

  useEffect(() => {
    const el = boxRef.current;
    if (!el) return;

    const stop = (e) => {
      e.preventDefault();
      e.stopPropagation();
    };
    const onDragOver = (e) => {
      stop(e);
      setDragOver(true);
    };
    const onDragLeave = (e) => {
      stop(e);
      if (!e.relatedTarget || !el.contains(e.relatedTarget)) setDragOver(false);
    };
    const onDrop = (e) => {
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

  const handleInputChange = (e) => {
    const f = e.target.files?.[0] ?? null;
    onFileSelect?.(f);
    e.target.value = '';
  };

  const handleClear = () => {
    onFileSelect?.(null);
    if (inputRef.current) inputRef.current.value = '';
  };

  const openDialog = () => inputRef.current?.click();

  const acceptText =
    accept ||
    '.xlsx, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel';

  return (
    <>
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
        className={`somx-dropzone ${dragOver ? 'somx-dropzone-dragover' : ''} ${className}`}
      >
        <input
          ref={inputRef}
          type="file"
          accept={accept}
          className="somx-dropzone-input"
          onChange={handleInputChange}
          onClick={(e) => e.stopPropagation()}
          aria-label="Seleccionar archivo"
        />

        {children ?? (
          <div className="somx-dropzone-content">
            <img src={uploadIcon} alt="" className="somx-dropzone-icon" />
            <p className="somx-dropzone-text">
              Arrastra y suelta el archivo <br />
              <span className="somx-dropzone-highlight">o haz clic aquí para explorar</span>
            </p>
            <p className="somx-dropzone-subtext">
              Formatos soportados: {acceptText}. Peso máximo: {maxSizeMb}&nbsp;mb.
            </p>
          </div>
        )}
      </div>

      {file && (
        <div className="somx-dropzone-file">
          <span className="truncate">{file.name}</span>
          <button
            type="button"
            className="somx-dropzone-clear-btn"
            onClick={handleClear}
            aria-label="Eliminar archivo"
            title="Eliminar archivo"
          >
            🗑
          </button>
        </div>
      )}
    </>
  );
}
