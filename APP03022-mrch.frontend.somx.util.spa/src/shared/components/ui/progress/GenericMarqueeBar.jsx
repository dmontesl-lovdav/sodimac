import { useId, useMemo } from 'react';
import PropTypes from 'prop-types';

export default function GenericMarqueeBar({
    className = '',
    height = 6,
    trackClass = 'bg-slate-200',
    barClass = 'bg-sky-600',
    speed = 1.2,
    width = 0.4,
    rounded = true,
    roleText = 'Loading',
}) {
    const id = useId();
    const animName = useMemo(() => `gmbar-${id.replace(/[^a-z0-9]/gi, '')}`, [id]);
    const barWidthPct = Math.max(0.05, Math.min(1, width)) * 100;

    return (
        <div
            className={`w-full overflow-hidden ${trackClass} ${rounded ? 'rounded-full' : ''} ${className}`}
            style={{ height }}
            aria-busy="true"
            aria-label={roleText}
        >
            <div
                className={`${barClass} ${rounded ? 'rounded-full' : ''}`}
                style={{
                    height: '100%',
                    width: `${barWidthPct}%`,
                    animation: `${animName} ${speed}s linear infinite`,
                }}
            />
            <style>
                {`
          @keyframes ${animName} {
            0%   { transform: translateX(-100%); }
            100% { transform: translateX(250%); }
          }
        `}
            </style>
        </div>
    );
}

GenericMarqueeBar.propTypes = {
    className: PropTypes.string,
    height: PropTypes.any,
    trackClass: PropTypes.string,
    barClass: PropTypes.string,
    speed: PropTypes.any,
    width: PropTypes.any,
    rounded: PropTypes.bool,
    roleText: PropTypes.string,
};
