import React from 'react';

const styles = {
    wrapper: {},
    track: {
        position: 'relative',
        height: '0.375rem',
        width: '100%',
        overflow: 'hidden',
        borderRadius: '9999px',
        backgroundColor: '#e2e8f0',
    },
    buffer: {
        position: 'absolute',
        left: 0,
        top: 0,
        height: '100%',
        backgroundColor: '#cbd5e1',
    },
    bar: {
        position: 'absolute',
        left: 0,
        top: 0,
        height: '100%',
        backgroundColor: '#0284c7',
        transition: 'width 200ms ease',
    },
    indeterminateBar: {
        position: 'absolute',
        left: '-100%',
        top: 0,
        height: '100%',
        width: '50%',
        backgroundColor: '#0284c7',
        animation: 'gp-ind 1.2s linear infinite',
    },
};

export default function GenericLinearProgress({
    indeterminate = false,
    value = 0,
    max = 100,
    buffer,
    fullWidth = false,
    className = '',
    'aria-label': ariaLabel = 'Cargando',
}) {
    const pct = Math.max(0, Math.min(100, (value / max) * 100));
    const bufferPct =
        typeof buffer === 'number' ? Math.max(0, Math.min(100, (buffer / max) * 100)) : undefined;

    return (
        <>
            <style>{`
                @keyframes gp-ind {
                    0% { transform: translateX(0%); }
                    100% { transform: translateX(200%); }
                }
            `}</style>
            <div
                role="progressbar"
                aria-label={ariaLabel}
                aria-valuemin={0}
                aria-valuemax={indeterminate ? undefined : 100}
                aria-valuenow={indeterminate ? undefined : Math.round(pct)}
                style={{
                    ...styles.wrapper,
                    width: fullWidth ? '100%' : 'auto',
                }}
                className={className}
            >
                <div style={styles.track}>
                    {!indeterminate && typeof bufferPct === 'number' && (
                        <div style={{ ...styles.buffer, width: `${bufferPct}%` }} />
                    )}

                    {!indeterminate ? (
                        <div style={{ ...styles.bar, width: `${pct}%` }} />
                    ) : (
                        <div style={styles.indeterminateBar} />
                    )}
                </div>
            </div>
        </>
    );
}
