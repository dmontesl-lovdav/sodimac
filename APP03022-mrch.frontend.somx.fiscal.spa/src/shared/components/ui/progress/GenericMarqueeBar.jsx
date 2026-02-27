import { useId, useMemo } from 'react';

export default function GenericMarqueeBar({
    className = '',
    height = 6,
    trackColor = '#e2e8f0',
    barColor = '#0284c7',
    speed = 1.2,
    width = 0.4,
    rounded = true,
    roleText = 'Loading',
}) {
    const id = useId();
    const animName = useMemo(() => `gmbar-${id.replace(/[^a-z0-9]/gi, '')}`, [id]);
    const barWidthPct = Math.max(0.05, Math.min(1, width)) * 100;

    const trackStyle = {
        width: '100%',
        overflow: 'hidden',
        backgroundColor: trackColor,
        height: `${height}px`,
        ...(rounded ? { borderRadius: '9999px' } : {}),
    };

    const barStyle = {
        height: '100%',
        width: `${barWidthPct}%`,
        backgroundColor: barColor,
        animation: `${animName} ${speed}s linear infinite`,
        ...(rounded ? { borderRadius: '9999px' } : {}),
    };

    return (
        <div
            style={trackStyle}
            role="progressbar"
            aria-busy="true"
            aria-valuetext={roleText}
            className={className}
        >
            <div style={barStyle} />
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
