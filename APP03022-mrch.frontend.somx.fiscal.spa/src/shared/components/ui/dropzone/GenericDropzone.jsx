import { useRef, useEffect, useState } from 'react';
import uploadIcon from '@assets/AttachmentUploaderIcon.svg';

const styles = {
    dropzone: {
        position: 'relative',
        border: '2px dashed #d1d5db',
        borderRadius: '0.375rem',
        textAlign: 'center',
        padding: '3rem',
        cursor: 'pointer',
        transition: 'all 150ms ease',
    },
    dropzoneDragOver: {
        backgroundColor: '#f0f9ff',
        borderColor: '#7dd3fc',
    },
    dropzoneHover: {
        backgroundColor: '#f9fafb',
    },
    hiddenInput: {
        position: 'absolute',
        inset: 0,
        opacity: 0,
        cursor: 'pointer',
    },
    content: {
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: '0.5rem',
        pointerEvents: 'none',
        userSelect: 'none',
    },
    icon: {
        width: '2.5rem',
        height: '2.5rem',
    },
    text: {
        fontSize: '0.875rem',
    },
    link: {
        color: '#0284c7',
        textDecoration: 'underline',
    },
    supportText: {
        fontSize: '0.75rem',
        color: '#6b7280',
    },
    fileInfo: {
        marginTop: '1rem',
        backgroundColor: '#f0fdf4',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '0.5rem 1rem',
        borderRadius: '0.25rem',
        fontSize: '0.875rem',
    },
    fileName: {
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        whiteSpace: 'nowrap',
    },
    clearButton: {
        cursor: 'pointer',
        opacity: 0.7,
        background: 'none',
        border: 'none',
        padding: 0,
        fontSize: '1rem',
        transition: 'opacity 150ms',
    },
};

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
    const [isHovered, setIsHovered] = useState(false);

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

    const getDropzoneStyle = () => {
        let style = { ...styles.dropzone };
        if (dragOver) style = { ...style, ...styles.dropzoneDragOver };
        else if (isHovered) style = { ...style, ...styles.dropzoneHover };
        return style;
    };

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
                onMouseEnter={() => setIsHovered(true)}
                onMouseLeave={() => setIsHovered(false)}
                style={getDropzoneStyle()}
                className={className}
            >
                <input
                    ref={inputRef}
                    type="file"
                    accept={accept}
                    style={styles.hiddenInput}
                    onChange={handleInputChange}
                    onClick={(e) => e.stopPropagation()}
                    aria-label="Seleccionar archivo"
                />

                {children ?? (
                    <div style={styles.content}>
                        <img src={uploadIcon} alt="" style={styles.icon} />
                        <p style={styles.text}>
                            Arrastra y suelta el archivo <br />
                            <span style={styles.link}>o haz clic aquí para explorar</span>
                        </p>
                        <p style={styles.supportText}>
                            Formatos soportados: {acceptText}. Peso máximo: {maxSizeMb}&nbsp;mb.
                        </p>
                    </div>
                )}
            </div>

            {file && (
                <div style={styles.fileInfo}>
                    <span style={styles.fileName}>{file.name}</span>
                    <button
                        type="button"
                        style={styles.clearButton}
                        onClick={handleClear}
                        onMouseEnter={(e) => e.currentTarget.style.opacity = '1'}
                        onMouseLeave={(e) => e.currentTarget.style.opacity = '0.7'}
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
