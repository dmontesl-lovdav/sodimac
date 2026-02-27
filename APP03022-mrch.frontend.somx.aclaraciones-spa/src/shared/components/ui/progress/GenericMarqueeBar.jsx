import { useId, useMemo } from 'react';

/**
 * GenericMarqueeBar
 * Barra de carga liviana con animación "marquee" (sin dependencias externas).
 *
 * Props:
 * - className: string            → clases extra para el contenedor
 * - height: number               → altura en px (default 6)
 * - trackClass: string           → clases del track (fondo) (default 'bg-slate-200')
 * - barClass: string             → clases de la barra animada (default 'bg-sky-600')
 * - speed: number                → duración en segundos (default 1.2)
 * - width: number                → ancho relativo de la barrita [0..1] (default 0.4)
 * - rounded: boolean             → bordes redondeados (default true)
 * - roleText: string             → aria-valuetext (default 'Loading')
 */
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
            role="progressbar"
            aria-busy="true"
            aria-valuetext={roleText}
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
