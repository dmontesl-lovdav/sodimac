
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
      className={`${fullWidth ? 'somx-linear-full' : 'somx-linear-wrapper'} ${className}`}
    >
      <div className="somx-linear-track">
        {!indeterminate && typeof bufferPct === 'number' && (
          <div className="somx-linear-buffer" style={{ width: `${bufferPct}%` }} />
        )}

        {!indeterminate ? (
          <div className="somx-linear-bar" style={{ width: `${pct}%` }} />
        ) : (
          <div className="somx-linear-ind-container">
            <div className="somx-linear-ind-bar" />
          </div>
        )}
      </div>
    </div>
  );
}