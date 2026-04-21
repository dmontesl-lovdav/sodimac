import { useRef, useEffect, useState } from 'react';
import uploadIcon from '@assets/AttachmentUploaderIcon.svg';
import './GenericDropzone.css';

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
            if (!e.relatedTarget || !el.contains(e.relatedTarget)) {
                setDragOver(false);
            }
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
                onClick={openDialog}
                className={`generic-dropzone ${dragOver ? 'drag-over' : ''} ${className}`}
            >
                <input
                    ref={inputRef}
                    type="file"
                    accept={accept}
                    onChange={handleInputChange}
                    onClick={(e) => e.stopPropagation()}
                    className="generic-dropzone-input"
                    aria-label="Seleccionar archivo"
                />

                {children ?? (
                    <div className="generic-dropzone-content">
                        <img
                            src={uploadIcon}
                            alt=""
                            className="generic-dropzone-icon"
                        />
                        <p className="generic-dropzone-title">
                            Arrastra y suelta el archivo <br />
                            <span className="generic-dropzone-link">
                                o haz clic aquí para explorar
                            </span>
                        </p>
                        <p className="generic-dropzone-hint">
                            Formatos soportados: {acceptText}. Peso máximo: {maxSizeMb} mb.
                        </p>
                    </div>
                )}
            </div>

            {file && (
                <div className="generic-dropzone-file">
                    <span className="generic-dropzone-filename">{file.name}</span>
                    <button
                        type="button"
                        onClick={handleClear}
                        className="generic-dropzone-remove"
                        title="Eliminar archivo"
                        aria-label="Eliminar archivo"
                    >
                        🗑
                    </button>
                </div>
            )}
        </>
    );
}
