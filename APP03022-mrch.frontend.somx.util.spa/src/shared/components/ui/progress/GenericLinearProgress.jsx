import React from 'react';

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
        <div
            role="progressbar"
            aria-label={ariaLabel}
            aria-valuemin={0}
            aria-valuemax={indeterminate ? undefined : 100}
            aria-valuenow={indeterminate ? undefined : Math.round(pct)}
            className={`${fullWidth ? 'w-full' : 'w-auto'} ${className}`}
        >
            <div className="relative h-1.5 w-full overflow-hidden rounded bg-slate-200">
                {!indeterminate && typeof bufferPct === 'number' && (
                    <div
                        className="absolute left-0 top-0 h-full bg-slate-300"
                        style={{ width: `${bufferPct}%` }}
                    />
                )}

                {!indeterminate ? (
                    <div
                        className="absolute left-0 top-0 h-full bg-sky-600 transition-[width] duration-200"
                        style={{ width: `${pct}%` }}
                    />
                ) : (
                    <>
                        <div className="absolute inset-0">
                            <div className="absolute -left-full top-0 h-full w-1/2 bg-sky-600 animate-[gp-ind_1.2s_linear_infinite]" />
                        </div>
                        <style>{`
              @keyframes gp-ind {
                0% { transform: translateX(0%); }
                100% { transform: translateX(200%); }
              }
            `}</style>
                    </>
                )}
            </div>
        </div>
    );
}
