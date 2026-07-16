
export default function GenericMarqueeBar({
  className = '',
  height = 6,
  trackClass = 'somx-marquee-track',
  barClass = 'somx-marquee-bar',
  speed = 1.2,
  width = 0.4,
  rounded = true,
  roleText = 'Loading',
}) {
  const barWidthPct = Math.max(0.05, Math.min(1, width)) * 100;

  return (
    <div
      className={`somx-marquee ${trackClass} ${rounded ? '' : 'somx-marquee-track--square'} ${className}`}
      style={{ height }}
      role="progressbar"
      aria-busy="true"
      aria-valuetext={roleText}
    >
      <div
        className={`${barClass} ${rounded ? '' : 'somx-marquee-bar--square'}`}
        style={{
          width: `${barWidthPct}%`,
          animation: `somx-marquee-anim ${speed}s linear infinite`,
        }}
      />
    </div>
  );
}
