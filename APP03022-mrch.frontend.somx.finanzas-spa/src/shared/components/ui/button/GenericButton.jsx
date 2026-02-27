
/**
 * Botón genérico reutilizable.
 *
 * • variant: 'primary' | 'outline' | 'link' | 'outlineFill'
 * • Cuando `disabled` es verdadero se añaden:
 *   - cursor-not-allowed
 *   - opacity-60
 *   y se elimina el cursor pointer.
 */

export default function GenericButton({
  children,
  icon,
  variant = 'primary',
  className = '',
  style = {},
  disabled = false,
  ...props
}) {
  const variants = {
    primary: 'somx-btn somx-btn-primary',
    outline: 'somx-btn somx-btn-outline',
    link: 'somx-btn somx-btn-link',
    outlineFill: 'somx-btn somx-btn-outlineFill',
  };

  const stateCls = disabled ? 'somx-btn-disabled' : 'somx-btn-enabled';

  return (
    <button
      {...props}
      disabled={disabled}
      style={style}
      className={`${variants[variant]} ${stateCls} ${className}`}
    >
      {children}
      {icon && <img src={icon} className="somx-btn-icon" />}
    </button>
  );
}
