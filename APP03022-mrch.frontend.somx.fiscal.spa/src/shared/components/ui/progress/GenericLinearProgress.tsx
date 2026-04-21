import React from 'react';
import './Progress.css';

export interface GenericLinearProgressProps {
  indeterminate?: boolean;
  value?: number;
  max?: number;
  buffer?: number;
  fullWidth?: boolean;
  className?: string;
  'aria-label'?: string;
}

export default function GenericLinearProgress({
  indeterminate = false,
  value = 0,
  max = 100,
  buffer,
  fullWidth = false,
  className = '',
  'aria-label': ariaLabel = 'Cargando',
}: GenericLinearProgressProps): React.ReactElement {
  const pct = Math.max(0, Math.min(100, (value / max) * 100));
  const bufferPct =
    typeof buffer === 'number' ? Math.max(0, Math.min(100, (buffer / max) * 100)) : undefined;

  const rootClass = `fiscal-progress-linear-root ${className}`.trim();
  const rootStyle = { width: fullWidth ? '100%' : 'auto' } as React.CSSProperties;

  return (
    <div
      role="progressbar"
      aria-label={ariaLabel}
      aria-valuemin={0}
      aria-valuemax={indeterminate ? undefined : 100}
      aria-valuenow={indeterminate ? undefined : Math.round(pct)}
      className={rootClass}
      style={rootStyle}
    >
      <div className="fiscal-progress-linear-track">
        {!indeterminate && typeof bufferPct === 'number' ? (
          <div
            className="fiscal-progress-linear-buffer"
            style={{ width: `${bufferPct}%` } as React.CSSProperties}
          />
        ) : null}

        {!indeterminate ? (
          <div
            className="fiscal-progress-linear-bar"
            style={{ width: `${pct}%` } as React.CSSProperties}
          />
        ) : (
          <div className="fiscal-progress-linear-indeterminate" />
        )}
      </div>
    </div>
  );
}
