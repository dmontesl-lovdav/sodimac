import { useId, useMemo } from 'react';
import './Progress.css';

export interface GenericMarqueeBarProps {
  className?: string;
  height?: number;
  trackColor?: string;
  barColor?: string;
  speed?: number;
  width?: number;
  rounded?: boolean;
  roleText?: string;
}

export default function GenericMarqueeBar({
  className = '',
  height = 6,
  trackColor = '#e2e8f0',
  barColor = '#0284c7',
  speed = 1.2,
  width = 0.4,
  rounded = true,
  roleText = 'Loading',
}: GenericMarqueeBarProps): React.ReactElement {
  const id = useId();
  const animName = useMemo(() => `fiscal-marquee-${id.replace(/[^a-z0-9]/gi, '')}`, [id]);
  const barWidthPct = Math.max(0.05, Math.min(1, width)) * 100;

  return (
    <div
      className={`fiscal-progress-marquee ${className}`.trim()}
      style={
        {
          backgroundColor: trackColor,
          height: `${height}px`,
          borderRadius: rounded ? '9999px' : undefined,
          ['--fiscal-marquee-bar-width' as string]: `${barWidthPct}%`,
          ['--fiscal-marquee-bar-color' as string]: barColor,
          ['--fiscal-marquee-anim' as string]: animName,
          ['--fiscal-marquee-speed' as string]: `${speed}s`,
        } as React.CSSProperties
      }
      role="progressbar"
      aria-busy="true"
      aria-valuetext={roleText}
    >
      <div
        className="fiscal-progress-marquee-bar"
        style={
          {
            width: 'var(--fiscal-marquee-bar-width)',
            backgroundColor: 'var(--fiscal-marquee-bar-color)',
            animation: `var(--fiscal-marquee-anim) var(--fiscal-marquee-speed) linear infinite`,
            borderRadius: rounded ? '9999px' : undefined,
          } as React.CSSProperties
        }
      />
      <style>{`
        @keyframes ${animName} {
          0%   { transform: translateX(-100%); }
          100% { transform: translateX(250%); }
        }
      `}</style>
    </div>
  );
}
